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

package it.eng.parer.ws.versamento.ejb.help;

import static it.eng.parer.ws.utils.Costanti.WS_AGGIUNTA_DOC_NOME;
import static it.eng.parer.ws.utils.Costanti.WS_VERSAMENTO_MM_NOME;
import static it.eng.parer.ws.utils.Costanti.WS_VERSAMENTO_NOME;
import static it.eng.parer.ws.utils.Costanti.WS_AGGIORNAMENTO_VERS_NOME;
import static it.eng.parer.ws.utils.Costanti.AwsConstants.MEATADATA_INGEST_NODE;
import static it.eng.parer.ws.utils.Costanti.AwsConstants.MEATADATA_INGEST_TIME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroCompDoc;
import it.eng.parer.entity.AroCompObjectStorage;
import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.AroUpdCompUnitaDoc;
import it.eng.parer.entity.AroUpdDatiSpecUdObjectStorage;
import it.eng.parer.entity.AroUpdDocUnitaDoc;
import it.eng.parer.entity.AroUpdUnitaDoc;
import it.eng.parer.entity.AroVersIniComp;
import it.eng.parer.entity.AroVersIniDatiSpecObjectStorage;
import it.eng.parer.entity.AroVersIniDoc;
import it.eng.parer.entity.AroVersIniUnitaDoc;
import it.eng.parer.entity.AroXmlDocObjectStorage;
import it.eng.parer.entity.AroXmlUnitaDocObjectStorage;
import it.eng.parer.entity.AroXmlUpdUdObjectStorage;
import it.eng.parer.entity.DecBackend;
import it.eng.parer.entity.DecConfigObjectStorage;
import it.eng.parer.entity.DecTipoUnitaDoc;
import it.eng.parer.entity.VrsDatiSessioneVersKo;
import it.eng.parer.entity.VrsFileSesObjectStorageKo;
import it.eng.parer.entity.VrsFileSessioneKo;
import it.eng.parer.entity.VrsSesUpdUnitaDocErr;
import it.eng.parer.entity.VrsSesUpdUnitaDocKo;
import it.eng.parer.entity.VrsXmlDatiSesObjectStorageKo;
import it.eng.parer.entity.VrsXmlSesUpdUdErrObjectStorage;
import it.eng.parer.entity.VrsXmlSesUpdUdKoObjectStorage;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiEntitaAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroVersIniDatiSpec.TiEntitaSacerAroVersIniDatiSpec;
import it.eng.parer.entity.inheritance.oop.AroXmlObjectStorage;
import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.versamento.dto.BackendStorage;
import it.eng.parer.ws.versamento.dto.ObjectStorageBackend;
import it.eng.parer.ws.versamento.dto.ObjectStorageResource;
import it.eng.parer.ws.versamento.ejb.AwsClient;
import it.eng.parer.ws.versamento.ejb.AwsPresigner;
import it.eng.parer.ws.versamento.exceptions.ObjectStorageException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

@Stateless(mappedName = "SalvataggioBackendHelper")
@LocalBean
public class SalvataggioBackendHelper {

    private final Logger log = LoggerFactory.getLogger(SalvataggioBackendHelper.class);

    private static final String NO_PARAMETER = "Impossibile ottenere il parametro {0}";
    private static final String LOG_MESSAGE_NO_SAVED = "Impossibile salvare il link dell'oggetto su DB";

    @EJB
    protected ConfigurationHelper configurationHelper;

    @EJB
    protected AwsPresigner presigner;

    @EJB
    protected AwsClient s3Clients;

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public enum BACKEND_VERSAMENTO {
        DATABASE, OBJECT_STORAGE
    }

    /**
     * Ottieni la configurazione applicativa relativa alla tipologia di Backend per il salvataggio in "staging"
     *
     * @return configurazione del backend. Può essere, per esempio OBJECT_STORAGE_STAGING oppure DATABASE_PRIMARIO
     *
     * @throws ObjectStorageException
     *             in caso di errore di recupero del parametro
     */
    public String getBackendStaging() throws ObjectStorageException {
        try {
            return configurationHelper.getValoreParamApplicByApplic(ParametroApplDB.BACKEND_VRS_SES_UD_STAGING);

        } catch (ParamApplicNotFoundException | IllegalArgumentException e) {
            throw ObjectStorageException.builder().message(NO_PARAMETER, ParametroApplDB.BACKEND_VRS_SES_UD_STAGING)
                    .cause(e).build();
        }
    }

    // MEV#29276
    /**
     * Ottieni la configurazione applicativa relativa alla tipologia di Backend per il salvataggio delle sessioni
     * errate/fallite dell'aggiornamento metadati
     *
     * @return configurazione del backend. Può essere, per esempio OBJECT_STORAGE_STAGING oppure DATABASE_PRIMARIO
     *
     * @throws ObjectStorageException
     *             in caso di errore di recupero del parametro
     */
    public String getBackendSessioniErrKoAggMd() throws ObjectStorageException {
        try {
            return configurationHelper.getValoreParamApplicByApplic(ParametroApplDB.BACKEND_XML_SES_AGG_MD_ERR_KO);

        } catch (ParamApplicNotFoundException | IllegalArgumentException e) {
            throw ObjectStorageException.builder().message(NO_PARAMETER, ParametroApplDB.BACKEND_XML_SES_AGG_MD_ERR_KO)
                    .cause(e).build();
        }
    }
    // end MEV#29276

