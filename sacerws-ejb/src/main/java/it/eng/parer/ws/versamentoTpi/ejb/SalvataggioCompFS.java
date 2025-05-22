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

package it.eng.parer.ws.versamentoTpi.ejb;

import it.eng.parer.entity.LogLockElab;
import it.eng.parer.entity.VrsDtVers;
import it.eng.parer.entity.VrsPathDtVers;
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsException;
import it.eng.parer.util.Constants;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.WsXAUtil;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xadisk.connector.outbound.XADiskConnection;
import org.xadisk.connector.outbound.XADiskConnectionFactory;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;

import static it.eng.parer.util.DateUtilsConverter.convert;
import static it.eng.parer.util.DateUtilsConverter.format;
import java.time.ZoneId;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "SalvataggioCompFS")
@LocalBean
public class SalvataggioCompFS {

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;
    //
    @Resource(mappedName = "jca/xadiskLocal")
    private XADiskConnectionFactory xadCf;
    //
    private static final Logger log = LoggerFactory.getLogger(SalvataggioCompFS.class);
    //
    private static final String LOCKNAME_CREA_DIRECTORY_DATA_TPI = "CREA_DIRECTORY_DATA_TPI";
    private static final String LOCKNAME_CREA_DIRECTORY_VERS_TPI = "CREA_DIRECTORY_VERS_TPI";
    private static final int LOCK_LOCK_TIMEOUT_IN_SECONDI = 30;
    private static final int ATTESA_RETRY_TEST_IN_MILLISECONDI = 2000;
    //
    public static final int MAX_NUMERO_TENTATIVI_TEST = 5;
    //
    private static final String LOCKTYPE_LOCK_UNICO = "LOCK_UNICO";
    private static final String ARK_STATUS_REGISTRATA = "REGISTRATA";

    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public RispostaControlli verificaDirTPIDataViaDb(StrutturaVersamento versamento,
	    StatoCreaCartelle statoCreaCartelle, int numeroTentativi) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrLong(-1);
	rispostaControlli.setrBoolean(false);

