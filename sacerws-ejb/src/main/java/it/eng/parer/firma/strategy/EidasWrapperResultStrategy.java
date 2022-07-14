package it.eng.parer.firma.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.detailedreport.jaxb.XmlBasicBuildingBlocks;
import eu.europa.esig.dss.detailedreport.jaxb.XmlCV;
import eu.europa.esig.dss.detailedreport.jaxb.XmlISC;
import eu.europa.esig.dss.detailedreport.jaxb.XmlRAC;
import eu.europa.esig.dss.detailedreport.jaxb.XmlSubXCV;
import eu.europa.esig.dss.detailedreport.jaxb.XmlXCV;
import eu.europa.esig.dss.diagnostic.CertificateRevocationWrapper;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.RevocationWrapper;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.RevocationType;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SubIndication;
import eu.europa.esig.dss.validation.reports.Reports;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.eidas.model.EsitoValidazioneEidas;
import it.eng.parer.eidas.model.ExtensionsDTO;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;
import it.eng.parer.firma.exception.VerificaFirmaWrapperGenericException;
import it.eng.parer.firma.util.VerificaFirmaEnums;
import it.eng.parer.firma.xml.VFAdditionalInfoBustaType;
import it.eng.parer.firma.xml.VFAdditionalInfoFirmaCompType;
import it.eng.parer.firma.xml.VFAdditionalInfoMarcaCompType;
import it.eng.parer.firma.xml.VFCertifCaType;
import it.eng.parer.firma.xml.VFCertifFirmatarioType;
import it.eng.parer.firma.xml.VFCertifOcspType;
import it.eng.parer.firma.xml.VFContrFirmaCompType;
import it.eng.parer.firma.xml.VFContrMarcaCompType;
import it.eng.parer.firma.xml.VFCrlType;
import it.eng.parer.firma.xml.VFFilePerFirmaType;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VFOcspType;
import it.eng.parer.firma.xml.VFTipoControlloType;
import it.eng.parer.firma.xml.VFTipoFileType;
import it.eng.parer.firma.xml.VFTipoFirmaType;
import it.eng.parer.firma.xml.VFUrlDistribCrlType;
import it.eng.parer.firma.xml.VFUrlDistribOcspType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.firma.xml.VerificaFirmaWrapper.VFBusta;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.XmlDateUtility;

