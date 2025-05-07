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

import static it.eng.parer.ws.utils.Costanti.S3Constants.KEY_PREFIX_COMPUD;
import static it.eng.parer.ws.utils.Costanti.S3Constants.KEY_PREFIX_SIPPUD;
import static it.eng.parer.ws.utils.Costanti.S3Constants.KEY_PREFIX_TMP;
import static it.eng.parer.ws.utils.Costanti.S3Constants.TAG_KEY_RVF_CDSERV;
import static it.eng.parer.ws.utils.Costanti.S3Constants.TAG_KEY_RVF_NMVERS;
import static it.eng.parer.ws.utils.Costanti.S3Constants.TAG_KEY_VRSOBJ_TYPE;
import static it.eng.parer.ws.utils.Costanti.S3Constants.TAG_VALUE_VRSOBJ_FILE_COMP;
import static it.eng.parer.ws.utils.Costanti.S3Constants.TAG_VALUE_VRSOBJ_METADATI;
import static it.eng.parer.ws.utils.Costanti.S3Constants.TAG_VALUE_VRSOBJ_TMP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroCompDoc;
import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroUpdUnitaDoc;
import it.eng.parer.entity.DecServizioVerificaCompDoc;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiEntitaAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroVersIniDatiSpec.TiEntitaSacerAroVersIniDatiSpec;
import it.eng.parer.entity.inheritance.oop.AroXmlObjectStorage;
import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.utils.CRC32CChecksum;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.versamento.dto.BackendStorage;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.ObjectStorageBackend;
import it.eng.parer.ws.versamento.dto.ObjectStorageResource;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import it.eng.parer.ws.versamento.ejb.help.SalvataggioBackendHelper;
import it.eng.parer.ws.versamento.exceptions.ObjectStorageException;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.spagoCore.util.UUIDMdcLogUtil;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.Tag;

@Stateless(mappedName = "ObjectStorageService")
@LocalBean
public class ObjectStorageService {

    private final Logger log = LoggerFactory.getLogger(ObjectStorageService.class);

    private static final String STAGING_W = "WRITE_STAGING";
    private static final String COMPONENTI_W = "WRITE_COMPONENTI";
    private static final String SIP_W = "WRITE_SIP";
    private static final String SIP_R = "READ_SIP";
    private static final String REPORTVF_W = "WRITE_REPORTVF";
    // MEV#29276
    private static final String AGG_MD_W = "WRITE_AGG_MD";
    private static final String AGG_MD_SES_ERR_KO_W = "WRITE_SESSIONI_AGG_MD_ERR_KO";
    // end MEV#29276

    private static final String PATTERN_FORMAT = "yyyyMMdd/HH/mm";
    //
    private static final int BUFFER_SIZE = 10 * 1024 * 1024;
    private static final String STD_FILE_SAVED_LOG_MESSAGE = "Salvato file {}/{}";

    @EJB
    private SalvataggioBackendHelper salvataggioBackendHelper;

    /**
     * Effettua la lookup per stabilire come sia configurato il backend per l'area di staging
     *
     * @return tipologia di backend. Al momento sono supportate {@link BackendStorage.STORAGE_TYPE#BLOB} e
     *         {@link BackendStorage.STORAGE_TYPE#OS}
     */
    public BackendStorage lookupBackendVrsStaging() {
        try {
            String tipoBackend = salvataggioBackendHelper.getBackendStaging();

            // tipo backend
            return salvataggioBackendHelper.getBackend(tipoBackend);

        } catch (ObjectStorageException e) {
            // EJB spec (14.2.2 in the EJB 3)
            throw new EJBException(e);
        }
    }

    // MEV#29276
    /**
     * Effettua la lookup per stabilire come sia configurato il backend per le sessioni errate/fallite
     * dell'aggiornamento metadati
     *
     * @return tipologia di backend. Al momento sono supportate {@link BackendStorage.STORAGE_TYPE#BLOB} e
     *         {@link BackendStorage.STORAGE_TYPE#OS}
     */
    public BackendStorage lookupBackendVrsSessioniErrKoAggMd() {
        try {
            String tipoBackend = salvataggioBackendHelper.getBackendSessioniErrKoAggMd();

            // tipo backend
            return salvataggioBackendHelper.getBackend(tipoBackend);

        } catch (Exception e) {
            // EJB spec (14.2.2 in the EJB 3)
            throw new EJBException(e);
        }
    }
    // end MEV#29276

