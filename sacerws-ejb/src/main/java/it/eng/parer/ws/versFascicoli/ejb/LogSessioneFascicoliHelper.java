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

package it.eng.parer.ws.versFascicoli.ejb;

import static it.eng.parer.util.DateUtilsConverter.convert;
import static it.eng.parer.util.DateUtilsConverter.convertLocal;
import static it.eng.parer.util.FlagUtilsConverter.booleanToFlag;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecErrSacer;
import it.eng.parer.entity.DecTipoFascicolo;
import it.eng.parer.entity.FasFascicolo;
import it.eng.parer.entity.IamUser;
import it.eng.parer.entity.MonContaFascicoliKo;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.VrsErrSesFascicoloKo;
import it.eng.parer.entity.VrsFascicoloKo;
import it.eng.parer.entity.VrsSesFascicoloErr;
import it.eng.parer.entity.VrsSesFascicoloKo;
import it.eng.parer.entity.VrsXmlSesFascicoloErr;
import it.eng.parer.entity.VrsXmlSesFascicoloKo;
import it.eng.parer.util.Constants;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.view_entity.VrsVUpdFascicoloKo;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.XmlFascCache;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.LogSessioneUtils;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSHelper;
import it.eng.parer.ws.versFascicoli.dto.CompRapportoVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.RispostaWSFascicolo;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;

