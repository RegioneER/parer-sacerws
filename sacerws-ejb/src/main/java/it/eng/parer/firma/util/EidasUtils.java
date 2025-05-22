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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.firma.util;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import eu.europa.esig.dss.detailedreport.DetailedReportFacade;
import eu.europa.esig.dss.detailedreport.DetailedReportXmlDefiner;
import eu.europa.esig.dss.diagnostic.DiagnosticDataFacade;
import eu.europa.esig.dss.diagnostic.DiagnosticDataXmlDefiner;
import eu.europa.esig.dss.simplereport.SimpleReportFacade;
import eu.europa.esig.dss.simplereport.SimpleReportXmlDefiner;
import it.eng.parer.eidas.model.EidasDataToValidateMetadata;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.eidas.model.EidasRemoteDocument;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.FileBinario;

/**
 *
 * @author Sinatti_S
 */
public class EidasUtils {

    private EidasUtils() {
	throw new IllegalStateException("Utility class");
    }

    /**
     *
     * Compila l'input per la verifica firma tramite DSS. Utilizzata la versione /v2 in cui si
     * utilizza multipart con stream da file.
     *
     * @param componenteVers       componente versato
     * @param sottoComponentiFirma sotto componente di tipo FIRMA
     * @param controlliAbilitati   lista controlli abilitati (flag true/false)
     * @param dataDiRiferimento    data di riferimento
     * @param uuid                 UUID generato
     *
     * @return Bean utilizzato per invocare il servizio di verifica EIDAS.
     */
    public static EidasDataToValidateMetadata buildDataToValidateMetadataFromCompVers(
	    ComponenteVers componenteVers, List<ComponenteVers> sottoComponentiFirma,
	    Map<String, Boolean> controlliAbilitati, ZonedDateTime dataDiRiferimento, String uuid) {

	final boolean hasFirmeDetached = sottoComponentiFirma != null
		&& !sottoComponentiFirma.isEmpty();
	//
	if (!hasFirmeDetached) {
	    /*
	     * Firma non detached : il file firmato è su componente
	     */
	    FileBinario signedFB = componenteVers.getRifFileBinario();
	    return buildDocuments(controlliAbilitati, dataDiRiferimento, signedFB,
		    componenteVers.getId(), uuid);
	} else {
	    /*
	     * Firma detached : il file firmato è su sottocomponente
	     */
	    FileBinario signedFB = sottoComponentiFirma.get(0).getRifFileBinario();
	    FileBinario originalFB = componenteVers.getRifFileBinario();
	    return buildDocuments(controlliAbilitati, dataDiRiferimento, signedFB,
		    componenteVers.getId(), true, originalFB, sottoComponentiFirma.get(0).getId(),
		    uuid);
	}
    }

    private static EidasDataToValidateMetadata buildDocuments(
	    Map<String, Boolean> controlliAbilitati, ZonedDateTime dataDiRiferimento,
	    FileBinario signedDoc, String idComponente, String uuid) {
	return buildDocuments(controlliAbilitati, dataDiRiferimento, signedDoc, idComponente, false,
		null, null, uuid);
    }

    private static EidasDataToValidateMetadata buildDocuments(
	    Map<String, Boolean> controlliAbilitati, ZonedDateTime dataDiRiferimento,
	    FileBinario signedDoc, String idComponente, boolean hasFirmeDetached,
	    FileBinario originalDoc, String idSottoComponente, String uuid) {

	EidasDataToValidateMetadata input = new EidasDataToValidateMetadata();

	boolean controlloCrittogaficoAbilitato = controlliAbilitati
		.get(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS);
	boolean controlloCatenaTrustAbilitato = controlliAbilitati
		.get(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS);
	boolean controlloCertificatoAbilitato = controlliAbilitati
		.get(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS);
	boolean controlloRevocaAbilitato = controlliAbilitati
		.get(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS);
	input.setControlloCrittograficoIgnorato(!controlloCrittogaficoAbilitato);
	input.setControlloCatenaTrustIgnorato(!controlloCatenaTrustAbilitato);
	input.setControlloCertificatoIgnorato(!controlloCertificatoAbilitato);
	input.setControlloRevocaIgnorato(!controlloRevocaAbilitato);

	// no raw
	boolean includeValues = controlliAbilitati.get(ParametroApplFl.FL_EIDAS_INCLUDI_FILEBASE64);
	input.setIncludeCertificateRevocationValues(includeValues);
	input.setIncludeCertificateTokenValues(includeValues);
	input.setIncludeTimestampTokenValues(includeValues);
	// id componente firmato
	input.setDocumentId(hasFirmeDetached ? idSottoComponente : idComponente);

	// signed
	EidasRemoteDocument eidasRemoteDocument = new EidasRemoteDocument();
	eidasRemoteDocument.setName(signedDoc.getFileName());
	if (signedDoc.getObjectStorageResource() != null) {
	    eidasRemoteDocument.setUri(signedDoc.getObjectStorageResource().getPresignedURL());
	}
	input.setRemoteSignedDocument(eidasRemoteDocument);

	if (hasFirmeDetached) {
	    EidasRemoteDocument eidasRemoteDocumentOrig = new EidasRemoteDocument();
	    eidasRemoteDocumentOrig.setName(originalDoc.getFileName());
	    if (originalDoc.getObjectStorageResource() != null) {
		eidasRemoteDocumentOrig
			.setUri(originalDoc.getObjectStorageResource().getPresignedURL());
	    }
	    input.setRemoteOriginalDocuments(Arrays.asList(eidasRemoteDocumentOrig));
	}
	//
	input.setUuid(uuid);
	input.setDataDiRiferimento(Date.from(dataDiRiferimento.toInstant()));

	return input;
    }

