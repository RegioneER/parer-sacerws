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
import java.util.HashMap;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.util.DateUtilsConverter;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.ControlliWS;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.utils.PayLoad;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.utils.ejb.JmsProducerUtilEjb;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.BackendStorage;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.ObjectStorageResource;
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.versamento.ejb.prs.VersamentoExtPrsr;
import it.eng.parer.ws.versamentoTpi.utils.FileServUtils;
import it.eng.parer.ws.xml.versResp.ECEsitoChiamataWSType;
import it.eng.parer.ws.xml.versResp.ECEsitoExtType;
import it.eng.parer.ws.xml.versResp.ECEsitoGeneraleType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegType;
import it.eng.parer.ws.xml.versResp.ECEsitoXSDType;
import it.eng.parer.ws.xml.versResp.ECStatoConsType;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;
import it.eng.spagoLite.security.User;

/**
 *
 * @author fioravanti_f
 */
public abstract class VersamentoSyncBase {

    //
    protected static final Logger logger = LoggerFactory.getLogger(VersamentoSyncBase.class);
    //
    // MEV#27048
    @Resource(mappedName = "jms/ProducerConnectionFactory")
    protected ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/queue/ElenchiDaElabQueue")
    protected Queue queue;
    // end MEV#27048
    //
    @Resource
    protected UserTransaction utx;
    @EJB
    protected SalvataggioSync mySalvataggioSync;
    @EJB
    protected LogSessioneSync myLogSessioneSync;
    @EJB
    protected ControlliWS myControlliWs;
    @EJB
    protected RapportoVersBuilder myRapportoVersBuilder;
    @EJB
    protected ControlliSubStrut myControlliSubStrut;
    @EJB
    protected ControlliSemantici myControlliSemantici;

    @EJB
    protected ObjectStorageService objectStorageService;

    @EJB
    protected VerificaFirmeHash veriFirme;

    @EJB
    protected VersamentoExtPrsr tmpPrsr;

    // MEV#27048
    @EJB
    protected JmsProducerUtilEjb jmsProducerUtilEjb;
    // end MEV#27048

