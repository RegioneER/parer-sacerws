/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import eu.europa.esig.dss.detailedreport.DetailedReportFacade;
import eu.europa.esig.dss.detailedreport.DetailedReportXmlDefiner;
import eu.europa.esig.dss.diagnostic.DiagnosticDataFacade;
import eu.europa.esig.dss.diagnostic.DiagnosticDataXmlDefiner;
import eu.europa.esig.dss.simplereport.SimpleReportFacade;
import eu.europa.esig.dss.simplereport.SimpleReportXmlDefiner;
import it.eng.parer.eidas.model.DataToValidateDTOExt;
import it.eng.parer.eidas.model.EidasMetadataToValidate;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.eidas.model.RemoteDocumentExt;
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.FileBinario;
import java.time.ZonedDateTime;

/**
 *
 * @author Sinatti_S
 */
public class EidasUtils {

    private EidasUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @deprecated
     * 
     *             Compila l'input per la verifica firma tramite DSS. Utilizzata la versione /v2 in cui si utilizza
     *             multipart con stream da file.
     *
     * @param componenteVers
     *            componente versato
     * @param sottoComponentiFirma
     *            sotto componente di tipo FIRMA
     * @param controlliAbilitati
     *            lista controlli abilitati (flag true/false)
     * @param dataDiRiferimento
     *            data di riferimento
     * @param verificaAllaDataDiFirma
     *            flag true/false per verifica alla data firma
     * @param uuid
     *            UUID generato
     *
     * @return Bean utilizzato per invocare il servizio di verifica EIDAS.
     */
    @Deprecated
    public static DataToValidateDTOExt buildEidasDtoFromCompVers(ComponenteVers componenteVers,
            List<ComponenteVers> sottoComponentiFirma, Map<String, Boolean> controlliAbilitati, Date dataDiRiferimento,
            boolean verificaAllaDataDiFirma, String uuid) {
        final boolean hasFirmeDetached = sottoComponentiFirma != null && !sottoComponentiFirma.isEmpty();

        //
        if (!hasFirmeDetached) {
            /*
             * Firma non detached : il file firmato è su componente
             */
            FileBinario signedFB = componenteVers.getRifFileBinario();
            return buildDocuments(controlliAbilitati, verificaAllaDataDiFirma, dataDiRiferimento,
                    signedFB.getFileName(), componenteVers.getId(), signedFB.getFileSuDisco(), uuid);
        } else {
            /*
             * Firma detached : il file firmato è su sottocomponente
             */
            FileBinario signedFB = sottoComponentiFirma.get(0).getRifFileBinario();
            FileBinario originalFB = componenteVers.getRifFileBinario();
            return buildDocuments(controlliAbilitati, verificaAllaDataDiFirma, dataDiRiferimento,
                    signedFB.getFileName(), componenteVers.getId(), signedFB.getFileSuDisco(), true,
                    originalFB.getFileName(), sottoComponentiFirma.get(0).getId(), originalFB.getFileSuDisco(), uuid);
        }
    }

    /**
     * @deprecated
     * 
     *             Compila l'input per la verifica firma tramite DSS. Utilizzata la versione /v2 in cui si utilizza
     *             multipart con stream da file.
     *
     * @param signedDoc
     *            documento firma
     * @param firme
     *            lista file firmati
     * @param marche
     *            lista file di tipo marche
     * @param controlliAbilitati
     *            lista controlli abilitati (flag true/false)
     * @param verificaAllaDataDiFirma
     *            flag true/false per verifica alla data firma
     * @param dataDiRiferimento
     *            data di riferimento
     * @param uuid
     *            UUID generato
     * @param idComponente
     *            ID componente firmato
     * @param idSottoComponente
     *            ID sotto componente firmato
     *
     * @return Bean utilizzato per invocare il servizio di verifica EIDAS.
     */
    @Deprecated
    public static DataToValidateDTOExt buildEidasDtoFromFiles(File signedDoc, List<File> firme, List<File> marche,
            Map<String, Boolean> controlliAbilitati, boolean verificaAllaDataDiFirma, Date dataDiRiferimento,
            String uuid, String idComponente, String idSottoComponente) {
        final boolean hasFirmeDetached = firme != null && !firme.isEmpty();

        if (!hasFirmeDetached) {
            return buildDocuments(controlliAbilitati, verificaAllaDataDiFirma, dataDiRiferimento, signedDoc.getName(),
                    idComponente, signedDoc, uuid);
        } else {
            File originalDoc = firme.get(0);
            return buildDocuments(controlliAbilitati, verificaAllaDataDiFirma, dataDiRiferimento, signedDoc.getName(),
                    idComponente, signedDoc, true, idSottoComponente, originalDoc.getName(), originalDoc, uuid);
        }
    }