    /**
     * Effettua il lookup per stabilire come sia configurato il backend per ogni tipo di servizio
     *
     * @param idTipoUnitaDoc
     *            id tipologia di unità documentaria
     * @param nomeWs
     *            nome del servizio (vedere {@link IWSDesc})
     *
     * @return tipologia di backend. Al momento sono supportate {@link BackendStorage.STORAGE_TYPE#BLOB} e
     *         {@link BackendStorage.STORAGE_TYPE#OS}
     */
    public BackendStorage lookupBackendByServiceName(long idTipoUnitaDoc, String nomeWs) {
        try {

            String tipoBackend = salvataggioBackendHelper.getBackendByServiceName(idTipoUnitaDoc, nomeWs);
            return salvataggioBackendHelper.getBackend(tipoBackend);

        } catch (ObjectStorageException e) {
            // EJB spec (14.2.2 in the EJB 3)
            throw new EJBException(e);
        }
    }

    /**
     * Salva il file nel bucket di Staging.
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param resource
     *            file da salvare
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createTmpResourceInStaging(String nomeBackend, File resource) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    STAGING_W);
            // 1. s3 key object
            final String key = createRandomKeyWithPrefix(KEY_PREFIX_TMP);
            // 2. generate std tag
            Set<Tag> tags = new HashSet<>();
            tags.add(Tag.builder().key(TAG_KEY_VRSOBJ_TYPE).value(TAG_VALUE_VRSOBJ_TMP).build());

            // 3. put on o.s. + save link
            try (FileInputStream fis = new FileInputStream(resource)) {

                ObjectStorageResource savedFile = salvataggioBackendHelper.putObject(fis, resource.length(), key,
                        configuration, Optional.empty(), Optional.of(tags),
                        Optional.of(calculateFileCRC32CBase64(resource.toPath())));
                log.debug(STD_FILE_SAVED_LOG_MESSAGE, savedFile.getBucket(), savedFile.getKey());

                return savedFile;
            }
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }

    }

    /**
     * Salva i metadati nel bucket di Staging.
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param xmlFiles
     *            mappa con i metadati da salvare
     * @param idDatiSessioneVersKo
     *            id dati sessione di versamento
     * @param idStrut
     *            id della struttura versante
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createSipInStaging(String nomeBackend, Map<String, String> xmlFiles,
            long idDatiSessioneVersKo, BigDecimal idStrut) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    STAGING_W);
            // generate std tag
            Set<Tag> tags = new HashSet<>();
            tags.add(Tag.builder().key(TAG_KEY_VRSOBJ_TYPE).value(TAG_VALUE_VRSOBJ_METADATI).build());
            // put on O.S. (s3 key prefix)
            ObjectStorageResource savedFile = createSipXmlMapAndPutOnBucketWithKeyPrefix(KEY_PREFIX_SIPPUD, xmlFiles,
                    configuration, tags);
            // link
            salvataggioBackendHelper.saveObjectStorageLinkSipSessione(savedFile, nomeBackend, idDatiSessioneVersKo,
                    idStrut);
            return savedFile;
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }
    }

    /**
     * Aggiunti l'oggetto all'object storage di destinazione, sul bucket definitivo.
     *
     * @param strutV
     *            wrapper informazioni versamento
     * @param compntEntity
     *            entity {@link AroCompDoc}
     *
     * @param nomeBackendDest
     *            nome del backend di destinazione
     * @param riferimentoBlob
     *            File (o riferimento) da caricare
     *
     * @return Dettaglio della risorsa caricata
     */
    public ObjectStorageResource createOrCopyResourceInComponenti(StrutturaVersamento strutV, AroCompDoc compntEntity,
            String nomeBackendDest, FileBinario riferimentoBlob) {
        try {

            ObjectStorageBackend destinationConfiguration = salvataggioBackendHelper
                    .getObjectStorageConfiguration(nomeBackendDest, COMPONENTI_W);

            final String urn = calculateS3UrnComponente(strutV, compntEntity);
            // generate destination s3 key object
            final String destKey = createRandomKeyWithUrn(urn);

            ObjectStorageResource newComponente;
            // stabilisci dove sia l'oggetto in staging (object storage o filesystem).
            BackendStorage lookupBackendStaging = lookupBackendVrsStaging();
            if (lookupBackendStaging.isObjectStorage()) {
                ObjectStorageBackend sourceConfiguration = salvataggioBackendHelper
                        .getObjectStorageConfiguration(lookupBackendStaging.getBackendName(), STAGING_W);
                final String sourceKey = riferimentoBlob.getObjectStorageResource().getKey();
                newComponente = salvataggioBackendHelper.getObjectFromSrcAndPutObjectOnDest(sourceKey,
                        sourceConfiguration, destKey, destinationConfiguration);
            } else {
                newComponente = createResourceInComponenti(riferimentoBlob.getFileSuDisco(), destKey,
                        destinationConfiguration);
            }

            salvataggioBackendHelper.saveObjectStorageLinkCompDoc(newComponente, nomeBackendDest,
                    compntEntity.getIdCompDoc().longValue());

            return newComponente;

        } catch (ObjectStorageException ex) {
            throw new EJBException(ex);
        }

    }

