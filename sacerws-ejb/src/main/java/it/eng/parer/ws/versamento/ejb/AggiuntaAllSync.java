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

package it.eng.parer.ws.versamento.ejb;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.util.DateUtilsConverter;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.ErrorTypeEnum;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.PayLoad;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.RispostaWSAggAll;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VersamentoExtAggAll;
import it.eng.parer.ws.versamento.ejb.prs.VersamentoExtAggAllPrsr;
import it.eng.parer.ws.versamentoTpi.utils.FileServUtils;
import it.eng.parer.ws.xml.versResp.ECEsitoChiamataWSType;
import it.eng.parer.ws.xml.versResp.ECEsitoExtType;
import it.eng.parer.ws.xml.versResp.ECEsitoGeneraleType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegType;
import it.eng.parer.ws.xml.versResp.ECEsitoXSDAggAllType;
import it.eng.parer.ws.xml.versResp.EsitoVersAggAllegati;
import it.eng.spagoLite.security.User;

/**
 *
 * @author Fioravanti_F
 */
/**
 * Session Bean implementation class AggiuntaAllSync
 */
@Stateless(mappedName = "AggiuntaAllSync")
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class AggiuntaAllSync extends VersamentoSyncBase {

    //
    private static final Logger log = LoggerFactory.getLogger(AggiuntaAllSync.class);
    //
    @EJB
    VersamentoExtAggAllPrsr tmpPrsr;
    @EJB
    VerificaFirmeHashAggAll veriFirme;

    public void init(RispostaWSAggAll rispostaWs, AvanzamentoWs avanzamento,
	    VersamentoExtAggAll versamento, EsitoVersAggAllegati myEsito) {
	log.debug("sono nel metodo init");
	rispostaWs.setSeverity(SeverityEnum.OK);
	rispostaWs.setErrorCode("");
	rispostaWs.setErrorMessage("");

	RispostaControlli rs = super.caricaXmlDefault(versamento);
	// if positive ... load ws versions
	if (rs.isrBoolean()) {
	    rs = super.loadWsVersions(versamento);
	}

	// prepara la classe esito e la aggancia alla rispostaWS
	myEsito.setEsitoGenerale(new ECEsitoGeneraleType());
	rispostaWs.setIstanzaEsito(myEsito);

	// aggancia alla rispostaWS
	rispostaWs.setAvanzamento(avanzamento);

	myEsito.setDataVersamento(XmlDateUtility.dateToXMLGregorianCalendar(new Date()));

	//
	if (!rs.isrBoolean()) {
	    rispostaWs.setSeverity(SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsError(rs.getCodErr(), rs.getDsErr());
	} else {
	    myEsito.getEsitoGenerale().setCodiceEsito(ECEsitoExtType.POSITIVO);
	    myEsito.getEsitoGenerale().setCodiceErrore("");
	    myEsito.getEsitoGenerale().setMessaggioErrore("");

	    // ultima versione rilasciata
	    myEsito.setVersione(
		    versamento.getDescrizione().getVersione(versamento.getWsVersions()));

	    myEsito.setEsitoXSD(new ECEsitoXSDAggAllType());
	    myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.POSITIVO);
	    myEsito.getEsitoXSD().setControlloStrutturaXML(ECEsitoPosNegType.POSITIVO.name());
	    myEsito.getEsitoXSD().setUnivocitaIDComponenti(ECEsitoPosNegType.POSITIVO.name());

	    myEsito.setEsitoChiamataWS(new ECEsitoChiamataWSType());
	    myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.POSITIVO);
	    myEsito.getEsitoChiamataWS().setFileAttesiRicevuti(ECEsitoPosNegType.POSITIVO);
	    myEsito.getEsitoChiamataWS().setVersioneWSCorretta(ECEsitoPosNegType.POSITIVO);
	}
    }

    public void verificaVersione(String versione, RispostaWSAggAll rispostaWs,
	    VersamentoExtAggAll versamento) {
	log.debug("sono nel metodo verificaVersione");
	EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();
	RispostaControlli tmpRispostaControlli;
	versamento.setVersioneWsChiamata(versione);
	myEsito.setVersioneXMLChiamata(versione);

	tmpRispostaControlli = myControlliWs.checkVersione(versione,
		versamento.getDescrizione().getNomeWs(), versamento.getWsVersions(),
		TipiWSPerControlli.VERSAMENTO_RECUPERO);
	if (!tmpRispostaControlli.isrBoolean()) {
	    rispostaWs.setSeverity(SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(),
		    tmpRispostaControlli.getDsErr());
	    myEsito.getEsitoChiamataWS().setVersioneWSCorretta(ECEsitoPosNegType.NEGATIVO);
	} else {
	    versamento.checkVersioneRequest(versione);
	    myEsito.setVersione(versamento.getVersioneCalc());
	}
    }

    public void verificaCredenziali(String loginName, String password, String indirizzoIp,
	    RispostaWSAggAll rispostaWs, VersamentoExtAggAll versamento) {
	EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();
	RispostaControlli tmpRispostaControlli = null;

	tmpRispostaControlli = myControlliWs.checkCredenziali(loginName, password, indirizzoIp,
		TipiWSPerControlli.VERSAMENTO_RECUPERO);
	if (!tmpRispostaControlli.isrBoolean()) {
	    rispostaWs.setSeverity(SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(),
		    tmpRispostaControlli.getDsErr());
	    myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.NEGATIVO);
	}

	versamento.setLoginName(loginName);
	versamento.setUtente((User) tmpRispostaControlli.getrObject());
    }

    public void parseXML(SyncFakeSessn sessione, RispostaWSAggAll rispostaWs,
	    VersamentoExtAggAll versamento) {
	EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();
	AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();
	if (versamento.getUtente() == null) {
	    rispostaWs.setSeverity(SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
		    "Errore: l'utente non è autenticato.");
	    return;
	}

	try {
	    tmpPrsr.parseXML(sessione, versamento, rispostaWs);
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
	    log.error("Eccezione nella fase di parsing dell'XML del EJB ", e);
	}

	if (rispostaWs.getSeverity() == SeverityEnum.ERROR) {
	    if (rispostaWs.isErroreElementoDoppio()) {
		RispostaControlli tmpRispostaControlli;
		tmpRispostaControlli = myRapportoVersBuilder.produciEsitoVecchioRapportoVers(
			versamento, rispostaWs.getIdElementoDoppio(),
			rispostaWs.getUrnPartDocumentoDoppio(), myEsito);
		if (tmpRispostaControlli.getCodErr() != null) {
		    rispostaWs.setSeverity(SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(tmpRispostaControlli.getCodErr(),
			    tmpRispostaControlli.getDsErr());
		    logger.error(
			    "Eccezione nella fase di produzione Rapporto di versamento Documento doppio {}",
			    tmpRispostaControlli.getDsErr());
		}
	    } else {
		myEsito.setXMLVersamento(sessione.getDatiIndiceSipXml());
	    }
	}
    }

    public void addFile(FileBinario fileIn, RispostaWSAggAll rispostaWs,
	    VersamentoExtAggAll versamento) {
	EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();

	if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
	    try {
		// verifica se il file doveva essere inserito, cercandolo nella lista dei
		// componenti
		// se c'è lo imposta nel componente trovato
		// setta a true il flag di dati letti
		// imposta il valore della dimensione del file nel frammento di xml di risposta
		// già pronto
		ComponenteVers tmpComponenteVers;
		tmpComponenteVers = versamento.getStrutturaComponenti().getFileAttesi()
			.get(fileIn.getId());
		if (tmpComponenteVers != null) {
		    // verifica se il componente è di tipo FILE... se non lo è dai errore
		    if (tmpComponenteVers.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
			tmpComponenteVers.setRifFileBinario(fileIn);
			tmpComponenteVers.setDatiLetti(true);
			// imposta la dimensione file nell'XML di risposta associato al componente
			// trovato
			// NOTA BENE: solo uno tra componente e sottocomponente è valorizzato,
			// perciò il doppio IF che segue comporta la scrittura della dimensione su
			// un
			// solo elemento
			if (tmpComponenteVers.getRifComponenteResp() != null) {
			    tmpComponenteVers.getRifComponenteResp()
				    .setDimensioneFile(BigInteger.valueOf(fileIn.getDimensione()));
			}
			if (tmpComponenteVers.getRifSottoComponenteResp() != null) {
			    tmpComponenteVers.getRifSottoComponenteResp()
				    .setDimensioneFile(BigInteger.valueOf(fileIn.getDimensione()));
			}
			// incrementa la dimensione in byte del totale dei file da memorizzare
			versamento.getStrutturaComponenti().setTotalSizeInBytes(
				versamento.getStrutturaComponenti().getTotalSizeInBytes()
					+ fileIn.getDimensione());
		    } else {
			rispostaWs.setSeverity(SeverityEnum.ERROR);
			rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK,
				"tentativo di caricare un file per un componente che non lo prevede");
			myEsito.getEsitoChiamataWS()
				.setFileAttesiRicevuti(ECEsitoPosNegType.NEGATIVO);
		    }
		} else {
		    rispostaWs.setSeverity(SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK,
			    "un file caricato non corrisponde a nessun componente dichiarato");
		    myEsito.getEsitoChiamataWS().setFileAttesiRicevuti(ECEsitoPosNegType.NEGATIVO);
		}
	    } catch (Exception e) {
		rispostaWs.setSeverity(SeverityEnum.ERROR);
		rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			"Errore nella fase di aggiunta di un nuovo file " + e.getMessage());
		log.error("Errore nella fase di aggiunta di un nuovo file ", e);
	    }

	    if (rispostaWs.getSeverity() == SeverityEnum.ERROR) {
		myEsito.setXMLVersamento(versamento.getDatiXml());
	    }
	}
    }

    public void verificaCoerenzaComponenti(RispostaWSAggAll rispostaWs,
	    VersamentoExtAggAll versamento) {
	EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();

	if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
	    // verifica che tutti i componenti dichiarati di tipo FILE abbiano avuto un file
	    for (ComponenteVers componente : versamento.getStrutturaComponenti().getFileAttesi()
		    .values()) {
		if (!componente.isDatiLetti()
			&& componente.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
		    rispostaWs.setSeverity(SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK,
			    "non tutti i componenti di tipo FILE hanno un file associato");
		    myEsito.getEsitoChiamataWS().setFileAttesiRicevuti(ECEsitoPosNegType.NEGATIVO);
		}
	    }
	}

	if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
	    // verifica che il payload del versamento possa essere contenuto nel filesystem
	    // di destinazione
	    if (versamento.getStrutturaComponenti()
		    .getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE) {
		try {
		    FileServUtils fileServUtils = new FileServUtils();
		    if (!fileServUtils.controllaSpazioLibero(
			    versamento.getStrutturaComponenti().getTpiRootTpiDaSacer(),
			    versamento.getStrutturaComponenti().getTotalSizeInBytes())) {
			rispostaWs.setSeverity(SeverityEnum.ERROR);
			rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_003_001);
		    }
		} catch (Exception e) {
		    rispostaWs.setSeverity(SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			    "Errore nella fase di controllo dello spazio libero sul file system TPI "
				    + e.getMessage());
		    log.error(
			    "Eccezione nella fase di controllo dello spazio libero sul file system TPI ",
			    e);
		}
	    }
	}

	if (rispostaWs.getSeverity() == SeverityEnum.ERROR) {
	    myEsito.setXMLVersamento(versamento.getDatiXml());
	}
    }

    public void controllaFirmeAndHash(RispostaWSAggAll rispostaWs, VersamentoExtAggAll versamento) {
	EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();

	if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
	    try {
		veriFirme.controlloFirmeMarcheHash(versamento, rispostaWs);
	    } catch (Exception e) {
		rispostaWs.setSeverity(SeverityEnum.ERROR);
		rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			"Errore nella fase di controllo firme/marche " + e.getMessage());
		log.error("Eccezione nella fase di controllo firme/marche ", e);
	    }
	}

	if (rispostaWs.getSeverity() == SeverityEnum.ERROR) {
	    myEsito.setXMLVersamento(versamento.getDatiXml());
	}
    }

    public void salvaTutto(SyncFakeSessn sessione, RispostaWSAggAll rispostaWs,
	    VersamentoExtAggAll versamento) {
	logger.debug("sono nel metodo salvaTutto");
	RispostaControlli tmpRispostaControlli;
	EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();
	AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();
	boolean prosegui = true;

	// patch di dubbio gusto per includere la riga di errore eventualmente
	// individuata nei controlli precedenti
	// nella lista di messaggi
	if (myEsito.getEsitoGenerale().getCodiceEsito() == ECEsitoExtType.NEGATIVO
		&& !versamento.isTrovatiErrori()) {
	    versamento.aggiungErroreFatale(myEsito.getEsitoGenerale().getCodiceErrore(),
		    myEsito.getEsitoGenerale().getMessaggioErrore());
	}
	//
	myEsito.setErroriUlteriori(versamento.produciEsitoErroriSec());
	myEsito.setWarningUlteriori(versamento.produciEsitoWarningSec());

	if (prosegui && !versamento.isSimulaScrittura()) {
	    this.beginTrans(rispostaWs);

	    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
		tmpAvanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.SalvataggioDatiVersati)
			.setFase("inizio").logAvanzamento();
		// salva tutto!!
		if (!mySalvataggioSync.salvaDatiVersamento(versamento, rispostaWs)) {
		    this.rollback(rispostaWs);
		    this.beginTrans(rispostaWs);
		    versamento.aggiungErroreFatale(myEsito.getEsitoGenerale().getCodiceErrore(),
			    myEsito.getEsitoGenerale().getMessaggioErrore());
		    // rigenera la lista errori e warning secondari - forse cambiata in caso di
		    // errore di persistenza
		    myEsito.setErroriUlteriori(versamento.produciEsitoErroriSec());
		    myEsito.setWarningUlteriori(versamento.produciEsitoWarningSec());
		}
		tmpAvanzamentoWs.setFase("fine").logAvanzamento();
	    }

	    // marca il timestamp della chiusura sessione di versamento
	    ZonedDateTime tmChiusura = ZonedDateTime.now();
	    sessione.setTmChiusura(tmChiusura);

	    // MAC #37372 - Data creazione versamento valorizzata con dt_chiusura sessione di
	    // versamento
	    if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
		if (!mySalvataggioSync.aggiornaDataCreazioneAggDoc(
			versamento.getStrutturaComponenti(), tmChiusura, rispostaWs)) {
		    this.rollback(rispostaWs);
		    this.beginTrans(rispostaWs);
		    versamento.aggiungErroreFatale(myEsito.getEsitoGenerale().getCodiceErrore(),
			    myEsito.getEsitoGenerale().getMessaggioErrore());
		    // rigenera la lista errori e warning secondari - forse cambiata in caso di
		    // errore di persistenza
		    myEsito.setErroriUlteriori(versamento.produciEsitoErroriSec());
		    myEsito.setWarningUlteriori(versamento.produciEsitoWarningSec());
		}
	    }

	    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
		tmpRispostaControlli = myRapportoVersBuilder
			.produciEsitoNuovoRapportoVers(versamento, sessione, myEsito);
		if (tmpRispostaControlli.getCodErr() != null) {
		    rispostaWs.setSeverity(SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
			    tmpRispostaControlli.getDsErr());
		    logger.error("Eccezione nella fase di produzione Rapporto di versamento {}",
			    tmpRispostaControlli.getDsErr());
		    this.rollback(rispostaWs);
		    this.beginTrans(rispostaWs);
		    versamento.aggiungErroreFatale(myEsito.getEsitoGenerale().getCodiceErrore(),
			    myEsito.getEsitoGenerale().getMessaggioErrore());
		    // rigenera la lista errori e warning secondari - forse cambiata in caso di
		    // errore
		    myEsito.setErroriUlteriori(versamento.produciEsitoErroriSec());
		    myEsito.setWarningUlteriori(versamento.produciEsitoWarningSec());
		}
	    }

	    if (prosegui && sessione.isSalvaSessione()
		    && rispostaWs.getErrorType() != ErrorTypeEnum.DB_FATAL) {
		tmpAvanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.SalvataggioSessioneWS)
			.setFase("inizio").logAvanzamento();
		tmpRispostaControlli = myLogSessioneSync.salvaSessioneVersamento(versamento,
			rispostaWs, sessione);
		if (tmpRispostaControlli.getCodErr() != null) {
		    // nota l'errore critico di persistenza viene contrassegnato con la lettera P
		    // per dare la possibilità all'eventuale chiamante di ripetere il tentativo
		    // quando possibile (non è infatti un errore "definitivo" dato dall'input, ma
		    // bensì
		    // un errore interno provocato da problemi al database)
		    rispostaWs.setSeverity(SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
			    tmpRispostaControlli.getDsErr());
		    log.error("Eccezione nella fase di Salvataggio dei dati di sessione {}",
			    tmpRispostaControlli.getDsErr());
		    this.rollback(rispostaWs);
		    prosegui = false;
		}
		tmpAvanzamentoWs.setFase("fine").logAvanzamento();
	    }

	    // MEV#27048
	    if (prosegui && rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
		tmpAvanzamentoWs.setCheckPoint(AvanzamentoWs.CheckPoints.InvioMessaggioCodaJMS)
			.setFase("inizio").logAvanzamento();
		if (CostantiDB.TipiStatoElementoVersato.IN_ATTESA_SCHED.name()
			.equals(versamento.getStrutturaComponenti().getDocumentiAttesi().get(0)
				.getTiStatoDocDaElab())) {
		    PayLoad pl = new PayLoad();
		    pl.setTipoEntitaSacer("DOC");
		    pl.setStato(CostantiDB.TipiStatoElementoVersato.IN_ATTESA_SCHED.name());
		    pl.setId(versamento.getStrutturaComponenti().getDocumentiAttesi().get(0)
			    .getIdRecDocumentoDB());
		    pl.setIdStrut(versamento.getStrutturaComponenti().getIdStruttura());
		    pl.setAaKeyUnitaDoc(
			    versamento.getVersamento().getIntestazione().getChiave().getAnno());
		    pl.setDtCreazione(DateUtilsConverter
			    .convert(versamento.getStrutturaComponenti().getDataVersamento())
			    .getTime());

		    tmpRispostaControlli = jmsProducerUtilEjb.manageMessageGroupingInFormatoJson(
			    connectionFactory, queue, pl, "CodaElenchiDaElab",
			    String.valueOf(pl.getIdStrut()));
		    if (tmpRispostaControlli.getCodErr() != null) {
			rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
			rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
				tmpRispostaControlli.getDsErr());
			logger.error("Eccezione nella fase di Invio messaggio alla coda JMS {}",
				tmpRispostaControlli.getDsErr());
			this.rollback(rispostaWs);
			prosegui = false;
		    }
		    tmpAvanzamentoWs.setFase("fine").logAvanzamento();
		}
	    }
	    // end MEV#27048

	    if (prosegui && rispostaWs.getErrorType() != ErrorTypeEnum.DB_FATAL) {
		this.commit(rispostaWs);
	    }
	}
    }

}
