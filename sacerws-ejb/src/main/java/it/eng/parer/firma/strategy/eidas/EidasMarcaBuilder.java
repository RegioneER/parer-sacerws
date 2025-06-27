/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.firma.strategy.eidas;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import eu.europa.esig.dss.enumerations.SubIndication;
import eu.europa.esig.dss.validation.reports.Reports;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.firma.util.VerificaFirmaEnums.SacerIndication;
import it.eng.parer.firma.xml.VFAdditionalInfoMarcaCompType;
import it.eng.parer.firma.xml.VFCertifCaType;
import it.eng.parer.firma.xml.VFContrMarcaCompType;
import it.eng.parer.firma.xml.VFCrlType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VFOcspType;
import it.eng.parer.firma.xml.VFTipoControlloType;
import it.eng.parer.firma.xml.VFUrlDistribCrlType;
import it.eng.parer.firma.xml.VFUrlDistribOcspType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.utils.XmlDateUtility;

public class EidasMarcaBuilder extends EidasBaseWrapperResult
	implements IEidasBuilderVFObj<VFMarcaCompType> {

    private static final Logger LOG = LoggerFactory.getLogger(EidasMarcaBuilder.class);

    private Map<String, Boolean> controlliAbilitati;

    public EidasMarcaBuilder(Map<String, Boolean> controlliAbilitati) {
	super();
	this.controlliAbilitati = controlliAbilitati;
    }

    @Override
    public VFMarcaCompType build(EidasWSReportsDTOTree eidasReportsDto,
	    VerificaFirmaWrapper vfWrapper, SignatureWrapper signatureW,
	    Optional<TimestampWrapper> timestampW, ZonedDateTime dataDiRiferimento,
	    BigDecimal[] pgs) throws NoSuchMethodException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {

	// get TimestampWrapper
	TimestampWrapper ts = timestampW.orElseThrow(
		() -> new NullPointerException("Errore compilazione marca, timestamp NULL."));

	// reports by dss
	Reports reports = new Reports(eidasReportsDto.getReport().getDiagnosticData(),
		eidasReportsDto.getReport().getDetailedReport(),
		eidasReportsDto.getReport().getSimpleReport(),
		null /* validation report non gestito */);

	// MARCHE
	VFMarcaCompType marcaCompType = new VFMarcaCompType();
	// set pgMarche
	marcaCompType.setPgMarca(pgs[PG_MARCA]);
	// set id (id componente PARER)
	marcaCompType.setId(eidasReportsDto.getIdComponente());

	// add info (empty)
	VFAdditionalInfoMarcaCompType additionalInfoMarcaCompType = new VFAdditionalInfoMarcaCompType();
	marcaCompType.setAdditionalInfo(additionalInfoMarcaCompType);

	// ********************************
	// USED CERTIFICATE
	// ********************************
	// CERTIFICATO TSA + BLOB
	VFCertifCaType certifCaType = new VFCertifCaType();
	marcaCompType.setCertifTsa(certifCaType);

	// Timestamp no signing certificate !
	if (ts.getSigningCertificate() == null) {
	    // building block
	    XmlBasicBuildingBlocks bbb = reports.getDetailedReport()
		    .getBasicBuildingBlockById(ts.getId());
	    if (bbb == null) {
		// TOFIX: cosa fare in questi casi ?!
		LOG.warn("BasicBuildingBlockById not found TS ID = {}", ts.getId());
	    }
	    // CONTROLLI MARCA
	    buildContrMarcaComp(marcaCompType, ts, null, bbb);
	    return marcaCompType;
	}

	CertificateWrapper xmlcertificateTsa = reports.getDiagnosticData()
		.getUsedCertificateById(ts.getSigningCertificate().getId());
	// test
	EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateTsa, "serialNumber",
		Optional.ofNullable(ts.getSigningCertificate().getId()));
	certifCaType.setDsSerialCertifCa(xmlcertificateTsa.getSerialNumber());

	// test
	EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateTsa, "notAfter",
		Optional.ofNullable(ts.getSigningCertificate().getId()));
	certifCaType.setDtFinValCertifCa(
		XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateTsa.getNotAfter()));

	// test
	EidasWrapperResultControl.fieldCannotBeNull(xmlcertificateTsa, "notBefore",
		Optional.ofNullable(ts.getSigningCertificate().getId()));
	certifCaType.setDtIniValCertifCa(
		XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateTsa.getNotBefore()));
	// Note: report EIDAS doens't cover it ! ( SubjectKeyId non presente su rerport)
	// certifCaType.setDsSubjectKeyId();
	int urlIdx = 1;
	// check NON empty URL
	List<String> crlFiltered = xmlcertificateTsa.getCRLDistributionPoints().stream()
		.filter(StringUtils::isNotBlank).collect(Collectors.toList());
	for (String url : crlFiltered) {
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
	CertificateWrapper xmlcertificateCA = reports.getDiagnosticData()
		.getUsedCertificateById(certificateCAId);
	// ocsp (from CA+certificate)
	// check NON empty URL
	Iterable<String> ocspCombinedUrls = CollectionUtils
		.union(xmlcertificateCA.getOCSPAccessUrls(), xmlcertificateTsa.getOCSPAccessUrls())
		.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
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

	// *************************
	// nullable element
	String dsAlgoMarca = null;
	if (ts.getDigestAlgorithm() != null && ts.getEncryptionAlgorithm() != null) {
	    dsAlgoMarca = ts.getDigestAlgorithm().getName().concat("with")
		    .concat(ts.getEncryptionAlgorithm().getName());
	}
	marcaCompType.setDsAlgoMarca(dsAlgoMarca);
	marcaCompType
		.setTmMarcaTemp(XmlDateUtility.dateToXMLGregorianCalendar(ts.getProductionTime()));
	marcaCompType.setTiFormatoMarca(signatureW.getSignatureFormat().toString());// ereditato
										    // dalla firma
	marcaCompType.setDtScadMarca(
		XmlDateUtility.dateToXMLGregorianCalendar(xmlcertificateTsa.getNotAfter()));
	// Note: report EIDAS doens't cover it ! ( MarcaBase64 non presente su rerport)
	// marcaCompType.setDsMarcaBase64();
	/**
	 * CONTROLLI / ESITI MARCA
	 */
	// building block
	XmlBasicBuildingBlocks bbb = reports.getDetailedReport()
		.getBasicBuildingBlockById(ts.getId());
	if (bbb == null) {
	    // TOFIX: cosa fare in questi casi ?!
	    LOG.warn("BasicBuildingBlockById not found TS ID = {}", ts.getId());
	}
	// CONTROLLI MARCA
	buildContrMarcaComp(marcaCompType, ts, xmlcertificateTsa, bbb);

	// OCSP
	buildMarcaCompTSAwithOCSP(marcaCompType, reports, xmlcertificateTsa, dataDiRiferimento,
		bbb);

	// CRL
	buildMarcaCompTSAwithCRL(marcaCompType, certifCaType, xmlcertificateTsa, dataDiRiferimento,
		bbb);

	return marcaCompType;
    }

    /*
     * Nota: non possiamo persistere più di una CRL (non supportato), viene costruita un'apposita
     * lista "filtrata" con il solo elemento utile
     *
     */
    private void buildMarcaCompTSAwithCRL(VFMarcaCompType marcaCompType,
	    VFCertifCaType certifCaType, CertificateWrapper xmlcertificateTsa,
	    ZonedDateTime dataDiRiferimento, XmlBasicBuildingBlocks bbb)
	    throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
	    InvocationTargetException {

	// OCSP prioritario rispetto CRL
	boolean hasOcsp = xmlcertificateTsa.getCertificateRevocationData().stream()
		.anyMatch(r -> r.getRevocationType().equals(RevocationType.OCSP));

	// CRL
	RevocationWrapper crl = findRevocationByType(xmlcertificateTsa, dataDiRiferimento,
		RevocationType.CRL);
	if (hasOcsp || crl == null) {
	    //
	    buildContrCRLNotFoundMarcaComp(marcaCompType, xmlcertificateTsa, hasOcsp);
	    return;
	} // no crl

	// CRL
	VFCrlType crlTypeTsa = buildCrl(crl);
	// CRL : CA
	crlTypeTsa.setCertifCa(certifCaType);
	// MARCA : CRL
	marcaCompType.setCrlTsa(crlTypeTsa);

	// controlli CRL
	buildContrCRLMarcaComp(marcaCompType, xmlcertificateTsa, crl, bbb, hasOcsp);
	// CRL - End
    }

    private void buildMarcaCompTSAwithOCSP(VFMarcaCompType marcaCompType, Reports reports,
	    CertificateWrapper xmlcertificateTsa, ZonedDateTime dataDiRiferimento,
	    XmlBasicBuildingBlocks bbb) throws NoSuchMethodException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	// verifica presenza CRL
	boolean hasCrl = xmlcertificateTsa.getCertificateRevocationData().stream()
		.anyMatch(r -> r.getRevocationType().equals(RevocationType.CRL));

	// OCSP
	CertificateRevocationWrapper ocsp = findRevocationByType(xmlcertificateTsa,
		dataDiRiferimento, RevocationType.OCSP);
	if (ocsp == null) {
	    //
	    buildContrOCSPNotFoundMarcaComp(marcaCompType, xmlcertificateTsa, hasCrl);
	    return;
	} // no crl

	// CA
	VFCertifCaType certifCaOcspType = buildOcspCertifCA(reports, xmlcertificateTsa, ocsp);

	// build OCSP
	VFOcspType ocspTypeTsa = buildOcspWithCertif(certifCaOcspType, ocsp);
	marcaCompType.setOcspTsa(ocspTypeTsa);
	//
	// CRONTROLLI OCSP
	buildContrOCSPMarcaComp(marcaCompType, xmlcertificateTsa, ocsp, bbb, hasCrl);
	// OCSP - End
    }

    private void buildContrOCSPNotFoundMarcaComp(VFMarcaCompType marcaCompType,
	    CertificateWrapper xmlcertificateTsa, boolean hasCrl) {
	buildContrOCSPMarcaComp(marcaCompType, xmlcertificateTsa, null, null, hasCrl);
    }

    private void buildContrOCSPMarcaComp(VFMarcaCompType marcaCompType,
	    CertificateWrapper xmlcertificateTsa, CertificateRevocationWrapper ocsp,
	    XmlBasicBuildingBlocks bbb, boolean hasCrl) {

	/*
	 * caso "particolare" di building block non trovato (vedi controllo precedente con WARNING)
	 * in questi casi si assumono come NON PASSATI i controlli sull'oggetto perché non
	 * reperibili
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

	if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS).booleanValue()) {
	    SacerIndication esito = SacerIndication.DISABILITATO;
	    //
	    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
	    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	} else {
	    // scenario 1 : revoche non presenti
	    if (noRevocations) {
		SacerIndication esito = SacerIndication.OCSP_NON_SCARICABILE;

		contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	    } else if (hasCrl && !hasOcsp) /* scenario 2 : presente la CRL ma NON OCSP */ {
		SacerIndication esito = SacerIndication.NON_NECESSARIO;
		//
		contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		contrMarcaCompType.setDsMsgEsitoContrMarca(
			esito.message() + ": non è necessario in quanto avviene tramite CRL");
	    } else /* scenario 2 : ocsp presente */ {
		// Nota : non trovato il build block oppure l'indicazione con il risultato di
		// validazione del certifcato
		if (!bbbFounded || bbb.getXCV().getSubXCV().stream()
			.noneMatch(c -> c.getId().equals(xmlcertificateTsa.getId()))) {
		    SacerIndication esito = SacerIndication.NEGATIVO;
		    //
		    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
		} else {
		    //
		    XmlSubXCV subXvc = bbb.getXCV().getSubXCV().stream()
			    .filter(c -> c.getId().equals(xmlcertificateTsa.getId()))
			    .collect(Collectors.toList()).get(0);
		    //
		    List<XmlRAC> xmlRacs = subXvc.getCRS().getRAC().stream()
			    .filter(r -> r.getId().equals(ocsp.getId()))
			    .collect(Collectors.toList());

		    if (xmlRacs.isEmpty() || !xmlRacs.get(0).getConclusion().getIndication()
			    .equals(Indication.PASSED)) {
			//
			logEidasConclusion(xmlcertificateTsa, xmlRacs.get(0),
				VFTipoControlloType.OCSP.name());
			// evalutate subindication
			SacerIndication esito = evaluateOCSPXmlRacSubIndication(ocsp,
				contrMarcaCompType.getTiContr().name(), xmlRacs);
			//
			contrMarcaCompType.setTiEsitoContrMarca(esito.name());
			contrMarcaCompType.setDsMsgEsitoContrMarca(generateErrorContrMsgEsito(
				esito.message(), xmlRacs.get(0).getConclusion().getIndication(),
				Optional.empty()));
		    } else {
			SacerIndication esito = SacerIndication.OCSP_VALIDO;
			//
			contrMarcaCompType.setTiEsitoContrMarca(esito.name());
			contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
		    }
		}
	    }
	}
    }

    private void buildContrMarcaComp(VFMarcaCompType marcaCompType, TimestampWrapper ts,
	    CertificateWrapper xmlcertificateTsa, XmlBasicBuildingBlocks bbb) {
	//
	boolean bbbFounded = bbb != null;
	// TOFIX: da verificare se diversa condizione
	boolean isTimestampCompliant = ts.isSignatureIntact() && ts.isSignatureValid();

	SacerIndication esitoContrConforme = isTimestampCompliant ? SacerIndication.POSITIVO
		: SacerIndication.FORMATO_NON_CONOSCIUTO;

	marcaCompType.setTiEsitoContrConforme(esitoContrConforme.name());
	marcaCompType.setDsMsgEsitoContrConforme(esitoContrConforme.message());

	if (!bbbFounded) {
	    SacerIndication esito = SacerIndication.NEGATIVO;
	    //
	    marcaCompType.setTiEsitoVerifMarca(esito.name());
	    marcaCompType.setDsMsgEsitoVerifMarca(esito.message());
	} else {
	    if (!bbb.getConclusion().getIndication().equals(Indication.PASSED)) {
		logEidasConclusion(xmlcertificateTsa, bbb.getConclusion());
		//
		SacerIndication esito = SacerIndication.WARNING;
		//
		marcaCompType.setTiEsitoVerifMarca(esito.name());
		marcaCompType.setDsMsgEsitoVerifMarca(generateErrorContrMsgEsito(esito.message(),
			bbb.getConclusion().getIndication(), Optional.empty()));
	    } else {
		SacerIndication esito = SacerIndication.POSITIVO;
		//
		marcaCompType.setTiEsitoVerifMarca(esito.name());
		marcaCompType.setDsMsgEsitoVerifMarca(esito.message());
	    }
	}

	VFContrMarcaCompType contrMarcaCompType = new VFContrMarcaCompType();
	marcaCompType.getContrMarcaComps().add(contrMarcaCompType);
	contrMarcaCompType.setTiContr(VFTipoControlloType.CRITTOGRAFICO);

	XmlCV cv = bbbFounded ? bbb.getCV() : null;

	if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS).booleanValue()) {
	    SacerIndication esito = SacerIndication.DISABILITATO;
	    //
	    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
	    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	} else {
	    if (!isTimestampCompliant) {
		SacerIndication esito = SacerIndication.NON_ESEGUITO;
		//
		contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	    } else if (!bbbFounded) {
		SacerIndication esito = SacerIndication.ERRORE;
		//
		contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	    } else {
		if (!cv.getConclusion().getIndication().equals(Indication.PASSED)) {
		    // init
		    SacerIndication esito = SacerIndication.NEGATIVO; // getSubIndication
		    SubIndication subIndication = cv.getConclusion().getSubIndication();
		    //
		    if (subIndication != null) {
			switch (subIndication) {
			case FORMAT_FAILURE:
			    esito = SacerIndication.NON_ESEGUITO;
			    break;
			case NO_POE:
			    esito = SacerIndication.NON_NECESSARIO;
			    break;
			default:
			    esito = SacerIndication.NEGATIVO; // default
			    LOG.debug(LOG_FMT_SUBINDICATION, esito, contrMarcaCompType.getTiContr(),
				    subIndication);
			    break;
			}
		    }
		    // log eidas message
		    logEidasConclusion(xmlcertificateTsa, cv,
			    VFTipoControlloType.CRITTOGRAFICO.name());

		    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		    contrMarcaCompType.setDsMsgEsitoContrMarca(generateErrorContrMsgEsito(
			    esito.message(), subIndication, Optional.empty()));
		} else {
		    contrMarcaCompType.setTiEsitoContrMarca(SacerIndication.POSITIVO.name());
		    contrMarcaCompType.setDsMsgEsitoContrMarca(SacerIndication.POSITIVO.message());
		}
	    }
	}

	// CONTROLLI MARCA - CERTIFICATO
	contrMarcaCompType = new VFContrMarcaCompType();
	marcaCompType.getContrMarcaComps().add(contrMarcaCompType);
	contrMarcaCompType.setTiContr(VFTipoControlloType.CERTIFICATO);

	if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS).booleanValue()) {
	    SacerIndication esito = SacerIndication.DISABILITATO;
	    //
	    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
	    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	} else {
	    // identificazione certificato
	    XmlISC isc = bbbFounded ? bbb.getISC() : null;
	    // validità del certificato
	    XmlXCV xcv = bbbFounded ? bbb.getXCV() : null;
	    //
	    if (!isTimestampCompliant) {
		SacerIndication esito = SacerIndication.DISABILITATO;
		//
		contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	    } else if (!bbbFounded) {
		SacerIndication esito = SacerIndication.ERRORE;
		//
		contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	    } else {
		if (!isc.getConclusion().getIndication().equals(Indication.PASSED)) {
		    // log eidas message
		    logEidasConclusion(xmlcertificateTsa, isc,
			    VFTipoControlloType.CERTIFICATO.name());

		    SacerIndication esito = SacerIndication.NEGATIVO;
		    //
		    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		    contrMarcaCompType
			    .setDsMsgEsitoContrMarca(generateErrorContrMsgEsito(esito.message(),
				    isc.getConclusion().getIndication(), Optional.empty()));
		} else if (!xcv.getConclusion().getIndication().equals(Indication.PASSED)) {
		    /*
		     * Unico esito supportato nelle marche è NEGATIVO (ERRORE su EIDAS non trova
		     * traduzione.....) Lo switch/case è "over" rispetto l'unico caso di esito
		     * possibile (se non PASSED), viene comunque implementato per "traccare" le
		     * possibili subindication
		     */
		    // init
		    SacerIndication esito = SacerIndication.NEGATIVO; // getSubIndication
		    SubIndication subIndication = xcv.getConclusion().getSubIndication();
		    Optional<String> details = Optional.empty();
		    //
		    if (subIndication != null) {
			switch (subIndication) {
			case OUT_OF_BOUNDS_NO_POE:
			case OUT_OF_BOUNDS_NOT_REVOKED:
			    // The current time is not in the validity range of the signers
			    // certificate.
			    // Non è chiaro se la data di firma sia precedente o successiva alla
			    // validità
			    // del
			    // certificato.
			    // vedi anche https://ec.europa.eu/cefdigital/tracker/browse/DSS-2070
			    esito = SacerIndication.NEGATIVO;
			    details = Optional.of((xmlcertificateTsa.getNotAfter() != null
				    || xmlcertificateTsa.getNotBefore() != null
					    ? ": Il certificato "
					    : "")
				    + (xmlcertificateTsa.getNotAfter() != null
					    ? " scaduto in data " + dateFormatter
						    .format(xmlcertificateTsa.getNotAfter())
					    : "")
				    + (xmlcertificateTsa.getNotBefore() != null
					    ? " valido a partire dalla data "
						    + dateFormatter.format(
							    xmlcertificateTsa.getNotBefore())
						    + " successivo al riferimento temporale utilizzato "
						    + dateFormatter
							    .format(xmlcertificateTsa.getNotAfter())
					    : ""));
			    break;
			default:
			    esito = SacerIndication.NEGATIVO; // default
			    LOG.debug(LOG_FMT_SUBINDICATION, esito, contrMarcaCompType.getTiContr(),
				    cv.getConclusion().getSubIndication());
			    break;
			}
		    }

		    // log eidas message
		    logEidasConclusion(xmlcertificateTsa, xcv,
			    VFTipoControlloType.CERTIFICATO.name());

		    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		    contrMarcaCompType.setDsMsgEsitoContrMarca(
			    generateErrorContrMsgEsito(esito.message(), subIndication, details));
		} else {
		    contrMarcaCompType.setTiEsitoContrMarca(SacerIndication.POSITIVO.name());
		    contrMarcaCompType.setDsMsgEsitoContrMarca(SacerIndication.POSITIVO.message());
		}
	    }
	}

	// CONTROLLI MARCA - CATENA_TRUSTED
	contrMarcaCompType = new VFContrMarcaCompType();
	marcaCompType.getContrMarcaComps().add(contrMarcaCompType);
	contrMarcaCompType.setTiContr(VFTipoControlloType.CATENA_TRUSTED);
	//
	if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS).booleanValue()) {
	    SacerIndication esito = SacerIndication.DISABILITATO;
	    //
	    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
	    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	} else if (!isTimestampCompliant || xmlcertificateTsa == null) {
	    SacerIndication esito = SacerIndication.NON_ESEGUITO;
	    //
	    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
	    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	} else {
	    SacerIndication esito = xmlcertificateTsa.isTrustedChain() ? SacerIndication.POSITIVO
		    : SacerIndication.NEGATIVO;
	    //
	    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
	    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	}

    }

    private void buildContrCRLNotFoundMarcaComp(VFMarcaCompType marcaCompType,
	    CertificateWrapper xmlcertificateTsa, boolean hasOcsp) {
	buildContrCRLMarcaComp(marcaCompType, xmlcertificateTsa, null, null, hasOcsp);
    }

    private void buildContrCRLMarcaComp(VFMarcaCompType marcaCompType,
	    CertificateWrapper xmlcertificateTsa, RevocationWrapper crl, XmlBasicBuildingBlocks bbb,
	    boolean hasOcsp) {
	/*
	 * caso "particolare" di building block non trovato (vedi controllo precedente con WARNING)
	 * in questi casi si assumono come NON PASSATI i controlli sull'oggetto perché non
	 * reperibili
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

	if (!controlliAbilitati.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS).booleanValue()) {
	    SacerIndication esito = SacerIndication.DISABILITATO;
	    //
	    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
	    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
	} else {
	    // scenario 1 : crl e ocsp non presente
	    if (noRevocations) {
		// caso 1 : 3/12/2009 <= notAfter && 3/12/2009 >= notBefore
		Date revokedDate = Date.from(LocalDate.of(2009, 12, 3).atStartOfDay()
			.atZone(ZoneId.systemDefault()).toInstant());
		if (xmlcertificateTsa.getNotAfter().before(revokedDate)
			&& xmlcertificateTsa.getNotBefore().after(revokedDate)) {
		    SacerIndication esito = SacerIndication.CERTIFICATO_SCADUTO_3_12_2009;
		    //
		    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
		} else /* caso 2 */ {
		    SacerIndication esito = SacerIndication.CRL_NON_SCARICABILE;
		    //
		    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());

		}
	    } else /* scenario 2 : solo ocsp */ if (onlyOcsp) {
		SacerIndication esito = SacerIndication.NON_NECESSARIO;
		//
		contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message()
			+ ": controllo non necessario in quanto avviene tramite OCSP");
	    } /* scenario 3 : solo CRL disponibile */ else {
		if (!bbbFounded || bbb.getXCV().getSubXCV().stream()
			.noneMatch(c -> c.getId().equals(xmlcertificateTsa.getId()))) {
		    SacerIndication esito = SacerIndication.NEGATIVO;
		    //
		    contrMarcaCompType.setTiEsitoContrMarca(esito.name());
		    contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
		} else {
		    //
		    XmlSubXCV subXvc = bbb.getXCV().getSubXCV().stream()
			    .filter(c -> c.getId().equals(xmlcertificateTsa.getId()))
			    .collect(Collectors.toList()).get(0);
		    //
		    List<XmlRAC> xmlRacs = subXvc.getCRS().getRAC().stream()
			    .filter(r -> r.getId().equals(crl.getId()))
			    .collect(Collectors.toList());
		    if (xmlRacs.isEmpty()) {
			SacerIndication esito = SacerIndication.NEGATIVO;
			//
			contrMarcaCompType.setTiEsitoContrMarca(esito.name());
			contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
		    } else if (!xmlRacs.get(0).getConclusion().getIndication()
			    .equals(Indication.PASSED)) {
			// log eidas message
			logEidasConclusion(xmlcertificateTsa, bbb.getConclusion(),
				VFTipoControlloType.CRL.name());

			// evaluate subindication (Revocation acceptence)
			SacerIndication esito = evaluateCRLXmlRacSubIndication(
				contrMarcaCompType.getTiContr().name(), xmlRacs);
			//
			contrMarcaCompType.setTiEsitoContrMarca(esito.name());
			contrMarcaCompType.setDsMsgEsitoContrMarca(generateErrorContrMsgEsito(
				esito.message(), xmlRacs.get(0).getConclusion().getIndication(),
				Optional.empty()));
		    } else {
			SacerIndication esito = SacerIndication.POSITIVO;
			//
			contrMarcaCompType.setTiEsitoContrMarca(esito.name());
			contrMarcaCompType.setDsMsgEsitoContrMarca(esito.message());
		    }
		}
	    }
	}
    }

}