    /**
     * Ottieni la tipologia di backend per servizio
     *
     * @param idTipoUnitaDoc
     *            id della tipologia dell'UD
     * @param nomeWs
     *            nome del servizio
     *
     * @return configurazione del backend. Può essere, per esempio OBJECT_STORAGE_STAGING oppure DATABASE_PRIMARIO
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public String getBackendByServiceName(long idTipoUnitaDoc, String nomeWs) throws ObjectStorageException {
        try {

            final String backendDatiVersamento;
            switch (nomeWs) {
            case WS_VERSAMENTO_NOME:
                backendDatiVersamento = ParametroApplDB.BACKEND_VERSAMENTO_SYNC;
                break;
            case WS_AGGIUNTA_DOC_NOME:
                backendDatiVersamento = ParametroApplDB.BACKEND_AGGIUNTALLEGATI_SYNC;
                break;
            case WS_VERSAMENTO_MM_NOME:
                backendDatiVersamento = ParametroApplDB.BACKEND_VERSAMENTO_MULTIMEDIA;
                break;
            // MEV#29276
            case WS_AGGIORNAMENTO_VERS_NOME:
                backendDatiVersamento = ParametroApplDB.BACKEND_VERSAMENTO_AGG_MD;
                break;
            // end MEV#29276
            default:
                throw new IllegalArgumentException("Tipo creazione documento non supportato");
            }

            return getParameter(backendDatiVersamento, idTipoUnitaDoc);

        } catch (ParamApplicNotFoundException | IllegalArgumentException e) {
            throw ObjectStorageException.builder()
                    .message("Impossibile ottenere il parametro per id tipo unita doc {0} e nome servizio {1}",
                            idTipoUnitaDoc, nomeWs)
                    .cause(e).build();
        }
    }

    private String getParameter(String parameterName, long idTipoUnitaDoc) {
        DecTipoUnitaDoc tipoUd = entityManager.find(DecTipoUnitaDoc.class, idTipoUnitaDoc);
        long idStrut = tipoUd.getOrgStrut().getIdStrut();

        long idAmbiente = tipoUd.getOrgStrut().getOrgEnte().getOrgAmbiente().getIdAmbiente();

        return configurationHelper.getValoreParamApplicByTipoUd(parameterName, idStrut, idAmbiente, idTipoUnitaDoc);
    }

    /**
     * Salva lo stream di dati sull'object storage della configurazione identificandolo con la chiave passata come
     * parametro.
     *
     * @param blob
     *            stream di dati
     * @param blobLength
     *            dimensione dello stream di dati
     * @param key
     *            chiave dell'oggetto
     * @param configuration
     *            configurazione dell'object storage in cui aggiungere l'oggetto
     *
     * @return riferimento alla risorsa appena inserita
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public ObjectStorageResource putObject(InputStream blob, long blobLength, final String key,
            ObjectStorageBackend configuration) throws ObjectStorageException {
        checkFullConfiguration(configuration);
        try {
            return putObject(blob, blobLength, key, configuration, Optional.empty(), Optional.empty(),
                    Optional.empty());
        } catch (Exception e) {
            throw ObjectStorageException.builder()
                    .message("Impossibile salvare oggetto {0} sul bucket {1}", key, configuration.getBucket()).cause(e)
                    .build();
        }

    }

    /**
     * Salva lo stream di dati sull'object storage della configurazione identificandolo con la chiave passata come
     * parametro.
     *
     * @param blob
     *            stream di dati
     * @param blobLength
     *            dimensione dello stream di dati
     * @param key
     *            chiave dell'oggetto
     * @param configuration
     *            configurazione dell'object storage in cui aggiungere l'oggetto
     * @param metadata
     *            eventuali metadati (nel caso non vengano passati vengono utilizzati quelli predefiniti)
     * @param tags
     *            eventuali tag (nel caso non vengano passati non vengnono apposti)
     * @param base64md5
     *            eventuale base64-encoded MD5 del file per data integrity check
     *
     * @return riferimento alla risorsa appena inserita
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public ObjectStorageResource putObject(InputStream blob, long blobLength, final String key,
            ObjectStorageBackend configuration, Optional<Map<String, String>> metadata, Optional<Set<Tag>> tags,
            Optional<String> base64md5) throws ObjectStorageException {

        checkFullConfiguration(configuration);

        final URI storageAddress = configuration.getAddress();
        final String accessKeyId = configuration.getAccessKeyId();
        final String secretKey = configuration.getSecretKey();
        final String bucket = configuration.getBucket();

        log.debug("Sto per inserire nell'os {} la chiave {} sul bucket {}", storageAddress, key, bucket);

        try {
            S3Client s3Client = s3Clients.getClient(storageAddress, accessKeyId, secretKey);

            Builder putObjectBuilder = PutObjectRequest.builder().bucket(bucket).key(key);

            if (metadata.isPresent()) {
                putObjectBuilder.metadata(metadata.get());
            } else {
                putObjectBuilder.metadata(defaultMetadata());
            }
            if (tags.isPresent()) {
                putObjectBuilder.tagging(Tagging.builder().tagSet(tags.get()).build());
            }
            if (base64md5.isPresent()) {
                putObjectBuilder.contentMD5(base64md5.get());
            }

            PutObjectRequest objectRequest = putObjectBuilder.build();
            final long start = System.currentTimeMillis();
            PutObjectResponse response = s3Client.putObject(objectRequest,
                    RequestBody.fromInputStream(blob, blobLength));

            final long end = System.currentTimeMillis() - start;
            if (log.isDebugEnabled()) {
                log.debug("Salvato oggetto {} di {} byte sul bucket {} con ETag {} in {} ms", key, blobLength, bucket,
                        response.eTag(), end);
            }
            final URL presignedUrl = presigner.getPresignedUrl(configuration, key);
            //
            final URI presignedURLasURI = presignedUrl.toURI();

            final String tenant = getDefaultTenant();

            return new ObjectStorageResource() {
                @Override
                public String getBucket() {
                    return bucket;
                }

                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public String getETag() {
                    return response.eTag();
                }

                @Override
                public String getExpiration() {
                    return response.expiration();
                }

                @Override
                public URI getPresignedURL() {
                    return presignedURLasURI;
                }

                @Override
                public String getTenant() {
                    return tenant;
                }
            };

        } catch (Exception e) {
            throw ObjectStorageException.builder().message("{0}: impossibile salvare oggetto {1} sul bucket {2}",
                    configuration.getBackendName(), key, configuration.getBucket()).cause(e).build();
        }
    }

    private Map<String, String> defaultMetadata() {

        Map<String, String> defaultMetadata = new HashMap<>();
        defaultMetadata.put(MEATADATA_INGEST_NODE, System.getProperty("jboss.node.name"));
        defaultMetadata.put(MEATADATA_INGEST_TIME, ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        return defaultMetadata;
    }

    /**
     * Copia dal bucket di staging l'oggetto sul bucket definitivo.
     *
     * @param sourceKey
     *            chiave del bucket sorgente
     * @param sourceConfiguration
     *            configurazione del bucket sorgente
     * @param destKey
     *            chiave del bucket destinazione
     * @param destConfiguration
     *            configurazione del bucket di destinazione
     *
     * @return riferimento della risorsa caricata
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public ObjectStorageResource createObjectComponenti(String sourceKey, ObjectStorageBackend sourceConfiguration,
            String destKey, ObjectStorageBackend destConfiguration) throws ObjectStorageException {

        checkFullConfiguration(sourceConfiguration);
        checkFullConfiguration(destConfiguration);

        S3Client s3SourceClient;
        GetObjectRequest getObjectRequest;
        try {
            s3SourceClient = s3Clients.getClient(sourceConfiguration.getAddress(), sourceConfiguration.getAccessKeyId(),
                    sourceConfiguration.getSecretKey());

            getObjectRequest = GetObjectRequest.builder().bucket(sourceConfiguration.getBucket()).key(sourceKey)
                    .build();
        } catch (Exception e) {
            throw ObjectStorageException.builder().message(
                    "{0}: errore generico creazione / copia oggetto da bucket {1} con chiave {2} verso bucket {3} con chiave {4}",
                    sourceConfiguration.getBackendName(), sourceConfiguration.getBucket(), sourceKey,
                    destConfiguration.getBucket(), destKey).cause(e).build();
        }
        try (ResponseInputStream<GetObjectResponse> objectStream = s3SourceClient.getObject(getObjectRequest);) {
            long objectLength = objectStream.response().contentLength();
            return putObject(objectStream, objectLength, destKey, destConfiguration);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(
                    "{0}: impossibile creare / copiare oggetto da bucket {1} con chiave {2} verso bucket {3} con chiave {4}",
                    sourceConfiguration.getBackendName(), sourceConfiguration.getBucket(), sourceKey,
                    destConfiguration.getBucket(), destKey).cause(e).build();
        }

    }

    /**
     * Copia il file memorizzato nel filesystem sul un oggetto nel bucket definitivo.
     *
     * @param componente
     *            file del componente
     * @param destKey
     *            chiave del bucket destinazione
     * @param destConfiguration
     *            configurazione del bucket di destinazione
     *
     * @return riferimento della risorsa caricata
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public ObjectStorageResource createObjectComponenti(File componente, String destKey,
            ObjectStorageBackend destConfiguration) throws ObjectStorageException {
        checkFullConfiguration(destConfiguration);

        try (FileInputStream fis = new FileInputStream(componente)) {
            return putObject(fis, componente.length(), destKey, destConfiguration);
        } catch (IOException e) {
            throw ObjectStorageException.builder()
                    .message("Impossibile caricare il file {0} sul bucket {1} con chiave {2}", componente.getName(),
                            destConfiguration.getBucket(), destKey)
                    .cause(e).build();
        }

    }

    /**
     * Effettua il salvataggio del collegamento tra l'id componente e la chiave sull'object storage
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idCompDoc
     *            id del componente
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkCompDoc(ObjectStorageResource object, String nmBackend, long idCompDoc)
            throws ObjectStorageException {
        try {
            AroCompDoc aroCompDoc = entityManager.find(AroCompDoc.class, idCompDoc);
            AroCompObjectStorage osLink = new AroCompObjectStorage();
            osLink.setAroCompDoc(aroCompDoc);

            osLink.setCdKeyFile(object.getKey());
            osLink.setNmBucket(object.getBucket());
            osLink.setNmTenant(object.getTenant());

            osLink.setDecBackend(getBackendEntity(nmBackend));
            entityManager.persist(osLink);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    /**
     * Effettua il salvataggio del collegamento tra i metadati dell'unita documentaria e la chiave sull'object storage
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idAroUnitaDoc
     *            id unita documentaria
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkSipUd(ObjectStorageResource object, String nmBackend, long idAroUnitaDoc)
            throws ObjectStorageException {
        try {
            AroUnitaDoc aroUnitaDoc = entityManager.find(AroUnitaDoc.class, idAroUnitaDoc);
            AroXmlUnitaDocObjectStorage osLink = new AroXmlUnitaDocObjectStorage();
            osLink.setAroUnitaDoc(aroUnitaDoc);

            osLink.setCdKeyFile(object.getKey());
            osLink.setNmBucket(object.getBucket());
            osLink.setNmTenant(object.getTenant());

            osLink.setDecBackend(getBackendEntity(nmBackend));
            entityManager.persist(osLink);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    /**
     * Effettua il salvataggio del collegamento tra i metadati del documento e la chiave sull'object storage
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idAroDoc
     *            id unita documentaria
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkSipDoc(ObjectStorageResource object, String nmBackend, long idAroDoc)
            throws ObjectStorageException {
        try {
            AroDoc aroDoc = entityManager.find(AroDoc.class, idAroDoc);
            AroXmlDocObjectStorage osLink = new AroXmlDocObjectStorage();
            osLink.setAroDoc(aroDoc);

            osLink.setCdKeyFile(object.getKey());
            osLink.setNmBucket(object.getBucket());
            osLink.setNmTenant(object.getTenant());

            osLink.setDecBackend(getBackendEntity(nmBackend));
            entityManager.persist(osLink);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    // MEV#29276
    /**
     * Effettua il salvataggio del collegamento tra i sip degli aggiornamenti metadati e la chiave sull'object storage
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idUpdUnitaDoc
     *            id aggiornamento metadati
     * @param idStrut
     *            id della struttura versante
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkSipAggMd(ObjectStorageResource object, String nmBackend, long idUpdUnitaDoc,
            BigDecimal idStrut) throws ObjectStorageException {
        try {
            AroUpdUnitaDoc aroUpdUnitaDoc = entityManager.find(AroUpdUnitaDoc.class, idUpdUnitaDoc);

            AroXmlUpdUdObjectStorage osLink = new AroXmlUpdUdObjectStorage();
            osLink.setAroUpdUnitaDoc(aroUpdUnitaDoc);

            osLink.setCdKeyFile(object.getKey());
            osLink.setNmBucket(object.getBucket());
            osLink.setNmTenant(object.getTenant());
            osLink.setDecBackend(getBackendEntity(nmBackend));
            osLink.setIdStrut(idStrut);

            entityManager.persist(osLink);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    /**
     * Effettua il salvataggio del collegamento tra i dati specifici degli aggiornamenti metadati e la chiave
     * sull'object storage
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idEntitySacerUpd
     *            id dell'entità sacer associata ai dati specifici dell'aggiornamento metadati
     * @param tipoEntitySacerUpd
     *            tipo entità sacer associata ai dati specifici dell'aggiornamento metadati
     * @param idStrut
     *            id della struttura versante
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkUpdDatiSpecAggMd(ObjectStorageResource object, String nmBackend,
            long idEntitySacerUpd, TiEntitaAroUpdDatiSpecUnitaDoc tipoEntitySacerUpd, BigDecimal idStrut)
            throws ObjectStorageException {

        try {

            AroUpdDatiSpecUdObjectStorage osLink = new AroUpdDatiSpecUdObjectStorage();

            switch (tipoEntitySacerUpd) {
            case UPD_UNI_DOC:
                AroUpdUnitaDoc aroUpdUnitaDoc = entityManager.find(AroUpdUnitaDoc.class, idEntitySacerUpd);
                osLink.setAroUpdUnitaDoc(aroUpdUnitaDoc);
                break;
            case UPD_DOC:
                AroUpdDocUnitaDoc aroUpdDocUnitaDoc = entityManager.find(AroUpdDocUnitaDoc.class, idEntitySacerUpd);
                osLink.setAroUpdUnitaDoc(aroUpdDocUnitaDoc.getAroUpdUnitaDoc());
                osLink.setAroUpdDocUnitaDoc(aroUpdDocUnitaDoc);
                break;
            case UPD_COMP:
                AroUpdCompUnitaDoc aroUpdCompUnitaDoc = entityManager.find(AroUpdCompUnitaDoc.class, idEntitySacerUpd);
                osLink.setAroUpdUnitaDoc(aroUpdCompUnitaDoc.getAroUpdDocUnitaDoc().getAroUpdUnitaDoc());
                osLink.setAroUpdDocUnitaDoc(aroUpdCompUnitaDoc.getAroUpdDocUnitaDoc());
                osLink.setAroUpdCompUnitaDoc(aroUpdCompUnitaDoc);
                break;
            }

            osLink.setTiEntitaSacer(tipoEntitySacerUpd);
            osLink.setCdKeyFile(object.getKey());
            osLink.setNmBucket(object.getBucket());
            osLink.setNmTenant(object.getTenant());
            osLink.setDecBackend(getBackendEntity(nmBackend));
            osLink.setIdStrut(idStrut);

            entityManager.persist(osLink);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    /**
     * Effettua il salvataggio del collegamento tra i dati specifici degli aggiornamenti metadati relativi ai metadati
     * iniziali e la chiave sull'object storage
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idEntitySacerVersIni
     *            id dell'entità sacer associata ai dati specifici dell'aggiornamento metadati relativi ai metadati
     *            iniziali
     * @param tipoEntitySacerVersIni
     *            tipo entità sacer associata ai dati specifici dell'aggiornamento metadati relativi ai metadati
     *            iniziali
     * @param idStrut
     *            id della struttura versante
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkVersIniDatiSpecAggMd(ObjectStorageResource object, String nmBackend,
            long idEntitySacerVersIni, TiEntitaSacerAroVersIniDatiSpec tipoEntitySacerVersIni, BigDecimal idStrut)
            throws ObjectStorageException {

        try {

            AroVersIniDatiSpecObjectStorage osLink = new AroVersIniDatiSpecObjectStorage();

            switch (tipoEntitySacerVersIni) {
            case UNI_DOC:
                AroVersIniUnitaDoc aroVersIniUnitaDoc = entityManager.find(AroVersIniUnitaDoc.class,
                        idEntitySacerVersIni);
                osLink.setAroVersIniUnitaDoc(aroVersIniUnitaDoc);
                break;
            case DOC:
                AroVersIniDoc aroVersIniDoc = entityManager.find(AroVersIniDoc.class, idEntitySacerVersIni);
                osLink.setAroVersIniUnitaDoc(aroVersIniDoc.getAroVersIniUnitaDoc());
                osLink.setAroVersIniDoc(aroVersIniDoc);
                break;
            case COMP:
                AroVersIniComp aroVersIniComp = entityManager.find(AroVersIniComp.class, idEntitySacerVersIni);
                osLink.setAroVersIniUnitaDoc(aroVersIniComp.getAroVersIniDoc().getAroVersIniUnitaDoc());
                osLink.setAroVersIniDoc(aroVersIniComp.getAroVersIniDoc());
                osLink.setAroVersIniComp(aroVersIniComp);
                break;
            }

            osLink.setTiEntitaSacer(tipoEntitySacerVersIni);
            osLink.setCdKeyFile(object.getKey());
            osLink.setNmBucket(object.getBucket());
            osLink.setNmTenant(object.getTenant());
            osLink.setDecBackend(getBackendEntity(nmBackend));
            osLink.setIdStrut(idStrut);

            entityManager.persist(osLink);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    /**
     * Effettua il salvataggio del collegamento tra i versamenti falliti e la loro chiave sull'object storage (nel
     * bucket "sessioni-err-ko-agg-md")
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idSesUpdUnitaDocKo
     *            id sessione fallita
     * @param idStrut
     *            id della struttura versante
     * 
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkSipSessioneKoAggMd(ObjectStorageResource object, String nmBackend,
            long idSesUpdUnitaDocKo, BigDecimal idStrut) throws ObjectStorageException {
        try {

            VrsSesUpdUnitaDocKo sesUpdUnitaDocKo = entityManager.find(VrsSesUpdUnitaDocKo.class, idSesUpdUnitaDocKo);

            VrsXmlSesUpdUdKoObjectStorage linkFalliti = new VrsXmlSesUpdUdKoObjectStorage();
            linkFalliti.setVrsSesUpdUnitaDocKo(sesUpdUnitaDocKo);
            linkFalliti.setDecBackend(getBackendEntity(nmBackend));
            linkFalliti.setNmTenant(object.getTenant());
            linkFalliti.setNmBucket(object.getBucket());
            linkFalliti.setCdKeyFile(object.getKey());
            linkFalliti.setIdStrut(idStrut);
            entityManager.persist(linkFalliti);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    /**
     * Effettua il salvataggio del collegamento tra i versamenti errati e la loro chiave sull'object storage (nel bucket
     * "sessioni-err-ko-agg-md")
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idSesUpdUnitaDocErr
     *            id sessione errata
     * @param idStrut
     *            id della struttura versante
     * 
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkSipSessioneErrAggMd(ObjectStorageResource object, String nmBackend,
            long idSesUpdUnitaDocErr, BigDecimal idStrut) throws ObjectStorageException {
        try {

            VrsSesUpdUnitaDocErr sesUpdUnitaDocErr = entityManager.find(VrsSesUpdUnitaDocErr.class,
                    idSesUpdUnitaDocErr);

            VrsXmlSesUpdUdErrObjectStorage linkErrati = new VrsXmlSesUpdUdErrObjectStorage();
            linkErrati.setVrsSesUpdUnitaDocErr(sesUpdUnitaDocErr);
            linkErrati.setDecBackend(getBackendEntity(nmBackend));
            linkErrati.setNmTenant(object.getTenant());
            linkErrati.setNmBucket(object.getBucket());
            linkErrati.setCdKeyFile(object.getKey());
            linkErrati.setIdStrut(idStrut);
            entityManager.persist(linkErrati);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }
    // end MEV#29276

    /**
     * Effettua il salvataggio del collegamento tra i sip di versamento (raggruppati dall'id dati sessione vers) e la
     * chiave sull'object storage
     *
     * @param object
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idDatiSessioneVers
     *            id dati sessione vers
     * @param idStrut
     *            id della struttura versante
     * 
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkSipSessione(ObjectStorageResource object, String nmBackend,
            long idDatiSessioneVers, BigDecimal idStrut) throws ObjectStorageException {
        try {
            VrsXmlDatiSesObjectStorageKo vrsXmlDatiSesObjectStorageKo = new VrsXmlDatiSesObjectStorageKo();
            vrsXmlDatiSesObjectStorageKo.setDecBackend(getBackendEntity(nmBackend));
            vrsXmlDatiSesObjectStorageKo.setNmTenant(object.getTenant());
            vrsXmlDatiSesObjectStorageKo.setNmBucket(object.getBucket());
            vrsXmlDatiSesObjectStorageKo.setNmKeyFile(object.getKey());
            vrsXmlDatiSesObjectStorageKo.setIdStrut(idStrut);
            vrsXmlDatiSesObjectStorageKo
                    .setDatiSessioneVersKo(entityManager.find(VrsDatiSessioneVersKo.class, idDatiSessioneVers));
            entityManager.persist(vrsXmlDatiSesObjectStorageKo);
        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    /**
     * Effettua il salvataggio del collegamento tra i versamenti falliti e la loro chiave sull'object storage (nel
     * bucket di staging)
     *
     * @param stagingResource
     *            informazioni dell'oggetto salvato
     * @param nmBackend
     *            nome del backend (di tipo OS) su cui è stato salvato
     * @param idFileSessione
     *            id file sessione
     * @param idStrut
     *            id della struttura versante
     * 
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public void saveObjectStorageLinkFileSessione(ObjectStorageResource stagingResource, String nmBackend,
            long idFileSessione, BigDecimal idStrut) throws ObjectStorageException {
        try {
            VrsFileSessioneKo fileSessioneKo = entityManager.find(VrsFileSessioneKo.class, idFileSessione);

            VrsFileSesObjectStorageKo linkFalliti = new VrsFileSesObjectStorageKo();
            linkFalliti.setFileSessioneKo(fileSessioneKo);
            linkFalliti.setDecBackend(getBackendEntity(nmBackend));
            linkFalliti.setNmTenant(stagingResource.getTenant());
            linkFalliti.setNmBucket(stagingResource.getBucket());
            linkFalliti.setNmKeyFile(stagingResource.getKey());
            linkFalliti.setIdStrut(idStrut);
            entityManager.persist(linkFalliti);
        } catch (Exception e) {
            throw ObjectStorageException.builder().message(LOG_MESSAGE_NO_SAVED).cause(e).build();
        }
    }

    /**
     * Effettua la copia dell'oggetto dal bucket di staging a quello definitivo (dei componenti).<strong>Questo metodo
     * funziona solamente se entrambi i bucket sono all'interno del medesimo OS</strong>.
     *
     * <strong>ATTENZIONE</strong> Questo metodo non è mai stato testato né chiamato. Rappresenta, però, una possibile
     * ottimizzazione della fase di versamento.
     *
     *
     * @param sourceKey
     *            chiave dell'oggetto nel bucket di staging
     * @param sourceConfiguration
     *            configurazione dell'Object storage sorgente
     * @param destKey
     *            chiave dell'oggetto nel bucket dei componenti
     * @param destConfiguration
     *            configurazione dell'Object storage destinazione
     *
     * @throws ObjectStorageException
     *             in caso di eccezione
     */
    public void copyObjectFromStaging(String sourceKey, ObjectStorageBackend sourceConfiguration, String destKey,
            ObjectStorageBackend destConfiguration) throws ObjectStorageException {

        checkFullConfiguration(sourceConfiguration);
        checkFullConfiguration(destConfiguration);

        // non è lo stesso Object storage, non posso effettuare la copia tra bucket.
        if (!sameServiceAccount(destConfiguration, destConfiguration)) {
            return;
        }

        final URI storageAddress = sourceConfiguration.getAddress();
        final String accessKeyId = sourceConfiguration.getAccessKeyId();
        final String secretKey = sourceConfiguration.getSecretKey();

        try {
            S3Client s3Client = s3Clients.getClient(storageAddress, accessKeyId, secretKey);
            GetUrlRequest request = GetUrlRequest.builder().bucket(sourceConfiguration.getBucket()).key(sourceKey)
                    .build();

            URL sourceUrl = s3Client.utilities().getUrl(request);

            CopyObjectRequest copyReq = CopyObjectRequest.builder().copySourceIfMatch(sourceUrl.toExternalForm())
                    .destinationBucket(destConfiguration.getBucket()).destinationKey(destKey).build();

            CopyObjectResponse copyRes = s3Client.copyObject(copyReq);
            if (log.isDebugEnabled()) {
                log.debug(
                        "Copiato oggetto {} dal bucket di staging {} al bucket dei componenti {} con chiave {} e con ETag {}",
                        sourceKey, sourceConfiguration.getBucket(), destConfiguration.getBucket(), destKey,
                        copyRes.copyObjectResult().eTag());
            }

        } catch (Exception e) {
            throw ObjectStorageException.builder().message("Impossibile copiare tra staging e componenti").cause(e)
                    .build();
        }
    }

