/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.versamento.ejb;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.versamento.dto.ObjectStorageBackend;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Classe che si occupa di istanziare un presigner S3. Nella nostra applicazione dobbiamo poter gestire più Object
 * storage e, di conseguenza, più presigner.
 *
 * Questa classe si occupa di creare una cache per questi oggetti.
 *
 * https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/s3/presigner/S3Presigner.html
 *
 * @author Snidero_L
 */
@Singleton
public class AwsPresigner {

    private Logger log = LoggerFactory.getLogger(AwsPresigner.class);

    private final Map<CacheKey, S3Presigner> presignerCache = new HashMap<>();

    @EJB
    protected ConfigurationHelper configurationHelper;

    private S3Presigner lookupPresigner(ObjectStorageBackend configuration) {

        final CacheKey cacheKey = new CacheKey(configuration.getAddress(), configuration.getBucket());
        S3Presigner presigner = presignerCache.computeIfAbsent(cacheKey, k -> createPresigner(configuration));
        log.debug("Il presigner per il bucket {} dell'object storage {} utilizzato", configuration.getBucket(),
                configuration.getAddress());
        return presigner;
    }

    /*
     * Nota: con l'introduzione di https://docs.aws.amazon.com/AmazonS3/latest/userguide/VirtualHosting.html è
     * necessario abilitare "forzatamente" la modalità path style (attenzione: in futuro sarà deprecata!)
     */
    private S3Presigner createPresigner(ObjectStorageBackend configuration) {

        final AwsCredentialsProvider credProvider = StaticCredentialsProvider
                .create(AwsBasicCredentials.create(configuration.getAccessKeyId(), configuration.getSecretKey()));

        return S3Presigner.builder().endpointOverride(configuration.getAddress()).region(Region.US_EAST_1)
                .credentialsProvider(credProvider)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build();
    }

    /**
     * Ottieni la PRE-signed URL per il backend indicato dalla configurazione. La durata predefinita è di 10 minuti. Se
     * risulta necessario modificare la durata utilizzare
     * {@link #getPresignedUrl(it.eng.parer.ws.versamento.dto.ObjectStorageBackend, java.lang.String, java.time.Duration) }
     *
     * @param configuration
     *            configurazione backend
     * @param key
     *            chiave per cui fornire la presigned URL
     *
     * @return URL pre firmata (accessibile per i prossimi 10 minuti)
     */
    public URL getPresignedUrl(ObjectStorageBackend configuration, String key) {
        return getPresignedUrl(configuration, key, presignedUrlDurationOfMinutes());

    }

    /**
     * Ottieni la PRE-signed URL per il backend indicato dalla configurazione.
     *
     *
     * @param configuration
     *            configurazione backend
     * @param key
     *            chiave per cui fornire la presigned URL
     * @param signatureDuration
     *            la validità temporale dell'URL firmata
     *
     * @return URL pre firmata (accessibile per la durata passata come parametro)
     */
    public URL getPresignedUrl(ObjectStorageBackend configuration, String key, Duration signatureDuration) {
        S3Presigner presigner = lookupPresigner(configuration);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(configuration.getBucket()).key(key)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(signatureDuration).getObjectRequest(getObjectRequest).build();

        PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);
        log.debug("Ora puoi accedere a questa URL per {} minuti {}", signatureDuration.toMinutes(),
                presignedGetObjectRequest.url());
        return presignedGetObjectRequest.url();
    }

    /**
     * Ottiene la durata, espressa in minuti, dei presigned URI. Se parametro non impostato corettamente, viene
     * utilizzato un default di 10 minuti.
     *
     * @return durata in minuti
     *
     */
    private final Duration presignedUrlDurationOfMinutes() {
        final String longParameterString = configurationHelper
                .getValoreParamApplicByApplic(ParametroApplDB.S3_PRESIGNED_URL_DURATION);
        return Duration.ofMinutes(NumberUtils.isDigits(longParameterString) ? Long.valueOf(longParameterString) : 10L); // default
    }

    @PreDestroy
    private void clear() {
        for (S3Presigner s3Presigner : presignerCache.values()) {
            if (s3Presigner != null) {
                s3Presigner.close();
            }
        }
    }

    private static class CacheKey {

        private URI storageAddress;
        private String bucket;

        CacheKey(URI storageAddress, String bucket) {
            this.storageAddress = storageAddress;
            this.bucket = bucket;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((storageAddress == null) ? 0 : storageAddress.hashCode());
            result = prime * result + ((bucket == null) ? 0 : bucket.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CacheKey other = (CacheKey) obj;
            if (!Objects.equals(this.bucket, other.bucket)) {
                return false;
            }
            return Objects.equals(this.storageAddress, other.storageAddress);
        }

    }
}