    private String calculateS3UrnComponente(StrutturaVersamento strutV, AroCompDoc compntEntity) {
        AroDoc docRif = compntEntity.getAroStrutDoc().getAroDoc();
        // 1. calc URN / S3 KEY
        // 2. calc particial parts ***
        // calculate ud base urn
        String tmpUrnUd = MessaggiWSFormat.formattaS3UrnPartUd(strutV.getVersatoreNonverificato(),
                strutV.getChiaveNonVerificata());
        String tmpUrnDoc = null;

        int progDoc = Objects.isNull(docRif.getNiOrdDoc()) ? docRif.getPgDoc().intValue()
                : docRif.getNiOrdDoc().intValue();
        String tmpUrnPartDoc = MessaggiWSFormat.formattaS3UrnPartDoc(progDoc, true);

        // calculate comp base urn
        if (compntEntity.getAroCompDoc() != null) {
            // E' UN SOTTOCOMPONENTE
            // DOCXXXXX:NNNNN
            tmpUrnDoc = MessaggiWSFormat.formattaS3UrnPartComp(tmpUrnPartDoc,
                    compntEntity.getNiOrdCompDoc().intValue());
            // DOCXXXXX:NNNNN:KK
            tmpUrnDoc = MessaggiWSFormat.formattaS3UrnPartComp(tmpUrnDoc, compntEntity.getNiOrdCompDoc().intValue());
        } else {
            // DOCXXXXX:NNNNN
            tmpUrnDoc = MessaggiWSFormat.formattaS3UrnPartComp(tmpUrnPartDoc,
                    compntEntity.getNiOrdCompDoc().intValue());
        }
        // ****
        // 3. calc complete URN
        return MessaggiWSFormat.formattaS3CompleteUrnDoc(tmpUrnUd, tmpUrnDoc);
    }

    private ObjectStorageResource createResourceInComponenti(File componente, String destKey,
            ObjectStorageBackend destConfiguration) throws ObjectStorageException {

        try (FileInputStream fis = new FileInputStream(componente)) {
            return salvataggioBackendHelper.putObject(fis, componente.length(), destKey, destConfiguration,
                    Optional.empty(), Optional.empty(), Optional.of(calculateFileCRC32CBase64(componente.toPath())));
        } catch (IOException e) {
            throw ObjectStorageException.builder()
                    .message("Impossibile caricare il file {0} sul bucket {1} con chiave {2}", componente.getName(),
                            destConfiguration.getBucket(), destKey)
                    .cause(e).build();
        }
    }

