package it.eng.parer.firma.ejb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Tag;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.entity.DecServizioVerificaCompDoc;
import it.eng.parer.entity.FirUrnReport;
import it.eng.parer.entity.constraint.FiUrnReport.TiUrnReport;
import it.eng.parer.exception.SacerWsException;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.utils.Costanti.AwsFormatter;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import static it.eng.parer.util.DateUtilsConverter.format;

@Stateless(mappedName = "VerificaFirmaReportAwsClient")
@LocalBean
public class VerificaFirmaReportAwsClient {

    private static final Logger LOG = LoggerFactory.getLogger(VerificaFirmaReportAwsClient.class);

    private AmazonS3 awsClient = null;

    @EJB
    private ConfigurationHelper configHelper;

    /*
     * <p> Buffer per la lettura da blob. Il valore è stato preso da {@link BufferedInputStream#DEFAULT_BUFFER_SIZE}.
     * </p> <p> Hint per la scelta della dimensione del buffer: </p> <pre> {@code SELECT AVG(DBMS_LOB.GETLENGTH
     * ("BL_CONTEN_COMP")) as "Media byte di un documento" FROM aro_contenuto_comp WHERE rownum < 1000000 } </pre> <p>
     * Il risultato della dimensione media di un blob (per 1M di blob inseriti) è
     * <em>253337,3456413456413456413456413456413456</em> byte. </p>
     */
    private static final int BUFFER = 8192;

    private String bucketName;

    /**
     * Inizializzazione del client S3. L'indirizzo corretto dovrebbe essere
     * https://s3.storagegrid.ente.regione.emr.it:8082
     */
    @PostConstruct
    private void init() {
        // address
        String storageAddress = configHelper.getParamApplicValue(ParametroApplDB.OBJECT_STORAGE_ADDR);
        // aws credentials
        final String reportvfAccessKeyId = configHelper.getParamApplicValue(ParametroApplDB.REPORTVF_S3_ACCESS_KEY_ID);
        final String reportvfSecretKeyId = configHelper.getParamApplicValue(ParametroApplDB.REPORTVF_S3_SECRET_KEY);
        // recupero le system properties
        final String accessKeyId = System.getProperty(reportvfAccessKeyId);
        final String secretKey = System.getProperty(reportvfSecretKeyId);
        // create basic credentials
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretKey);

        LOG.info("Sto per effettuare il collegamento all'endpoint S3 [{}]", storageAddress);
        awsClient = AmazonS3Client.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(storageAddress, Regions.US_EAST_1.name()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withPathStyleAccessEnabled(Boolean.TRUE)
                .build();

        // bucket
        bucketName = configHelper.getParamApplicValue(ParametroApplDB.BUCKET_REPORT_VERIFICAFIRMA);
    }

    /**
     * Invio report verifica firma (zip archive)
     *
     * @param idCompDoc
     *            id componente
     * @param idFirReport
     *            id report
     * @param strutV
     *            wrapper dati versamento
     * @param servizioFirma
     *            servizio verifica firma
     * @param firUrnReports
     *            URN generati
     * @param reportZip
     *            report verifia firma (zip archive)
     *
     * @return mappa immutabile (single element) con risultato chiamata e chiave file calcolato
     */
    public Map<String, PutObjectResult> sendReportZipToObjStorage(long idCompDoc, long idFirReport,
            StrutturaVersamento strutV, DecServizioVerificaCompDoc servizioFirma, List<FirUrnReport> firUrnReports,
            Path reportZip) {
        try {
            // calculate key
            final String cdKeyFile = MessaggiWSFormat.formattaComponenteCdKeyFile(strutV.getVersatoreNonverificato(),
                    strutV.getChiaveNonVerificata(), idCompDoc, Optional.of(idFirReport),
                    AwsFormatter.COMP_REPORTVF_CD_KEY_FILE_FMT);

            // 1. generate metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata("cd-service", servizioFirma.getCdServizio().toString());
            metadata.addUserMetadata("nm-version", servizioFirma.getNmVersione());
            metadata.addUserMetadata("creation-date", format(strutV.getDataVersamento()));
            metadata.setContentType("Content-Type: application/zip; charset=UTF-8");
            metadata.setContentLength(reportZip.toFile().length());

            // 2. generate tags
            List<Tag> tags = new ArrayList<>();
            // Opzionale
            tags.add(new Tag("urn-originale",
                    firUrnReports.stream().filter(r -> r.getTiUrn().equals(TiUrnReport.ORIGINALE))
                            .collect(Collectors.toList()).get(0).getDsUrn()));
            tags.add(new Tag("urn-normalizzato",
                    firUrnReports.stream().filter(r -> r.getTiUrn().equals(TiUrnReport.NORMALIZZATO))
                            .collect(Collectors.toList()).get(0).getDsUrn()));

            PutObjectResult result = putObjectAsStream(cdKeyFile, metadata, Optional.of(new ObjectTagging(tags)),
                    reportZip.toFile());
            // immutable map
            return Collections.singletonMap(cdKeyFile, result);
        } catch (SacerWsException ex) {
            LOG.error("VerificaFirmaReportAwsClient.sendReportZipToObjStorage errore su invio report ad object storage",
                    ex);
            return null;
        }
    }

    /**
     *
     * Send report file
     *
     * @param key
     *            chiave file
     * @param metadata
     *            metadati (se esistono)
     * @param tags
     *            tag (se esistono)
     * @param reportVerificaFirma
     *            file report
     *
     * @return object result from AWS
     *
     * @throws SacerWsException
     *             eccezione generica
     */
    private PutObjectResult putObjectAsStream(String key, ObjectMetadata metadata, Optional<ObjectTagging> tags,
            File reportVerificaFirma) throws SacerWsException {
        try {
            // 1. create request (base)
            PutObjectRequest req = new PutObjectRequest(bucketName, key,
                    new BufferedInputStream(new FileInputStream(reportVerificaFirma), BUFFER), metadata);
            // 1.b check tags
            if (tags.isPresent()) {
                req.withTagging(tags.get());
            }
            // 2. put object
            return awsClient.putObject(req);
        } catch (AmazonClientException | FileNotFoundException ex) {
            throw new SacerWsException("AWS: Errore generico su invio file, bucketname " + bucketName + " key " + key,
                    ex, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    /**
     * Nome bucket (da configurazione database)
     *
     * @return nome bucket
     */
    public String getBucketName() {
        return bucketName;
    }

    /*
     * Client Shutdown
     */
    @PreDestroy
    private void destroy() {
        LOG.info("Shutdown endpoint S3...");
        if (awsClient != null) {
            awsClient.shutdown();
        }
    }

}