    /**
     * Controlla se il service account utilizzato per accedere all'object storage è lo stesso. In questo caso si può
     * procedere all'operazione di copia tra bucket.
     *
     * @param conf1
     *            prima configurazione
     * @param conf2
     *            seconda configurazione
     *
     * @return true se il service account è il medesimo
     */
    private static boolean sameServiceAccount(ObjectStorageBackend conf1, ObjectStorageBackend conf2) {
        return conf1.getAddress().equals(conf2.getAddress()) && conf1.getAccessKeyId().equals(conf2.getAccessKeyId())
                && conf1.getSecretKey().equals(conf2.getSecretKey());
    }

    private DecBackend getBackendEntity(String nomeBackend) {
        TypedQuery<DecBackend> query = entityManager
                .createQuery("Select d from DecBackend d where d.nmBackend = :nomeBackend", DecBackend.class);
        query.setParameter("nomeBackend", nomeBackend);
        return query.getSingleResult();
    }

    /**
     * Ottieni la configurazione del backend a partire dal nome del backend
     *
     * @param nomeBackend
     *            per esempio "OBJECT_STORAGE_PRIMARIO"
     *
     * @return Informazioni sul Backend identificato
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public BackendStorage getBackend(String nomeBackend) throws ObjectStorageException {
        try {

            DecBackend backend = getBackendEntity(nomeBackend);
            final BackendStorage.STORAGE_TYPE type = BackendStorage.STORAGE_TYPE.valueOf(backend.getNmTipoBackend());
            final String backendName = backend.getNmBackend();

            return new BackendStorage() {
                private static final long serialVersionUID = 1L;

                @Override
                public BackendStorage.STORAGE_TYPE getType() {
                    return type;
                }

                @Override
                public String getBackendName() {
                    return backendName;
                }
            };

        } catch (IllegalArgumentException | NonUniqueResultException e) {
            throw ObjectStorageException.builder().message("Impossibile ottenere le informazioni di backend").cause(e)
                    .build();
        }

    }

    private static final String BUCKET = "BUCKET";
    private static final String ACCESS_KEY_ID_SYS_PROP = "ACCESS_KEY_ID_SYS_PROP";
    private static final String SECRET_KEY_SYS_PROP = "SECRET_KEY_SYS_PROP";

    /**
     * Ottieni la configurazione per potersi collegare a quel bucket dell'Object Storage scelto.
     *
     * @param nomeBackend
     *            nome del backend <strong> di tipo DEC_BACKEND.NM_TIPO_BACKEND = 'OS' </strong>come censito su
     *            DEC_BACKEND (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param tipoUsoOs
     *            ambito di utilizzo di questo backend (per esempio STAGING)
     *
     * @return Configurazione dell'Object Storage per quell'ambito
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public ObjectStorageBackend getObjectStorageConfiguration(final String nomeBackend, final String tipoUsoOs)
            throws ObjectStorageException {
        TypedQuery<DecConfigObjectStorage> query = entityManager.createQuery(
                "Select c from DecConfigObjectStorage c where c.tiUsoConfigObjectStorage = :tipoUsoOs and c.decBackend.nmBackend = :nomeBackend order by c.nmConfigObjectStorage",
                DecConfigObjectStorage.class);
        query.setParameter("tipoUsoOs", tipoUsoOs).setParameter("nomeBackend", nomeBackend);
        List<DecConfigObjectStorage> resultList = query.getResultList();
        String bucket = null;
        String nomeSystemPropertyAccessKeyId = null;
        String nomeSystemPropertySecretKey = null;
        String storageAddress = null;

        for (DecConfigObjectStorage decConfigObjectStorage : resultList) {
            switch (decConfigObjectStorage.getNmConfigObjectStorage()) {
            case ACCESS_KEY_ID_SYS_PROP:
                nomeSystemPropertyAccessKeyId = decConfigObjectStorage.getDsValoreConfigObjectStorage();
                break;
            case BUCKET:
                bucket = decConfigObjectStorage.getDsValoreConfigObjectStorage();
                break;
            case SECRET_KEY_SYS_PROP:
                nomeSystemPropertySecretKey = decConfigObjectStorage.getDsValoreConfigObjectStorage();
                break;
            default:
                throw ObjectStorageException.builder()
                        .message("Impossibile stabilire la tipologia del parametro per l'object storage").build();
            }
            // identico per tutti perché definito nella tabella padre
            storageAddress = decConfigObjectStorage.getDecBackend().getDlBackendUri();
        }
        if (StringUtils.isBlank(bucket) || StringUtils.isBlank(nomeSystemPropertyAccessKeyId)
                || StringUtils.isBlank(nomeSystemPropertySecretKey) || StringUtils.isBlank(storageAddress)) {
            throw ObjectStorageException.builder()
                    .message("Impossibile stabilire la tipologia del parametro per l'object storage").build();
        }

        final String accessKeyId = System.getProperty(nomeSystemPropertyAccessKeyId);
        final String secretKey = System.getProperty(nomeSystemPropertySecretKey);
        final URI osURI = URI.create(storageAddress);
        final String stagingBucket = bucket;

        return new ObjectStorageBackend() {
            private static final long serialVersionUID = -7818781527374773374L;

            @Override
            public String getBackendName() {
                return nomeBackend;
            }

            @Override
            public URI getAddress() {
                return osURI;
            }

            @Override
            public String getBucket() {
                return stagingBucket;
            }

            @Override
            public String getAccessKeyId() {
                return accessKeyId;
            }

            @Override
            public String getSecretKey() {
                return secretKey;
            }
        };

    }

    /**
     * Genera la chiave del componente da salvare sull'object storage.
     *
     * @param idComponente
     *            identificativo del componente di cui salvare il contenuto
     *
     * @return chiave che verrà utilizzata sul bucket componenti
     *
     * @throws ObjectStorageException
     *             in caso di errore.
     */
    public String generateKeyComponente(long idComponente) throws ObjectStorageException {
        try {

            AroCompDoc compDoc = entityManager.find(AroCompDoc.class, idComponente);

            // la devo "pescare" l'UD passando dalla ARO_STRUT_DOC
            AroUnitaDoc unitaDoc = compDoc.getAroStrutDoc().getAroDoc().getAroUnitaDoc();

            String nmStrutNorm = unitaDoc.getOrgStrut().getCdStrutNormaliz();

            String nmEnteNorm = unitaDoc.getOrgStrut().getOrgEnte().getCdEnteNormaliz();

            String cdRegistroNorm = unitaDoc.getDecRegistroUnitaDoc().getCdRegistroNormaliz();
            int anno = unitaDoc.getAaKeyUnitaDoc().intValue();

            return createKeyComponenti(nmEnteNorm, nmStrutNorm, cdRegistroNorm, anno, idComponente);

        } catch (Exception e) {
            throw ObjectStorageException.builder().message("Impossibile generare la chiave del componente").cause(e)
                    .build();
        }
    }

