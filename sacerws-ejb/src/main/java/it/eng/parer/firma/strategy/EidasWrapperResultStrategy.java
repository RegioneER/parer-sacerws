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

package it.eng.parer.firma.strategy;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import eu.europa.esig.dss.validation.reports.Reports;
import it.eng.parer.eidas.model.EidasValidationResponse;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;
import it.eng.parer.firma.strategy.eidas.EidasBaseWrapperResult;
import it.eng.parer.firma.strategy.eidas.factory.EidasVFFirmaCompBuilder;
import it.eng.parer.firma.strategy.eidas.factory.EidasVFMarcaCompBuilder;
import it.eng.parer.firma.xml.VFAdditionalInfoBustaType;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.firma.xml.VerificaFirmaWrapper.VFBusta;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.XmlDateUtility;

public class EidasWrapperResultStrategy extends EidasBaseWrapperResult
	implements IVerificaFirmaWrapperResultStrategy<EidasValidationResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(EidasWrapperResultStrategy.class);

    private Map<String, Boolean> controlliAbilitati;
    private boolean isDataDiRiferimentoOnCompVers;
    private ZonedDateTime dataDiRiferimento;
    private Set<Costanti.ModificatoriWS> modificatoriWSCalc;

    /**
     * Firma necessaria al costrutture per le logiche successive che serviranno a determinare, sulla
     * base dell'output di verifica, come gestire determinate casistiche (vedi controlli e tipo rif
     * alla data)
     *
     * @param controlliAbilitati            lista dei controlli (flag true/false)
     * @param isDataDiRiferimentoOnCompVers data di riferimento specificata su componente
     * @param dataDiRiferimento             data di rifiremento da utilizzare per verifica firma
     * @param modificatoriWSCalc            modificatori versione SIP
     */
    public EidasWrapperResultStrategy(Map<String, Boolean> controlliAbilitati,
	    boolean isDataDiRiferimentoOnCompVers, ZonedDateTime dataDiRiferimento,
	    Set<Costanti.ModificatoriWS> modificatoriWSCalc) {
	super();
	this.controlliAbilitati = controlliAbilitati;
	this.isDataDiRiferimentoOnCompVers = isDataDiRiferimentoOnCompVers;
	this.dataDiRiferimento = dataDiRiferimento;
	this.modificatoriWSCalc = modificatoriWSCalc;
    }

    @Override
    public String getCode() {
	return CdServizioVerificaCompDoc.EIDAS.name();
    }

    @Override
    public void fromVerificaOutOnWrapper(EidasValidationResponse esito,
	    VerificaFirmaWrapper wrapper) throws NoSuchMethodException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	/*
	 * Nota: contatori GLOBALI
	 */
	BigDecimal[] pgs = new BigDecimal[] {
		BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE };

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
	compileWrapper(wrapper, esito.getEidasWSReportsDTOTree(), pgs);
    }

    private void compileWrapper(VerificaFirmaWrapper wrapper, EidasWSReportsDTOTree dto,
	    BigDecimal[] pgs) throws NoSuchMethodException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	// root element
	if (!dto.isParent()) {
	    // build busta
	    buildBusta(wrapper, dto, pgs);
	}
	// only signed child && at least one signature
	List<EidasWSReportsDTOTree> childSigned = dto.getChildren().stream().filter(
		c -> !c.isUnsigned() && c.getReport().getSimpleReport().getSignaturesCount() != 0)
		.collect(Collectors.toList());
	for (EidasWSReportsDTOTree child : childSigned) {
	    // build busta
	    buildBusta(wrapper, child, pgs);
	    // recursive
	    compileWrapper(wrapper, child, pgs);
	}
    }

    private void buildBusta(VerificaFirmaWrapper wrapper, EidasWSReportsDTOTree dto,
	    BigDecimal[] pgs) throws NoSuchMethodException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	// reports by dss
	Reports reports = new Reports(dto.getReport().getDiagnosticData(),
		dto.getReport().getDetailedReport(), dto.getReport().getSimpleReport(),
		null /* validation report non gestito */);

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
	    // build firma
	    VFFirmaCompType firmaCompType = new EidasVFFirmaCompBuilder().create(controlliAbilitati,
		    isDataDiRiferimentoOnCompVers, dataDiRiferimento, modificatoriWSCalc, dto,
		    wrapper, signature, Optional.empty(), pgs);

	    // inc pgFirma
	    pgs[PG_FIRMA] = pgs[PG_FIRMA].add(BigDecimal.ONE);

	    // marche temporali
	    for (TimestampWrapper ts : signature.getTimestampList()) {
		// build marca
		VFMarcaCompType marcaCompType = new EidasVFMarcaCompBuilder().create(
			controlliAbilitati, isDataDiRiferimentoOnCompVers, dataDiRiferimento,
			modificatoriWSCalc, dto, wrapper, signature, Optional.of(ts), pgs);

		// add on list
		firmaCompType.getMarcaComps().add(marcaCompType);

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
    }

}