    // tree no binaries
    /**
     * @deprecated
     *
     *             Metodo non più utilizzato in favore di una gestione votata all'I/O e all'utilizzo
     *             di stream su file anziché oggetti che occupano risorse
     *
     * @param dto {@link EidasWSReportsDTOTree}
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
	    usedcert.getRevocations()
		    .forEach(r -> r.getRevocation().setBase64Encoded(StringUtils.EMPTY.getBytes()));
	    // set null on chain
	    usedcert.getCertificateChain().forEach(
		    c -> c.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
	    // set null on signincertificate
	    if (usedcert.getSigningCertificate() != null) {
		usedcert.getSigningCertificate().getCertificate()
			.setBase64Encoded(StringUtils.EMPTY.getBytes());
	    }
	});
	// used revocations
	dto.getReport().getDiagnosticData().getUsedRevocations().forEach(usedrev -> {
	    // set null
	    usedrev.setBase64Encoded(StringUtils.EMPTY.getBytes());
	    // set null signcert
	    if (usedrev.getSigningCertificate() != null) {
		usedrev.getSigningCertificate().getCertificate()
			.setBase64Encoded(StringUtils.EMPTY.getBytes());
	    }
	    // set null on chain
	    usedrev.getCertificateChain().forEach(
		    r -> r.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
	});
	// used timestamp
	dto.getReport().getDiagnosticData().getUsedTimestamps().forEach(usedts -> {
	    // set null
	    usedts.setBase64Encoded(StringUtils.EMPTY.getBytes());
	    // set null signigncertificate
	    if (usedts.getSigningCertificate() != null) {
		usedts.getSigningCertificate().getCertificate()
			.setBase64Encoded(StringUtils.EMPTY.getBytes());
	    }
	    // set null on chain
	    usedts.getCertificateChain().forEach(
		    c -> c.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
	});

	// signatures
	dto.getReport().getDiagnosticData().getSignatures().forEach(s -> {
	    // all founded obj
	    // set null on timestamps
	    s.getFoundTimestamps()
		    .forEach(t -> t.getTimestamp().setBase64Encoded(StringUtils.EMPTY.getBytes()));
	    // set null on related cert
	    s.getFoundCertificates().getRelatedCertificates().forEach(
		    c -> c.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
	    // set null on related revocation
	    s.getFoundRevocations().getRelatedRevocations()
		    .forEach(r -> r.getRevocation().setBase64Encoded(StringUtils.EMPTY.getBytes()));
	    // set null on chain
	    s.getCertificateChain().forEach(
		    c -> c.getCertificate().setBase64Encoded(StringUtils.EMPTY.getBytes()));
	});
    }

    /*
     * Le classi sottostanti sono state create come estensione delle facede presenti su librie per
     * necessità di override del metodo getSchema(). Originariamente tale metodo utilizza una
     * factory con le direttive "secure" al fine di garantire che non sia possibile la entity
     * injection purtroppo su sacerws tali direttive al momento non sono accettate e generano una
     * eccezione di tipo SaxException (non rispettato lo standard JAXP 1.5).
     *
     * Nota: dalla versione 5.8 a fronte di un re-factor, è stata cambiata la visibilità delle
     * costanti in cui viene indicato il percorso con l'xsd, di conseguenza sono state replicate
     * sulla classe figlia.
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
		sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		schema = sf.newSchema(new Source[] {
			new StreamSource(inputStream) });
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
		sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		schema = sf.newSchema(new Source[] {
			new StreamSource(inputStream) });
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
		sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		schema = sf.newSchema(new Source[] {
			new StreamSource(isXSDDiagnosticData) });
	    }

	    return schema;
	}

    }

}
