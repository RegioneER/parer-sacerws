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
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore;
import it.eng.parer.ws.versamentoUpd.dto.ControlloEseguito;
import it.eng.parer.ws.versamentoUpd.dto.ControlloEseguito.FamigliaControllo;
import it.eng.parer.ws.versamentoUpd.dto.ControlloWSResp;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.xml.versUpdResp.CodiceEsitoType;
import it.eng.parer.ws.xml.versUpdResp.ControlliFallitiUlterioriType;
import it.eng.parer.ws.xml.versUpdResp.ControlliType;
import it.eng.parer.ws.xml.versUpdResp.ControlloFallitoType;
import it.eng.parer.ws.xml.versUpdResp.ControlloType;
import it.eng.parer.ws.xml.versUpdResp.ErroreType;
import it.eng.parer.ws.xml.versUpdResp.WarningType;
import it.eng.parer.ws.xml.versUpdResp.WarningsType;

/**
 *
 * @author sinatti_s
 */
public interface IControlliWsMultipli {

    Set<ControlloEseguito> getControlliDiSistema();

    Set<ControlloEseguito> getControlliGenerali();

    Set<ControlloEseguito> getControlliFallitiUlteriori();

    Set<ControlloEseguito> getControlliUnitaDocumentaria();

    Set<ControlloEseguito> getWarnings();

    Map<CSChiave, Set<ControlloEseguito>> getControlliCollegamenti();

    Set<ControlloEseguito> getControlliCollegamento(CSChiave csChiave);

    Map<String, Set<ControlloEseguito>> getControlliDocPrincipale();

    Set<ControlloEseguito> getControlliDocPrincipale(String idDocumento);

    Map<String, Set<ControlloEseguito>> getControlliAllegati();

    Set<ControlloEseguito> getControlliAllegato(String idDocumento);

    Map<String, Set<ControlloEseguito>> getControlliAnnessi();

    Set<ControlloEseguito> getControlliAnnesso(String idDocumento);

    Map<String, Set<ControlloEseguito>> getControlliAnnotazioni();

    Set<ControlloEseguito> getControlliAnnotazione(String idDocumento);

    Map<String, Set<ControlloEseguito>> getControlliComponentiDocPrincipale();

    Set<ControlloEseguito> getControlliComponenteDocPrincipale(String idComponente);

    Map<String, Set<ControlloEseguito>> getControlliComponentiAllegati();

    Set<ControlloEseguito> getControlliComponenteAllegati(String idComponente);

    Map<String, Set<ControlloEseguito>> getControlliComponentiAnnessi();

    Set<ControlloEseguito> getControlliComponenteAnnessi(String idComponente);

    Map<String, Set<ControlloEseguito>> getControlliComponentiAnnotazioni();

    Set<ControlloEseguito> getControlliComponenteAnnotazioni(String idComponente);

    // NEGATIVO
    void addEsitoControlloOnSistema(ControlloWSResp ctlrTypeWS, String errCode, String errMessage,
	    Object... params);