	// verifica se ho salvato sul DB il riferimento alla cartella relativa
	// alla data versamento
	Date tmpTestData = DateUtils.truncate(convert(versamento.getDataVersamento()),
		Calendar.DAY_OF_MONTH);
	// genero il nome della directory corrispondente da usare per il test e la creazione
	statoCreaCartelle.setPathCartellaData(
		MessageFormat.format("{0}/{1}/{2}", versamento.getTpiRootTpiDaSacer(),
			versamento.getTpiRootVers(), versamento.getSubPathDataVers()));
	try {
	    // NOTA BENE: uso una query nativa perché devo leggere il contenuto del database
	    // senza rischiare problemi dovuti alla latenza della cache usata da JPA.
	    // La riga che sto cercando infatti potrebbe essere stata scritta pochissimi istanti
	    // prima da un altro nodo del cluster e questa scrittura potrebbe non essere stata
	    // ancora recepita.
	    BigDecimal idTrovato = null;
	    String queryStr = "select t.ID_DT_VERS " + "from vrs_dt_vers t "
		    + "where t.DT_VERS = ?";

	    /*
	     * nota bene: il codice strano che segue ha senso poiché apparentemente la scrittura
	     * della tabella da parte di un eventuale altro nodo del cluster non viene registrata
	     * immediatamente dagli altri nodi. Questo consente di verificare se il record ricercato
	     * esiste, dando il tempo alla cache di aggiornarsi.
	     */
	    for (int volte = 0; volte < numeroTentativi; volte++) {
		log.debug("verificaDirTPIDataViaDb : cerco riga DT_VERS Data {}", tmpTestData);
		javax.persistence.Query query = entityManager.createNativeQuery(queryStr);
		query.setParameter(1, tmpTestData);
		List<BigDecimal> tmpList = query.getResultList();
		if (!tmpList.isEmpty()) {
		    idTrovato = tmpList.get(0);
		    break;
		} else {
		    log.debug("verificaDirTPIDataViaDb : non ho trovato riga DT_VERS Data {}",
			    tmpTestData);
		    // se non ho trovato il record, aspetto un paio di secondi e poi ci riprovo
		    Thread.sleep(ATTESA_RETRY_TEST_IN_MILLISECONDI);
		}
	    }
	    if (idTrovato != null) {
		statoCreaCartelle.setIdDtVers(idTrovato.longValue());
		statoCreaCartelle.setCreaCartellaData(false);
	    } else {
		statoCreaCartelle.setCreaCartellaData(true);
		statoCreaCartelle.setCreaCartellaVersatore(true);
	    }
	    rispostaControlli.setrLong(1);
	    rispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "SalvataggioCompFS.verificaDirTPIData: " + e.getMessage()));
	    log.error("Si è verificato un errore durante le operazioni su database", e);
	}

	return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public RispostaControlli verificaDirTPIVersatoreViaDb(StrutturaVersamento versamento,
	    StatoCreaCartelle statoCreaCartelle, int numeroTentativi) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrLong(-1);
	rispostaControlli.setrBoolean(false);

	// genero il nome della directory corrispondente da usare per il test e la creazione
	statoCreaCartelle.setPathCartellaVersatore(MessageFormat.format("{0}/{1}/{2}/{3}",
		versamento.getTpiRootTpiDaSacer(), versamento.getTpiRootVers(),
		versamento.getSubPathDataVers(), versamento.getSubPathVersatoreArk()));

	try {
	    // NOTA BENE: uso una query nativa perché devo leggere il contenuto del database
	    // senza rischiare problemi dovuti alla latenza della cache usata da JPA.
	    // La riga che sto cercando infatti potrebbe essere stata scritta pochissimi istanti
	    // prima da un altro nodo del cluster e questa scrittura potrebbe non essere stata
	    // ancora recepita.
	    String queryStr = "select count(*) " + "from VRS_PATH_DT_VERS t "
		    + "where t.ID_DT_VERS = ? " + "and t.DL_PATH = ?";

	    /*
	     * nota bene: il codice strano che segue ha senso poiché apparentemente la scrittura
	     * della tabella da parte di un eventuale altro nodo del cluster non viene registrata
	     * immediatamente dagli altri nodi. Questo consente di verificare se il record ricercato
	     * esiste, dando il tempo alla cache di aggiornarsi.
	     */
	    boolean recordPresente = false;
	    for (int volte = 0; volte < numeroTentativi; volte++) {
		log.debug(
			"verificaDirTPIVersatoreViaDb : cerco riga VRS_PATH_DT_VERS IdDtVers {} subpath {}",
			statoCreaCartelle.getIdDtVers(), versamento.getSubPathVersatoreArk());
		javax.persistence.Query query = entityManager.createNativeQuery(queryStr);
		query.setParameter(1, statoCreaCartelle.getIdDtVers());
		query.setParameter(2, versamento.getSubPathVersatoreArk());
		BigDecimal tmpBigDecimal = (BigDecimal) query.getSingleResult();
		if (tmpBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
		    recordPresente = true;
		    break;
		} else {
		    log.debug(
			    "verificaDirTPIVersatoreViaDb : non ho trovato riga VRS_PATH_DT_VERS IdDtVers {} subpath {}",
			    statoCreaCartelle.getIdDtVers(), versamento.getSubPathVersatoreArk());
		    // se non ho trovato il record, aspetto un paio di secondi e poi ci riprovo
		    Thread.sleep(ATTESA_RETRY_TEST_IN_MILLISECONDI);
		}
	    }
	    statoCreaCartelle.setCreaCartellaVersatore(!recordPresente);
	    rispostaControlli.setrLong(1);
	    rispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "SalvataggioCompFS.verificaDirTPIVersatore: " + e.getMessage()));
	    log.error("Si è verificato un errore durante le operazioni su database", e);
	}

	return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public RispostaControlli verificaDirSuFs(String testDir) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrLong(-1);
	rispostaControlli.setrBoolean(false);
	/*
	 * nota bene: il codice strano che segue ha senso dal momento che la share NFS su cui
	 * vengono depositati i file non aggiorna subito il contenuto a tutti i sistemi verso cui
	 * viene esportata. La semplice invocazione del metodo di verifica file eseguita pochi
	 * millisecondi dopo la creazione, come avviene nel caso di versamenti paralleli in
	 * produzione, fornisce quasi sempre esito negativo. Il ciclo for... che segue consente di
	 * ritentare più volte la verifica con pause di circa 2 secondi per fornire al file system
	 * il tempo di registrare la creazione della directory.
	 */
	XADiskConnection xadConn = null;
	try {
	    File testFile = new File(testDir);
	    boolean filePresente = false;

	    xadConn = xadCf.getConnection();
	    for (int volte = 0; volte < MAX_NUMERO_TENTATIVI_TEST; volte++) {
		log.debug("verificaDirSuFs : cerco directory {}", testDir);
		filePresente = WsXAUtil.fileExistsAndIsDirectory(xadConn, testFile);
		if (filePresente) {
		    break;
		} else {
		    log.debug("verificaDirSuFs : non ho trovato la directory {}", testDir);
		    // se non ho trovato il file, aspetto un paio di secondi e poi ci riprovo
		    Thread.sleep(ATTESA_RETRY_TEST_IN_MILLISECONDI);
		}
	    }
	    log.debug("verificafile: File presente: {}", filePresente);
	    rispostaControlli.setrBoolean(filePresente);
	} catch (Exception e) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "SalvataggioCompFS.verificaDirSuFs: " + e.getMessage()));
	    log.error("Si è verificato un errore durante le operazioni su filesystem", e);
	} finally {
	    if (xadConn != null) {
		xadConn.close();
		log.debug("Effettuata chiusura della connessione XADisk");
	    }
	}
	return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public RispostaControlli generaDirDataTPIBlock(StrutturaVersamento versamento,
	    StatoCreaCartelle statoCreaCartelle) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrLong(-1);
	rispostaControlli.setrBoolean(false);

	VrsDtVers vrsDtVers = null;
	XADiskConnection xadConn = null;

	/*
	 * lock sulla funzione di creazione directory TPI, per evitare problemi di concorrenza tra i
	 * vari application server
	 */
	LogLockElab lockRecord;
	Query query = entityManager.createQuery("SELECT lock FROM LogLockElab lock "
		+ "WHERE lock.nmElab = :nmElab AND lock.tiLockElab = :tiLock");
	query.setParameter("nmElab", LOCKNAME_CREA_DIRECTORY_DATA_TPI);
	query.setParameter("tiLock", LOCKTYPE_LOCK_UNICO);
	lockRecord = (LogLockElab) query.getSingleResult();
	//
	Map<String, Object> properties = new HashMap<>();
	properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT,
		LOCK_LOCK_TIMEOUT_IN_SECONDI * 1000);
	entityManager.find(LogLockElab.class, lockRecord.getIdLockElab(),
		LockModeType.PESSIMISTIC_WRITE, properties);
	//
	try {
	    RispostaControlli rc = this.verificaDirTPIDataViaDb(versamento, statoCreaCartelle, 1);
	    if (rc.isrBoolean()) {
		if (statoCreaCartelle.isCreaCartellaData()) {
		    log.info(
			    "SalvataggioCompFS.generaDirDataTPIBlock: devo creare la cartella Data");
		    String testDir = MessageFormat.format("{0}/{1}", versamento.getTpiRootTpi(),
			    versamento.getTpiRootArkVers());
		    vrsDtVers = new VrsDtVers();
		    // MAC#27269
		    vrsDtVers.setDtVers(convert(versamento.getDataVersamento()).toInstant()
			    .atZone(ZoneId.systemDefault()).toLocalDate());
		    // end MAC#27269
		    vrsDtVers.setTiStatoDtVers(ARK_STATUS_REGISTRATA);
		    vrsDtVers.setFlMigraz("0");
		    vrsDtVers.setFlArk("0");
		    vrsDtVers.setFlPresenzaSecondario("0");
		    vrsDtVers.setDtCreazioneDtVers(convert(versamento.getDataVersamento()));
		    vrsDtVers.setDtStatoDtVers(convert(versamento.getDataVersamento()));
		    vrsDtVers.setDlPathDtVers(testDir);
		    entityManager.persist(vrsDtVers);
		    entityManager.flush();
		    //
		    xadConn = xadCf.getConnection();
		    WsXAUtil.createDirectory(xadConn,
			    new File(statoCreaCartelle.getPathCartellaData()));
		    entityManager.flush();
		    //
		    // memorizzo l'id della riga creata.
		    // nel caso isCreaCartellaData fosse stato negativo, l'id sarebbe già stato
		    // memorizzato
		    statoCreaCartelle.setIdDtVers(vrsDtVers.getIdDtVers());
		    statoCreaCartelle.setCreaCartellaData(false);
		} else {
		    log.debug("SalvataggioCompFS.generaDirDataTPIBlock: la cartella Data "
			    + "risulta già presente ad una seconda verifica");
		}

		rispostaControlli.setrLong(1);
		rispostaControlli.setrBoolean(true);
	    } else {
		// errore
		throw new Exception(rc.getDsErr());
	    }
	} catch (Exception e) {
	    if (ExceptionUtils.indexOfThrowable(e,
		    java.sql.SQLIntegrityConstraintViolationException.class) > -1) {
		// se ho una violazione di integrità della constraint, vuol dire che sto cercando di
		// inserire
		// un record che la query precedente non ha trovato ma che in realtà c'è.
		// in questo caso rendo una risposta positiva ma lascio lo stato creazione
		// inalterato
		// (in particolare manca l'ID del record di VrsDtVers, non trovato e non creato da
		// me)
		rispostaControlli.setrLong(1);
		rispostaControlli.setrBoolean(true);
		log.warn(
			"Ho tentato di scrivere la riga di VrsDtVers relativa alla data {} ma ho fallito con una violazione di constraint. Provo a fingere non sia successo nulla",
			format(versamento.getDataVersamento()));
	    } else {
		rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
			"SalvataggioCompFS.generaDirDataTPIBlock: impossibile creare "
				+ "la directory o la riga corrispondente alla data versamento che figura non "
				+ "registrata nel database." + e.getMessage()));
		log.error("Si è verificato un errore durante le operazioni su filesystem", e);
	    }
	} finally {
	    if (xadConn != null) {
		xadConn.close();
		log.debug("Effettuata chiusura della connessione XADisk");
	    }
	}
	return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public RispostaControlli generaDirVersatoreTPIBlock(StrutturaVersamento versamento,
	    StatoCreaCartelle statoCreaCartelle) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrLong(-1);
	rispostaControlli.setrBoolean(false);

	VrsPathDtVers vrsPathDtVers;
	XADiskConnection xadConn = null;

	/*
	 * lock sulla funzione di creazione directory TPI, per evitare problemi di concorrenza tra i
	 * vari application server
	 */
	LogLockElab lockRecord;
	Query query = entityManager.createQuery("SELECT lock FROM LogLockElab lock "
		+ "WHERE lock.nmElab = :nmElab AND lock.tiLockElab = :tiLock");
	query.setParameter("nmElab", LOCKNAME_CREA_DIRECTORY_VERS_TPI);
	query.setParameter("tiLock", LOCKTYPE_LOCK_UNICO);
	lockRecord = (LogLockElab) query.getSingleResult();
	//
	Map<String, Object> properties = new HashMap<>();
	properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT,
		LOCK_LOCK_TIMEOUT_IN_SECONDI * 1000);
	entityManager.find(LogLockElab.class, lockRecord.getIdLockElab(),
		LockModeType.PESSIMISTIC_WRITE, properties);
	//
	try {
	    RispostaControlli rc = this.verificaDirTPIVersatoreViaDb(versamento, statoCreaCartelle,
		    1);
	    if (rc.isrBoolean()) {
		if (statoCreaCartelle.isCreaCartellaVersatore()) {
		    log.info(
			    "SalvataggioCompFS.generaDirVersatoreTPIBlock: devo creare la cartella Versatore");
		    VrsDtVers vrsDtVers = entityManager.find(VrsDtVers.class,
			    statoCreaCartelle.getIdDtVers());
		    //
		    vrsPathDtVers = new VrsPathDtVers();
		    vrsPathDtVers.setVrsDtVers(vrsDtVers);
		    vrsPathDtVers.setDlPath(versamento.getSubPathVersatoreArk());
		    vrsPathDtVers.setFlPathArk("0");
		    entityManager.persist(vrsPathDtVers);
		    entityManager.flush();
		    //
		    xadConn = xadCf.getConnection();
		    WsXAUtil.createDirectory(xadConn,
			    new File(statoCreaCartelle.getPathCartellaVersatore()));
		    entityManager.flush();
		    //
		    statoCreaCartelle.setCreaCartellaVersatore(false);
		} else {
		    log.debug("SalvataggioCompFS.generaDirVersatoreTPIBlock: la cartella Versatore "
			    + "risulta già presente ad una seconda verifica");
		}
		rispostaControlli.setrLong(1);
		rispostaControlli.setrBoolean(true);
	    } else {
		// errore
		throw new SacerWsException(rc.getDsErr(), SacerWsErrorCategory.INTERNAL_ERROR);
	    }
	} catch (Exception e) {
	    if (ExceptionUtils.indexOfThrowable(e,
		    java.sql.SQLIntegrityConstraintViolationException.class) > -1) {
		// se ho una violazione di integrità della constraint, vuol dire che sto cercando di
		// inserire
		// un record che la query precedente non ha trovato ma che in realtà c'è.
		// in questo caso rendo una risposta positiva ma lascio lo stato creazione
		// inalterato
		rispostaControlli.setrLong(1);
		rispostaControlli.setrBoolean(true);
		log.warn(
			"Ho tentato di scrivere la riga di VrsPathDtVers relativa al versatore {} ma ho fallito con una violazione di constraint. Provo a fingere non sia successo nulla",
			versamento.getSubPathVersatoreArk());
	    } else {
		rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
			"SalvataggioCompFS.generaDirVersatoreTPIBlock: impossibile creare "
				+ "la directory o la riga corrispondente al versatore che figura non "
				+ "registrata nel database." + e.getMessage()));
		log.error("Si è verificato un errore durante le operazioni su filesystem", e);
	    }
	} finally {
	    if (xadConn != null) {
		xadConn.close();
		log.debug("Effettuata chiusura della connessione XADisk");
	    }
	}
	return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli generaDirUDTPI(StrutturaVersamento versamento,
	    StatoCreaCartelle statoCreaCartelle) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrLong(-1);
	rispostaControlli.setrBoolean(false);

	XADiskConnection xadConn = null;
	String testDir;

	try {
	    xadConn = xadCf.getConnection();
	    testDir = MessageFormat.format("{0}/{1}/{2}/{3}/{4}", versamento.getTpiRootTpiDaSacer(),
		    versamento.getTpiRootVers(), versamento.getSubPathDataVers(),
		    versamento.getSubPathVersatoreArk(), versamento.getSubPathUnitaDocArk());
	    if (!WsXAUtil.fileExistsAndIsDirectory(xadConn, new File(testDir))) {
		WsXAUtil.createDirectory(xadConn, new File(testDir));
	    }
	    //
	    rispostaControlli.setrLong(1);
	    rispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "SalvataggioCompFS.generaDirUDTPI: " + e.getMessage()));
	    log.error("Si è verificato un errore durante le operazioni su filesystem", e);
	} finally {
	    if (xadConn != null) {
		xadConn.close();
		log.debug("Effettuata chiusura della connessione XADisk");
	    }
	}

	return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli copiaCompSuFS(StrutturaVersamento versamento,
	    ComponenteVers componenteVers) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrLong(-1);
	rispostaControlli.setrBoolean(false);

	XADiskConnection xadConn = null;
	String tmpFilePath = MessaggiWSFormat.formattaFilePathArk(versamento.getTpiRootTpiDaSacer(),
		versamento.getTpiRootVers(), versamento.getSubPathDataVers(),
		versamento.getSubPathVersatoreArk(), versamento.getSubPathUnitaDocArk(),
		componenteVers.getNomeFileArk());

	try {
	    xadConn = xadCf.getConnection();
	    WsXAUtil.copyFile(xadConn, componenteVers.getRifFileBinario().getFileSuDisco(),
		    new File(tmpFilePath));
	    rispostaControlli.setrLong(1);
	    rispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "SalvataggioCompFS.copiaCompSuFS: " + e.getMessage()));
	    log.error("Si è verificato un errore durante le operazioni su filesystem", e);
	} finally {
	    if (xadConn != null) {
		xadConn.close();
		log.debug("Effettuata chiusura della connessione XADisk");
	    }
	}

	return rispostaControlli;
    }

}