    private String getDefaultTenant() {
        return configurationHelper.getValoreParamApplicByApplic(ParametroApplDB.TENANT_OBJECT_STORAGE);

    }

    private String createKeyComponenti(String nmEnteNorm, String nmStrutNorm, String cdRegistroNorm, int anno,
            long idCompDoc) {
        // Non serve a nulla
        String nmTenant = getDefaultTenant();

        return nmTenant + "/" + nmEnteNorm + "/" + nmStrutNorm + "/" + cdRegistroNorm + "/" + anno + "/" + idCompDoc;
    }

    /*
     * Full configuration = S3 URI + access_key + secret_key + bucket name
     */
    private static void checkFullConfiguration(ObjectStorageBackend configuration) throws ObjectStorageException {
        checkConfiguration(configuration, true);
    }

    /*
     * Minimal configuration = S3 URI + access_key + secret_key
     */
    private static void checkMinimalConfiguration(ObjectStorageBackend configuration) throws ObjectStorageException {
        checkConfiguration(configuration, false);
    }

    private static void checkConfiguration(ObjectStorageBackend configuration, boolean checkIfBucketExists)
            throws ObjectStorageException {
        List<String> errors = new ArrayList<>();
        if (configuration.getAddress() == null) {
            errors.add("indirizzo object storage");
        }
        if (StringUtils.isBlank(configuration.getAccessKeyId())) {
            errors.add("access key Id");
        }
        if (StringUtils.isBlank(configuration.getSecretKey())) {
            errors.add("secret Key");
        }
        if (checkIfBucketExists && StringUtils.isBlank(configuration.getBucket())) {
            errors.add("nome bucket");
        }
        if (!errors.isEmpty()) {
            throw ObjectStorageException.builder()
                    .message("Parametri mancanti per il collegamento a object storage: {0}", String.join(",", errors))
                    .build();
        }

    }