/**
 *
 * @author fioravanti_f
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "LogSessioneFascicoliHelper")
@LocalBean
public class LogSessioneFascicoliHelper {

    private static final Logger log = LoggerFactory.getLogger(LogSessioneFascicoliHelper.class);
    //

    @EJB
    MessaggiWSHelper messaggiWSHelper;

    @EJB
    XmlFascCache xmlFascCache;

    @EJB
    private AppServerInstance appServerInstance;

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    // costanti per la tabella VrsSesFascicoloErr
    private static final String SESSIONE_NON_VERIFICATA = "NON_VERIFICATO";

    // costanti per la tabella VrsFascicoloKo
    public static final String FAS_NON_VERIFICATO = "NON_VERIFICATO";
    public static final String FAS_RISOLTO = "RISOLTO";
    //
    private static final String TIPO_ERR_FATALE = "FATALE";
    private static final String TIPO_ERR_WARNING = "WARNING";
    private static final String CD_DS_ERR_DIVERSI = "Diversi";

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli verificaPartizioneFascicoloErr() {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrBoolean(false);
	long conta = 0;
	try {
	    String queryStr = "select count(t) from OrgVChkPartitionFascErr t "
		    + "where t.flPartSesfascerrOk = :flPartSesfascerrOk ";
	    javax.persistence.Query query = entityManager.createQuery(queryStr);
	    query.setParameter("flPartSesfascerrOk", "1");
	    conta = (Long) query.getSingleResult();

	    if (conta == 0) {
		rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
			"La tabella delle sessioni errate non è correttamente partizionata. "
				+ "Impossibile salvare il fascicolo in errore"));
		return rispostaControlli;
	    }
	    rispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "LogSessioneFascicoliHelper.verificaPartizioneFascicoloErr: "
			    + e.getMessage()));
	    log.error("Eccezione nella lettura della tabella di decodifica ", e);
	}

	return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli verificaPartizioneFascicoloByAaStrutKo(VersFascicoloExt versamento) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrBoolean(false);
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	long conta = 0;
	try {
	    String queryStr = "select count(t) from OrgVChkPartitionFascByAa t "
		    + "where t.idStrut = :idStrut and t.anno = :anno "
		    + "and t.flPartFasckoOk = :flPartFasckoOk "
		    + "and t.flPartFasckoAnnoOk = :flPartFasckoAnnoOk "
		    + "and t.flPartSesfasckoOk = :flPartSesfasckoOk "
		    + "and t.flPartSesfasckoAnnoOk = :flPartSesfasckoAnnoOk "
		    + "and t.flPartXmlsesfasckoOk = :flPartXmlsesfasckoOk "
		    + "and t.flPartXmlsesfasckoDataOk = :flPartXmlsesfasckoDataOk ";
	    javax.persistence.Query query = entityManager.createQuery(queryStr);

	    query.setParameter("idStrut", new BigDecimal(svf.getIdStruttura()));
	    query.setParameter("anno", new BigDecimal(svf.getChiaveNonVerificata().getAnno()));

	    query.setParameter("flPartFasckoOk", "1");
	    query.setParameter("flPartFasckoAnnoOk", "1");
	    query.setParameter("flPartSesfasckoOk", "1");
	    query.setParameter("flPartSesfasckoAnnoOk", "1");
	    query.setParameter("flPartSesfasckoAnnoOk", "1");
	    query.setParameter("flPartXmlsesfasckoOk", "1");
	    query.setParameter("flPartXmlsesfasckoDataOk", "1");

	    conta = (Long) query.getSingleResult();

	    if (conta == 0) {
		rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
			"La tabella delle sessioni fallite non è correttamente partizionata. "
				+ "Impossibile salvare il fascicolo fallito"));
		return rispostaControlli;
	    }
	    rispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "LogSessioneFascicoliHelper.verificaPartizioneFascicoloKo: " + e.getMessage()));
	    log.error("Eccezione nella lettura della tabella di decodifica ", e);
	}

	return rispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviFascicoloErr(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, SyncFakeSessn sessione) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	// salvo sessione errata
	tmpRispostaControlli.setrBoolean(false);
	try {
	    VrsSesFascicoloErr tmpFascicoloErr = new VrsSesFascicoloErr();
	    tmpFascicoloErr.setCdVersioneWs(sessione.getVersioneWS());
	    tmpFascicoloErr.setTsIniSes(convert(sessione.getTmApertura()));
	    tmpFascicoloErr.setTsFineSes(convert(sessione.getTmChiusura()));
	    tmpFascicoloErr.setNmUseridWs(sessione.getLoginName());
	    // salvo il nome del server/istanza nel cluster che sta salvando i dati e ha gestito il
	    // versamento
	    tmpFascicoloErr.setCdIndServer(appServerInstance.getName());
	    // salvo l'indirizzo IP del sistema che ha effettuato la richiesta di
	    // versamento/aggiunta
	    tmpFascicoloErr.setCdIndIpClient(sessione.getIpChiamante());
	    tmpFascicoloErr.setTiStatoSes(SESSIONE_NON_VERIFICATA);
	    if (svf.getVersatoreNonverificato() != null) {
		tmpFascicoloErr.setNmAmbiente(svf.getVersatoreNonverificato().getAmbiente());
		tmpFascicoloErr.setNmEnte(svf.getVersatoreNonverificato().getEnte());
		tmpFascicoloErr.setNmStrut(svf.getVersatoreNonverificato().getStruttura());
	    }
	    if (svf.getIdStruttura() > 0) {
		tmpFascicoloErr
			.setOrgStrut(entityManager.find(OrgStrut.class, svf.getIdStruttura()));
	    }
	    if (svf.getChiaveNonVerificata() != null) {
		tmpFascicoloErr
			.setAaFascicolo(new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
		tmpFascicoloErr.setCdKeyFascicolo(svf.getChiaveNonVerificata().getNumero());
	    }
	    if (svf.getIdTipoFascicolo() > 0) {
		tmpFascicoloErr.setDecTipoFascicolo(
			entityManager.find(DecTipoFascicolo.class, svf.getIdTipoFascicolo()));
	    }
	    tmpFascicoloErr.setNmTipoFascicolo(svf.getTipoFascicoloNonverificato());
	    //
	    CompRapportoVersFascicolo esito = rispostaWs.getCompRapportoVersFascicolo();
	    if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
		tmpFascicoloErr.setDecErrSacer(messaggiWSHelper
			.caricaDecErrore(esito.getEsitoGenerale().getCodiceErrore()));
		tmpFascicoloErr.setDsErr(LogSessioneUtils
			.getDsErrAtMaxLen(esito.getEsitoGenerale().getMessaggioErrore()));
	    }

	    entityManager.persist(tmpFascicoloErr);
	    tmpRispostaControlli.setrObject(tmpFascicoloErr);
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio sessione errata: " + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviXmlFascicoloErr(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, SyncFakeSessn sessione, VrsSesFascicoloErr fascicoloErr) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	// salvo xml di request e response sessione errata
	tmpRispostaControlli.setrBoolean(false);
	CompRapportoVersFascicolo esito = rispostaWs.getCompRapportoVersFascicolo();
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	try {
	    VrsXmlSesFascicoloErr tmpXmlSesFascicoloErr = new VrsXmlSesFascicoloErr();
	    tmpXmlSesFascicoloErr.setVrsSesFascicoloErr(fascicoloErr);
	    tmpXmlSesFascicoloErr.setTiXml(CostantiDB.TipiXmlDati.RICHIESTA);
	    tmpXmlSesFascicoloErr.setCdVersioneXml(svf.getVersioneIndiceSipNonVerificata());
	    tmpXmlSesFascicoloErr.setBlXml(
		    versamento.getDatiXml().length() == 0 ? "--" : versamento.getDatiXml());
	    entityManager.persist(tmpXmlSesFascicoloErr);

	    String xmlesito = this.generaRapportoVersamento(rispostaWs);

	    tmpXmlSesFascicoloErr = new VrsXmlSesFascicoloErr();
	    tmpXmlSesFascicoloErr.setVrsSesFascicoloErr(fascicoloErr);
	    tmpXmlSesFascicoloErr.setTiXml(CostantiDB.TipiXmlDati.RISPOSTA);
	    tmpXmlSesFascicoloErr.setCdVersioneXml(esito.getVersioneRapportoVersamento());
	    tmpXmlSesFascicoloErr.setBlXml(xmlesito);
	    entityManager.persist(tmpXmlSesFascicoloErr);

	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio  dati xml sessione errata: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli cercaFascicoloKo(VersFascicoloExt versamento) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	// cerco e recupero il fascicolo fallito
	tmpRispostaControlli.setrBoolean(false);
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	List<VrsFascicoloKo> fasFascicolos;

	try {
	    String queryStr = "select ud " + "from VrsFascicoloKo ud "
		    + "where ud.orgStrut.idStrut = :idStrutIn "
		    + " and ud.cdKeyFascicolo = :cdKeyFascicolo "
		    + " and ud.aaFascicolo = :aaFascicolo ";
	    javax.persistence.Query query = entityManager.createQuery(queryStr,
		    VrsFascicoloKo.class);
	    query.setParameter("idStrutIn", svf.getIdStruttura());
	    query.setParameter("cdKeyFascicolo", svf.getChiaveNonVerificata().getNumero());
	    query.setParameter("aaFascicolo",
		    new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
	    fasFascicolos = query.getResultList();
	    // rendo comunque true, per indicare che la query è andata bene:
	    // è perfettamente normale non trovare nulla se questo è il primo
	    // errore per il fascicolo. in questo caso dovrò creare la riga in tabella
	    // nel caso di scrittura di sessione fallita.
	    tmpRispostaControlli.setrBoolean(true);
	    if (!fasFascicolos.isEmpty()) {
		// se poi ho anche trovato qualcosa di utile, lo restituisco al
		// chiamante dopo averlo bloccato in modo esclusivo
		Map<String, Object> properties = new HashMap<>();
		properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT, 25000);
		VrsFascicoloKo tmpFascicoloKo = entityManager.find(VrsFascicoloKo.class,
			fasFascicolos.get(0).getIdFascicoloKo(), LockModeType.PESSIMISTIC_WRITE,
			properties);

		// questo stesso lock viene usato sia in fase di scrittura sessione KO
		// che di scrittura sessione buona. In questo secondo caso la riga verrà
		// cancellata. Dal momento che per condizioni sfortunate di concorrenza è
		// possibile che la query iniziale renda dei dati ma la find non
		// trovi nulla, verifico che nel frattempo la riga non sia stata cancellata.
		if (tmpFascicoloKo != null) {
		    tmpRispostaControlli.setrLong(tmpFascicoloKo.getIdFascicoloKo());
		    tmpRispostaControlli.setrObject(tmpFascicoloKo);
		} else {
		    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
		    tmpRispostaControlli
			    .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
				    "Errore di concorrenza nel salvataggio della sessione:"
					    + "il fascicolo KO è stato rimosso. "
					    + "Si consiglia di ritentare il versamento."));
		    tmpRispostaControlli.setrBoolean(false);
		}
	    }
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase ricerca Fascicolo KO (SF): " + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviFascicoloKo(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, SyncFakeSessn sessione) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	// scrive una nuova istanza di Fascicolo KO per il versamento
	tmpRispostaControlli.setrBoolean(false);
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	CompRapportoVersFascicolo esito = rispostaWs.getCompRapportoVersFascicolo();

	try {
	    VrsFascicoloKo tmpFascicoloKo = new VrsFascicoloKo();
	    tmpFascicoloKo.setOrgStrut(entityManager.find(OrgStrut.class, svf.getIdStruttura()));
	    tmpFascicoloKo.setAaFascicolo(new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
	    tmpFascicoloKo.setCdKeyFascicolo(svf.getChiaveNonVerificata().getNumero());
	    tmpFascicoloKo.setTsIniFirstSes(convert(sessione.getTmApertura()));
	    tmpFascicoloKo.setTsIniLastSes(convert(sessione.getTmApertura())); // coincidono: c'è
									       // una sola sessione
	    tmpFascicoloKo.setDecErrSacer(
		    messaggiWSHelper.caricaDecErrore(esito.getEsitoGenerale().getCodiceErrore()));
	    tmpFascicoloKo.setDsErrPrinc(LogSessioneUtils
		    .getDsErrAtMaxLen(esito.getEsitoGenerale().getMessaggioErrore()));
	    tmpFascicoloKo.setDecTipoFascicolo(entityManager.find(DecTipoFascicolo.class,
		    versamento.getStrutturaComponenti().getIdTipoFascicolo()));
	    tmpFascicoloKo.setTiStatoFascicoloKo(FAS_NON_VERIFICATO);

	    // gli altri due campi li potrò popolare solo una volta persistita la sessione KO
	    entityManager.persist(tmpFascicoloKo);
	    tmpRispostaControlli.setrObject(tmpFascicoloKo);

	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);

	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio fascicoloKO: " + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviSessioneFascicoloKo(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, SyncFakeSessn sessione, VrsFascicoloKo fascicoloKo,
	    FasFascicolo fascicoloOk) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	tmpRispostaControlli.setrBoolean(false);
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	CompRapportoVersFascicolo esito = rispostaWs.getCompRapportoVersFascicolo();

	try {
	    String tmpStatoSessione = null;
	    VrsSesFascicoloKo tmpSesFascicoloKo = new VrsSesFascicoloKo();
	    if (fascicoloOk != null) {
		// se sto aggiungendo una sessione fallita ad un fascicolo
		// già presente, questa è _risolta_ per definizione
		tmpSesFascicoloKo.setFasFascicolo(fascicoloOk);
		tmpStatoSessione = FAS_RISOLTO;
	    } else {
		// altrimenti è _non verificata_
		tmpSesFascicoloKo.setVrsFascicoloKo(fascicoloKo);
		tmpStatoSessione = FAS_NON_VERIFICATO;
	    }
	    tmpSesFascicoloKo.setDecTipoFascicolo(
		    entityManager.find(DecTipoFascicolo.class, svf.getIdTipoFascicolo()));
	    tmpSesFascicoloKo.setTsIniSes(convert(sessione.getTmApertura()));
	    tmpSesFascicoloKo.setTsFineSes(convert(sessione.getTmChiusura()));
	    // è possibile avere una sessione fallita anche senza che sia indicata
	    // la versione del versamento
	    if (StringUtils.isEmpty(sessione.getVersioneWS())) {
		tmpSesFascicoloKo.setCdVersioneWs("N/A");
	    } else {
		tmpSesFascicoloKo.setCdVersioneWs(sessione.getVersioneWS());
	    }
	    // salvo l'utente solo se identificato, altrimenti il campo può essere NULL
	    // anche in questo caso è possibile avere una versione fallita senza aver indentificato
	    // l'utente versante
	    if (versamento.getUtente() != null) {
		tmpSesFascicoloKo.setIamUser(
			entityManager.find(IamUser.class, versamento.getUtente().getIdUtente()));
	    }
	    // salvo il nome del server/istanza nel cluster che sta salvando i dati e ha gestito il
	    // versamento
	    tmpSesFascicoloKo.setCdIndServer(appServerInstance.getName());
	    // salvo l'indirizzo IP del sistema che ha effettuato la richiesta di
	    // versamento/aggiunta
	    tmpSesFascicoloKo.setCdIndIpClient(sessione.getIpChiamante());
	    tmpSesFascicoloKo.setDecErrSacer(
		    messaggiWSHelper.caricaDecErrore(esito.getEsitoGenerale().getCodiceErrore()));
	    tmpSesFascicoloKo.setDsErrPrinc(LogSessioneUtils
		    .getDsErrAtMaxLen(esito.getEsitoGenerale().getMessaggioErrore()));
	    tmpSesFascicoloKo.setTiStatoSes(tmpStatoSessione);
	    tmpSesFascicoloKo.setOrgStrut(entityManager.find(OrgStrut.class, svf.getIdStruttura()));
	    tmpSesFascicoloKo
		    .setAaFascicolo(new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
	    entityManager.persist(tmpSesFascicoloKo);
	    tmpRispostaControlli.setrObject(tmpSesFascicoloKo);
	    entityManager.flush();

	    // se sto aggiungendo una sessione ad un fascicolo KO, lo devo aggiornare
	    // inoltre devo aggiornare - forse - la tabella MON_CONTA_FASCICOLI_KO.
	    // la stessa cosa la dovrò fare quando creo un nuovo fascicolo per il
	    // quale esistono sessioni fallite
	    if (fascicoloKo != null) {
		if (fascicoloKo.getTsIniLastSes()
			.before(LogSessioneUtils.getDatePart(convert(sessione.getTmApertura())))) {
		    this.aggiornaConteggioMonContaFasKo(fascicoloKo);
		}
		//
		VrsVUpdFascicoloKo updFasKo = entityManager.find(VrsVUpdFascicoloKo.class,
			fascicoloKo.getIdFascicoloKo());
		if (updFasKo != null) {
		    if (updFasKo.getIdErrSacerPrinc() != null) {
			fascicoloKo.setDecErrSacer(entityManager.find(DecErrSacer.class,
				updFasKo.getIdErrSacerPrinc().longValue()));
			fascicoloKo.setDsErrPrinc(
				LogSessioneUtils.getDsErrAtMaxLen(updFasKo.getDsErrPrinc()));
		    } else {
			fascicoloKo.setDecErrSacer(null);
			fascicoloKo.setDsErrPrinc(
				LogSessioneUtils.getDsErrAtMaxLen(CD_DS_ERR_DIVERSI));
		    }
		    fascicoloKo.setTiStatoFascicoloKo(updFasKo.getTiStatoFascicoloKo());
		    // questo ha senso solo se questa è la prima sessione fallita per il fascicolo
		    fascicoloKo.setIdSesFascicoloKoFirst(updFasKo.getIdSesFascicoloKoFirst());
		}
		fascicoloKo.setDecTipoFascicolo(entityManager.find(DecTipoFascicolo.class,
			versamento.getStrutturaComponenti().getIdTipoFascicolo()));
		fascicoloKo.setTsIniLastSes(convert(sessione.getTmApertura()));
		fascicoloKo.setIdSesFascicoloKoLast(
			new BigDecimal(tmpSesFascicoloKo.getIdSesFascicoloKo()));
		// aggiungo la sessione appena creata al fascicolo KO
		fascicoloKo.getVrsSesFascicoloKos().add(tmpSesFascicoloKo);
	    } else {
		// altrimenti aggiorno il flag di errore del fascicolo OK,
		// che forse è già alzato, ma male non fa...
		fascicoloOk.setFlSesFascicoloKo("1");
	    }

	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);

	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio  dati xml sessione KO: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    public void aggiornaConteggioMonContaFasKo(VrsFascicoloKo fascicoloKo) {
	List<MonContaFascicoliKo> mcfks;

	String queryStr = "select ud " + "from MonContaFascicoliKo ud "
		+ "where ud.orgStrut.idStrut = :idStrutIn " + " and ud.dtRifConta = :dtRifConta "
		+ " and ud.aaFascicolo = :aaFascicolo "
		+ " and ud.tiStatoFascicoloKo = :tiStatoFascicoloKo "
		+ " and ud.decTipoFascicolo.idTipoFascicolo = :idTipoFascicolo ";
	javax.persistence.Query query = entityManager.createQuery(queryStr,
		MonContaFascicoliKo.class);
	query.setParameter("idStrutIn", fascicoloKo.getOrgStrut().getIdStrut());
	query.setParameter("dtRifConta",
		LogSessioneUtils.getDatePart(fascicoloKo.getTsIniLastSes()));
	query.setParameter("aaFascicolo", fascicoloKo.getAaFascicolo());
	query.setParameter("tiStatoFascicoloKo", fascicoloKo.getTiStatoFascicoloKo());
	query.setParameter("idTipoFascicolo",
		fascicoloKo.getDecTipoFascicolo().getIdTipoFascicolo());

	mcfks = query.getResultList();
	if (!mcfks.isEmpty()) {
	    mcfks.get(0).setNiFascicoliKo(mcfks.get(0).getNiFascicoliKo().subtract(BigDecimal.ONE));
	    entityManager.flush();
	}
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviXmlFascicoloKo(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, SyncFakeSessn sessione,
	    VrsSesFascicoloKo sessFascicoloKo) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	// salvo xml di request e response sessione fallita
	tmpRispostaControlli.setrBoolean(false);
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	CompRapportoVersFascicolo esito = rispostaWs.getCompRapportoVersFascicolo();

	try {
	    VrsXmlSesFascicoloKo tmpXmlSesFascicoloErr = new VrsXmlSesFascicoloKo();

	    tmpXmlSesFascicoloErr.setVrsSesFascicoloKo(sessFascicoloKo);
	    tmpXmlSesFascicoloErr.setTiXml(CostantiDB.TipiXmlDati.RICHIESTA);
	    tmpXmlSesFascicoloErr.setCdVersioneXml(svf.getVersioneIndiceSipNonVerificata());
	    tmpXmlSesFascicoloErr.setBlXml(
		    versamento.getDatiXml().length() == 0 ? "--" : versamento.getDatiXml());

	    tmpXmlSesFascicoloErr.setIdStrut(new BigDecimal(svf.getIdStruttura()));
	    tmpXmlSesFascicoloErr.setDtRegXmlSesKo(convertLocal(sessione.getTmApertura()));

	    entityManager.persist(tmpXmlSesFascicoloErr);
	    if (sessFascicoloKo.getVrsXmlSesFascicoloKos() == null) {
		sessFascicoloKo.setVrsXmlSesFascicoloKos(new ArrayList<>());
	    }
	    sessFascicoloKo.getVrsXmlSesFascicoloKos().add(tmpXmlSesFascicoloErr);

	    String xmlesito = this.generaRapportoVersamento(rispostaWs);

	    tmpXmlSesFascicoloErr = new VrsXmlSesFascicoloKo();
	    tmpXmlSesFascicoloErr.setVrsSesFascicoloKo(sessFascicoloKo);
	    tmpXmlSesFascicoloErr.setTiXml(CostantiDB.TipiXmlDati.RISPOSTA);
	    tmpXmlSesFascicoloErr.setCdVersioneXml(esito.getVersioneRapportoVersamento());
	    tmpXmlSesFascicoloErr.setBlXml(xmlesito);
	    tmpXmlSesFascicoloErr.setIdStrut(new BigDecimal(svf.getIdStruttura()));
	    tmpXmlSesFascicoloErr.setDtRegXmlSesKo(convertLocal(sessione.getTmApertura()));

	    entityManager.persist(tmpXmlSesFascicoloErr);
	    sessFascicoloKo.getVrsXmlSesFascicoloKos().add(tmpXmlSesFascicoloErr);

	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio  dati xml sessione fallita: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviErroriFascicoloKo(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, VrsSesFascicoloKo sessFascicoloKo) {
	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	// salvo gli errori ed i warning sessione fallita
	tmpRispostaControlli.setrBoolean(false);

	int progErrore = 1;
	try {
	    VrsErrSesFascicoloKo tmpErrSessioneVers;
	    for (VoceDiErrore tmpVoceDiErrore : versamento.getErroriTrovati()) {
		tmpErrSessioneVers = new VrsErrSesFascicoloKo();
		tmpErrSessioneVers.setVrsSesFascicoloKo(sessFascicoloKo);
		//
		tmpErrSessioneVers.setPgErr(new BigDecimal(progErrore));
		if (tmpVoceDiErrore.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
		    tmpErrSessioneVers.setTiErr(TIPO_ERR_FATALE);
		} else {
		    tmpErrSessioneVers.setTiErr(TIPO_ERR_WARNING);
		}

		tmpErrSessioneVers.setDecErrSacer(
			messaggiWSHelper.caricaDecErrore(tmpVoceDiErrore.getErrorCode()));
		tmpErrSessioneVers.setDsErr(
			LogSessioneUtils.getDsErrAtMaxLen(tmpVoceDiErrore.getErrorMessage()));
		tmpErrSessioneVers
			.setFlErrPrinc(booleanToFlag(tmpVoceDiErrore.isElementoPrincipale()));
		//
		entityManager.persist(tmpErrSessioneVers);
		sessFascicoloKo.getVrsErrSesFascicoloKos().add(tmpErrSessioneVers);
		progErrore++;
	    }
	    entityManager.flush();
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio errori sessione fallita: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String generaRapportoVersamento(RispostaWSFascicolo rispostaWs) throws JAXBException {
	// questo metodo viene invocato sia internamente alla classe (_non_ attraverso il
	// container) che dall'EJB di salvataggio fascicolo. Dal momento che la transazione
	// è sempre REQUIRED, non c'è molta differenza agli effetti pratici.
	StringWriter tmpStreamWriter = new StringWriter();
	CompRapportoVersFascicolo esito = rispostaWs.getCompRapportoVersFascicolo();
	JAXBContext tmpcontesto = xmlFascCache.getVersRespFascicoloCtx();
	Marshaller tmpMarshaller = tmpcontesto.createMarshaller();
	tmpMarshaller.marshal(esito.produciEsitoFascicolo(), tmpStreamWriter);

	return tmpStreamWriter.toString();
    }
}