    public void init(RispostaWS rispostaWs, AvanzamentoWs avanzamento, VersamentoExt versamento,
	    EsitoVersamento myEsito) {
	logger.debug("sono nel metodo init");
	rispostaWs.setSeverity(IRispostaWS.SeverityEnum.OK);
	rispostaWs.setErrorCode("");
	rispostaWs.setErrorMessage("");

	RispostaControlli rs = this.caricaXmlDefault(versamento);
	// if positive ... load ws versions
	if (rs.isrBoolean()) {
	    rs = this.loadWsVersions(versamento);
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
	    //
	    myEsito.setVersione(
		    versamento.getDescrizione().getVersione(versamento.getWsVersions()));
	    //
	    myEsito.setEsitoXSD(new ECEsitoXSDType());
	    myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.POSITIVO);
	    myEsito.getEsitoXSD().setControlloStrutturaXML(ECEsitoPosNegType.POSITIVO.name());
	    myEsito.getEsitoXSD().setUnivocitaIDComponenti(ECEsitoPosNegType.POSITIVO.name());
	    myEsito.getEsitoXSD().setUnivocitaIDDocumenti(ECEsitoPosNegType.POSITIVO.name());
	    myEsito.getEsitoXSD().setCorrispondenzaAllegatiDichiarati(ECEsitoPosNegType.POSITIVO);
	    myEsito.getEsitoXSD().setCorrispondenzaAnnessiDichiarati(ECEsitoPosNegType.POSITIVO);
	    myEsito.getEsitoXSD()
		    .setCorrispondenzaAnnotazioniDichiarate(ECEsitoPosNegType.POSITIVO);
	    myEsito.setEsitoChiamataWS(new ECEsitoChiamataWSType());
	    myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.POSITIVO);
	    myEsito.getEsitoChiamataWS().setFileAttesiRicevuti(ECEsitoPosNegType.POSITIVO);
	    myEsito.getEsitoChiamataWS().setVersioneWSCorretta(ECEsitoPosNegType.POSITIVO);
	}
    }

    public void verificaVersione(String versione, RispostaWS rispostaWs, VersamentoExt versamento) {
	logger.debug("sono nel metodo verificaVersione");
	EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
	RispostaControlli tmpRispostaControlli;
	versamento.setVersioneWsChiamata(versione);
	myEsito.setVersioneXMLChiamata(versione);

	tmpRispostaControlli = myControlliWs.checkVersione(versione,
		versamento.getDescrizione().getNomeWs(), versamento.getWsVersions(),
		TipiWSPerControlli.VERSAMENTO_RECUPERO);
	if (!tmpRispostaControlli.isrBoolean()) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(),
		    tmpRispostaControlli.getDsErr());
	    myEsito.getEsitoChiamataWS().setVersioneWSCorretta(ECEsitoPosNegType.NEGATIVO);
	} else {
	    versamento.checkVersioneRequest(versione);
	    myEsito.setVersione(versamento.getVersioneCalc());
	}
    }

    public void verificaCredenziali(String loginName, String password, String indirizzoIp,
	    RispostaWS rispostaWs, VersamentoExt versamento) {
	logger.debug("sono nel metodo verificaCredenziali");
	EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();

	RispostaControlli tmpRispostaControlli = myControlliWs.checkCredenziali(loginName, password,
		indirizzoIp, TipiWSPerControlli.VERSAMENTO_RECUPERO);
	if (!tmpRispostaControlli.isrBoolean()) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(),
		    tmpRispostaControlli.getDsErr());
	    myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.NEGATIVO);
	}

	versamento.setLoginName(loginName);
	versamento.setUtente((User) tmpRispostaControlli.getrObject());
    }

    public void parseXML(SyncFakeSessn sessione, RispostaWS rispostaWs, VersamentoExt versamento) {
	logger.debug("sono nel metodo parseXML");
	EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
	AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();

	if (versamento.getUtente() == null) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
		    "Errore: l'utente non è autenticato.");
	    return;
	}

	try {
	    tmpPrsr.parseXML(sessione, rispostaWs, versamento);
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

	// calcola la substruttura del versamento se finora la verifica sta andando bene
	if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
	    RispostaControlli tmpRispostaControlli;
	    tmpRispostaControlli = myControlliSubStrut
		    .calcolaSubStrut(versamento.getStrutturaComponenti());
	    if (tmpRispostaControlli.getCodErr() != null) {
		rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(),
			tmpRispostaControlli.getDsErr());
	    }
	}

	if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
	    if (rispostaWs.isErroreElementoDoppio()) {
		RispostaControlli tmpRispostaControlli;
		tmpRispostaControlli = myRapportoVersBuilder.produciEsitoVecchioRapportoVers(
			versamento, rispostaWs.getIdElementoDoppio(), myEsito);
		if (tmpRispostaControlli.getCodErr() != null) {
		    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(),
			    tmpRispostaControlli.getDsErr());
		    logger.error(
			    "Eccezione nella fase di produzione Rapporto di versamento UD Doppia {}",
			    tmpRispostaControlli.getDsErr());
		}
	    } else {
		myEsito.setXMLVersamento(sessione.getDatiIndiceSipXml());
	    }
	}
    }

    public void addFile(FileBinario fileIn, RispostaWS rispostaWs, VersamentoExt versamento) {
	EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
	if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
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
			rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
			rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK,
				"tentativo di caricare un file per un componente che non lo prevede");
			myEsito.getEsitoChiamataWS()
				.setFileAttesiRicevuti(ECEsitoPosNegType.NEGATIVO);
		    }
		} else {
		    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK,
			    "un file caricato non corrisponde a nessun componente dichiarato");
		    myEsito.getEsitoChiamataWS().setFileAttesiRicevuti(ECEsitoPosNegType.NEGATIVO);
		}
	    } catch (Exception e) {
		rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			"Errore nella fase di aggiunta di un nuovo file " + e.getMessage());
		logger.error("Errore nella fase di aggiunta di un nuovo file ", e);
	    }
	    if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
		myEsito.setXMLVersamento(versamento.getDatiXml());
	    }
	}
    }

    public void verificaCoerenzaComponenti(RispostaWS rispostaWs, VersamentoExt versamento) {
	EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
	if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
	    // verifica che tutti i componenti dichiarati di tipo FILE abbiano avuto un file
	    for (ComponenteVers componente : versamento.getStrutturaComponenti().getFileAttesi()
		    .values()) {
		if (!componente.isDatiLetti()
			&& componente.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
		    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.WS_CHECK,
			    "non tutti i componenti di tipo FILE hanno un file associato");
		    myEsito.getEsitoChiamataWS().setFileAttesiRicevuti(ECEsitoPosNegType.NEGATIVO);
		}
	    }
	}
	if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
	    if (versamento.getStrutturaComponenti()
		    .getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE
		    && versamento.getStrutturaComponenti().isTpiAbilitato()) {
		try {
		    FileServUtils fileServUtils = new FileServUtils();
		    if (!fileServUtils.controllaSpazioLibero(
			    versamento.getStrutturaComponenti().getTpiRootTpiDaSacer(),
			    versamento.getStrutturaComponenti().getTotalSizeInBytes())) {
			rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
			rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_003_001);
		    }
		} catch (Exception e) {
		    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
			    "Errore nella fase di controllo dello spazio libero sul file system TPI "
				    + e.getMessage());
		    logger.error(
			    "Eccezione nella fase di controllo dello spazio libero sul file system TPI ",
			    e);
		}
	    }
	}
	if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
	    myEsito.setXMLVersamento(versamento.getDatiXml());
	}
    }

    /**
     * Effettua l'upload dei file sull'object storage nel caso il backend configurato sia di tipo OS
     * - Versamento sync/Aggiunta documenti. Nel caso in cui non risulti disponibile l'object
     * storage verrà emesso un errore bloccante non gestito.
     *
     * @param sessioneFinta Contenitore dei file
     */
    public void uploadComponentiStaging(SyncFakeSessn sessioneFinta) {

	BackendStorage backendStaging = objectStorageService.lookupBackendVrsStaging();

	if (backendStaging.isObjectStorage()) {
	    for (FileBinario rifFileBinario : sessioneFinta.getFileBinari()) {
		logger.debug("Sto per salvare {} su OS", rifFileBinario.getFileName());
		ObjectStorageResource componenteStaging = objectStorageService
			.createTmpResourceInStaging(backendStaging.getBackendName(),
				rifFileBinario.getFileSuDisco());
		rifFileBinario.setObjectStorageResource(componenteStaging);
	    }
	}

    }

    public void controllaFirmeAndHash(RispostaWS rispostaWs, VersamentoExt versamento) {
	logger.debug("sono nel metodo controllaFirme");
	EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();

	if (versamento.getUtente() == null) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
		    "Errore: l'utente non è autenticato.");
	    return;
	}

	if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
	    try {
		veriFirme.controlloFirmeMarcheHash(versamento, rispostaWs);
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
		logger.error("Eccezione nella fase di controllo firme/marche ", e);
	    }
	}

	if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
	    myEsito.setXMLVersamento(versamento.getDatiXml());
	} else // nel caso il salvataggio sia su filesystem/Tivoli,
	// lo stato del documento passerà ad IN_ATTESA_SCHED
	// solo dopo la effettiva memorizzazione in Tivoli
	{
	    if (versamento.getStrutturaComponenti()
		    .getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE
		    && versamento.getStrutturaComponenti().isTpiAbilitato()) {
		myEsito.getUnitaDocumentaria()
			.setStatoConservazione(ECStatoConsType.IN_ATTESA_MEMORIZZAZIONE);
	    } else {
		myEsito.getUnitaDocumentaria()
			.setStatoConservazione(ECStatoConsType.IN_ATTESA_SCHED);
	    }
	}
    }

    public void salvaTutto(SyncFakeSessn sessione, RispostaWS rispostaWs,
	    VersamentoExt versamento) {
	logger.debug("sono nel metodo salvaTutto");
	RispostaControlli tmpRispostaControlli;
	EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
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

	    if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
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
		if (!mySalvataggioSync.aggiornaDataCreazione(versamento.getStrutturaComponenti(),
			tmChiusura, rispostaWs)) {
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

	    if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR) {
		tmpRispostaControlli = myRapportoVersBuilder
			.produciEsitoNuovoRapportoVers(versamento, sessione, myEsito);
		if (tmpRispostaControlli.getCodErr() != null) {
		    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
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

	    // Salvo la sessione di versamento (andata a buon fine o KO) basta che non sia un errore
	    // DB_FATAL
	    if (prosegui && sessione.isSalvaSessione()
		    && rispostaWs.getErrorType() != IRispostaWS.ErrorTypeEnum.DB_FATAL) {
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
		    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
		    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
			    tmpRispostaControlli.getDsErr());
		    logger.error("Eccezione nella fase di Salvataggio dei dati di sessione {}",
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
			.equals(versamento.getStrutturaComponenti().getTiStatoUdDaElab())) {
		    PayLoad pl = new PayLoad();
		    pl.setTipoEntitaSacer("UNI_DOC");
		    pl.setStato(CostantiDB.TipiStatoElementoVersato.IN_ATTESA_SCHED.name());
		    pl.setId(versamento.getStrutturaComponenti().getIdUnitaDoc());
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

	    if (prosegui && rispostaWs.getErrorType() != IRispostaWS.ErrorTypeEnum.DB_FATAL) {
		this.commit(rispostaWs);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    protected RispostaControlli caricaXmlDefault(AbsVersamentoExt ave) {
	RispostaControlli rs = myControlliSemantici
		.caricaDefaultDaDB(ParametroApplDB.TipoParametroAppl.VERSAMENTO_DEFAULT);
	// if positive ...
	if (rs.isrBoolean()) {
	    ave.setXmlDefaults((HashMap<String, String>) rs.getrObject());
	}
	return rs;
    }

    @SuppressWarnings("unchecked")
    protected RispostaControlli loadWsVersions(AbsVersamentoExt versamento) {
	RispostaControlli rs = myControlliWs.loadWsVersions(versamento.getDescrizione());
	// if positive ...
	if (rs.isrBoolean()) {
	    versamento.setWsVersions((HashMap<String, String>) rs.getrObject());
	}
	return rs;
    }

    protected void beginTrans(IRispostaWS rispostaWs) {
	try {
	    utx.begin();
	} catch (NotSupportedException | SystemException e) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
	    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
		    "Errore nella fase di apertura transazione db del EJB "
			    + ExceptionUtils.getRootCauseMessage(e));
	    logger.error("Eccezione nell'init ejb versamento ", e);
	}
    }

    protected void commit(IRispostaWS rispostaWs) {
	try {
	    utx.commit();
	} catch (SecurityException | IllegalStateException | RollbackException
		| HeuristicMixedException | HeuristicRollbackException | SystemException e) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
	    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
		    "Errore nella fase di commit transazione db del EJB "
			    + ExceptionUtils.getRootCauseMessage(e));
	    logger.error("Eccezione nel commit ejb versamento ", e);
	}
    }

    protected void rollback(IRispostaWS rispostaWs) {
	try {
	    utx.rollback();
	} catch (IllegalStateException | SecurityException | SystemException e) {
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setErrorType(IRispostaWS.ErrorTypeEnum.DB_FATAL);
	    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
		    "Errore nella fase di rollack transazione db del EJB "
			    + ExceptionUtils.getRootCauseMessage(e));
	    logger.error("Eccezione nel rollback ejb versamento ", e);
	}
    }

}