    /**
     * Ottieni il collegamento tra sip dell'unita documentaria e il suo bucket/chiave su OS.
     *
     * @param idUnitaDoc
     *            id unita documentaria
     *
     * @return record contenete il link
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public AroXmlObjectStorage getLinkSipUnitaDocOs(long idUnitaDoc) throws ObjectStorageException {
        try {
            return entityManager.find(AroXmlUnitaDocObjectStorage.class, idUnitaDoc);

        } catch (IllegalArgumentException e) {
            throw ObjectStorageException.builder()
                    .message("Errore durante il recupero da AroXmlUnitaDocObjectStorage per id unita doc vers {0} ",
                            idUnitaDoc)
                    .cause(e).build();
        }

    }

    /**
     * Ottieni il collegamento tra sip del documento e il suo bucket/chiave su OS.
     *
     * @param idDoc
     *            id documento
     *
     * @return record contenete il link
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public AroXmlObjectStorage getLinkSipDocOs(long idDoc) throws ObjectStorageException {
        try {
            return entityManager.find(AroXmlDocObjectStorage.class, idDoc);

        } catch (IllegalArgumentException e) {
            throw ObjectStorageException.builder()
                    .message("Errore durante il recupero da AroXmlUnitaDocObjectStorage per id doc vers {0} ", idDoc)
                    .cause(e).build();
        }

    }

    /**
     * Ottieni l'oggetto dall'object storage selezionato sotto-forma di InputStream.
     *
     * @param configuration
     *            configurazione per accedere all'object storage
     * @param bucket
     *            bucket
     * @param objectKey
     *            chiave
     *
     * @return InputStream dell'oggetto ottenuto
     *
     * @throws ObjectStorageException
     *             in caso di errore
     */
    public ResponseInputStream<GetObjectResponse> getObject(ObjectStorageBackend configuration, String bucket,
            String objectKey) throws ObjectStorageException {
        try {
            S3Client s3SourceClient = s3Clients.getClient(configuration.getAddress(), configuration.getAccessKeyId(),
                    configuration.getSecretKey());

            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(objectKey).build();
            return s3SourceClient.getObject(getObjectRequest);

        } catch (AwsServiceException | SdkClientException e) {
            throw ObjectStorageException.builder()
                    .message("{0}: impossibile ottenere dal bucket {1} oggetto con chiave {2}",
                            configuration.getBackendName(), bucket, objectKey)
                    .cause(e).build();
        }

    }

