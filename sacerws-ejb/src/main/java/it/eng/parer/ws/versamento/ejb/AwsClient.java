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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Cache per i client AWS. Siccome la creazione di un client è un'attività computazionalmente pesante e a noi ne servono
 * pochi (direi al massimo 2) lasciamoli in memoria
 *
 * @author Snidero_L
 */
@Singleton
public class AwsClient {

    private final Logger log = LoggerFactory.getLogger(AwsClient.class);

    private final Map<CacheKey, S3Client> clientCache = new HashMap<>();

    @EJB
    protected ConfigurationHelper configurationHelper;

    private S3Client lookupClient(URI storageAddress, String accessKeyId, String secretKey) {

        final CacheKey key = new CacheKey(storageAddress, accessKeyId, secretKey);

        S3Client client = clientCache.computeIfAbsent(key, k -> createS3Client(storageAddress, accessKeyId, secretKey));
        log.debug("Sto per effettuare il collegamento all'endpoint S3 [{}] dal thread {}", storageAddress,
                Thread.currentThread().getName());
        return client;

    }

    /*
     * Nota: con l'introduzione di https://docs.aws.amazon.com/AmazonS3/latest/userguide/VirtualHosting.html è
     * necessario abilitare "forzatamente" la modalità path style (attenzione: in futuro sarà deprecata!)
     */
    private S3Client createS3Client(URI storageAddress, String accessKeyId, String secretKey) {

        final AwsCredentialsProvider credProvider = StaticCredentialsProvider
                .create(AwsBasicCredentials.create(accessKeyId, secretKey));

        return S3Client.builder().endpointOverride(storageAddress).region(Region.US_EAST_1)
                .credentialsProvider(credProvider).forcePathStyle(true)
                .httpClientBuilder(ApacheHttpClient.builder().maxConnections(maxConnections())
                        .connectionTimeout(connectionTimeoutOfMinutes()).socketTimeout(socketTimeoutOfMinutes()))
                .build();
    }

    /**
     * Ottiene il numero di connessioni massimo per client. Se parametro non impostato corettamente, viene utilizzato un
     * default di 100
     *
     * @return numero di connessioni
     *
     */
    private final Integer maxConnections() {
        final String longParameterString = configurationHelper
                .getValoreParamApplicByApplic(ParametroApplDB.S3_CLIENT_MAX_CONNECTIONS);
        return NumberUtils.isDigits(longParameterString) ? Integer.valueOf(longParameterString) : 100; // default
    }

    /**
     * Ottiene il timeout di connessione espresso in minuti. Se parametro non impostato corettamente, viene utilizzato
     * un default di 1 minuto
     *
     * @return minuti
     *
     */
    private final Duration connectionTimeoutOfMinutes() {
        final String longParameterString = configurationHelper
                .getValoreParamApplicByApplic(ParametroApplDB.S3_CLIENT_CONNECTION_TIMEOUT);
        return Duration.ofMinutes(NumberUtils.isDigits(longParameterString) ? Long.valueOf(longParameterString) : 1L); // default
    }

    /**
     * Ottiene il timeout di socket espresso in minuti. Se parametro non impostato corettamente, viene utilizzato un
     * default di 10 minuti
     *
     * @return minuti
     *
     */
    private final Duration socketTimeoutOfMinutes() {
        final String longParameterString = configurationHelper
                .getValoreParamApplicByApplic(ParametroApplDB.S3_CLIENT_SOCKET_TIMEOUT);
        return Duration.ofMinutes(NumberUtils.isDigits(longParameterString) ? Long.valueOf(longParameterString) : 10L); // default
    }

    /**
     * Ottieni il client (possibilmente in cache) per il collegamento a questo OS
     *
     * @param storageAddress
     *            URI dell'object storage
     * @param accessKeyId
     *            accessKeyID per l'accesso a questo bucket
     * @param secretKey
     *            secretKe per l'accesso a questo bucket
     *
     * @return client S3 configurato
     */
    public S3Client getClient(URI storageAddress, String accessKeyId, String secretKey) {
        return lookupClient(storageAddress, accessKeyId, secretKey);
    }

    @PreDestroy
    private void clear() {
        for (S3Client s3Client : clientCache.values()) {
            if (s3Client != null) {
                s3Client.close();
            }
        }
    }

    private static class CacheKey {

        private URI storageAddress;
        private String accessKeyId;
        private String secretKey;

        public CacheKey(URI storageAddress, String accessKeyId, String secretKey) {
            this.storageAddress = storageAddress;
            this.accessKeyId = accessKeyId;
            this.secretKey = secretKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((storageAddress == null) ? 0 : storageAddress.hashCode());
            result = prime * result + ((accessKeyId == null) ? 0 : accessKeyId.hashCode());
            result = prime * result + ((secretKey == null) ? 0 : secretKey.hashCode());
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
            if (!Objects.equals(this.accessKeyId, other.accessKeyId)) {
                return false;
            }
            if (!Objects.equals(this.secretKey, other.secretKey)) {
                return false;
            }
            return Objects.equals(this.storageAddress, other.storageAddress);
        }
    }

}
