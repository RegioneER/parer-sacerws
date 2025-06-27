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
package it.eng.parer.ws.versamentoUpd.ejb;

import java.util.Date;
import java.util.HashMap;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.ControlliWS;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.ControlliWSHelper;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.ControlloEseguito;
import it.eng.parer.ws.versamentoUpd.dto.ControlloWSResp;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.ejb.prs.UpdVersamentoPrsr;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.UpdDocumentiUtils;
import it.eng.parer.ws.xml.versUpdResp.CodiceEsitoType;
import it.eng.parer.ws.xml.versUpdResp.ComponenteType;
import it.eng.parer.ws.xml.versUpdResp.DocumentoCollegatoType.DocumentoCollegato;
import it.eng.parer.ws.xml.versUpdResp.DocumentoType;
import it.eng.parer.ws.xml.versUpdResp.EsitoGeneraleNegativoType;
import it.eng.parer.ws.xml.versUpdResp.EsitoGeneralePositivoType;
import it.eng.parer.ws.xml.versUpdResp.UnitaDocumentariaType;
import it.eng.spagoLite.security.User;

/**
 *
 * @author sinatti_s
 */
/**
 * Session Bean implementation class AggiornamentoVersamentoSync
 */
@Stateless(mappedName = "AggiornamentoVersamentoSync")
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class AggiornamentoVersamentoSync {

    //
    private static final Logger logger = LoggerFactory.getLogger(AggiornamentoVersamentoSync.class);
    @EJB
    private ControlliWS myControlliWs;

    @EJB
    private ControlliWSHelper controlliWSHelper;

    @EJB
    private ControlliSemantici controlliSemantici;

    @EJB
    private SalvataggioUpdVersamento salvataggioUpdVersamento;

    @EJB
    private RecupSessDubbieUpdVersamento updRecupSessDubbie;

    @EJB
    private LogSessioneUpdVersamento updLogSessione;

    @EJB
    private UpdVersamentoPrsr updVersamentoPrsr;

    public void init(RispostaWSUpdVers rispostaWs, AvanzamentoWs avanzamento,
	    UpdVersamentoExt versamento) {
	logger.debug("sono nel metodo init");
	rispostaWs.setSeverity(SeverityEnum.OK);
	rispostaWs.setErrorCode("");
	rispostaWs.setErrorMessage("");

	// TODO: da valutare se serve pre-caricare i valodi da DB
	RispostaControlli rs = this.caricaXmlDefault(versamento);
	// if positive ... load ws versions
	if (rs.isrBoolean()) {
	    rs = this.loadWsVersions(versamento);
	}

	// prepara la classe composita esito e la aggancia alla rispostaWS
	CompRapportoUpdVers myEsitoComposito = new CompRapportoUpdVers();
	rispostaWs.setCompRapportoUpdVers(myEsitoComposito);
	// aggancia alla rispostaWS
	rispostaWs.setAvanzamento(avanzamento);
	//
	myEsitoComposito.setVersioneRapportoVersamento(
		versamento.getDescrizione().getVersione(versamento.getWsVersions()));
	//
	myEsitoComposito
		.setDataRapportoVersamento(XmlDateUtility.dateToXMLGregorianCalendar(new Date()));
	//
	myEsitoComposito.setEsitoGeneralePositivo(new EsitoGeneralePositivoType());
	myEsitoComposito.getEsitoGeneralePositivo().setCodiceEsito(CodiceEsitoType.POSITIVO);// TODO:
											     // ancora
											     // da
											     // definire
											     // a
											     // livello
											     // di
											     // controlli

	myEsitoComposito.setEsitoGeneraleNegativo(new EsitoGeneraleNegativoType());
	myEsitoComposito.getEsitoGeneraleNegativo().setCodiceEsito(CodiceEsitoType.NON_ATTIVATO);// TODO:
												 // ancora
												 // da
												 // definire
												 // a
												 // livello
												 // di
												 // controlli

	myEsitoComposito.setUnitaDocumentaria(new UnitaDocumentariaType());

	// preparara i controlli "minimi" che verranno comunque eseguiti
	for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
		ControlloEseguito.FamigliaControllo.CONTROLLIGENERALI.name())) {
	    versamento.addControlloNAOnGenerali(controllo);
	}

	for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
		ControlloEseguito.FamigliaControllo.CONTROLLIXSD.name())) {
	    versamento.addControlloNAOnGenerali(controllo);
	}

	for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
		ControlloEseguito.FamigliaControllo.CONTROLLIINTESTAZIONE.name())) {
	    versamento.addControlloNAOnGenerali(controllo);
	}

	for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
		ControlloEseguito.FamigliaControllo.CONTROLLIUNITADOC.name())) {
	    versamento.addControlloNAOnControlliUnitaDocumentaria(controllo);
	}

	//
	if (!rs.isrBoolean()) {
	    rispostaWs.setSeverity(SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsError(rs.getCodErr(), rs.getDsErr());
	}
    }

    @SuppressWarnings("unchecked")
    protected RispostaControlli caricaXmlDefault(UpdVersamentoExt versamento) {
	RispostaControlli rispostaControlli = controlliSemantici
		.caricaDefaultDaDB(ParametroApplDB.TipoParametroAppl.VERSAMENTO_DEFAULT);
	if (rispostaControlli.isrBoolean()) {
	    versamento.setXmlDefaults((HashMap<String, String>) rispostaControlli.getrObject());
	}
	return rispostaControlli;
    }

    @SuppressWarnings("unchecked")
    protected RispostaControlli loadWsVersions(UpdVersamentoExt versamento) {
	RispostaControlli rs = myControlliWs.loadWsVersions(versamento.getDescrizione());
	// if positive ...
	if (rs.isrBoolean()) {
	    versamento.setWsVersions((HashMap<String, String>) rs.getrObject());
	}
	return rs;
    }

    public void verificaVersione(String versione, RispostaWSUpdVers rispostaWs,
	    UpdVersamentoExt versamento) {
	logger.debug("sono nel metodo verificaVersione");
	CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();
	versamento.setVersioneWsChiamata(versione);
	myEsito.setVersioneIndiceSIPAggiornamento(versione);
	RispostaControlli tmpRispostaControlli = myControlliWs.checkVersione(versione,
		versamento.getDescrizione().getNomeWs(), versamento.getWsVersions(),
		Costanti.TipiWSPerControlli.AGGIORNAMENTO_VERSAMENTO);
	if (!tmpRispostaControlli.isrBoolean()) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    // esito generale
	    rispostaWs.setEsitoWsError(
		    ControlliWSBundle
			    .getControllo(ControlliWSBundle.CTRL_GENERALI_ESISTEVERSIONEXSD),
		    tmpRispostaControlli.getCodErr(), tmpRispostaControlli.getDsErr());

	    // aggiunta su controlli generali
	    versamento.addEsitoControlloOnGenerali(
		    ControlliWSBundle
			    .getControllo(ControlliWSBundle.CTRL_GENERALI_ESISTEVERSIONEXSD),
		    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO,
		    tmpRispostaControlli.getCodErr(), tmpRispostaControlli.getDsErr());

	    // se la versione è sbagliata o inesistente, tento comunque di produrre una
	    // sessione dubbia / da verificare in fase di trasformazione errata in fallita
	    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
	} else {
	    // imposto la versione dell'xml di versamento in via provvisioria al valore del
	    // ws
	    // se riuscirò a leggere l'XML imposterò il valore effettivo
	    versamento.getStrutturaUpdVers().setVersioneIndiceSipNonVerificata(versione);
	    versamento.checkVersioneRequest(versione);
	    myEsito.setVersioneRapportoVersamento(versamento.getVersioneCalc());

	    // passato controllo versione (e ws)
	    versamento.addControlloOkOnGenerali(ControlliWSBundle
		    .getControllo(ControlliWSBundle.CTRL_GENERALI_ESISTEVERSIONEXSD));
	}
    }

    public void verificaCredenziali(String loginName, String password, String indirizzoIp,
	    RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento) {
	logger.debug("sono nel metodo verificaCredenziali");
	rispostaWs.getCompRapportoUpdVers();
	User tmpUser = null;
	RispostaControlli tmpRispostaControlli = myControlliWs.checkCredenziali(loginName, password,
		indirizzoIp, TipiWSPerControlli.AGGIORNAMENTO_VERSAMENTO);
	if (!tmpRispostaControlli.isrBoolean()) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    // esito generale
	    rispostaWs.setEsitoWsError(
		    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_UTENTE),
		    tmpRispostaControlli.getCodErr(), tmpRispostaControlli.getDsErr());

	    // aggiunta su controlli generali
	    versamento.addEsitoControlloOnGenerali(
		    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_UTENTE),
		    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO,
		    tmpRispostaControlli.getCodErr(), tmpRispostaControlli.getDsErr());

	    // sessione dubbia / da verificare in fase di trasformazione errata in fallita
	    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
	}
	//
	// esito POSITIVO
	if (tmpRispostaControlli.isrBoolean()) {
	    tmpUser = (User) tmpRispostaControlli.getrObject();
	    // passato controllo versione (e ws)
	    versamento.addControlloOkOnGenerali(
		    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_UTENTE));
	}

	versamento.setLoginName(loginName);
	versamento.setUtente(tmpUser);

    }

    public void parseXML(SyncFakeSessn sessione, RispostaWSUpdVers rispostaWs,
	    UpdVersamentoExt versamento) {
	logger.debug("sono nel metodo parseXML");
	rispostaWs.getCompRapportoUpdVers();
	AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();

	if (versamento.getUtente() == null) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
		    "Errore: l'utente non è autenticato.");
	    return;
	}

	try {
	    updVersamentoPrsr.parseXML(sessione, versamento, rispostaWs);
	    tmpAvanzamentoWs.resetFase();
	} catch (Exception e) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    if (ExceptionUtils.getRootCause(e) instanceof ParamApplicNotFoundException) {
		rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_018_001,
			((ParamApplicNotFoundException) ExceptionUtils.getRootCause(e))
				.getNmParamApplic());
	    } else {
		rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			"Errore nella fase di parsing dell'XML del EJB " + e.getMessage());
	    }
	    logger.error("Eccezione nella fase di parsing dell'XML del EJB ", e);
	}
    }

    // TODO: da fare
    /*
     * (non-Javadoc)
     *
     * @see it.eng.parer.ws.versamento.ejb.VersamentoSyncBase#salvaTutto(it.eng.parer.ws.
     * versamento.dto.SyncFakeSessn, it.eng.parer.ws.versamento.dto.RispostaWS,
     * it.eng.parer.ws.versamento.dto.VersamentoExt) 1. save su INI entities la prima versione dei
     * metadati se non presenti e sulla base di QUELLO che viene comunicato 2. saverOrUpdate su UPD
     * (sempre sulla base del comunicato + reperito da UD : in pratica sempre TUTTO dell'UD //TODO :
     * anche la parte sui documenti/annessi/etc.? ) 3. save or not su ERR / KO
     */

    public void salvaTutto(SyncFakeSessn sessione, RispostaWSUpdVers rispostaWs,
	    UpdVersamentoExt versamento) {
	logger.debug("sono nel metodo salvaTutto");
	CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();
	AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();

	// *************************************************************************************
	// build controlli on response (mandatories!)
	myEsito.setControlliGenerali(versamento.produciControlliGenerali());
	myEsito.setControlliUnitaDocumentaria(versamento.produciControlliUnitaDocumentaria());

	myEsito.setWarnings(versamento.produciWarnings());
	myEsito.setControlliFallitiUlteriori(
		versamento.produciControlliFallitiUlteriori(rispostaWs));
	// *************************************************************************************

	// *************************************************************************************
	// build controlli collegamento, documenti, etc.
	this.buildControlliDocs(versamento, myEsito);
	// *************************************************************************************

	// *************************************************************************************
	// salvataggio metadati iniziali, update, recupercupo sessioni errate,
	// conversioni fallite
	// *************************************************************************************

	// *************************************************************************************
	// registra aggiornamenti da effettuare
	// *************************************************************************************
	if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR && rispostaWs
		.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.OK) {

	    this.buildAggiornamentiDaEffettuare(versamento, myEsito);
	}

	// verifico se è simulazione di salvataggio e ho qualcosa da salvare
	if (!versamento.isSimulaScrittura() && rispostaWs
		.getStatoSessioneVersamento() != IRispostaWS.StatiSessioneVersEnum.ASSENTE) {
	    if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR && rispostaWs
		    .getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.OK) {
		tmpAvanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.SalvataggioDatiVersati)
			.setFase("inizio").logAvanzamento();
		// registra aggiornamento positivo
		if (!salvataggioUpdVersamento.salvaAggiornamento(rispostaWs, versamento,
			sessione)) {
		    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
		}
		tmpAvanzamentoWs.setFase("fine").logAvanzamento();
	    }

	    // TODO: logica da verificare
	    // recupero sessione dubbia (dalle errate per trasformarle in fallite)
	    // Nota: se in precendenza si è verificato un errore di sistema lo gestisco diversamente
	    if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.ERROR
		    && !rispostaWs.isErroreDiSistema()) {
		tmpAvanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.SalvataggioSessioneWS)
			.setFase("inizio").logAvanzamento();
		if (rispostaWs
			.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.DUBBIA) {
		    // recupero sessione (da dubbia a fallita, se possibile, altrimenti diventa
		    // errata)
		    updRecupSessDubbie.recuperaSessioneErrata(rispostaWs, versamento);
		}
		// salva sessione
		if (rispostaWs
			.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.FALLITA
			&& !rispostaWs.isErroreDiSistema()) {

		    myEsito.setControlliGenerali(versamento.produciControlliGenerali());
		    myEsito.setControlliUnitaDocumentaria(
			    versamento.produciControlliUnitaDocumentaria());

		    myEsito.setWarnings(versamento.produciWarnings());
		    myEsito.setControlliFallitiUlteriori(
			    versamento.produciControlliFallitiUlteriori(rispostaWs));
		    // *************************************************************************************
		    // build controlli collegamento, documenti, etc.
		    buildControlliDocs(versamento, myEsito);
		    // *************************************************************************************

		    if (!updLogSessione.registraSessioneFallita(rispostaWs, versamento, sessione)) {
			// se fallisco il salvataggio della sessione fallita, ci riprovo salvando
			// una
			// sessione errata con gli stessi dati. Anche in questo caso non ho usato un
			// costrutto if ... else
			rispostaWs.setStatoSessioneVersamento(
				IRispostaWS.StatiSessioneVersEnum.ERRATA);
		    }
		}
		if (rispostaWs
			.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.ERRATA
			&& !rispostaWs.isErroreDiSistema()) {

		    myEsito.setControlliGenerali(versamento.produciControlliGenerali());
		    myEsito.setControlliUnitaDocumentaria(
			    versamento.produciControlliUnitaDocumentaria());

		    myEsito.setWarnings(versamento.produciWarnings());
		    myEsito.setControlliFallitiUlteriori(
			    versamento.produciControlliFallitiUlteriori(rispostaWs));
		    // *************************************************************************************
		    // build controlli collegamento, documenti, etc.
		    buildControlliDocs(versamento, myEsito);
		    // *************************************************************************************

		    updLogSessione.registraSessioneErrata(rispostaWs, versamento, sessione);
		}

		tmpAvanzamentoWs.setFase("fine").logAvanzamento();
	    }

	    /*
	     * Registro sessione ERRATA o FALLITA per ERRORI DI SISTEMA
	     */
	    if ((rispostaWs.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.ERRATA
		    || rispostaWs
			    .getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.FALLITA)
		    && rispostaWs.isErroreDiSistema()) {
		tmpAvanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.SalvataggioSessioneWS)
			.setFase("inizio").logAvanzamento();

		salvaSessioneErrataSysError(sessione, rispostaWs, versamento);

		tmpAvanzamentoWs.setFase("fine").logAvanzamento();
	    }
	}

    }

    public void salvaSessioneErrataSysError(SyncFakeSessn sessione, RispostaWSUpdVers rispostaWs,
	    AvanzamentoWs avanzamento, UpdVersamentoExt versamento) {

	// init esito se non esiste (NON dovrebbe mai accadere)
	if (rispostaWs.getCompRapportoUpdVers() == null) {
	    init(rispostaWs, avanzamento, versamento);
	}

	// salva la sessione ERRATA
	salvaSessioneErrataSysError(sessione, rispostaWs, versamento);
    }

    private void salvaSessioneErrataSysError(SyncFakeSessn sessione, RispostaWSUpdVers rispostaWs,
	    UpdVersamentoExt versamento) {

	CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();
	/*
	 * Registro sessione ERRATA / ERRORI DI SISTEMA
	 */
	// reset controlli su versamento
	versamento.resetControlli();
	// necessario resettare tutti i controlli
	myEsito.resetControlliWithUD();

	// aggiungo l'esito negativo ai controlli (utilizzato solo in ambito di
	// registrazione sessione errata)
	versamento.addEsitoControlloOnSistema(rispostaWs.getControlloWs(),
		rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());

	updLogSessione.registraSessioneErrata(rispostaWs, versamento, sessione);

	// Nota : se fallisce anche la persistenza su sessione errata verrà resituito l'errore al
	// client
    }

    private void buildControlliDocs(UpdVersamentoExt versamento, CompRapportoUpdVers myEsito) {
	if (versamento.hasDocumentiCollegatiToUpd()
		&& myEsito.getUnitaDocumentaria().getDocumentiCollegati() != null) {
	    // per ogni documento collegato (normalizzato) .....
	    for (DocumentoCollegato doccollegato : myEsito.getUnitaDocumentaria()
		    .getDocumentiCollegati().getDocumentoCollegato()) {
		CSChiave key = new CSChiave();
		key.setAnno(Long.parseLong(doccollegato.getChiaveCollegamento().getAnno()));
		key.setNumero(doccollegato.getChiaveCollegamento().getNumero());
		key.setTipoRegistro(doccollegato.getChiaveCollegamento().getRegistro());

		doccollegato.setControlliCollegamento(versamento.produciControlliCollegamento(key));
	    }
	}

	// DocPrincipale
	if (versamento.hasDocumentoPrincipaleToUpd()
		&& myEsito.getUnitaDocumentaria().getDocumentoPrincipale() != null) {
	    if (!versamento.getControlliDocPrincipale().isEmpty()) {
		// aggiunta su controlli doc principale
		myEsito.getUnitaDocumentaria().getDocumentoPrincipale()
			.setControlliDocumento(versamento.produciControlliDocPrincipale(myEsito
				.getUnitaDocumentaria().getDocumentoPrincipale().getIDDocumento()));
	    }
	    // componenti
	    if (!versamento.getControlliComponentiDocPrincipale().isEmpty()) {
		int idx = 0;
		for (ComponenteType componente : myEsito.getUnitaDocumentaria()
			.getDocumentoPrincipale().getStrutturaOriginale().getComponenti()
			.getComponente()) {
		    // calcolo ctrl key
		    String keyCtrl = UpdDocumentiUtils.buildComponenteKeyCtrl(
			    CategoriaDocumento.Principale.name(),
			    componente.getOrdinePresentazione(), idx);
		    // aggiunta su controlli componenti
		    componente.setControlliComponente(
			    versamento.produciControlliComponenteDocPrincipale(keyCtrl));
		    idx++;
		}
	    }
	}

	// Allegati
	if (versamento.hasAllegatiToUpd() && myEsito.getUnitaDocumentaria().getAllegati() != null) {
	    // aggiunta su controlli allegati
	    for (DocumentoType allegato : myEsito.getUnitaDocumentaria().getAllegati()
		    .getAllegato()) {
		if (!versamento.getControlliAllegati().isEmpty()) {
		    allegato.setControlliDocumento(
			    versamento.produciControlliAllegato(allegato.getIDDocumento()));

		}
		// componenti su allegato
		if (!versamento.getControlliComponentiAllegati().isEmpty()) {
		    int idx = 0;
		    for (ComponenteType componente : allegato.getStrutturaOriginale()
			    .getComponenti().getComponente()) {
			// calcolo ctrl key
			String keyCtrl = UpdDocumentiUtils.buildComponenteKeyCtrl(
				CategoriaDocumento.Allegato.name(),
				componente.getOrdinePresentazione(), idx);
			// aggiunta su controlli componenti
			componente.setControlliComponente(
				versamento.produciControlliComponenteAllegati(keyCtrl));
			idx++;
		    }
		}
	    }
	}

	// Annessi
	if (versamento.hasAnnessiToUpd() && myEsito.getUnitaDocumentaria().getAnnessi() != null) {
	    // aggiunta su controlli annessi
	    for (DocumentoType annesso : myEsito.getUnitaDocumentaria().getAnnessi().getAnnesso()) {
		if (!versamento.getControlliAnnessi().isEmpty()) {
		    annesso.setControlliDocumento(
			    versamento.produciControlliAnnesso(annesso.getIDDocumento()));
		}
		// componenti
		if (!versamento.getControlliComponentiAnnessi().isEmpty()) {
		    int idx = 0;
		    for (ComponenteType componente : annesso.getStrutturaOriginale().getComponenti()
			    .getComponente()) {
			// calcolo ctrl key
			String keyCtrl = UpdDocumentiUtils.buildComponenteKeyCtrl(
				CategoriaDocumento.Annesso.name(),
				componente.getOrdinePresentazione(), idx);
			// aggiunta su controlli componenti
			componente.setControlliComponente(
				versamento.produciControlliComponenteAnnessi(keyCtrl));
			idx++;
		    }
		}

	    }
	}

	// Annotazioni
	if (versamento.hasAnnotazioniToUpd()
		&& myEsito.getUnitaDocumentaria().getAnnotazioni() != null) {
	    for (DocumentoType annotazione : myEsito.getUnitaDocumentaria().getAnnotazioni()
		    .getAnnotazione()) {
		if (!versamento.getControlliAnnotazioni().isEmpty()) {
		    // aggiunta su controlli annotazioni
		    annotazione.setControlliDocumento(
			    versamento.produciControlliAnnotazione(annotazione.getIDDocumento()));
		}
		// componenti
		if (!versamento.getControlliComponentiAnnotazioni().isEmpty()) {
		    int idx = 0;
		    for (ComponenteType componente : annotazione.getStrutturaOriginale()
			    .getComponenti().getComponente()) {
			// calcolo ctrl key
			String keyCtrl = UpdDocumentiUtils.buildComponenteKeyCtrl(
				CategoriaDocumento.Annotazione.name(),
				componente.getOrdinePresentazione(), idx);
			// aggiunta su controlli componenti
			componente.setControlliComponente(
				versamento.produciControlliComponenteAnnotazioni(keyCtrl));
			idx++;
		    }
		}
	    }
	}
    }

    private void buildAggiornamentiDaEffettuare(UpdVersamentoExt versamento,
	    CompRapportoUpdVers myEsito) {

	// *************************************************************************************
	// unità documentaria
	// *************************************************************************************
	myEsito.getUnitaDocumentaria()
		.setAggiornamentiEffettuati(versamento.produciAggiornamentiUnitaDocumentaria());
	// *************************************************************************************
	// documenti
	// *************************************************************************************
	// DocPrincipale
	if (versamento.hasDocumentoPrincipaleToUpd()
		&& myEsito.getUnitaDocumentaria().getDocumentoPrincipale() != null) {
	    if (!versamento.getAggiornamentiDocPrincipale().isEmpty()) {
		// aggiunta su controlli doc principale
		myEsito.getUnitaDocumentaria().getDocumentoPrincipale().setAggiornamentiEffettuati(
			versamento.produciAggiornamentiDocPrincipale(myEsito.getUnitaDocumentaria()
				.getDocumentoPrincipale().getIDDocumento()));
	    }
	    // componenti
	    if (!versamento.getAggiornamentiCompDocPrincipale().isEmpty()) {
		int idx = 0;
		for (ComponenteType componente : myEsito.getUnitaDocumentaria()
			.getDocumentoPrincipale().getStrutturaOriginale().getComponenti()
			.getComponente()) {
		    // calcolo ctrl key
		    String keyCtrl = UpdDocumentiUtils.buildComponenteKeyCtrl(
			    CategoriaDocumento.Principale.name(),
			    componente.getOrdinePresentazione(), idx);
		    // aggiunta su controlli componenti
		    componente.setAggiornamentiEffettuati(
			    versamento.produciAggiornamentiCompDocPrincipale(keyCtrl));
		    idx++;
		}
	    }
	}

	// Allegati
	if (versamento.hasAllegatiToUpd() && myEsito.getUnitaDocumentaria().getAllegati() != null) {
	    // aggiunta su controlli allegati
	    for (DocumentoType allegato : myEsito.getUnitaDocumentaria().getAllegati()
		    .getAllegato()) {
		if (!versamento.getAggiornamentiAllegati().isEmpty()) {
		    allegato.setAggiornamentiEffettuati(
			    versamento.produciAggiornamentiAllegato(allegato.getIDDocumento()));

		}
		// componenti su allegato
		if (!versamento.getAggiornamentiCompAllegati().isEmpty()) {
		    int idx = 0;
		    for (ComponenteType componente : allegato.getStrutturaOriginale()
			    .getComponenti().getComponente()) {
			// calcolo ctrl key
			String keyCtrl = UpdDocumentiUtils.buildComponenteKeyCtrl(
				CategoriaDocumento.Allegato.name(),
				componente.getOrdinePresentazione(), idx);
			// aggiunta su controlli componenti
			componente.setAggiornamentiEffettuati(
				versamento.produciAggiornamentiCompAllegato(keyCtrl));
			idx++;
		    }
		}
	    }
	}

	// Annessi
	if (versamento.hasAnnessiToUpd() && myEsito.getUnitaDocumentaria().getAnnessi() != null) {
	    // aggiunta su controlli annessi
	    for (DocumentoType annesso : myEsito.getUnitaDocumentaria().getAnnessi().getAnnesso()) {
		if (!versamento.getAggiornamentiAnnessi().isEmpty()) {
		    annesso.setAggiornamentiEffettuati(
			    versamento.produciAggiornamentiAnnesso(annesso.getIDDocumento()));
		}
		// componenti
		if (!versamento.getAggiornamentiCompAnnessi().isEmpty()) {
		    int idx = 0;
		    for (ComponenteType componente : annesso.getStrutturaOriginale().getComponenti()
			    .getComponente()) {
			// calcolo ctrl key
			String keyCtrl = UpdDocumentiUtils.buildComponenteKeyCtrl(
				CategoriaDocumento.Annesso.name(),
				componente.getOrdinePresentazione(), idx);
			// aggiunta su controlli componenti
			componente.setAggiornamentiEffettuati(
				versamento.produciAggiornamentiCompAnnesso(keyCtrl));
			idx++;
		    }
		}

	    }
	}

	// Annotazioni
	if (versamento.hasAnnotazioniToUpd()
		&& myEsito.getUnitaDocumentaria().getAnnotazioni() != null) {
	    for (DocumentoType annotazione : myEsito.getUnitaDocumentaria().getAnnotazioni()
		    .getAnnotazione()) {
		if (!versamento.getAggiornamentiAnnotazioni().isEmpty()) {
		    // aggiunta su controlli annotazioni
		    annotazione.setAggiornamentiEffettuati(versamento
			    .produciAggiornamentiAnnotazione(annotazione.getIDDocumento()));
		}
		// componenti
		if (!versamento.getAggiornamentiCompAnnotazioni().isEmpty()) {
		    int idx = 0;
		    for (ComponenteType componente : annotazione.getStrutturaOriginale()
			    .getComponenti().getComponente()) {
			// calcolo ctrl key
			String keyCtrl = UpdDocumentiUtils.buildComponenteKeyCtrl(
				CategoriaDocumento.Annotazione.name(),
				componente.getOrdinePresentazione(), idx);
			// aggiunta su controlli componenti
			componente.setAggiornamentiEffettuati(
				versamento.produciAggiornamentiCompAnnotazione(keyCtrl));
			idx++;
		    }
		}
	    }
	}

    }

}
