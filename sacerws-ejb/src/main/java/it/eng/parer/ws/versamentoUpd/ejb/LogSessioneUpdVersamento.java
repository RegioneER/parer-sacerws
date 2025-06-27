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

package it.eng.parer.ws.versamentoUpd.ejb;

import static it.eng.parer.util.DateUtilsConverter.convert;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.AroUpdUnitaDoc;
import it.eng.parer.entity.VrsSesUpdUnitaDocErr;
import it.eng.parer.entity.VrsSesUpdUnitaDocKo;
import it.eng.parer.entity.VrsUpdUnitaDocKo;
import it.eng.parer.util.Constants;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.BackendStorage;
import it.eng.parer.ws.versamento.dto.ObjectStorageResource;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.ejb.ObjectStorageService;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.ejb.help.LogSessioneUpdVersamentoHelper;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import java.math.BigDecimal;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "LogSessioneUpdVersamento")
@LocalBean
public class LogSessioneUpdVersamento {

    private static final Logger log = LoggerFactory.getLogger(LogSessioneUpdVersamento.class);
    //

    @EJB
    private LogSessioneUpdVersamentoHelper updLogSessioneHelper;

    // MEV#29276
    @EJB
    ObjectStorageService objectStorageService;
    // end MEV#29276

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @Resource
    private EJBContext context;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean registraSessioneErrata(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento,
	    SyncFakeSessn sessione) {
	// nota l'errore critico di persistenza viene contrassegnato con la lettera P
	// per dare la possibilità all'eventuale chiamante di ripetere il tentativo
	// quando possibile (non è infatti un errore "definitivo" dato dall'input, ma
	// bensì
	// un errore interno provocato da problemi al database)
	RispostaControlli tmpRispostaControlli = null;
	VrsSesUpdUnitaDocErr tmpSesUpdUnitaDocErr = null;
	// MEV#29276
	BackendStorage backendMetadata = null;
	Map<String, String> sipBlob = new HashMap<>();
	// end MEV#29276
	try {

	    // MEV#29276
	    backendMetadata = objectStorageService.lookupBackendVrsSessioniErrKoAggMd();
	    // end MEV#29276

	    // salvo sessione errata:
	    tmpRispostaControlli = updLogSessioneHelper.scriviUpdUnitaDocErr(rispostaWs, versamento,
		    sessione);
	    if (!tmpRispostaControlli.isrBoolean()) {
		// probabilmente è inutile visto che se arrivo qui è per una runtimeexception
		// che forza il rollback in ogni caso, ma meglio andare sul sicuro, dal momento
		// che non posso escludere di terminare male un salvataggio anche senza una
		// eccezione (magari in altri metodi invocati con lo stesso pattern)
		context.setRollbackOnly();
		// errore di persistenza da aggiungere tra i controlli ?!
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }

	    // salvo gli xml di request & response
	    tmpSesUpdUnitaDocErr = (VrsSesUpdUnitaDocErr) tmpRispostaControlli.getrObject();
	    tmpRispostaControlli = updLogSessioneHelper.scriviXmlSesUpdUnitaDocErr(rispostaWs,
		    versamento, sessione, tmpSesUpdUnitaDocErr, backendMetadata, sipBlob);
	    if (!tmpRispostaControlli.isrBoolean()) {
		// probabilmente è inutile visto che se arrivo qui è per una runtimeexception
		// che forza il rollback in ogni caso, ma meglio andare sul sicuro, dal momento
		// che non posso escludere di terminare male un salvataggio anche senza una
		// eccezione (magari in altri metodi invocati con lo stesso pattern)
		context.setRollbackOnly();
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }

	    // salvo i warning e gli errori ulteriori
	    tmpRispostaControlli = updLogSessioneHelper.scriviCtrlErrWarnUpdUnitaDocErr(rispostaWs,
		    versamento, sessione, tmpSesUpdUnitaDocErr);
	    if (!tmpRispostaControlli.isrBoolean()) {
		context.setRollbackOnly();
		// errore di persistenza da aggiungere tra i controlli ?!
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }
	    //
	    // MEV#29276
	    /*
	     * Se backendMetadata di tipo O.S. si effettua il salvataggio (con link su apposita
	     * entity)
	     */
	    if (backendMetadata.isObjectStorage()) {
		ObjectStorageResource res = objectStorageService.createSipInSessioniErrAggMd(
			backendMetadata.getBackendName(), sipBlob,
			tmpSesUpdUnitaDocErr.getIdSesUpdUnitaDocErr(), getIdStrut(versamento));
		log.debug("Salvati i SIP nel bucket {} con chiave {} ", res.getBucket(),
			res.getKey());
	    }
	    // end MEV#29276
	    entityManager.flush();
	} catch (Exception e) {
	    // l'errore di persistenza viene aggiunto alla pila
	    // di errori esistenti e in seguito serializzato nell'xml
	    // di risposta.
	    context.setRollbackOnly();
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio sessione errata: " + e.getMessage());
	    log.error("Errore interno nella fase di salvataggio sessione errata.", e);
	    return false;
	}
	return true;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean registraSessioneFallita(RispostaWSUpdVers rispostaWs,
	    UpdVersamentoExt versamento, SyncFakeSessn sessione) {
	// nota l'errore critico di persistenza viene contrassegnato con la lettera P
	// per dare la possibilità all'eventuale chiamante di ripetere il tentativo
	// quando possibile (non è infatti un errore "definitivo" dato dall'input, ma bensì
	// un errore interno provocato da problemi al database)
	//
	// gli errori di persistenza in questa fase vengono aggiunti alla pila
	// di errori esistenti e in seguito serializzati nell'xml
	// di risposta. Inoltre tenterò di salvare una sessione errata con questi stessi dati.
	RispostaControlli tmpRispostaControlli = null;
	AroUpdUnitaDoc tmpAroUpdUnitaDoc = null;
	VrsUpdUnitaDocKo tmpUpdUnitaDocKo = null;
	VrsSesUpdUnitaDocKo tmpSesUpdUnitaDocKo = null;
	StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
	// MEV#29276
	BackendStorage backendMetadata = null;
	Map<String, String> sipBlob = new HashMap<>();
	// end MEV#29276
	try {

	    // MEV#29276
	    backendMetadata = objectStorageService.lookupBackendVrsSessioniErrKoAggMd();
	    // end MEV#29276

	    // se esiste LOCK SU UD
	    // dovranno aspettare il rilascio del LOCK che avverrà al termine di tutte le
	    // operazioni di aggiornamento)
	    if (strutturaUpdVers.getIdUd() > 0) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT, 25000);
		entityManager.find(AroUnitaDoc.class, strutturaUpdVers.getIdUd(),
			LockModeType.PESSIMISTIC_WRITE, properties);
	    }

	    // cerco se esiste una precedente registrazione fallita della
	    // stesso aggiornamento UD e se esiste, lo blocco in modo esclusivo: lo devo modificare
	    tmpRispostaControlli = updLogSessioneHelper.cercaAggiornamentoKo(versamento);
	    if (!tmpRispostaControlli.isrBoolean()) {
		context.setRollbackOnly();
		// errore di persistenza da aggiungere tra i controlli ?!
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }
	    if (tmpRispostaControlli.getrObject() != null) {
		tmpUpdUnitaDocKo = (VrsUpdUnitaDocKo) tmpRispostaControlli.getrObject();
	    } else {
		// se non ho aggiornamento buoni o cattivi già creati -> creo aggiornamento fallito
		tmpRispostaControlli = updLogSessioneHelper.scriviVrsUpdUnitaDocKo(rispostaWs,
			versamento, sessione);
		if (!tmpRispostaControlli.isrBoolean()) {
		    context.setRollbackOnly();
		    // errore di persistenza da aggiungere tra i controlli ?!
		    this.impostaErrore(rispostaWs, tmpRispostaControlli);
		    return false;
		}
		tmpUpdUnitaDocKo = (VrsUpdUnitaDocKo) tmpRispostaControlli.getrObject();
	    }

	    /**
	     * verifica se in MON_KEY_TOTAL_UD_KO la chiave di totalizzazione delle sessioni fallite
	     * è definita
	     */
	    tmpRispostaControlli = updLogSessioneHelper.aggiornaMonKeyTotalUdKo(tmpUpdUnitaDocKo,
		    convert(sessione.getTmApertura()));
	    if (!tmpRispostaControlli.isrBoolean()) {
		context.setRollbackOnly();
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }

	    /**
	     * determina il primo aggiornamento avvenuto con successo (ARO_UPD_UNITA_DOC) il cui
	     * timestamp di inizio sessione sia successivo al timestamp di inizio sessione errata
	     */
	    tmpRispostaControlli = updLogSessioneHelper.recuperoUpdUnitaDocOk(versamento, sessione);
	    if (!tmpRispostaControlli.isrBoolean()) {
		context.setRollbackOnly();
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }

	    if (tmpRispostaControlli.getrLong() != -1) {
		tmpAroUpdUnitaDoc = (AroUpdUnitaDoc) tmpRispostaControlli.getrObject();
	    }

	    // salvo sessione fallita
	    tmpRispostaControlli = updLogSessioneHelper.scriviSessioneUpdUnitaDocKo(rispostaWs,
		    versamento, sessione, tmpUpdUnitaDocKo, tmpAroUpdUnitaDoc);
	    if (!tmpRispostaControlli.isrBoolean()) {
		context.setRollbackOnly();
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }
	    tmpSesUpdUnitaDocKo = (VrsSesUpdUnitaDocKo) tmpRispostaControlli.getrObject();
	    // salvo xml di request & response
	    tmpRispostaControlli = updLogSessioneHelper.scriviXmlUpdUnitaDocKo(rispostaWs,
		    versamento, sessione, tmpSesUpdUnitaDocKo, backendMetadata, sipBlob);
	    if (!tmpRispostaControlli.isrBoolean()) {
		context.setRollbackOnly();
		// errore di persistenza da aggiungere tra i controlli ?!
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }

	    // salvo i warning e gli errori ulteriori
	    tmpRispostaControlli = updLogSessioneHelper.scriviCtrlErrWarnUpdUnitaDocKo(rispostaWs,
		    versamento, sessione, tmpSesUpdUnitaDocKo);
	    if (!tmpRispostaControlli.isrBoolean()) {
		context.setRollbackOnly();
		// errore di persistenza da aggiungere tra i controlli ?!
		this.impostaErrore(rispostaWs, tmpRispostaControlli);
		return false;
	    }
	    //
	    // MEV#29276
	    /*
	     * Se backendMetadata di tipo O.S. si effettua il salvataggio (con link su apposita
	     * entity)
	     */
	    if (backendMetadata.isObjectStorage()) {
		ObjectStorageResource res = objectStorageService.createSipInSessioniKoAggMd(
			backendMetadata.getBackendName(), sipBlob,
			tmpSesUpdUnitaDocKo.getIdSesUpdUnitaDocKo(), getIdStrut(versamento));
		log.debug("Salvati i SIP nel bucket {} con chiave {} ", res.getBucket(),
			res.getKey());
	    }
	    // end MEV#29276
	    entityManager.flush();
	} catch (Exception e) {
	    // l'errore di persistenza viene aggiunto alla pila
	    // di errori esistenti e in seguito serializzato nell'xml
	    // di risposta. Inoltre tenterò di salvare una sessione errata con questi stessi dati.
	    context.setRollbackOnly();
	    rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	    rispostaWs.setEsitoWsErrBundle(
		    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_ERRORIDB),
		    MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio sessione fallita: " + e.getMessage());
	    log.error("Errore interno nella fase di salvataggio sessione fallita.", e);
	    return false;
	}
	return true;
    }

    private void impostaErrore(RispostaWSUpdVers rispostaWs,
	    RispostaControlli tmpRispostaControlli) {
	rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
	rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(),
		tmpRispostaControlli.getDsErr());

    }

    private BigDecimal getIdStrut(UpdVersamentoExt versamento) {
	if (versamento.getStrutturaUpdVers() != null
		&& versamento.getStrutturaUpdVers().getIdStruttura() != 0) {
	    return BigDecimal.valueOf(versamento.getStrutturaUpdVers().getIdStruttura());
	}
	return null;
    }

}