    /**
     * Copia la componente su bucket staging con prefisso 'tmp/' creando un nuovo oggetto. Inoltre, persiste nella
     * tabella di raccordo <code>VRS_FILE_SES_OBJECT_STORAGE_KO</code>, la chiave del file presente nell'area di staging
     * che rappresenta il versamento fallito.
     *
     * @param nomeBackend
     *            nome del backend della suddetta risorsa
     * @param stagingResource
     *            coordinate risorsa su bucket staging
     * @param idFileSessione
     *            identificativo del file della sessione
     * @param idStrut
     *            id della struttura versante
     *
     * @return resituisce {@link ObjectStorageResource} con i riferimenti dell'oggetto copiato
     */
    public ObjectStorageResource copyTmpResourceToCompUdInStaging(String nomeBackend,
            ObjectStorageResource stagingResource, long idFileSessione, BigDecimal idStrut) {
        try {
            // 0. get staging configuration
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    STAGING_W);

            // 1. generate destination s3 key object
            final String destKey = createRandomKeyWithPrefix(KEY_PREFIX_COMPUD);
            // 2. get source key
            final String sourceKey = stagingResource.getKey();
            ObjectStorageResource newComponenteInStaging = salvataggioBackendHelper
                    .getObjectFromSrcAndPutObjectOnDest(sourceKey, configuration, destKey, configuration);

            salvataggioBackendHelper.saveObjectStorageLinkFileSessione(newComponenteInStaging, nomeBackend,
                    idFileSessione, idStrut);

            return newComponenteInStaging;

        } catch (ObjectStorageException ex) {
            throw new EJBException(ex);
        }

    }

    /**
     * Salva il file nel bucket dei SIP per l'unita documentaria
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param xmlFiles
     *            file Xml da salvare (previa creazione file zip)
     * @param strutV
     *            wrapper dati di versamento
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createResourcesInSipUnitaDoc(String nomeBackend, Map<String, String> xmlFiles,
            StrutturaVersamento strutV) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    SIP_W);
            // calculate urn
            String tmpUrnUd = MessaggiWSFormat.formattaS3UrnPartUd(strutV.getVersatoreNonverificato(),
                    strutV.getChiaveNonVerificata());
            final String urn = MessaggiWSFormat.formattaS3CompleteUrnSipUd(tmpUrnUd);

            // put on O.S.
            ObjectStorageResource savedFile = createSipXmlMapAndPutOnBucketWithKeyUrn(urn, xmlFiles, configuration,
                    SetUtils.emptySet());
            // link
            salvataggioBackendHelper.saveObjectStorageLinkSipUd(savedFile, nomeBackend, strutV.getIdUnitaDoc());
            return savedFile;
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }

    }

    /**
     * Salva il file nel bucket dei SIP per il documento
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param xmlFiles
     *            file Xml da salvare (previa creazione file zip)
     * @param strutV
     *            wrapper dati di versamento
     * @param idDoc
     *            id documento
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createResourcesInSipDocumento(String nomeBackend, Map<String, String> xmlFiles,
            StrutturaVersamento strutV, long idDoc) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    SIP_W);

            // calculate urn
            String tmpUrnUd = MessaggiWSFormat.formattaS3UrnPartUd(strutV.getVersatoreNonverificato(),
                    strutV.getChiaveNonVerificata());
            final String urn = MessaggiWSFormat.formattaS3CompleteUrnSipDoc(tmpUrnUd);

            // put on O.S.
            ObjectStorageResource savedFile = createSipXmlMapAndPutOnBucketWithKeyUrn(urn, xmlFiles, configuration,
                    SetUtils.emptySet());
            // link
            salvataggioBackendHelper.saveObjectStorageLinkSipDoc(savedFile, nomeBackend, idDoc);
            return savedFile;
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }

    }

    // MEV#29276
    /**
     * Salva i metadati nel bucket delle sessioni fallite.
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param xmlFiles
     *            mappa con i metadati da salvare
     * @param idSesUpdUnitaDocKo
     *            id sessione fallita
     * @param idStrut
     *            id della struttura versante
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createSipInSessioniKoAggMd(String nomeBackend, Map<String, String> xmlFiles,
            long idSesUpdUnitaDocKo, BigDecimal idStrut) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    AGG_MD_SES_ERR_KO_W);
            // punt on O.S.
            ObjectStorageResource savedFile = createSipXmlMapAndPutOnBucket(xmlFiles, configuration,
                    SetUtils.emptySet());
            // link
            salvataggioBackendHelper.saveObjectStorageLinkSipSessioneKoAggMd(savedFile, nomeBackend, idSesUpdUnitaDocKo,
                    idStrut);
            return savedFile;
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }
    }

    /**
     * Salva i metadati nel bucket delle sessioni errate.
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param xmlFiles
     *            mappa con i metadati da salvare
     * @param idSesUpdUnitaDocErr
     *            id sessione errata
     * @param idStrut
     *            id della struttura versante
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createSipInSessioniErrAggMd(String nomeBackend, Map<String, String> xmlFiles,
            long idSesUpdUnitaDocErr, BigDecimal idStrut) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    AGG_MD_SES_ERR_KO_W);
            // punt on O.S.
            ObjectStorageResource savedFile = createSipXmlMapAndPutOnBucket(xmlFiles, configuration,
                    SetUtils.emptySet());
            // link
            salvataggioBackendHelper.saveObjectStorageLinkSipSessioneErrAggMd(savedFile, nomeBackend,
                    idSesUpdUnitaDocErr, idStrut);
            return savedFile;
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }
    }

    /**
     * Salva il file nel bucket dei SIP per l'aggiornamento metadati
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param xmlFiles
     *            file Xml da salvare (previa creazione file zip)
     * @param strutUpdVers
     *            struttura aggiornamento versamento
     * @param tmpAroUpdUnitaDoc
     *            aggiornamento metadati
     * @param idStrut
     *            id della struttura versante
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createResourcesInSipAggMd(String nomeBackend, Map<String, String> xmlFiles,
            StrutturaUpdVers strutUpdVers, AroUpdUnitaDoc tmpAroUpdUnitaDoc, BigDecimal idStrut) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    AGG_MD_W);

            // calculate urn
            final String urn = calculateS3UrnAggMd(strutUpdVers, tmpAroUpdUnitaDoc);

            // put on O.S.
            ObjectStorageResource savedFile = createSipXmlMapAndPutOnBucketWithKeyUrn(urn, xmlFiles, configuration,
                    SetUtils.emptySet());
            // link
            salvataggioBackendHelper.saveObjectStorageLinkSipAggMd(savedFile, nomeBackend,
                    tmpAroUpdUnitaDoc.getIdUpdUnitaDoc(), idStrut);
            return savedFile;
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }

    }

    /**
     * Salva il file nel bucket dei Dati Specifici per l'aggiornamento metadati
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param xmlFiles
     *            file Xml da salvare (previa creazione file zip)
     * @param idEntitySacerUpd
     *            id aggiornamento metadati
     * @param tipoEntitySacerUpd
     *            tipo entità aggiornamento (per esempio UPD_UNI_DOC)
     * @param strutUpdVers
     *            struttura aggiornamento versamento
     * @param tmpAroUpdUnitaDoc
     *            aggiornamento metadati
     * @param idStrut
     *            id della struttura versante
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createResourcesInUpdDatiSpecAggMd(String nomeBackend, Map<String, String> xmlFiles,
            long idEntitySacerUpd, TiEntitaAroUpdDatiSpecUnitaDoc tipoEntitySacerUpd, StrutturaUpdVers strutUpdVers,
            AroUpdUnitaDoc tmpAroUpdUnitaDoc, BigDecimal idStrut) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    AGG_MD_W);

            // calculate urn
            final String urn = calculateS3UrnAggMd(strutUpdVers, tmpAroUpdUnitaDoc);

            // put on O.S.
            ObjectStorageResource savedFile = createDatiSpecXmlMapAndPutOnBucket(urn, xmlFiles, configuration,
                    SetUtils.emptySet());
            // link
            salvataggioBackendHelper.saveObjectStorageLinkUpdDatiSpecAggMd(savedFile, nomeBackend, idEntitySacerUpd,
                    tipoEntitySacerUpd, idStrut);
            return savedFile;
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }

    }

    /**
     * Salva il file nel bucket dei Dati Specifici relativi ai metadati iniziali per l'aggiornamento metadati
     *
     * @param nomeBackend
     *            backend configurato (per esempio OBJECT_STORAGE_PRIMARIO)
     * @param xmlFiles
     *            file Xml da salvare (previa creazione file zip)
     * @param idEntitySacerVersIni
     *            id versamento iniziale
     * @param tipoEntitySacerVersIni
     *            tipo entità versamento iniziale (per esempio UNI_DOC)
     * @param strutUpdVers
     *            struttura aggiornamento versamento
     * @param tmpAroUpdUnitaDoc
     *            aggiornamento metadati
     * @param idStrut
     *            id della struttura versante
     *
     * @return risorsa su OS che identifica il file caricato
     */
    public ObjectStorageResource createResourcesInVersIniDatiSpecAggMd(String nomeBackend, Map<String, String> xmlFiles,
            long idEntitySacerVersIni, TiEntitaSacerAroVersIniDatiSpec tipoEntitySacerVersIni,
            StrutturaUpdVers strutUpdVers, AroUpdUnitaDoc tmpAroUpdUnitaDoc, BigDecimal idStrut) {
        try {
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    AGG_MD_W);

            // calculate urn
            final String urn = calculateS3UrnAggMd(strutUpdVers, tmpAroUpdUnitaDoc);

            // put on O.S.
            ObjectStorageResource savedFile = createDatiSpecXmlMapAndPutOnBucket(urn, xmlFiles, configuration,
                    SetUtils.emptySet());
            // link
            salvataggioBackendHelper.saveObjectStorageLinkVersIniDatiSpecAggMd(savedFile, nomeBackend,
                    idEntitySacerVersIni, tipoEntitySacerVersIni, idStrut);
            return savedFile;
        } catch (ObjectStorageException | IOException ex) {
            throw new EJBException(ex);
        }

    }

    private String calculateS3UrnAggMd(StrutturaUpdVers strutUpdVers, AroUpdUnitaDoc tmpAroUpdUnitaDoc) {

        // 1. calc URN / S3 KEY
        // 2. calc particial parts ***
        // calculate agg_md base urn
        String tmpUrnVersAggMd = MessaggiWSFormat.formattaS3UrnPartVersAggMd(strutUpdVers.getVersatoreNonverificato());
        String tmpUrnKeyAggMd = MessaggiWSFormat.formattaS3UrnPartKeyAggMd(strutUpdVers.getChiaveNonVerificata());
        // ****
        // 3. calc complete URN
        return MessaggiWSFormat.formattaS3CompleteUrnAggMd(tmpUrnVersAggMd, tmpUrnKeyAggMd,
                tmpAroUpdUnitaDoc.getPgUpdUnitaDoc().longValue(), true, Costanti.UrnFormatter.PAD5DIGITS_FMT);
    }

    private ObjectStorageResource createDatiSpecXmlMapAndPutOnBucket(final String urn, Map<String, String> xmlFiles,
            ObjectStorageBackend configuration, Set<Tag> tags) throws IOException, ObjectStorageException {
        ObjectStorageResource savedFile = null;
        Path tempZip = Files.createTempFile("dati_spec-", ".zip");
        //
        try (InputStream is = Files.newInputStream(tempZip)) {
            // create key
            final String key = createRandomKeyWithUrn(urn) + ".zip";
            // create zip file
            createZipFile(xmlFiles, tempZip);
            // put on O.S.
            savedFile = salvataggioBackendHelper.putObject(is, Files.size(tempZip), key, configuration,
                    Optional.empty(), Optional.of(tags), Optional.of(calculateFileCRC32CBase64(tempZip)));
            log.debug(STD_FILE_SAVED_LOG_MESSAGE, savedFile.getBucket(), savedFile.getKey());
        } finally {
            if (tempZip != null) {
                Files.delete(tempZip);
            }
        }

        return savedFile;
    }
    // end MEV#29276

    private ObjectStorageResource createSipXmlMapAndPutOnBucket(final Map<String, String> xmlFiles,
            ObjectStorageBackend configuration, final Set<Tag> tags) throws IOException, ObjectStorageException {

        return createSipXmlMapAndPutOnBucket(Optional.empty(), Optional.empty(), xmlFiles, configuration, tags);
    }

    private ObjectStorageResource createSipXmlMapAndPutOnBucketWithKeyPrefix(final String prefix,
            final Map<String, String> xmlFiles, ObjectStorageBackend configuration, final Set<Tag> tags)
            throws IOException, ObjectStorageException {

        return createSipXmlMapAndPutOnBucket(Optional.of(prefix), Optional.empty(), xmlFiles, configuration, tags);
    }

    private ObjectStorageResource createSipXmlMapAndPutOnBucketWithKeyUrn(final String urn,
            final Map<String, String> xmlFiles, ObjectStorageBackend configuration, final Set<Tag> tags)
            throws IOException, ObjectStorageException {

        return createSipXmlMapAndPutOnBucket(Optional.empty(), Optional.of(urn), xmlFiles, configuration, tags);
    }

    private ObjectStorageResource createSipXmlMapAndPutOnBucket(Optional<String> keyPrefix, Optional<String> urn,
            final Map<String, String> xmlFiles, ObjectStorageBackend configuration, final Set<Tag> tags)
            throws IOException, ObjectStorageException {
        ObjectStorageResource savedFile = null;
        Path tempZip = Files.createTempFile("sip-", ".zip");
        //
        try (InputStream is = Files.newInputStream(tempZip)) {
            // create key
            final String key = createRandomKey(keyPrefix, urn) + ".zip";
            // create zip file
            createZipFile(xmlFiles, tempZip);
            // put on O.S.
            savedFile = salvataggioBackendHelper.putObject(is, Files.size(tempZip), key, configuration,
                    Optional.empty(), Optional.of(tags), Optional.of(calculateFileCRC32CBase64(tempZip)));
            log.debug(STD_FILE_SAVED_LOG_MESSAGE, savedFile.getBucket(), savedFile.getKey());
        } finally {
            if (tempZip != null) {
                Files.delete(tempZip);
            }
        }

        return savedFile;
    }

    /**
     * Invio report verifica firma (zip archive)
     *
     * A differenza degli altri metodi definiti in questa classe, questo <strong> può fallire</strong>. In caso di
     * fallimento, quindi, verrà restituto <em>null</em>.
     *
     * @param nomeBackend
     *            ome del backend dei report della verifica delle firme
     * @param compntEntity
     *            componente {@link AroCompDoc}
     * @param strutV
     *            wrapper dati versamento
     * @param servizioFirma
     *            servizio verifica firma
     * @param reportZip
     *            report verifia firma (zip archive)
     *
     * @return risorsa su OS che identifica il file caricato oppure null
     *
     * @throws ObjectStorageException
     *             eccezione generica
     */
    public ObjectStorageResource createResourceInRerportvf(String nomeBackend, AroCompDoc compntEntity,
            StrutturaVersamento strutV, DecServizioVerificaCompDoc servizioFirma, Path reportZip)
            throws ObjectStorageException {
        //
        try (InputStream is = Files.newInputStream(reportZip)) {

            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    REPORTVF_W);

            // calculate key
            String tmpUrnComp = calculateS3UrnComponente(strutV, compntEntity);
            final String urn = MessaggiWSFormat.formattaS3CompleteUrnReportvf(tmpUrnComp);
            // s3 key object
            final String key = createRandomKeyWithUrn(urn) + ".zip";

            // 1. generate tags
            Set<Tag> tags = new HashSet<>();
            // Opzionale
            tags.add(Tag.builder().key(TAG_KEY_RVF_CDSERV).value(servizioFirma.getCdServizio().toString()).build());
            tags.add(Tag.builder().key(TAG_KEY_RVF_NMVERS).value(servizioFirma.getNmVersione()).build());

            ObjectStorageResource resource = salvataggioBackendHelper.putObject(is, Files.size(reportZip), key,
                    configuration, Optional.empty(), Optional.of(tags),
                    Optional.of(calculateFileCRC32CBase64(reportZip)));

            log.debug("Salvato {} sul bucket {} con eTag {}", key, resource.getBucket(), resource.getETag());

            return resource;
        } catch (IOException ex) {
            log.error("createResourceInRerportvf errore su invio report ad object storage", ex);
            return null;
        }
    }

    /**
     * Crea i file zip contenente i vari xml di versamento.Possono essere di tipo:
     * <ul>
     * <li>{@link CostantiDB.TipiXmlDati#RICHIESTA}, obbligatorio è il sip di versamento</li>
     * <li>{@link CostantiDB.TipiXmlDati#RISPOSTA}, obbligatorio è la risposta del sip di versamento</li>
     * <li>{@link CostantiDB.TipiXmlDati#RAPP_VERS}, obbligatorio è il rapporto di versamento</li>
     * <li>{@link CostantiDB.TipiXmlDati#INDICE_FILE}, è presente solo nel caso di Versamento Multimedia</li>
     * </ul>
     *
     *
     * @param xmlFiles
     *            mappa dei file delle tipologie indicate in descrizione.
     * @param zipFile
     *            file zip su cui salvare tutto
     *
     * @throws IOException
     *             in caso di errore
     */
    private void createZipFile(Map<String, String> xmlFiles, Path zipFile) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (Entry<String, String> sipBlob : xmlFiles.entrySet()) {
                ZipEntry entry = new ZipEntry(sipBlob.getKey() + ".xml");
                out.putNextEntry(entry);
                out.write(sipBlob.getValue().getBytes());
                out.closeEntry();
            }
        }
    }

    private static String createRandomKeyWithPrefix(final String prefix) {
        return createRandomKey(Optional.of(prefix), Optional.empty());
    }

    private static String createRandomKeyWithUrn(final String urn) {
        return createRandomKey(Optional.empty(), Optional.of(urn));
    }

    /**
     * Crea una chiave utilizzando i seguenti elementi separati dal carattere <code>/</code>:
     * <ul>
     * <li>(opzionale) prefix, ossia prefisso della chiave (per esempio <strong>tmp</strong>)</li>
     * <li>data in formato anno mese giorno (per esempio <strong>20221124</strong>)</li>
     * <li>ora a due cifre (per esempio <strong>14</strong>)</li>
     * <li>minuto a due cifre (per esempio <strong>05</strong>)</li>
     * <li>(default) UUID sessione di versamento <strong>550e8400-e29b-41d4-a716-446655440000</strong>) recuperato
     * dall'MDC ossia dal Mapped Diagnostic Context (se non esiste viene generato comunque un UUID)</li>
     * <li>(opzionale) URN dell'oggetto pre-calcolato</li>
     * <li>UUID generato runtime <strong>28fd282d-fbe6-4528-bd28-2dfbe685286f</strong>) per ogni oggetto caricato</li>
     * </ul>
     *
     * Esempio di chiave completa:
     * <code>20221124/14/05/550e8400-e29b-41d4-a716-446655440000/28fd282d-fbe6-4528-bd28-2dfbe685286f</code>
     *
     * @return chiave dell'oggetto
     */
    private static String createRandomKey(Optional<String> prefix, Optional<String> urn) {

        String when = DateTimeFormatter.ofPattern(PATTERN_FORMAT).withZone(ZoneId.systemDefault())
                .format(Instant.now());

        return (prefix.isPresent() ? prefix.get() + "/" : StringUtils.EMPTY) + when + "/"
                + (urn.isPresent() ? urn.get() : UUIDMdcLogUtil.getUuid()) + "/" + UUID.randomUUID().toString();
    }

    /**
     * Calcola il checksum CRC32C (base64 encoded) del file da inviare via S3
     *
     * Nota: questa scelta deriva dal modello supportato dal vendor
     * (https://docs.aws.amazon.com/AmazonS3/latest/userguide/checking-object-integrity.html)
     *
     * @param path
     *            file
     *
     * @return rappresentazione base64 del contenuto calcolato
     *
     * @throws IOException
     *             errore generico
     */
    private String calculateFileCRC32CBase64(Path resource) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int readed;
        CRC32CChecksum crc32c = new CRC32CChecksum();
        try (InputStream is = Files.newInputStream(resource)) {
            while ((readed = is.read(buffer)) != -1) {
                crc32c.update(buffer, 0, readed);
            }
        }
        return Base64.getEncoder().encodeToString(crc32c.getValueAsBytes());
    }

    /**
     * Ottieni, in una mappa, la lista degli xml di versamento classificati nelle tipologie definite qui
     * {@link it.eng.parer.ws.utils.CostantiDB.TipiXmlDati} per l'unita documentaria
     *
     * @param idUnitaDoc
     *            id unita documentaria
     *
     * @return mappa degli XML
     */
    public Map<String, String> getObjectSipUnitaDoc(long idUnitaDoc) {
        try {
            return getObjectSip(salvataggioBackendHelper.getLinkSipUnitaDocOs(idUnitaDoc));
        } catch (IOException | ObjectStorageException e) {
            // EJB spec (14.2.2 in the EJB 3)
            throw new EJBException(e);
        }
    }

    /**
     * Ottieni, in una mappa, la lista degli xml di versamento classificati nelle tipologie definite qui
     * {@link it.eng.parer.ws.utils.CostantiDB.TipiXmlDati} per il documento
     *
     * @param idDoc
     *            id documento
     *
     * @return mappa degli XML
     */
    public Map<String, String> getObjectSipDoc(long idDoc) {
        try {
            return getObjectSip(salvataggioBackendHelper.getLinkSipDocOs(idDoc));
        } catch (IOException | ObjectStorageException e) {
            // EJB spec (14.2.2 in the EJB 3)
            throw new EJBException(e);
        }
    }

    private Map<String, String> getObjectSip(AroXmlObjectStorage xmlObjectStorage)
            throws ObjectStorageException, IOException {
        if (!Objects.isNull(xmlObjectStorage)) {
            ObjectStorageBackend config = salvataggioBackendHelper
                    .getObjectStorageConfiguration(xmlObjectStorage.getDecBackend().getNmBackend(), SIP_R);
            ResponseInputStream<GetObjectResponse> object = salvataggioBackendHelper.getObject(config,
                    xmlObjectStorage.getNmBucket(), xmlObjectStorage.getCdKeyFile());
            return unzipFile(object);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<String, String> unzipFile(ResponseInputStream<GetObjectResponse> inputStream) throws IOException {
        // TipiXmlDati
        final String xml = ".xml";
        final Map<String, String> xmlVers = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(inputStream);) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String value = IOUtils.toString(zis, StandardCharsets.UTF_8);
                if (ze.getName().equals(CostantiDB.TipiXmlDati.RICHIESTA + xml)) {
                    xmlVers.put(CostantiDB.TipiXmlDati.RICHIESTA, value);
                } else if (ze.getName().equals(CostantiDB.TipiXmlDati.RISPOSTA + xml)) {
                    xmlVers.put(CostantiDB.TipiXmlDati.RISPOSTA, value);
                } else if (ze.getName().equals(CostantiDB.TipiXmlDati.RAPP_VERS + xml)) {
                    xmlVers.put(CostantiDB.TipiXmlDati.RAPP_VERS, value);
                } else if (ze.getName().equals(CostantiDB.TipiXmlDati.INDICE_FILE + xml)) {
                    xmlVers.put(CostantiDB.TipiXmlDati.INDICE_FILE, value);
                } else {
                    log.warn(
                            "Attenzione, l'entry con nome {} non è stata riconosciuta nel file zip dei SIP di versamento",
                            ze.getName());
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
        }
        return xmlVers;
    }

    /**
     *
     * @deprecated deprecato, api S3 (object-tagging) not implemented on GCP
     *
     *             Tagging della componente su bucket per gestione lifecycle su tagging
     *             vrs-object-type=file_componente_uddoc
     *
     * @param stagingResource
     *            risorsa salvata in staging
     * @param nomeBackend
     *            nome del backend della suddetta risorsa
     */
    @Deprecated(since = "Integrazione GCP (tagging not implemented)", forRemoval = true)
    public void tagComponenteInStaging(ObjectStorageResource stagingResource, String nomeBackend) {
        try {
            // get base config for staging writing
            ObjectStorageBackend configuration = salvataggioBackendHelper.getObjectStorageConfiguration(nomeBackend,
                    STAGING_W);

            // generate std tag
            Set<Tag> tags = new HashSet<>();
            tags.add(Tag.builder().key(TAG_KEY_VRSOBJ_TYPE).value(TAG_VALUE_VRSOBJ_FILE_COMP).build());

            salvataggioBackendHelper.taggingObject(stagingResource, configuration, tags);
        } catch (ObjectStorageException ex) {
            throw new EJBException(ex);

        }
    }

}