    /**
     * Effettua il tagging di un oggetto esistente su bucket con chiave e nell'object storage indicato (su base
     * configurazioni)
     * 
     * @param osResource
     *            risorsa salvata su object storage
     * @param configuration
     *            configurazione per accedere all'object storage
     * @param tags
     *            tag da impostare sull'oggetto
     * 
     * @throws ObjectStorageException
     *             eccezzione generica (S3 error code / varie)
     */
    public void taggingObject(ObjectStorageResource osResource, ObjectStorageBackend configuration, Set<Tag> tags)
            throws ObjectStorageException {

        checkMinimalConfiguration(configuration);

        final URI storageAddress = configuration.getAddress();
        final String accessKeyId = configuration.getAccessKeyId();
        final String secretKey = configuration.getSecretKey();

        log.debug("Sto per taggare con {} nell'os {} la chiave {} sul bucket {}", tags, storageAddress,
                osResource.getKey(), osResource.getBucket());

        try {
            S3Client s3Client = s3Clients.getClient(storageAddress, accessKeyId, secretKey);
            PutObjectTaggingRequest objectRequest = PutObjectTaggingRequest.builder().bucket(osResource.getBucket())
                    .key(osResource.getKey()).tagging(Tagging.builder().tagSet(tags).build()).build();
            final long start = System.currentTimeMillis();
            // s3 tagging
            PutObjectTaggingResponse response = s3Client.putObjectTagging(objectRequest);
            //
            if (!response.sdkHttpResponse().isSuccessful()) {
                throw ObjectStorageException.builder()
                        .message("{0}: errore tagging oggetto {1} sul bucket {2}, response {3}",
                                configuration.getBackendName(), osResource.getKey(), osResource.getBucket(), response)
                        .build();
            }

            final long end = System.currentTimeMillis() - start;
            if (log.isDebugEnabled()) {
                log.debug("Tagging oggetto {} sul bucket {} effettuato in {} ms", osResource.getKey(),
                        osResource.getBucket(), end);
            }
        } catch (Exception e) {
            throw ObjectStorageException.builder()
                    .message("{0}: errore imprevisto tagging oggetto {1} sul bucket {2}",
                            configuration.getBackendName(), osResource.getKey(), osResource.getBucket())
                    .cause(e).build();
        }
    }
}