    private static DataToValidateDTOExt buildDocuments(Map<String, Boolean> controlliAbilitati,
            boolean verificaAllaDataDiFirma, Date dataDiRiferimento, String fileName, String idComponente,
            File signedDoc, String uuid) {
        return buildDocuments(controlliAbilitati, verificaAllaDataDiFirma, dataDiRiferimento, fileName, idComponente,
                signedDoc, false, null, null, null, uuid);
    }

    private static DataToValidateDTOExt buildDocuments(Map<String, Boolean> controlliAbilitati,
            boolean verificaAllaDataDiFirma, Date dataDiRiferimento, String signedDocFileName, String idComponente,
            File signedDoc, boolean hasFirmeDetached, String originalDocFileName, String idSottoComponente,
            File originalDoc, String uuid) {

        DataToValidateDTOExt input = new DataToValidateDTOExt();

        boolean controlloCrittogaficoAbilitato = controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS);
        boolean controlloCatenaTrustAbilitato = controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS);
        boolean controlloCertificatoAbilitato = controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS);
        boolean controlloRevocaAbilitato = controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS);
        input.setControlloCrittograficoIgnorato(!controlloCrittogaficoAbilitato);
        input.setControlloCatenaTrustIgnorato(!controlloCatenaTrustAbilitato);
        input.setControlloCertificatoIgnorato(!controlloCertificatoAbilitato);
        input.setControlloRevocaIgnorato(!controlloRevocaAbilitato);

        if (!hasFirmeDetached) {
            // signed
            RemoteDocumentExt signedDocument = new RemoteDocumentExt();
            signedDocument.setName(StringUtils.defaultString(signedDocFileName, signedDoc.getName()));
            signedDocument.setBytes(toByteArray(signedDoc));
            input.setSignedDocumentExt(signedDocument);
            // id componente firmato
            input.setIdComponente(idComponente);
        } else {
            RemoteDocumentExt originalDocument = new RemoteDocumentExt();
            originalDocument.setName(StringUtils.defaultString(originalDocFileName, originalDoc.getName()));
            originalDocument.setBytes(toByteArray(originalDoc));
            input.setOriginalDocumentsExt(Arrays.asList(originalDocument));
            // signed
            RemoteDocumentExt signedDocument = new RemoteDocumentExt();
            signedDocument.setName(StringUtils.defaultString(signedDocFileName, signedDoc.getName()));
            signedDocument.setBytes(toByteArray(signedDoc));
            input.setSignedDocumentExt(signedDocument);
            // id componente firmato
            input.setIdComponente(idSottoComponente);
        }
        //
        input.setUuid(uuid);
        //
        input.setVerificaAllaDataDiFirma(verificaAllaDataDiFirma);
        //
        input.setDataDiRiferimento(dataDiRiferimento);

        return input;
    }

    public static EidasMetadataToValidate buildEidasMetadata(ComponenteVers componenteVers,
            List<ComponenteVers> sottoComponentiFirma, Map<String, Boolean> controlliAbilitati,
            boolean verificaAllaDataDiFirma, ZonedDateTime dataDiRiferimento, String uuid) {

        final boolean hasFirmeDetached = sottoComponentiFirma != null && !sottoComponentiFirma.isEmpty();

        if (!hasFirmeDetached) {
            return buildEidasMetadata(controlliAbilitati, verificaAllaDataDiFirma, dataDiRiferimento,
                    componenteVers.getId(), componenteVers.getRifFileBinario().getFileName(), StringUtils.EMPTY, uuid);
        } else {
            return buildEidasMetadata(controlliAbilitati, verificaAllaDataDiFirma, dataDiRiferimento,
                    sottoComponentiFirma.get(0).getId(), sottoComponentiFirma.get(0).getRifFileBinario().getFileName(),
                    componenteVers.getRifFileBinario().getFileName(), uuid);
        }

    }

    private static EidasMetadataToValidate buildEidasMetadata(Map<String, Boolean> controlliAbilitati,
            boolean verificaAllaDataDiFirma, ZonedDateTime dataDiRiferimento, String idDocuments,
            String signedDocumentName, String originalDocumentName, String uuid) {

        EidasMetadataToValidate input = new EidasMetadataToValidate();

        // control flag
        boolean controlloCrittogaficoAbilitato = controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS);
        boolean controlloCatenaTrustAbilitato = controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS);
        boolean controlloCertificatoAbilitato = controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS);
        boolean controlloRevocaAbilitato = controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS);

        input.setControlloCrittograficoIgnorato(!controlloCrittogaficoAbilitato);
        input.setControlloCatenaTrustIgnorato(!controlloCatenaTrustAbilitato);
        input.setControlloCertificatoIgnorato(!controlloCertificatoAbilitato);
        input.setControlloRevocaIgnorato(!controlloRevocaAbilitato);
        // uuid
        input.setUuid(uuid);
        // verificaAllaDataDiFirma
        input.setVerificaAllaDataDiFirma(verificaAllaDataDiFirma);
        // dataDiRiferimento
        if (dataDiRiferimento != null) {
            input.setDataDiRiferimento(Date.from(dataDiRiferimento.toInstant()));
        }

        // id componente / sotto componente
        input.setIdDocuments(idDocuments);
        // signed document name
        input.setSignedDocumentName(signedDocumentName);
        // original document name
        /*
         * nota: attenzione questo meccanismo è posizionale ossia, fa il "paio" con l'array di file originali passati
         * nel body del multipart (parametro "originalFiles") e.g. pos = 0 file0.ext / originalFiles 0 pos = 1 file1.ext
         * / originalFiles 1
         */
        input.setOriginalDocumentNames(new String[] { originalDocumentName });
        return input;
    }

    private static byte[] toByteArray(File file) {
        if (file == null) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return IOUtils.toByteArray(fis);

        } catch (IOException e) {
            throw new SacerWsRuntimeException(e, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    // tree no binaries
    /**
     * @deprecated
     * 
     *             Metodo non più utilizzato in favore di una gestione votata all'I/O e all'utilizzo di stream su file
     *             anziché oggetti che occupano risorse
     * 
     * @param dto
     *            {@link EidasWSReportsDTOTree}
     */
    @Deprecated
    public static void buildSkinnyReports(EidasWSReportsDTOTree dto) {
        // root element
        if (!dto.isParent()) {
            removeraw(dto);
        }
        // per ogni figlio (signed)
        for (EidasWSReportsDTOTree child : dto.getChildren()) {
            if (child.isUnsigned() /* ultimo livello documento non firmato */) {
                break;
            }
            removeraw(child);
            if (child.isParent() && child.getChildren() != null && !child.getChildren().isEmpty()) {
                buildSkinnyReports(child);
            }
        }
    }

    private static void removeraw(EidasWSReportsDTOTree dto) {
        // used certificate
        dto.getReport().getDiagnosticData().getUsedCertificates().forEach(usedcert -> {
            // set null
            usedcert.setBase64Encoded(StringUtils.EMPTY.getBytes());
            // set null on revocation
            usedcert.getRevocations().forEach(r -> r.getRevocation().setBase64Encoded(StringUtils.EMPTY.getBytes()));
            // set null on chain
            usedcert.getCertificateChain()
                    .forEach(c -> c.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
            // set null on signincertificate
            if (usedcert.getSigningCertificate() != null) {
                usedcert.getSigningCertificate().getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes());
            }
        });
        // used revocations
        dto.getReport().getDiagnosticData().getUsedRevocations().forEach(usedrev -> {
            // set null
            usedrev.setBase64Encoded(StringUtils.EMPTY.getBytes());
            // set null signcert
            if (usedrev.getSigningCertificate() != null) {
                usedrev.getSigningCertificate().getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes());
            }
            // set null on chain
            usedrev.getCertificateChain()
                    .forEach(r -> r.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
        });
        // used timestamp
        dto.getReport().getDiagnosticData().getUsedTimestamps().forEach(usedts -> {
            // set null
            usedts.setBase64Encoded(StringUtils.EMPTY.getBytes());
            // set null signigncertificate
            if (usedts.getSigningCertificate() != null) {
                usedts.getSigningCertificate().getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes());
            }
            // set null on chain
            usedts.getCertificateChain()
                    .forEach(c -> c.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
        });

        // signatures
        dto.getReport().getDiagnosticData().getSignatures().forEach(s -> {
            // all founded obj
            // set null on timestamps
            s.getFoundTimestamps().forEach(t -> t.getTimestamp().setBase64Encoded(StringUtils.EMPTY.getBytes()));
            // set null on related cert
            s.getFoundCertificates().getRelatedCertificates()
                    .forEach(c -> c.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
            // set null on related revocation
            s.getFoundRevocations().getRelatedRevocations()
                    .forEach(r -> r.getRevocation().setBase64Encoded(StringUtils.EMPTY.getBytes()));
            // set null on chain
            s.getCertificateChain().forEach(c -> c.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
        });
        // extensions
        dto.setExtensions(null);
    }

    /*
     * Le classi sottostanti sono state create come estensione delle facede presenti su librie per necessità di override
     * del metodo getSchema(). Originariamente tale metodo utilizza una factory con le direttive "secure" al fine di
     * garantire che non sia possibile la entity injection purtroppo su sacerws tali direttive al momento non sono
     * accettate e generano una eccezione di tipo SaxException (non rispettato lo standard JAXP 1.5).
     * 
     * Nota: dalla versione 5.8 a fronte di un re-factor, è stata cambiata la visibilità delle costanti in cui viene
     * indicato il percorso con l'xsd, di conseguenza sono state replicate sulla classe figlia.
     */
    public static class SimpleReportFacedUnsecure extends SimpleReportFacade {

        /** The XSD Simple Report schema */
        private static final String SIMPLE_REPORT_SCHEMA_LOCATION = "/xsd/SimpleReport.xsd";

        public static SimpleReportFacedUnsecure newUnsecureFacade() {
            return new SimpleReportFacedUnsecure();
        }

        @Override
        protected Schema getSchema() throws IOException, SAXException {
            Schema schema = null;
            try (InputStream inputStream = SimpleReportXmlDefiner.class
                    .getResourceAsStream(SIMPLE_REPORT_SCHEMA_LOCATION)) {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = sf.newSchema(new Source[] { new StreamSource(inputStream) });
            }

            return schema;
        }

    }

    public static class DetailedReportFacedUnsecure extends DetailedReportFacade {

        /** The XSD Detailed Report schema */
        private static final String DETAILED_REPORT_SCHEMA_LOCATION = "/xsd/DetailedReport.xsd";

        public static DetailedReportFacedUnsecure newUnsecureFacade() {
            return new DetailedReportFacedUnsecure();
        }

        @Override
        protected Schema getSchema() throws IOException, SAXException {
            Schema schema = null;
            try (InputStream inputStream = DetailedReportXmlDefiner.class
                    .getResourceAsStream(DETAILED_REPORT_SCHEMA_LOCATION)) {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = sf.newSchema(new Source[] { new StreamSource(inputStream) });
            }

            return schema;
        }

    }

    public static class DiagnosticReportFacedUnsecure extends DiagnosticDataFacade {

        /** The XSD Diagnostic Report schema */
        private static final String DIAGNOSTIC_DATA_SCHEMA_LOCATION = "/xsd/DiagnosticData.xsd";

        public static DiagnosticReportFacedUnsecure newUnsecureFacade() {
            return new DiagnosticReportFacedUnsecure();
        }

        @Override
        public Schema getSchema() throws IOException, SAXException {
            Schema schema = null;
            try (InputStream isXSDDiagnosticData = DiagnosticDataXmlDefiner.class
                    .getResourceAsStream(DIAGNOSTIC_DATA_SCHEMA_LOCATION)) {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = sf.newSchema(new Source[] { new StreamSource(isXSDDiagnosticData) });
            }

            return schema;
        }

    }

}