public class EidasWrapperResultStrategy extends EidasBaseWrapperResult
        implements IVerificaFirmaWrapperResultStrategy<EsitoValidazioneEidas> {

    private static final Logger LOG = LoggerFactory.getLogger(EidasWrapperResultStrategy.class);
    private static final String LOG_FMT_SUBINDICATION = "Eidas esito = {} controllo tipo = {} subindication = {}";

    private boolean verificaAllaDataDiFirma;
    private Map<String, Boolean> controlliAbilitati;
    private AbsVersamentoExt versamento;

    private static final int PG_BUSTA = 0;
    private static final int PG_FIRMA = 1;
    private static final int PG_MARCA = 2;

    /**
     * Firma necessaria al costrutture per le logiche successive che serviranno a determinare, sulla base dell'output di
     * verifica, come gestire determinate casistiche (vedi controlli e tipo rif alla data)
     *
     * @param verificaAllaDataDiFirma
     *            boolean true/false per verifica alla data firma
     * @param versamento
     *            astrazione con metadati di versamento
     * 
     * @param controlliAbilitati
     *            lista dei controlli (flag true/false)
     */
    public EidasWrapperResultStrategy(boolean verificaAllaDataDiFirma, Map<String, Boolean> controlliAbilitati,
            AbsVersamentoExt versamento) {
        this.verificaAllaDataDiFirma = verificaAllaDataDiFirma;
        this.controlliAbilitati = controlliAbilitati;
        this.versamento = versamento;
    }

    @Override
    public String getCode() {
        return CdServizioVerificaCompDoc.EIDAS.name();
    }

    private Map<String, Boolean> getControlliAbilitati() {
        return controlliAbilitati;
    }

    private boolean isVerificaAllaDataDiFirma() {
        return verificaAllaDataDiFirma;
    }

    @Override
    public void fromVerificaOutOnWrapper(EsitoValidazioneEidas esito, VerificaFirmaWrapper wrapper, ZonedDateTime dtRef)
            throws VerificaFirmaWrapperGenericException {
        /*
         * Nota: contatori GLOBALI
         */
        BigDecimal[] pgs = new BigDecimal[] { BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE };

        // set service version
        wrapper.setServiceVersion(esito.getVService());
        // set library version
        wrapper.setLibraryVersion(esito.getVLibrary());
        // set self link
        wrapper.setSelfLink(esito.getSelfLink());
        // set start/end validation
        wrapper.setStartDate(XmlDateUtility.dateToXMLGregorianCalendar(esito.getStartValidation()));
        wrapper.setEndDate(XmlDateUtility.dateToXMLGregorianCalendar(esito.getEndValidation()));
        // signatures founded (= esito principale è "signed", esiste almeno una firma)
        wrapper.setSignsDetected(!esito.getEidasWSReportsDTOTree().isUnsigned());
        LOG.debug("Documento firmato [{}]", wrapper.isSignsDetected());
        // detached mimetype (verifica formato)
        String tikaMime = null;
        if (wrapper.getAdditionalInfo().isIsDetached()) {
            tikaMime = esito.getMimeType();
        } else {
            tikaMime = getMimeTypeUnsigned(esito.getEidasWSReportsDTOTree(),
                    esito.getEidasWSReportsDTOTree().getChildren());
        }
        wrapper.getAdditionalInfo().setTikaMime(tikaMime);
        //
        compileWrapper(wrapper, esito.getEidasWSReportsDTOTree(), dtRef, pgs);
    }

    private void compileWrapper(VerificaFirmaWrapper wrapper, EidasWSReportsDTOTree dto, ZonedDateTime dtRef,
            BigDecimal[] pgs) throws VerificaFirmaWrapperGenericException {
        // root element
        if (!dto.isParent()) {
            // reports by dss
            Reports reports = new Reports(dto.getReport().getDiagnosticData(), dto.getReport().getDetailedReport(),
                    dto.getReport().getSimpleReport(), null /* validation report non gestito */);
            pgs = buildBusta(wrapper, dtRef, dto, reports, pgs);
        }
        // per ogni figlio
        for (EidasWSReportsDTOTree child : dto.getChildren()) {
            if (child.isUnsigned() /* ultimo livello documento non firmato */) {
                break;
            }
            if (child.getReport().getSimpleReport()
                    .getSignaturesCount() != 0/* report "valido" esiste almeno una firma */) {
                // reports by dss
                Reports reports = new Reports(child.getReport().getDiagnosticData(),
                        child.getReport().getDetailedReport(), child.getReport().getSimpleReport(),
                        null /* validation report non gestito */);
                pgs = buildBusta(wrapper, dtRef, child, reports, pgs);
                // recursive call
                compileWrapper(wrapper, child, dtRef, pgs);
            }
        }
    }

    private BigDecimal[] buildBusta(VerificaFirmaWrapper wrapper, ZonedDateTime dtRef, EidasWSReportsDTOTree tree,
            Reports reports, BigDecimal[] pgs) throws VerificaFirmaWrapperGenericException {
        // Extensions
        Map<String, ExtensionsDTO> extensions = tree.getExtensions();

        // creo busta
        VFBusta busta = new VFBusta();
        wrapper.getVFBusta().add(busta);
        // init busta
        // 1.assign pg
        busta.setPgBusta(pgs[PG_BUSTA]);
        // inc pgBusta
        pgs[PG_BUSTA] = pgs[PG_BUSTA].add(BigDecimal.ONE);
        // 2. add info (empty)
        VFAdditionalInfoBustaType additionalInfoBustaType = new VFAdditionalInfoBustaType();
        busta.setAdditionalInfo(additionalInfoBustaType);

        // for each signature
        List<SignatureWrapper> signatures = reports.getDiagnosticData().getSignatures();
        // strutta per gestione firme e contro firme
        Map<String, VFFirmaCompType> firmaCompParent = new HashMap<>();
        for (SignatureWrapper signature : signatures) {

            // FIRMA
            VFFirmaCompType firmaCompType = new VFFirmaCompType();
            // set setPgFirma
            firmaCompType.setPgFirma(pgs[PG_FIRMA]);
            // set id (id componente PARER)
            firmaCompType.setId(tree.getIdComponente());

            // build firma
            buildFirmaComp(wrapper, extensions, firmaCompType, signature, dtRef, reports);

            // inc pgFirma
            pgs[PG_FIRMA] = pgs[PG_FIRMA].add(BigDecimal.ONE);

            // marche temporali
            for (TimestampWrapper ts : signature.getTimestampList()) {
                // MARCHE
                VFMarcaCompType marcaCompType = new VFMarcaCompType();
                // set pgMarche
                marcaCompType.setPgMarca(pgs[PG_MARCA]);
                // set id (id componente PARER)
                marcaCompType.setId(tree.getIdComponente());
                // add on list
                firmaCompType.getMarcaComps().add(marcaCompType);

                // build marca
                buildMarcaComp(wrapper, reports, extensions, marcaCompType, ts, signature.getSignatureFormat(), dtRef);
                // inc pgMarche
                pgs[PG_MARCA] = pgs[PG_MARCA].add(BigDecimal.ONE);
            }

            // contro firma
            if (!signature.isCounterSignature()) {
                firmaCompParent.put(signature.getId(), firmaCompType);
                // add on list
                busta.getFirmaComps().add(firmaCompType);
            } else {
                // creazione relazione firma padre-figlio (contro firme)
                firmaCompParent.keySet().forEach(id -> {
                    if (signature.getParent().getId().equals(id)) {
                        // update parent
                        firmaCompParent.get(id).getControfirmaFirmaFiglios().add(firmaCompType);
                    }
                });
            }
        }

        return pgs;
    }

    private void buildMarcaComp(VerificaFirmaWrapper wrapper, Reports reports, Map<String, ExtensionsDTO> extensions,
            VFMarcaCompType marcaCompType, TimestampWrapper ts, SignatureLevel signatureFormat, ZonedDateTime dtRef)
            throws VerificaFirmaWrapperGenericException {

        // add info (empty)
        VFAdditionalInfoMarcaCompType additionalInfoMarcaCompType = new VFAdditionalInfoMarcaCompType();
        marcaCompType.setAdditionalInfo(additionalInfoMarcaCompType);

        // ********************************
        // USED CERTIFICATE
        // ********************************
        // CERTIFICATO TSA + BLOB
        VFCertifCaType certifCaType = new VFCertifCaType();
        marcaCompType.setCertifTsa(certifCaType);

        CertificateWrapper xmlcertificateTsa = reports.getDiagnosticData()
                .getUsedCertificateById(ts.getSigningCertificate().getId());
        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateTsa, "serialNumber",
                Optional.ofNullable(ts.getSigningCertificate().getId()));
        certifCaType.setNiSerialCertifCa(new BigDecimal(xmlcertificateTsa.getSerialNumber()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateTsa, "notAfter",
                Optional.ofNullable(ts.getSigningCertificate().getId()));
        certifCaType.setDtFinValCertifCa(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateTsa.getNotAfter()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateTsa, "notBefore",
                Optional.ofNullable(ts.getSigningCertificate().getId()));
        certifCaType.setDtIniValCertifCa(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateTsa.getNotBefore()));

        // check subjectKeyId
        if (extensions.containsKey(xmlcertificateTsa.getId())) {
            certifCaType.setDsSubjectKeyId(extensions.get(xmlcertificateTsa.getId()).getSubjectKeyIdentifier());
        }

        int urlIdx = 1;
        for (String url : xmlcertificateTsa.getCRLDistributionPoints()) {
            VFUrlDistribCrlType urlDistribCrlType = new VFUrlDistribCrlType();
            urlDistribCrlType.setDlUrlDistribCrl(url);
            urlDistribCrlType.setNiOrdUrlDistribCrl(new BigDecimal(urlIdx));

            certifCaType.getUrlDistribCrls().add(urlDistribCrlType);

            // inc idc
            urlIdx++;
        }
        // CA
        String certificateCAId = getCertificateCAId(ts, xmlcertificateTsa);
        //
        CertificateWrapper xmlcertificateCA = reports.getDiagnosticData().getUsedCertificateById(certificateCAId);
        // ocsp (from CA+certificate)
        Iterable<String> ocspCombinedUrls = CollectionUtils.union(xmlcertificateCA.getOCSPAccessUrls(),
                xmlcertificateTsa.getOCSPAccessUrls());
        // ocsp
        urlIdx = 1;
        for (String url : ocspCombinedUrls) {
            VFUrlDistribOcspType urlDistribOcspType = new VFUrlDistribOcspType();
            urlDistribOcspType.setDlUrlDistribOcsp(url);
            urlDistribOcspType.setNiOrdUrlDistribOcsp(new BigDecimal(urlIdx));

            certifCaType.getUrlDistribOcsps().add(urlDistribOcspType);
            // inc idx
            urlIdx++;
        }

        certifCaType.setDlDnIssuerCertifCa(xmlcertificateTsa.getCertificateIssuerDN());
        certifCaType.setDlDnSubjectCertifCa(xmlcertificateTsa.getCertificateDN());

        VFFilePerFirmaType filePerFirmaType = new VFFilePerFirmaType();
        certifCaType.setFilePerFirma(filePerFirmaType);

        filePerFirmaType.setBlFilePerFirma(xmlcertificateTsa.getBinaries());
        filePerFirmaType.setTiFilePerFirma(VFTipoFileType.CERTIF_CA);

        // *************************
        // nullable element
        String dsAlgoMarca = null;
        if (ts.getDigestAlgorithm() != null && ts.getEncryptionAlgorithm() != null) {
            dsAlgoMarca = ts.getDigestAlgorithm().getName().concat("with")
                    .concat(ts.getEncryptionAlgorithm().getName());
        }
        marcaCompType.setDsAlgoMarca(dsAlgoMarca);
        marcaCompType.setTmMarcaTemp(XmlDateUtility.dateToXMLGregorianCalendar(ts.getProductionTime()));
        marcaCompType.setTiFormatoMarca(signatureFormat.toString());// ereditato dalla firma
        marcaCompType.setDtScadMarca(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateTsa.getNotAfter()));
        // check marcaBase64
        if (extensions.containsKey(ts.getId())) {
            marcaCompType.setDsMarcaBase64(extensions.get(ts.getId()).getMarcaBase64());
        }
        /**
         * CONTROLLI / ESITI MARCA
         */
        // building block
        XmlBasicBuildingBlocks bbb = reports.getDetailedReport().getBasicBuildingBlockById(ts.getId());
        if (bbb == null) {
            // TOFIX: cosa fare in questi casi ?!
            LOG.warn("BasicBuildingBlockById not found TS ID = {}", ts.getId());
        }
        // CONTROLLI MARCA
        buildContrMarcaComp(marcaCompType, ts, xmlcertificateTsa, bbb);

        // esito controllo CERTIFICATO
        VFContrMarcaCompType contrMarcaCompCert = marcaCompType.getContrMarcaComps().stream()
                .filter(c -> c.getTiContr().equals(VFTipoControlloType.CERTIFICATO)).collect(Collectors.toList())
                .get(0);

        // OCSP
        buildMarcaCompTSAwithOCSP(wrapper, marcaCompType, reports, xmlcertificateTsa, dtRef, extensions, bbb);

        // CRL
        buildMarcaCompTSAwithCRL(wrapper, marcaCompType, certifCaType, xmlcertificateTsa, dtRef, extensions, bbb,
                contrMarcaCompCert);

    }

    /*
     * Nota: non possiamo persistere più di una CRL (non supportato), viene costruita un'apposita lista "filtrata" con
     * il solo elemento utile
     *
     */
    private void buildMarcaCompTSAwithCRL(VerificaFirmaWrapper wrapper, VFMarcaCompType marcaCompType,
            VFCertifCaType certifCaType, CertificateWrapper xmlcertificateTsa, ZonedDateTime dtRef,
            Map<String, ExtensionsDTO> extensions, XmlBasicBuildingBlocks bbb, VFContrMarcaCompType contrMarcaCompCert)
            throws VerificaFirmaWrapperGenericException {

        // OCSP prioritario rispetto CRL
        boolean hasOcsp = xmlcertificateTsa.getCertificateRevocationData().stream()
                .anyMatch(r -> r.getRevocationType().equals(RevocationType.OCSP));

        // CRL
        RevocationWrapper crl = findRevocationByType(xmlcertificateTsa, dtRef, RevocationType.CRL);
        if (hasOcsp || crl == null) {
            //
            buildContrCRLNotFoundMarcaComp(marcaCompType, xmlcertificateTsa, hasOcsp);
            return;
        } // no crl

        // CRL
        VFCrlType crlTypeTsa = buildCrl(wrapper, extensions, crl);
        // CRL : CA
        crlTypeTsa.setCertifCa(certifCaType);
        // MARCA : CRL
        marcaCompType.setCrlTsa(crlTypeTsa);

        // controlli CRL
        buildContrCRLMarcaComp(marcaCompType, xmlcertificateTsa, crl, bbb, contrMarcaCompCert, hasOcsp);
        // CRL - End
    }

    private void buildMarcaCompTSAwithOCSP(VerificaFirmaWrapper wrapper, VFMarcaCompType marcaCompType, Reports reports,
            CertificateWrapper xmlcertificateTsa, ZonedDateTime dtRef, Map<String, ExtensionsDTO> extensions,
            XmlBasicBuildingBlocks bbb) throws VerificaFirmaWrapperGenericException {
        // verifica presenza CRL
        boolean hasCrl = xmlcertificateTsa.getCertificateRevocationData().stream()
                .anyMatch(r -> r.getRevocationType().equals(RevocationType.CRL));

        // OCSP
        CertificateRevocationWrapper ocsp = findRevocationByType(xmlcertificateTsa, dtRef, RevocationType.OCSP);
        if (ocsp == null) {
            //
            buildContrOCSPNotFoundMarcaComp(marcaCompType, xmlcertificateTsa, hasCrl);
            return;
        } // no crl

        // CA
        VFCertifCaType certifCaOcspType = buildOcspCertifCA(wrapper, reports, xmlcertificateTsa, extensions, ocsp);

        // build OCSP
        VFOcspType ocspTypeTsa = buildOcspWithCertif(wrapper, certifCaOcspType, ocsp);
        marcaCompType.setOcspTsa(ocspTypeTsa);
        //
        // CRONTROLLI OCSP
        buildContrOCSPMarcaComp(marcaCompType, xmlcertificateTsa, ocsp, bbb, hasCrl);
        // OCSP - End
    }

    private void buildContrCRLNotFoundMarcaComp(VFMarcaCompType marcaCompType, CertificateWrapper xmlcertificateTsa,
            boolean hasOcsp) {
        buildContrCRLMarcaComp(marcaCompType, xmlcertificateTsa, null, null, null, hasOcsp);
    }

    private void buildContrCRLMarcaComp(VFMarcaCompType marcaCompType, CertificateWrapper xmlcertificateTsa,
            RevocationWrapper crl, XmlBasicBuildingBlocks bbb, VFContrMarcaCompType contrMarcaCompCert,
            boolean hasOcsp) {
        /*
         * caso "particolare" di building block non trovato (vedi controllo precedente con WARNING) in questi casi si
         * assumono come NON PASSATI i controlli sull'oggetto perché non reperibili
         */
        boolean bbbFounded = bbb != null;
        // presente la CRL
        boolean hasCrl = crl != null;
        // presente solo la CRL
        boolean onlyCrl = hasCrl && !hasOcsp;
        // presente solo OCSP
        boolean onlyOcsp = !onlyCrl;
        // no revoche
        boolean noRevocations = !hasCrl && !hasOcsp;

        // CONTROLLI MARCA - CRL
        VFContrMarcaCompType contrMarcaCompType = new VFContrMarcaCompType();
        marcaCompType.getContrMarcaComps().add(contrMarcaCompType);
        contrMarcaCompType.setTiContr(VFTipoControlloType.CRL);

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS).booleanValue()) {
            //
            contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            // scenario 1 : solo crl ed errore su certificato
            if (onlyCrl && !contrMarcaCompCert.getTiEsitoContrMarca()
                    .equals(VerificaFirmaEnums.EsitoControllo.POSITIVO.name())) {
                // presente crl e non ocsp, errore sul controllo del certificato
                contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.name());
                contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.message());
            } else /* scenario 2 : crl e ocsp non presente */ if (noRevocations) {
                // caso 1 : 3/12/2009 <= notAfter && 3/12/2009 >= notBefore
                Date revokedDate = Date
                        .from(LocalDate.of(2009, 12, 3).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                if (xmlcertificateTsa.getNotAfter().before(revokedDate)
                        && xmlcertificateTsa.getNotBefore().after(revokedDate)) {
                    contrMarcaCompType.setTiEsitoContrMarca(
                            VerificaFirmaEnums.EsitoControllo.CERTIFICATO_SCADUTO_3_12_2009.name());
                    contrMarcaCompType.setDsMsgEsitoContrMarca(
                            VerificaFirmaEnums.EsitoControllo.CERTIFICATO_SCADUTO_3_12_2009.message());
                } else /* caso 2 */ {
                    contrMarcaCompType
                            .setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.CRL_NON_SCARICABILE.name());
                    contrMarcaCompType
                            .setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.CRL_NON_SCARICABILE.message());

                }
            } else /* scenario 3 : solo ocsp */ if (onlyOcsp) {
                contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.CRL_NON_SCARICABILE.name());
                contrMarcaCompType.setDsMsgEsitoContrMarca(
                        VerificaFirmaEnums.EsitoControllo.CRL_NON_SCARICABILE.alternative()[0]);
            } /* scenario 4 : solo CRL disponibile */ else {
                if (!bbbFounded || bbb.getXCV().getSubXCV().stream()
                        .noneMatch(c -> c.getId().equals(xmlcertificateTsa.getId()))) {
                    contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                    contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                } else {
                    //
                    XmlSubXCV subXvc = bbb.getXCV().getSubXCV().stream()
                            .filter(c -> c.getId().equals(xmlcertificateTsa.getId())).collect(Collectors.toList())
                            .get(0);
                    //
                    List<XmlRAC> xmlRacs = subXvc.getRAC().stream().filter(r -> r.getId().equals(crl.getId()))
                            .collect(Collectors.toList());
                    if (xmlRacs.isEmpty()) {
                        contrMarcaCompType
                                .setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                        contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                    } else if (!xmlRacs.get(0).getConclusion().getIndication().equals(Indication.PASSED)) {
                        // evaluate subindication (Revocation acceptence)
                        VerificaFirmaEnums.EsitoControllo esito = evaluateCRLXmlRacSubIndication(
                                contrMarcaCompType.getTiContr().name(), xmlRacs);

                        // log eidas message
                        logEidasConclusion(xmlcertificateTsa, bbb.getConclusion(), VFTipoControlloType.CRL.name());

                        contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
                        contrMarcaCompType.setTiEsitoContrMarca(esito.name());
                    } else {
                        contrMarcaCompType
                                .setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.POSITIVO.message());
                        contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.POSITIVO.name());
                    }
                }
            }
        }
    }

    private void buildContrOCSPNotFoundMarcaComp(VFMarcaCompType marcaCompType, CertificateWrapper xmlcertificateTsa,
            boolean hasCrl) {
        buildContrOCSPMarcaComp(marcaCompType, xmlcertificateTsa, null, null, hasCrl);
    }

    private void buildContrOCSPMarcaComp(VFMarcaCompType marcaCompType, CertificateWrapper xmlcertificateTsa,
            CertificateRevocationWrapper ocsp, XmlBasicBuildingBlocks bbb, boolean hasCrl) {

        /*
         * caso "particolare" di building block non trovato (vedi controllo precedente con WARNING) in questi casi si
         * assumono come NON PASSATI i controlli sull'oggetto perché non reperibili
         */
        boolean bbbFounded = bbb != null;
        // presente OCSP
        boolean hasOcsp = ocsp != null;
        // no revoche
        boolean noRevocations = !hasCrl && !hasOcsp;

        // CONTROLLI MARCA - OCSP
        VFContrMarcaCompType contrMarcaCompType = new VFContrMarcaCompType();
        marcaCompType.getContrMarcaComps().add(contrMarcaCompType);
        contrMarcaCompType.setTiContr(VFTipoControlloType.OCSP);

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS).booleanValue()) {
            //
            contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            // scenario 1 : revoche non presenti
            if (noRevocations) {
                contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.OCSP_NON_SCARICABILE.name());
                contrMarcaCompType
                        .setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.OCSP_NON_SCARICABILE.message());
            } else if (hasCrl && !hasOcsp) /* scenario 2 : presente la CRL ma NON OCSP */ {
                //
                contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.name());
                contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.message());
            } else /* scenario 3 : ocsp presente */ {
                // Nota : non trovato il build block oppure l'indicazione con il risultato di
                // validazione del certifcato
                if (!bbbFounded || bbb.getXCV().getSubXCV().stream()
                        .noneMatch(c -> c.getId().equals(xmlcertificateTsa.getId()))) {
                    contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                    contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                } else {
                    //
                    XmlSubXCV subXvc = bbb.getXCV().getSubXCV().stream()
                            .filter(c -> c.getId().equals(xmlcertificateTsa.getId())).collect(Collectors.toList())
                            .get(0);
                    //
                    List<XmlRAC> xmlRacs = subXvc.getRAC().stream().filter(r -> r.getId().equals(ocsp.getId()))
                            .collect(Collectors.toList());

                    if (xmlRacs.isEmpty()
                            || !xmlRacs.get(0).getConclusion().getIndication().equals(Indication.PASSED)) {
                        // evalutate subindication
                        VerificaFirmaEnums.EsitoControllo esito = evaluateOCSPXmlRacSubIndication(ocsp,
                                contrMarcaCompType.getTiContr().name(), xmlRacs);

                        logEidasConclusion(xmlcertificateTsa, xmlRacs.get(0).getConclusion(),
                                VFTipoControlloType.OCSP.name());

                        contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
                        contrMarcaCompType.setTiEsitoContrMarca(esito.name());
                    } else {
                        contrMarcaCompType
                                .setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.OCSP_VALIDO.message());
                        contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.OCSP_VALIDO.name());
                    }
                }
            }
        }
    }

    private void buildContrMarcaComp(VFMarcaCompType marcaCompType, TimestampWrapper ts,
            CertificateWrapper xmlcertificateTsa, XmlBasicBuildingBlocks bbb) {
        //
        boolean bbbFounded = bbb != null;
        //
        boolean isTimestampCompliant = ts.isSignatureIntact() && ts.isSignatureValid(); // TODO: da verificare se
        // diversa condizione
        marcaCompType.setTiEsitoContrConforme(isTimestampCompliant ? VerificaFirmaEnums.EsitoControllo.POSITIVO.name()
                : VerificaFirmaEnums.EsitoControllo.FORMATO_NON_CONOSCIUTO.name());
        marcaCompType
                .setDsMsgEsitoContrConforme(isTimestampCompliant ? VerificaFirmaEnums.EsitoControllo.POSITIVO.message()
                        : VerificaFirmaEnums.EsitoControllo.FORMATO_NON_CONOSCIUTO.name());

        if (!bbbFounded) {
            marcaCompType.setTiEsitoVerifMarca(VerificaFirmaEnums.EsitoControllo.WARNING.name());
            marcaCompType.setDsMsgEsitoVerifMarca(VerificaFirmaEnums.EsitoControllo.WARNING.message());
        } else {
            if (!bbb.getConclusion().getIndication().equals(Indication.PASSED)) {
                logEidasConclusion(xmlcertificateTsa, bbb.getConclusion());
                //
                marcaCompType.setTiEsitoVerifMarca(VerificaFirmaEnums.EsitoControllo.WARNING.name());
                marcaCompType.setDsMsgEsitoVerifMarca(VerificaFirmaEnums.EsitoControllo.WARNING.message());
            } else {
                marcaCompType.setTiEsitoVerifMarca(VerificaFirmaEnums.EsitoControllo.POSITIVO.name());
                marcaCompType.setDsMsgEsitoVerifMarca(VerificaFirmaEnums.EsitoControllo.POSITIVO.message());
            }
        }

        VFContrMarcaCompType contrMarcaCompType = new VFContrMarcaCompType();
        marcaCompType.getContrMarcaComps().add(contrMarcaCompType);
        contrMarcaCompType.setTiContr(VFTipoControlloType.CRITTOGRAFICO);

        XmlCV cv = bbbFounded ? bbb.getCV() : null;

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS).booleanValue()) {
            contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            if (!isTimestampCompliant) {
                contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.name());
                contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.message());
            } else if (!bbbFounded) {
                contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.ERRORE.name());
                contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.ERRORE.message());
            } else {
                if (!cv.getConclusion().getIndication().equals(Indication.PASSED)) {
                    VerificaFirmaEnums.EsitoControllo esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO; // getSubIndication
                    // ==
                    // null
                    // subindication
                    if (cv.getConclusion().getSubIndication() != null) {
                        switch (cv.getConclusion().getSubIndication()) {
                        case FORMAT_FAILURE:
                            esito = VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO;
                            break;
                        case NO_POE:
                            esito = VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO;
                            break;
                        default:
                            esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO; // default
                            LOG.debug(LOG_FMT_SUBINDICATION, esito, contrMarcaCompType.getTiContr(),
                                    cv.getConclusion().getSubIndication());
                            break;
                        }
                    }
                    // log eidas message
                    logEidasConclusion(xmlcertificateTsa, cv.getConclusion(), VFTipoControlloType.CRITTOGRAFICO.name());

                    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
                    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
                } else {
                    contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.POSITIVO.name());
                    contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.POSITIVO.message());
                }
            }
        }

        // CONTROLLI MARCA - CERTIFICATO
        contrMarcaCompType = new VFContrMarcaCompType();
        marcaCompType.getContrMarcaComps().add(contrMarcaCompType);
        contrMarcaCompType.setTiContr(VFTipoControlloType.CERTIFICATO);

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS).booleanValue()) {
            contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            // identificazione certificato
            XmlISC isc = bbbFounded ? bbb.getISC() : null;
            // validità del certificato
            XmlXCV xcv = bbbFounded ? bbb.getXCV() : null;
            //
            if (!isTimestampCompliant) {
                contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.name());
                contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.message());
            } else if (!bbbFounded) {
                contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.ERRORE.message());
                contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.ERRORE.name());
            } else {
                if (!isc.getConclusion().getIndication().equals(Indication.PASSED)) {
                    // log eidas message
                    logEidasConclusion(xmlcertificateTsa, xcv.getConclusion(), VFTipoControlloType.CERTIFICATO.name());

                    contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                    contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                } else if (!xcv.getConclusion().getIndication().equals(Indication.PASSED)) {
                    /*
                     * Unico esito supportato nelle marche è NEGATIVO (ERRORE su EIDAS non trova traduzione.....) Lo
                     * switch/case è "over" rispetto l'unico caso di esito possibile (se non PASSED), viene comunque
                     * implementato per "traccare" le possibili subindication
                     */
                    VerificaFirmaEnums.EsitoControllo esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO; // getSubIndication
                    // ==
                    // null
                    // subindication
                    if (xcv.getConclusion().getSubIndication() != null) {
                        switch (xcv.getConclusion().getSubIndication()) {
                        case OUT_OF_BOUNDS_NO_POE:
                            esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO;
                            break;
                        case OUT_OF_BOUNDS_NOT_REVOKED:
                            // The current time is not in the validity range of the signers certificate.
                            // Non è chiaro se la data di firma sia precedente o successiva alla validità
                            // del
                            // certificato.
                            // vedi anche https://ec.europa.eu/cefdigital/tracker/browse/DSS-2070
                            esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO;
                            break;
                        default:
                            esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO; // default
                            LOG.debug(LOG_FMT_SUBINDICATION, esito, contrMarcaCompType.getTiContr(),
                                    cv.getConclusion().getSubIndication());
                            break;
                        }
                    }

                    // log eidas message
                    logEidasConclusion(xmlcertificateTsa, xcv.getConclusion(), VFTipoControlloType.CERTIFICATO.name());

                    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
                    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
                } else {
                    contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.POSITIVO.name());
                    contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.POSITIVO.message());
                }
            }
        }

        // CONTROLLI MARCA - CATENA_TRUSTED
        contrMarcaCompType = new VFContrMarcaCompType();
        marcaCompType.getContrMarcaComps().add(contrMarcaCompType);
        contrMarcaCompType.setTiContr(VFTipoControlloType.CATENA_TRUSTED);
        //
        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS).booleanValue()) {
            contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else if (!isTimestampCompliant) {
            contrMarcaCompType.setTiEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.name());
            contrMarcaCompType.setDsMsgEsitoContrMarca(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.message());
        } else {
            //
            contrMarcaCompType.setTiEsitoContrMarca(
                    xmlcertificateTsa.isTrustedChain() ? VerificaFirmaEnums.EsitoControllo.POSITIVO.name()
                            : VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
            //
            contrMarcaCompType.setDsMsgEsitoContrMarca(
                    xmlcertificateTsa.isTrustedChain() ? VerificaFirmaEnums.EsitoControllo.POSITIVO.message()
                            : VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
        }

    }

    private void buildFirmaComp(VerificaFirmaWrapper wrapper, Map<String, ExtensionsDTO> extensions,
            VFFirmaCompType firmaCompType, SignatureWrapper signature, ZonedDateTime dtRef, Reports reports)
            throws VerificaFirmaWrapperGenericException {

        // add info (empty)
        VFAdditionalInfoFirmaCompType additionalInfoFirmaCompType = new VFAdditionalInfoFirmaCompType();
        firmaCompType.setAdditionalInfo(additionalInfoFirmaCompType);

        // SIGNATURE CERTIFICATE
        // certificate
        CertificateWrapper xmlcertificate = reports.getDiagnosticData()
                .getUsedCertificateById(signature.getSigningCertificate().getId());

        // FIRMATARIO + BLOB
        VFCertifFirmatarioType certifFirmatarioType = new VFCertifFirmatarioType();
        firmaCompType.setCertifFirmatario(certifFirmatarioType);

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificate, "serialNumber",
                Optional.ofNullable(xmlcertificate.getId()));
        certifFirmatarioType.setNiSerialCertifFirmatario(new BigDecimal(xmlcertificate.getSerialNumber()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificate, "notBefore",
                Optional.ofNullable(xmlcertificate.getId()));
        certifFirmatarioType
                .setDtIniValCertifFirmatario(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificate.getNotBefore()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificate, "notAfter",
                Optional.ofNullable(xmlcertificate.getId()));
        certifFirmatarioType
                .setDtFinValCertifFirmatario(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificate.getNotAfter()));

        VFFilePerFirmaType filePerFirmaType = new VFFilePerFirmaType();
        certifFirmatarioType.setFilePerFirma(filePerFirmaType);

        filePerFirmaType.setBlFilePerFirma(xmlcertificate.getBinaries());
        filePerFirmaType.setTiFilePerFirma(VFTipoFileType.CERTIF_FIRMATARIO);

        // CA
        String certificateCAId = getCertificateCAId(signature, xmlcertificate);
        //
        CertificateWrapper xmlcertificateCA = reports.getDiagnosticData().getUsedCertificateById(certificateCAId);

        VFCertifCaType certifCaType = new VFCertifCaType();
        certifFirmatarioType.setCertifCaFirmatario(certifCaType);

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateCA, "serialNumber",
                Optional.ofNullable(xmlcertificateCA.getId()));
        certifCaType.setNiSerialCertifCa(new BigDecimal(xmlcertificateCA.getSerialNumber()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateCA, "notBefore",
                Optional.ofNullable(xmlcertificateCA.getId()));
        certifCaType.setDtIniValCertifCa(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateCA.getNotBefore()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateCA, "notAfter",
                Optional.ofNullable(xmlcertificateCA.getId()));
        certifCaType.setDtFinValCertifCa(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateCA.getNotAfter()));
        // check subjectKeyId
        if (extensions.containsKey(xmlcertificateCA.getId())) {
            certifCaType.setDsSubjectKeyId(extensions.get(xmlcertificateCA.getId()).getSubjectKeyIdentifier());
        }
        certifCaType.setDlDnIssuerCertifCa(xmlcertificateCA.getCertificateIssuerDN());
        certifCaType.setDlDnSubjectCertifCa(xmlcertificateCA.getCertificateDN());

        int urlIdx = 1;
        for (String url : xmlcertificateCA.getCRLDistributionPoints()) {
            VFUrlDistribCrlType urlDistribCrlType = new VFUrlDistribCrlType();
            urlDistribCrlType.setDlUrlDistribCrl(url);
            urlDistribCrlType.setNiOrdUrlDistribCrl(new BigDecimal(urlIdx));

            certifCaType.getUrlDistribCrls().add(urlDistribCrlType);
            // inc idx
            urlIdx++;
        }
        // ocsp (from CA+certificate)
        Iterable<String> ocspCombinedUrls = CollectionUtils.union(xmlcertificateCA.getOCSPAccessUrls(),
                xmlcertificate.getOCSPAccessUrls());
        urlIdx = 1;
        for (String url : ocspCombinedUrls) {
            VFUrlDistribOcspType urlDistribOcspType = new VFUrlDistribOcspType();
            urlDistribOcspType.setDlUrlDistribOcsp(url);
            urlDistribOcspType.setNiOrdUrlDistribOcsp(new BigDecimal(urlIdx));

            certifCaType.getUrlDistribOcsps().add(urlDistribOcspType);
            // inc idx
            urlIdx++;
        }
        //
        filePerFirmaType = new VFFilePerFirmaType();
        certifCaType.setFilePerFirma(filePerFirmaType);

        filePerFirmaType.setBlFilePerFirma(xmlcertificateCA.getBinaries());
        filePerFirmaType.setTiFilePerFirma(VFTipoFileType.CERTIF_CA);
        // FIRMA : CA
        firmaCompType.setCertifCa(certifCaType);
        // nullable element
        String dsAlgoFirma = null;
        if (signature.getDigestAlgorithm() != null && signature.getEncryptionAlgorithm() != null) {
            dsAlgoFirma = signature.getDigestAlgorithm().getName().concat("with")
                    .concat(signature.getEncryptionAlgorithm().getName());
        }
        firmaCompType.setDsAlgoFirma(dsAlgoFirma);
        firmaCompType.setDlDnFirmatario(xmlcertificate.getCertificateDN());
        firmaCompType.setDsFirmaBase64(Base64.encodeBase64String(signature.getSignatureValue()));

        String cdFirmatario = null;
        if (StringUtils.isNotBlank(xmlcertificate.getSubjectSerialNumber())) {
            cdFirmatario = xmlcertificate.getSubjectSerialNumber();
        } else {
            // test
            EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificate, "serialNumber",
                    Optional.ofNullable(xmlcertificate.getId()));
            cdFirmatario = xmlcertificate.getSerialNumber();
        }
        firmaCompType.setCdFirmatario(cdFirmatario);
        firmaCompType.setDtFirma(XmlDateUtility.dateToXMLGregorianCalendar(signature.getClaimedSigningTime()));

        //
        if (!signature.getTimestampList().isEmpty()) {
            firmaCompType
                    .setTmRifTempUsato(XmlDateUtility.dateToXMLGregorianCalendar(signature.getClaimedSigningTime()));
            firmaCompType.setTipoRiferimentoTemporaleUsato(VerificaFirmaEnums.TipoRifTemporale.MT_VERS_NORMA.name());
        } else if (isVerificaAllaDataDiFirma()) {
            firmaCompType
                    .setTmRifTempUsato(XmlDateUtility.dateToXMLGregorianCalendar(signature.getClaimedSigningTime()));
            firmaCompType.setTipoRiferimentoTemporaleUsato(VerificaFirmaEnums.TipoRifTemporale.DATA_FIRMA.name());
        } else {
            firmaCompType.setTmRifTempUsato(XmlDateUtility.dateToXMLGregorianCalendar(dtRef));
            firmaCompType.setTipoRiferimentoTemporaleUsato(VerificaFirmaEnums.TipoRifTemporale.RIF_TEMP_VERS.name());
        }

        //
        firmaCompType.setTiFormatoFirma(signature.getSignatureFormat().toString());
        //
        firmaCompType.setNmCognomeFirmatario(xmlcertificate.getSurname());
        firmaCompType.setNmFirmatario(xmlcertificate.getGivenName());
        //
        firmaCompType.setTiFirma(VFTipoFirmaType.DIGITALE);

        // basic building block
        XmlBasicBuildingBlocks bbb = reports.getDetailedReport().getBasicBuildingBlockById(signature.getId());
        if (bbb == null) {
            // TOFIX: cosa fare in questi casi ?!
            LOG.warn("BasicBuildingBlockById not found SIGNATURE ID = {}", signature.getId());
        }
        // basic building block counter signature parent
        XmlBasicBuildingBlocks bbbParent = null;
        if (signature.isCounterSignature()) {
            bbbParent = reports.getDetailedReport().getBasicBuildingBlockById(signature.getParent().getId());
            if (bbbParent == null) {
                // TOFIX: cosa fare in questi casi ?!
                LOG.warn("Parent BasicBuildingBlockById not found SIGNATURE ID = {}", signature.getParent().getId());
            }
        }
        // CONTROLLI FIRMA
        // global result
        boolean isSignaturePassed = reports.getSimpleReport().isValid(signature.getId());
        buildContrFirmaComp(firmaCompType, isSignaturePassed, signature, xmlcertificateCA, bbb, bbbParent);

        // controllo CERTIFICATO (CRL+OCSP)
        VFContrFirmaCompType contrFirmaCert = firmaCompType.getContrFirmaComps().stream()
                .filter(c -> c.getTiContr().equals(VFTipoControlloType.CERTIFICATO)).collect(Collectors.toList())
                .get(0);

        // OCSP CA
        buildFirmaCompCAwithOCSP(wrapper, reports, xmlcertificateCA, firmaCompType, dtRef, extensions);
        // OCSP + CONTR
        buildFirmaCompCERwithOCSP(wrapper, reports, xmlcertificate, firmaCompType, dtRef, extensions, bbb);

        // CRL CA
        buildFirmaCompCAwithCRL(wrapper, xmlcertificateCA, certifCaType, firmaCompType, dtRef, extensions);
        // CRL + CONTR
        buildFirmaCompCERwithCRL(wrapper, xmlcertificate, certifCaType, firmaCompType, dtRef, extensions, bbb,
                contrFirmaCert);

    }

    private void buildContrFirmaComp(VFFirmaCompType firmaCompType, boolean isSignaturePassed,
            SignatureWrapper signature, CertificateWrapper xmlcertificate, XmlBasicBuildingBlocks bbb,
            XmlBasicBuildingBlocks bbbParent) {

        // format checking
        boolean isFormatCompliant = false;
        if (bbbParent != null) {
            // counter signature : non ha il controllo di formato (lo eredita dal padre)
            isFormatCompliant = bbbParent.getFC() != null
                    && bbbParent.getFC().getConclusion().getIndication().equals(Indication.PASSED);
        } else {
            isFormatCompliant = bbb != null && bbb.getFC() != null
                    && bbb.getFC().getConclusion().getIndication().equals(Indication.PASSED);
        }
        firmaCompType.setTiEsitoContrConforme(isFormatCompliant ? VerificaFirmaEnums.EsitoControllo.POSITIVO.name()
                : VerificaFirmaEnums.EsitoControllo.FORMATO_NON_CONFORME.name());
        //
        firmaCompType
                .setDsMsgEsitoContrConforme(isFormatCompliant ? VerificaFirmaEnums.EsitoControllo.POSITIVO.message()
                        : VerificaFirmaEnums.EsitoControllo.FORMATO_NON_CONFORME.name());

        // verifica risultato "globale" di verifica firma
        firmaCompType.setTiEsitoVerifFirma(isSignaturePassed ? VerificaFirmaEnums.EsitoControllo.POSITIVO.name()
                : VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
        //
        firmaCompType.setDsMsgEsitoVerifFirma(isSignaturePassed ? VerificaFirmaEnums.EsitoControllo.POSITIVO.message()
                : VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());

        // caso "particolare" di building block non trovato (vedi controllo
        // precedente con WARNING)
        // in questi casi si assumono come NON PASSATI i controlli sull'oggetto perché
        // non reperibili
        boolean bbbFounded = bbb != null;

        // CONTROLLI FIRMA - CRITTOGRAFICO signatureValidations
        // CertificateReliability
        VFContrFirmaCompType contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CRITTOGRAFICO);

        // valiazione crittografica
        XmlCV cv = bbbFounded ? bbb.getCV() : null;
        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS).booleanValue()) {
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            if (!isFormatCompliant) {
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.message());
            } else if (!bbbFounded) {
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
            } else {
                if (!cv.getConclusion().getIndication().equals(Indication.PASSED) && !isSignaturePassed) {
                    VerificaFirmaEnums.EsitoControllo esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO; // getSubIndication
                    // ==
                    // null //
                    // subindication
                    if (cv.getConclusion().getSubIndication() != null) {
                        switch (cv.getConclusion().getSubIndication()) {
                        case FORMAT_FAILURE:
                            esito = VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO;
                            break;
                        case NO_POE:
                            esito = VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO;
                            break;
                        case SIG_CRYPTO_FAILURE:
                            esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO;
                            break;
                        case HASH_FAILURE:
                            esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO;
                            break;
                        default:
                            esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO; // default
                            LOG.debug(LOG_FMT_SUBINDICATION, esito, contrFirmaCompType.getTiContr(),
                                    cv.getConclusion().getSubIndication());
                            break;
                        }
                    }
                    // log eidas message
                    logEidasConclusion(xmlcertificate, cv.getConclusion(), VFTipoControlloType.CRITTOGRAFICO.name());

                    contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                } else {
                    contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.POSITIVO.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.POSITIVO.message());
                }
            }
        }

        // TOFIX: al momento è lo stesso risultato raccolto sopra
        // CONTROLLI FIRMA - CRITTOGRAFICO_ABILITATO signatureValidations
        contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CRITTOGRAFICO_ABILITATO);

        if (!isFormatCompliant) {
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.message());
        } else if (!bbbFounded) {
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
        } else {
            if (!cv.getConclusion().getIndication().equals(Indication.PASSED) && !isSignaturePassed) {
                VerificaFirmaEnums.EsitoControllo esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO; // getSubIndication
                // ==
                // null //
                // subindication
                // subindication
                if (cv.getConclusion().getSubIndication() != null) {
                    switch (cv.getConclusion().getSubIndication()) {
                    case FORMAT_FAILURE:
                        esito = VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO;
                        break;
                    case NO_POE:
                        esito = VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO;
                        break;
                    case SIG_CRYPTO_FAILURE:
                        esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO;
                        break;
                    case HASH_FAILURE:
                        esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO;
                        break;
                    default:
                        esito = VerificaFirmaEnums.EsitoControllo.NEGATIVO; // default
                        LOG.debug(LOG_FMT_SUBINDICATION, esito, contrFirmaCompType.getTiContr(),
                                cv.getConclusion().getSubIndication());
                        break;
                    }
                }
                // log eidas message
                logEidasConclusion(xmlcertificate, cv.getConclusion(),
                        VFTipoControlloType.CRITTOGRAFICO_ABILITATO.name());

                contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
            } else {
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.POSITIVO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.POSITIVO.message());
            }
        }

        // CONTROLLI FIRMA - CATENA_TRUSTED_ABILITATO CertificateAssociation &&
        // CertificateReliability
        contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CATENA_TRUSTED_ABILITATO);

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS).booleanValue()) {
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else if (!isFormatCompliant) {
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.message());
        } else {
            contrFirmaCompType.setTiEsitoContrFirma(
                    signature.isTrustedChain() || isSignaturePassed ? VerificaFirmaEnums.EsitoControllo.POSITIVO.name()
                            : VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(signature.isTrustedChain() || isSignaturePassed
                    ? VerificaFirmaEnums.EsitoControllo.POSITIVO.message()
                    : VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
        }

        // CONTROLLI FIRMA - CERTIFICATO CertificateExpiration
        contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CERTIFICATO);

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS).booleanValue()) {
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            if (!isFormatCompliant) {
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.message());
            } else {
                // identificazione certificato
                XmlISC isc = bbbFounded ? bbb.getISC() : null;
                // validità del certificato
                XmlXCV xcv = bbbFounded ? bbb.getXCV() : null;
                if (!bbbFounded) {
                    contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                    contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                } else {
                    // isc
                    if (!isc.getConclusion().getIndication().equals(Indication.PASSED) && !isSignaturePassed) {
                        // log eidas message
                        logEidasConclusion(xmlcertificate, isc.getConclusion(), VFTipoControlloType.CERTIFICATO.name());

                        // Nota: non si effettua verifica di sottoindicazione in quanto se questo tipo
                        // di controllo è
                        // errato
                        // si assume che il certificato di firma non è valido
                        contrFirmaCompType
                                .setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.CERTIFICATO_NON_VALIDO.name());
                        contrFirmaCompType.setDsMsgEsitoContrFirma(
                                VerificaFirmaEnums.EsitoControllo.CERTIFICATO_NON_VALIDO.alternative()[0]);
                    } else if (!xcv.getConclusion().getIndication().equals(Indication.PASSED) && !isSignaturePassed) {
                        String alternativeMsg = null;
                        VerificaFirmaEnums.EsitoControllo esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_ERRATO; // getSubIndication
                        // ==
                        // null
                        // subindication xcv
                        if (xcv.getConclusion().getSubIndication() != null) {
                            switch (xcv.getConclusion().getSubIndication()) {
                            case OUT_OF_BOUNDS_NO_POE:
                                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_SCADUTO;
                                break;
                            case OUT_OF_BOUNDS_NOT_REVOKED:
                                // The current time is not in the validity range of the signers certificate.
                                // Non è chiaro se la data di firma sia precedente o successiva alla validità
                                // del
                                // certificato.
                                // vedi anche https://ec.europa.eu/cefdigital/tracker/browse/DSS-2070
                                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_SCADUTO;
                                /*
                                 * MAC 25213 - in questo caso abbiamo un esito INDETERMINATE da eidas MA sia che la
                                 * verifica sia a alla data di versamento, sia che lo sia alla data della firma se
                                 * rientra nel periodo di valità del certificato deve essere positivo.
                                 */
                                Date dataRiferimento = XmlDateUtility
                                        .xmlGregorianCalendarToDate(firmaCompType.getTmRifTempUsato());
                                Date dataInizio = XmlDateUtility.xmlGregorianCalendarToDate(
                                        firmaCompType.getCertifFirmatario().getDtIniValCertifFirmatario());
                                Date dataFine = XmlDateUtility.xmlGregorianCalendarToDate(
                                        firmaCompType.getCertifFirmatario().getDtFinValCertifFirmatario());

                                if (dataRiferimento != null && isBetween(dataRiferimento, dataInizio, dataFine)) {
                                    esito = VerificaFirmaEnums.EsitoControllo.POSITIVO;
                                }

                                break;
                            case EXPIRED:
                                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_SCADUTO;
                                break;
                            case CRYPTO_CONSTRAINTS_FAILURE_NO_POE:
                                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_NON_VALIDO;
                                break;
                            case NOT_YET_VALID:
                                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_NON_VALIDO;
                                break;
                            case TRY_LATER:
                                // revoche non presenti
                                if (versamento.getModificatoriWSCalc()
                                        .contains(Costanti.ModificatoriWS.TAG_FIRMA_1_5)) {
                                    esito = VerificaFirmaEnums.EsitoControllo.REVOCHE_NON_CONSISTENTI;
                                } else {
                                    esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_ERRATO;
                                    // alternative message (che quindi non è il defalut dell'esito)
                                    alternativeMsg = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_ERRATO
                                            .alternative()[0];
                                }
                                break;
                            case REVOKED:
                                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_REVOCATO;
                                break;
                            case NO_POE:
                                esito = VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO;
                                break;
                            default:
                                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_ERRATO; // default
                                LOG.debug(LOG_FMT_SUBINDICATION, esito, contrFirmaCompType.getTiContr(),
                                        cv.getConclusion().getSubIndication());
                                break;
                            }
                        }

                        // log eidas message
                        logEidasConclusion(xmlcertificate, xcv.getConclusion(), VFTipoControlloType.CERTIFICATO.name());

                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                        contrFirmaCompType.setDsMsgEsitoContrFirma(
                                StringUtils.isBlank(alternativeMsg) ? esito.message() : alternativeMsg);
                    } else {
                        contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.POSITIVO.name());
                        contrFirmaCompType
                                .setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.POSITIVO.message());
                    }
                }
            }
        }

        // CONTROLLI FIRMA - CATENA_TRUSTED CertificateAssociation &&
        // CertificateReliability
        // Nota: su DB risultano solo per casi molto vecchi il riferimento di questo
        // controllo con la relativa CRL
        contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CATENA_TRUSTED);

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS).booleanValue()) {
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            if (!isFormatCompliant) {
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_ESEGUITO.message());
            } else {
                contrFirmaCompType.setTiEsitoContrFirma(signature.isTrustedChain() || isSignaturePassed
                        ? VerificaFirmaEnums.EsitoControllo.POSITIVO.name()
                        : VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(signature.isTrustedChain() || isSignaturePassed
                        ? VerificaFirmaEnums.EsitoControllo.POSITIVO.message()
                        : VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
            }
        }

    }

    private void buildFirmaCompCERwithCRL(VerificaFirmaWrapper wrapper, CertificateWrapper xmlcertificate,
            VFCertifCaType certifCaType, VFFirmaCompType firmaCompType, ZonedDateTime dtRef,
            Map<String, ExtensionsDTO> extensions, XmlBasicBuildingBlocks bbb, VFContrFirmaCompType contrFirmaCert)
            throws VerificaFirmaWrapperGenericException {
        // OCSP prioritario rispetto CRL
        boolean hasOcsp = xmlcertificate.getCertificateRevocationData().stream()
                .anyMatch(r -> r.getRevocationType().equals(RevocationType.OCSP));

        // CRL
        RevocationWrapper crl = findRevocationByType(xmlcertificate, dtRef, RevocationType.CRL);
        if (hasOcsp || crl == null) {
            //
            buildContrCRLNotFoundFirmaComp(firmaCompType, xmlcertificate, contrFirmaCert, hasOcsp);
            return;
        }

        // CRL
        VFCrlType crlType = buildCrl(wrapper, extensions, crl);

        // FIRMA : CRL
        firmaCompType.setCrl(crlType);
        // CRL: CA
        crlType.setCertifCa(certifCaType);

        // controlli CRL
        buildContrCRLFirmaComp(firmaCompType, xmlcertificate, crl, bbb, contrFirmaCert, hasOcsp);
        // CRL - END
    }

    private void buildFirmaCompCERwithOCSP(VerificaFirmaWrapper wrapper, Reports reports,
            CertificateWrapper xmlcertificate, VFFirmaCompType firmaCompType, ZonedDateTime dtRef,
            Map<String, ExtensionsDTO> extensions, XmlBasicBuildingBlocks bbb)
            throws VerificaFirmaWrapperGenericException {
        // verifica presenza CRL
        boolean hasCrl = xmlcertificate.getCertificateRevocationData().stream()
                .anyMatch(r -> r.getRevocationType().equals(RevocationType.CRL));
        // OCSP
        CertificateRevocationWrapper ocsp = findRevocationByType(xmlcertificate, dtRef, RevocationType.OCSP);
        if (ocsp == null) {
            //
            buildContrOCSPNotFoundFirmaComp(firmaCompType, xmlcertificate, hasCrl);
            return;
        } // no ocsp

        // CA
        VFCertifCaType certifCaOcspType = buildOcspCertifCA(wrapper, reports, xmlcertificate, extensions, ocsp);

        // OCSP + certif
        VFOcspType ocspType = buildOcspWithCertif(wrapper, certifCaOcspType, ocsp);
        // FIRMA : OCSP
        firmaCompType.setOcsp(ocspType);

        // controlli OCSP
        buildContrOCSPFirmaComp(firmaCompType, xmlcertificate, ocsp, bbb, hasCrl);
        // OCSP - END

    }

    private void buildFirmaCompCAwithCRL(VerificaFirmaWrapper wrapper, CertificateWrapper xmlcertificateCA,
            VFCertifCaType certifCaType, VFFirmaCompType firmaCompType, ZonedDateTime dtRef,
            Map<String, ExtensionsDTO> extensions) throws VerificaFirmaWrapperGenericException {

        // OCSP prioritario rispetto CRL
        boolean hasOcsp = xmlcertificateCA.getCertificateRevocationData().stream()
                .anyMatch(r -> r.getRevocationType().equals(RevocationType.OCSP));

        // CRL
        RevocationWrapper crl = findRevocationByType(xmlcertificateCA, dtRef, RevocationType.CRL);
        if (hasOcsp || crl == null) {
            return;
        }

        // CRL
        VFCrlType crlType = buildCrl(wrapper, extensions, crl);

        // CRL : CA
        crlType.setCertifCa(certifCaType);
        // FIRMA : CRL CA
        firmaCompType.setCrlCertifCa(crlType);
        // CRL - END
    }

    private void buildFirmaCompCAwithOCSP(VerificaFirmaWrapper wrapper, Reports reports,
            CertificateWrapper xmlcertificateCA, VFFirmaCompType firmaCompType, ZonedDateTime dtRef,
            Map<String, ExtensionsDTO> extensions) throws VerificaFirmaWrapperGenericException {
        // OCSP
        RevocationWrapper ocsp = findRevocationByType(xmlcertificateCA, dtRef, RevocationType.OCSP);
        if (ocsp == null) {
            return;
        } // no ocsp

        // CA
        VFCertifCaType certifCaOcspType = buildOcspCertifCA(wrapper, reports, xmlcertificateCA, extensions, ocsp);

        // OCSP + certif
        VFOcspType ocspType = buildOcspWithCertif(wrapper, certifCaOcspType, ocsp);
        // FIRMA : OCSP
        firmaCompType.setOcspCertifCa(ocspType);
        // OCSP - END
    }

    private VFCertifCaType buildOcspCertifCA(VerificaFirmaWrapper wrapper, Reports reports,
            CertificateWrapper xmlcertificate, Map<String, ExtensionsDTO> extensions, RevocationWrapper ocsp)
            throws VerificaFirmaWrapperGenericException {
        // CA
        String certificateCAId = getCertificateCAId(ocsp, xmlcertificate);
        //
        CertificateWrapper xmlcertificateCAOCSP = reports.getDiagnosticData().getUsedCertificateById(certificateCAId);

        VFCertifCaType certifCaOcspType = new VFCertifCaType();
        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateCAOCSP, "serialNumber",
                Optional.ofNullable(xmlcertificateCAOCSP.getId()));
        certifCaOcspType.setNiSerialCertifCa(new BigDecimal(xmlcertificate.getSerialNumber()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateCAOCSP, "notBefore",
                Optional.ofNullable(xmlcertificate.getId()));
        certifCaOcspType.setDtIniValCertifCa(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificate.getNotBefore()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateCAOCSP, "notAfter",
                Optional.ofNullable(xmlcertificate.getId()));
        certifCaOcspType
                .setDtFinValCertifCa(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateCAOCSP.getNotAfter()));
        // check subjectKeyId
        if (extensions.containsKey(xmlcertificateCAOCSP.getId())) {
            certifCaOcspType.setDsSubjectKeyId(extensions.get(xmlcertificateCAOCSP.getId()).getSubjectKeyIdentifier());
        }
        certifCaOcspType.setDlDnIssuerCertifCa(xmlcertificateCAOCSP.getCertificateIssuerDN());
        certifCaOcspType.setDlDnSubjectCertifCa(xmlcertificateCAOCSP.getCertificateDN());

        int urlIdx = 1;
        for (String url : xmlcertificateCAOCSP.getCRLDistributionPoints()) {
            VFUrlDistribCrlType urlDistribCrlType = new VFUrlDistribCrlType();
            urlDistribCrlType.setDlUrlDistribCrl(url);
            urlDistribCrlType.setNiOrdUrlDistribCrl(new BigDecimal(urlIdx));

            certifCaOcspType.getUrlDistribCrls().add(urlDistribCrlType);
            // inc idx
            urlIdx++;
        }
        // ocsp (from CA+certificate)
        Iterable<String> ocspCombinedUrls = CollectionUtils.union(xmlcertificateCAOCSP.getOCSPAccessUrls(),
                xmlcertificate.getOCSPAccessUrls());
        urlIdx = 1;
        for (String url : ocspCombinedUrls) {
            VFUrlDistribOcspType urlDistribOcspType = new VFUrlDistribOcspType();
            urlDistribOcspType.setDlUrlDistribOcsp(url);
            urlDistribOcspType.setNiOrdUrlDistribOcsp(new BigDecimal(urlIdx));

            certifCaOcspType.getUrlDistribOcsps().add(urlDistribOcspType);
            // inc idx
            urlIdx++;
        }

        VFFilePerFirmaType filePerFirmaType = new VFFilePerFirmaType();
        certifCaOcspType.setFilePerFirma(filePerFirmaType);

        filePerFirmaType.setBlFilePerFirma(xmlcertificate.getBinaries());
        filePerFirmaType.setTiFilePerFirma(VFTipoFileType.CERTIF_CA);

        return certifCaOcspType;
    }

    // No CRL
    private void buildContrCRLNotFoundFirmaComp(VFFirmaCompType firmaCompType, CertificateWrapper xmlcertificate,
            VFContrFirmaCompType contrFirmaCert, boolean hasOcsp) {
        buildContrCRLFirmaComp(firmaCompType, xmlcertificate, null, null, contrFirmaCert, hasOcsp);
    }

    private void buildContrCRLFirmaComp(VFFirmaCompType firmaCompType, CertificateWrapper xmlcertificate,
            RevocationWrapper crl, XmlBasicBuildingBlocks bbb, VFContrFirmaCompType contrFirmaCert, boolean hasOcsp) {

        /*
         * caso "particolare" di building block non trovato (vedi controllo precedente con WARNING) in questi casi si
         * assumono come NON PASSATI i controlli sull'oggetto perché non reperibili
         */
        boolean bbbFounded = bbb != null;
        // presente la CRL
        boolean hasCrl = crl != null;
        // presente solo la CRL
        boolean onlyCrl = hasCrl && !hasOcsp;
        // presente solo OCSP
        boolean onlyOcsp = !onlyCrl;
        // no revoche
        boolean noRevocations = !hasCrl && !hasOcsp;

        VFContrFirmaCompType contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.CRL);

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS).booleanValue()) {
            //
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            // scenario 1 : solo crl ed errore su certificato
            if (onlyCrl
                    && !contrFirmaCert.getTiEsitoContrFirma().equals(VerificaFirmaEnums.EsitoControllo.POSITIVO.name())
                    && !contrFirmaCert.getTiEsitoContrFirma()
                            .equals(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name())
                    && !contrFirmaCert.getTiEsitoContrFirma()
                            .equals(VerificaFirmaEnums.EsitoControllo.REVOCHE_NON_CONSISTENTI.name())) {
                // presente crl e non ocsp, errore sul controllo del certificato
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.message());
            } else if (noRevocations) /* scenario 2 : crl e ocsp non presente */ {
                // valutazione CERTIFICATO_SCADUTO_3_12_2009
                // caso 1 : 3/12/2009 <= notAfter && 3/12/2009 >= notBefore
                Date revokedDate = Date
                        .from(LocalDate.of(2009, 12, 3).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                if (xmlcertificate.getNotAfter().before(revokedDate)
                        && xmlcertificate.getNotBefore().after(revokedDate)) {
                    contrFirmaCompType.setTiEsitoContrFirma(
                            VerificaFirmaEnums.EsitoControllo.CERTIFICATO_SCADUTO_3_12_2009.name());
                    contrFirmaCompType.setDsMsgEsitoContrFirma(
                            VerificaFirmaEnums.EsitoControllo.CERTIFICATO_SCADUTO_3_12_2009.message());
                } else /* caso 2 */ {
                    contrFirmaCompType
                            .setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.CRL_NON_SCARICABILE.name());
                    contrFirmaCompType
                            .setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.CRL_NON_SCARICABILE.message());
                }
            } else if (onlyOcsp) /* scenario 3 : solo ocsp */ {
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.name());
                contrFirmaCompType
                        .setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.alternative()[0]);
            } else /* scenario 4 : solo CRL disponibile */ {
                // Nota : non trovato il build block oppure l'indicazione con il risultato di
                // validazione del certifcato
                if (!bbbFounded
                        || bbb.getXCV().getSubXCV().stream().noneMatch(c -> c.getId().equals(xmlcertificate.getId()))) {
                    contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                    contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                } else {
                    //
                    XmlSubXCV subXvc = bbb.getXCV().getSubXCV().stream()
                            .filter(c -> c.getId().equals(xmlcertificate.getId())).collect(Collectors.toList()).get(0);
                    //
                    List<XmlRAC> xmlRacs = subXvc.getRAC().stream().filter(r -> r.getId().equals(crl.getId()))
                            .collect(Collectors.toList());

                    if (xmlRacs.isEmpty()) {
                        contrFirmaCompType
                                .setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                        contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                    } else if (!xmlRacs.get(0).getConclusion().getIndication().equals(Indication.PASSED)) {
                        // evaluate subindication (Revocation acceptence)
                        VerificaFirmaEnums.EsitoControllo esito = evaluateCRLXmlRacSubIndication(
                                contrFirmaCompType.getTiContr().name(), xmlRacs);

                        // log eidas message
                        logEidasConclusion(xmlcertificate, bbb.getConclusion(), VFTipoControlloType.CRL.name());

                        contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    } else {
                        contrFirmaCompType
                                .setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.POSITIVO.message());
                        contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.POSITIVO.name());
                    }
                }
            }
        }
    }

    private VerificaFirmaEnums.EsitoControllo evaluateCRLXmlRacSubIndication(String tiContr, List<XmlRAC> xmlRacs) {
        VerificaFirmaEnums.EsitoControllo esito = VerificaFirmaEnums.EsitoControllo.CRL_NON_VALIDA;
        SubIndication subIndication = xmlRacs.get(0).getConclusion().getSubIndication();
        if (subIndication != null) {
            switch (subIndication) {
            case OUT_OF_BOUNDS_NO_POE:
                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_SCADUTO;
                break;
            case REVOKED:
                esito = VerificaFirmaEnums.EsitoControllo.CERTIFICATO_REVOCATO;
                break;
            case TRY_LATER:
                esito = VerificaFirmaEnums.EsitoControllo.CRL_NON_VALIDA;
                break;
            case NOT_YET_VALID:
                esito = VerificaFirmaEnums.EsitoControllo.CRL_NON_VALIDA;
                break;
            case EXPIRED:
                esito = VerificaFirmaEnums.EsitoControllo.CRL_SCADUTA;
                break;
            default:
                esito = VerificaFirmaEnums.EsitoControllo.CRL_NON_VALIDA; // default
                LOG.debug(LOG_FMT_SUBINDICATION, esito, tiContr, subIndication);
                break;
            }
        }
        return esito;
    }

    /*
     * CRL + FILE
     */
    private VFCrlType buildCrl(VerificaFirmaWrapper wrapper, Map<String, ExtensionsDTO> extensions,
            RevocationWrapper crl) throws VerificaFirmaWrapperGenericException {
        VFCrlType crlType = new VFCrlType();
        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, crl, "thisUpdate", Optional.ofNullable(crl.getId()));
        crlType.setDtIniCrl(XmlDateUtility.dateToXMLGregorianCalendar(crl.getThisUpdate()));

        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, crl, "nextUpdate", Optional.ofNullable(crl.getId()));
        crlType.setDtScadCrl(XmlDateUtility.dateToXMLGregorianCalendar(crl.getNextUpdate()));

        if (extensions.containsKey(crl.getId()) && extensions.get(crl.getId()).getCrlNumber() != null) {
            crlType.setNiSerialCrl(new BigDecimal(extensions.get(crl.getId()).getCrlNumber()));
        }

        VFFilePerFirmaType filePerFirmaType = new VFFilePerFirmaType();
        crlType.setFilePerFirma(filePerFirmaType);

        filePerFirmaType.setBlFilePerFirma(crl.getBinaries());
        filePerFirmaType.setTiFilePerFirma(VFTipoFileType.CRL);

        return crlType;
    }

    /*
     * OCSP REVOCATION + CERTIFICATE
     */
    private VFOcspType buildOcspWithCertif(VerificaFirmaWrapper wrapper, VFCertifCaType certifCaType,
            RevocationWrapper ocsp) throws VerificaFirmaWrapperGenericException {
        // certificate
        CertificateWrapper xmlcertificateOcspResp = ocsp.getSigningCertificate();

        // certif
        VFCertifOcspType certifOcspType = new VFCertifOcspType();
        certifOcspType.setCertifCa(certifCaType);
        // Nota: certificato ocsp responder presenta un serialnumber e non un
        // subjectserial number
        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateOcspResp, "serialNumber",
                Optional.ofNullable(xmlcertificateOcspResp.getId()));
        certifOcspType.setNiSerialCertifOcsp(new BigDecimal(xmlcertificateOcspResp.getSerialNumber()));
        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateOcspResp, "notBefore",
                Optional.ofNullable(xmlcertificateOcspResp.getId()));
        certifOcspType.setDtIniValCertifOcsp(
                XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateOcspResp.getNotBefore()));
        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateOcspResp, "notAfter",
                Optional.ofNullable(xmlcertificateOcspResp.getId()));
        certifOcspType
                .setDtFinValCertifOcsp(XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateOcspResp.getNotAfter()));
        // test
        EidasWrapperResultControl.fieldCannotBeNull(wrapper, xmlcertificateOcspResp, "certificateDN",
                Optional.ofNullable(xmlcertificateOcspResp.getId()));
        certifOcspType.setDlDnSubject(xmlcertificateOcspResp.getCertificateDN());

        // file
        VFFilePerFirmaType filePerFirmaCertifOcspType = new VFFilePerFirmaType();
        certifOcspType.setFilePerFirma(filePerFirmaCertifOcspType);

        filePerFirmaCertifOcspType.setBlFilePerFirma(ocsp.getBinaries());
        filePerFirmaCertifOcspType.setTiFilePerFirma(VFTipoFileType.CERTIF_OCSP);

        // OCSP
        VFOcspType ocspType = new VFOcspType();
        // TOFIX: da verificare (ResponderID)
        // https://tools.ietf.org/html/rfc6960#section-4.2.2.3
        if (StringUtils.isNotBlank(ocsp.getSigningCertificateReference().getIssuerName())) {
            ocspType.setDsCertifIssuername(ocsp.getSigningCertificateReference().getIssuerName());
        }
        if (ocsp.getSigningCertificateReference().isIssuerSerialPresent()) {
            ocspType.setDsCertifSerialBase64(
                    Base64.encodeBase64String(ocsp.getSigningCertificateReference().getIssuerSerial()));
        }
        if (ocsp.getSigningCertificateReference().getSki() != null) {
            ocspType.setDsCertifSkiBase64(Base64.encodeBase64String(ocsp.getSigningCertificateReference().getSki()));
        }

        ocspType.setCertifOcsp(certifOcspType);
        // file
        VFFilePerFirmaType filePerFirmaOcspType = new VFFilePerFirmaType();
        ocspType.setFilePerFirma(filePerFirmaOcspType);

        filePerFirmaOcspType.setBlFilePerFirma(ocsp.getBinaries());
        filePerFirmaOcspType.setTiFilePerFirma(VFTipoFileType.OCSP);

        return ocspType;
    }

    private void buildContrOCSPNotFoundFirmaComp(VFFirmaCompType firmaCompType, CertificateWrapper xmlcertificate,
            boolean hasCrl) {
        buildContrOCSPFirmaComp(firmaCompType, xmlcertificate, null, null, hasCrl);
    }

    private void buildContrOCSPFirmaComp(VFFirmaCompType firmaCompType, CertificateWrapper xmlcertificate,
            CertificateRevocationWrapper ocsp, XmlBasicBuildingBlocks bbb, boolean hasCrl) {

        /*
         * caso "particolare" di building block non trovato (vedi controllo precedente con WARNING) in questi casi si
         * assumono come NON PASSATI i controlli sull'oggetto perché non reperibili
         */
        boolean bbbFounded = bbb != null;
        // presente OCSP
        boolean hasOcsp = ocsp != null;
        // no revoche
        boolean noRevocations = !hasCrl && !hasOcsp;

        VFContrFirmaCompType contrFirmaCompType = new VFContrFirmaCompType();
        firmaCompType.getContrFirmaComps().add(contrFirmaCompType);
        contrFirmaCompType.setTiContr(VFTipoControlloType.OCSP);

        if (!getControlliAbilitati().get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS).booleanValue()) {
            //
            contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.name());
            contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.DISABILITATO.message());
        } else {
            // scenario 1 : revoche non presenti
            if (noRevocations) {
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.OCSP_NON_SCARICABILE.name());
                contrFirmaCompType
                        .setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.OCSP_NON_SCARICABILE.message());
            } else if (hasCrl && !hasOcsp) /* scenario 2 : presente la CRL ma NON OCSP */ {
                //
                contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.name());
                contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NON_NECESSARIO.message());
            } else /* scenario 3 : ocsp presente */ {
                // Nota : non trovato il build block oppure l'indicazione con il risultato di
                // validazione del certifcato
                if (!bbbFounded
                        || bbb.getXCV().getSubXCV().stream().noneMatch(c -> c.getId().equals(xmlcertificate.getId()))) {
                    contrFirmaCompType.setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                    contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                } else {
                    //
                    XmlSubXCV subXvc = bbb.getXCV().getSubXCV().stream()
                            .filter(c -> c.getId().equals(xmlcertificate.getId())).collect(Collectors.toList()).get(0);
                    //
                    List<XmlRAC> xmlRacs = subXvc.getRAC().stream().filter(r -> r.getId().equals(ocsp.getId()))
                            .collect(Collectors.toList());

                    if (xmlRacs.isEmpty()) {
                        contrFirmaCompType
                                .setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.message());
                        contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.NEGATIVO.name());
                    } else if (!xmlRacs.get(0).getConclusion().getIndication().equals(Indication.PASSED)) {
                        // evalutate subindication
                        VerificaFirmaEnums.EsitoControllo esito = evaluateOCSPXmlRacSubIndication(ocsp,
                                contrFirmaCompType.getTiContr().name(), xmlRacs);

                        // log eidas message
                        logEidasConclusion(xmlcertificate, xmlRacs.get(0).getConclusion(),
                                VFTipoControlloType.OCSP.name());

                        contrFirmaCompType.setDsMsgEsitoContrFirma(esito.message());
                        contrFirmaCompType.setTiEsitoContrFirma(esito.name());
                    } else {
                        contrFirmaCompType
                                .setDsMsgEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.OCSP_VALIDO.message());
                        contrFirmaCompType.setTiEsitoContrFirma(VerificaFirmaEnums.EsitoControllo.OCSP_VALIDO.name());
                    }
                }
            }
        }
    }

    private VerificaFirmaEnums.EsitoControllo evaluateOCSPXmlRacSubIndication(CertificateRevocationWrapper ocsp,
            String tiContr, List<XmlRAC> xmlRacs) {
        VerificaFirmaEnums.EsitoControllo esito = VerificaFirmaEnums.EsitoControllo.OCSP_SCONOSCIUTO;
        // ocsp response known by server
        if (ocsp.isKnown()) {
            // subindication
            SubIndication subIndication = xmlRacs.get(0).getConclusion().getSubIndication();
            if (subIndication != null) {
                switch (subIndication) {
                case TRY_LATER:
                    esito = VerificaFirmaEnums.EsitoControllo.OCSP_NON_AGGIORNATO;
                    break;
                default:
                    esito = VerificaFirmaEnums.EsitoControllo.OCSP_SCONOSCIUTO; // default
                    LOG.debug(LOG_FMT_SUBINDICATION, esito, tiContr, subIndication);
                    break;
                }
            }
        }
        // revoked response by server
        if (ocsp.isRevoked()) {
            esito = VerificaFirmaEnums.EsitoControllo.OCSP_REVOCATO;
        }
        return esito;
    }

    private boolean isBetween(Date ref, Date from, Date to) {
        return !ref.after(to) && !ref.before(from);
    }

}