    default void addEsitoControlloOnSistemaBundle(ControlloWSResp ctlrTypeWS, String errCode,
	    Object... params) {
	addEsitoControlloOnSistema(ctlrTypeWS, errCode, null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnGenerali(ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
	    VoceDiErrore.TipiEsitoErrore esito, String errCode, String errMessage,
	    Object... params);

    default void addEsitoControlloOnGeneraliBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String errCode,
	    Object... params) {
	addEsitoControlloOnGenerali(ctlrTypeWS, severity, esito, errCode, null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnFallitiUlteriori(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String errCode,
	    String errMessage, Object... params);

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliUnitaDocumentaria(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String errCode,
	    String errMessage, Object... params);

    default void addEsitoControlloOnControlliUnitaDocumentariaBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String errCode,
	    Object... params) {
	addEsitoControlloOnControlliUnitaDocumentaria(ctlrTypeWS, severity, esito, errCode, null,
		params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliDocPrincipale(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idDocumento, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliDocPrincipaleBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idDocumento, String errCode, Object... params) {
	addEsitoControlloOnControlliDocPrincipale(ctlrTypeWS, severity, esito, idDocumento, errCode,
		null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliAllegati(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idDocumento, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliAllegatiBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idDocumento, String errCode, Object... params) {
	addEsitoControlloOnControlliAllegati(ctlrTypeWS, severity, esito, idDocumento, errCode,
		null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliAnnessi(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idDocumento, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliAnnessiBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idDocumento, String errCode, Object... params) {
	addEsitoControlloOnControlliAnnessi(ctlrTypeWS, severity, esito, idDocumento, errCode, null,
		params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliAnnotazioni(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idDocumento, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliAnnotazioniBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idDocumento, String errCode, Object... params) {
	addEsitoControlloOnControlliAnnotazioni(ctlrTypeWS, severity, esito, idDocumento, errCode,
		null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliComponentiDocPrincipale(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idComponente, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliComponentiDocPrincipaleBundle(
	    ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
	    VoceDiErrore.TipiEsitoErrore esito, String idComponente, String errCode,
	    Object... params) {
	addEsitoControlloOnControlliComponentiDocPrincipale(ctlrTypeWS, severity, esito,
		idComponente, errCode, null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliComponentiAllegati(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idComponente, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliComponentiAllegatiBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idComponente, String errCode, Object... params) {
	addEsitoControlloOnControlliComponentiAllegati(ctlrTypeWS, severity, esito, idComponente,
		errCode, null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliComponentiAnnessi(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idComponente, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliComponentiAnnessiBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idComponente, String errCode, Object... params) {
	addEsitoControlloOnControlliComponentiAnnessi(ctlrTypeWS, severity, esito, idComponente,
		errCode, null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliComponentiAnnotazioni(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idComponente, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliComponentiAnnotazioniBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    String idComponente, String errCode, Object... params) {
	addEsitoControlloOnControlliComponentiAnnotazioni(ctlrTypeWS, severity, esito, idComponente,
		errCode, null, params);
    }

    // NEGATIVO/WARNING
    void addEsitoControlloOnControlliCollegamento(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    CSChiave csChiave, String errCode, String errMessage, Object... params);

    default void addEsitoControlloOnControlliCollegamentoBundle(ControlloWSResp ctlrTypeWS,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    CSChiave csChiave, String errCode, Object... params) {
	addEsitoControlloOnControlliCollegamento(ctlrTypeWS, severity, esito, csChiave, errCode,
		null, params);
    }

    // NEGATIVO/WARNING
    void addControlloOnWarnings(ControlloWSResp ctlrTypeWS, String errCode, String errMessage,
	    Object... params);

    default void addControlloOnWarningsBundle(ControlloWSResp ctlrTypeWS, String errCode,
	    Object... params) {
	addControlloOnWarnings(ctlrTypeWS, errCode, null, params);
    }

    // produci esito (per tipologia)

    ControlliType produciControlliGenerali();

    ControlliFallitiUlterioriType produciControlliFallitiUlteriori(RispostaWSUpdVers rispostaWs);

    ControlliType produciControlliUnitaDocumentaria();

    WarningsType produciWarnings();

    ControlliType produciControlliDocPrincipale(String idDocumento);

    ControlliType produciControlliAllegato(String idDocumento);

    ControlliType produciControlliAnnesso(String idDocumento);

    ControlliType produciControlliAnnotazione(String idDocumento);

    ControlliType produciControlliComponenteDocPrincipale(String idComponente);

    ControlliType produciControlliComponenteAllegati(String idComponente);

    ControlliType produciControlliComponenteAnnessi(String idComponente);

    ControlliType produciControlliComponenteAnnotazioni(String idComponente);

    ControlliType produciControlliCollegamento(CSChiave csChiave);

    // utils
    default boolean allMatchEsitoControlli(List<ControlloEseguito> controlli,
	    TipiEsitoErrore esito) {
	return !controlli.isEmpty() && controlli.stream().allMatch(e -> e.getEsito().equals(esito));
    }

    default boolean anyMatchEsitoControlli(List<ControlloEseguito> controlli,
	    TipiEsitoErrore esito) {
	return !controlli.isEmpty() && controlli.stream().anyMatch(e -> e.getEsito().equals(esito));
    }

    default boolean allMatchEsitoControlliByCdFamiglia(List<ControlloEseguito> controlli,
	    FamigliaControllo famiglia, TipiEsitoErrore esito) {
	return !controlli.isEmpty()
		&& controlli.stream().filter(c -> c.getFamiglia().equals(famiglia))
			.allMatch(e -> e.getEsito().equals(esito));
    }

    default boolean anyMatchEsitoControlliByCdFamiglia(List<ControlloEseguito> controlli,
	    FamigliaControllo famiglia, TipiEsitoErrore esito) {
	return !controlli.isEmpty()
		&& controlli.stream().filter(c -> c.getFamiglia().equals(famiglia))
			.anyMatch(e -> e.getEsito().equals(esito));
    }

    // sort lista controlli
    default List<ControlloEseguito> sortControlli(Set<ControlloEseguito> controlli) {
	List<ControlloEseguito> sortedList = new ArrayList<ControlloEseguito>(controlli);
	// per tipo controllo
	Collections.sort(sortedList, (c1, c2) -> c1.getNiOrd().compareTo(c2.getNiOrd()));
	// per tipo famiglia
	Collections.sort(sortedList,
		(c1, c2) -> c1.getFamiglia().getNiOrd().compareTo(c2.getFamiglia().getNiOrd()));

	// return sorted
	return sortedList;
    }

    public void resetControlli();

    default ControlloType buildControlloType(String esito, String tipo) {
	ControlloType controllo = new ControlloType();
	controllo.setEsito(CodiceEsitoType.valueOf(esito));
	controllo.setTipoControllo(tipo);
	return controllo;
    }

    default ControlloFallitoType buildControlloFallitoType(String esito, String tipo) {
	ControlloFallitoType controllo = new ControlloFallitoType();
	controllo.setTipoControllo(tipo);
	return controllo;
    }

    default ErroreType buildErrorType(String errCode, String errMessage) {
	ErroreType errore = new ErroreType();
	errore.setCodice(errCode);
	errore.setMessaggio(errMessage);
	return errore;
    }

    default ErroreType buildErrorType(String errCode, Object... params) {
	ErroreType errore = new ErroreType();
	errore.setCodice(errCode);
	errore.setMessaggio(params.length > 0 ? MessaggiWSBundle.getString(errCode, params)
		: MessaggiWSBundle.getString(errCode));
	return errore;
    }

    default WarningType buildWarningType(String controlType) {
	WarningType warning = new WarningType();
	warning.setTipoControllo(controlType);
	return warning;
    }

    default ControlloEseguito buildControlloEseguito(Set<ControlloEseguito> controlli,
	    IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito,
	    ControlloWSResp ctlrTypeWS, String errCode, String errMessage, Object... params) {
	ControlloEseguito cft = findOrCreateControlloEseguito(controlli, ctlrTypeWS);
	// esito controllo (generale del controllo ...)
	cft.setEsito(esito);

	// skip no error
	if (StringUtils.isNotBlank(errCode)) {
	    VoceDiErrore voceDiErrore = makeAddVoceDiErrore(severity, esito, errCode, errMessage,
		    params);
	    cft.getErrori().add(voceDiErrore);
	}

	return cft;
    }

    default VoceDiErrore makeAddVoceDiErrore(IRispostaWS.SeverityEnum severity,
	    VoceDiErrore.TipiEsitoErrore esito, String errCode, String errMessage,
	    Object... params) {
	// errore registrato
	VoceDiErrore tmpErrore = new VoceDiErrore();
	tmpErrore.setSeverity(severity);
	tmpErrore.setCodiceEsito(esito);
	tmpErrore.setErrorCode(errCode);
	if (StringUtils.isNotBlank(errMessage)) {
	    tmpErrore.setErrorMessage(errMessage);
	} else {
	    tmpErrore
		    .setErrorMessage(params.length > 0 ? MessaggiWSBundle.getString(errCode, params)
			    : MessaggiWSBundle.getString(errCode));
	}

	return tmpErrore;
    }

    default ControlloEseguito findOrCreateControlloEseguito(Set<ControlloEseguito> list,
	    ControlloWSResp ctlrTypeWS) {
	ControlloEseguito controllo = null;
	/****
	 * Check if controlType already exists! 1. if not -> create new one! 2. else -> add error!
	 */
	List<ControlloEseguito> cftFiltered = list.stream()
		.filter(c -> c.getCdControllo().equals(ctlrTypeWS.getCdControllo()))
		.collect(Collectors.toList());
	if (cftFiltered != null && !cftFiltered.isEmpty()) {
	    // get one
	    controllo = cftFiltered.get(0);
	} else {
	    controllo = new ControlloEseguito();
	    controllo.setFamiglia(ControlloEseguito.FamigliaControllo
		    .valueOf(ctlrTypeWS.getCdFamiglia().toUpperCase()));
	    controllo.setCdControllo(ctlrTypeWS.getCdControllo());
	    controllo.setNiOrd(ctlrTypeWS.getNiOrd());
	    controllo.setDsControllo(ctlrTypeWS.toString());
	}
	return controllo;
    }

    default boolean verificaErrorePrinc(RispostaWSUpdVers rispostaWs, ControlloEseguito controllo,
	    VoceDiErrore errore) {
	return rispostaWs.getControlloWs().getCdControllo()
		.equalsIgnoreCase(controllo.getCdControllo())
		&& rispostaWs.getErrorCode().equalsIgnoreCase(errore.getErrorCode())
		&& rispostaWs.getErrorMessage().equalsIgnoreCase(errore.getErrorMessage());
    }

}
