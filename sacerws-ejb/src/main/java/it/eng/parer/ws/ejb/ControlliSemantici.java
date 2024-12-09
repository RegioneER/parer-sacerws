/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.ejb;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.paginator.util.HibernateUtils;
import it.eng.parer.entity.AroCompDoc;
import it.eng.parer.entity.AroDoc;
import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.DecAaRegistroUnitaDoc;
import it.eng.parer.entity.DecAttribDatiSpec;
import it.eng.parer.entity.DecFormatoFileDoc;
import it.eng.parer.entity.DecFormatoFileStandard;
import it.eng.parer.entity.DecParteNumeroRegistro;
import it.eng.parer.entity.DecRegistroUnitaDoc;
import it.eng.parer.entity.DecTipoCompDoc;
import it.eng.parer.entity.DecTipoDoc;
import it.eng.parer.entity.DecTipoRapprComp;
import it.eng.parer.entity.DecTipoStrutDoc;
import it.eng.parer.entity.DecTipoUnitaDoc;
import it.eng.parer.entity.DecTipoUnitaDocAmmesso;
import it.eng.parer.entity.DecXsdDatiSpec;
import it.eng.parer.entity.OrgAmbiente;
import it.eng.parer.entity.OrgEnte;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.view_entity.AroVDtVersMaxByUnitaDoc;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.StatoConservazioneUnitaDoc;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiUsoDatiSpec;
import it.eng.parer.ws.utils.KeyOrdUtility;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.versamento.dto.ConfigRegAnno;
import it.eng.parer.ws.versamento.dto.DatiRegistroFiscale;
import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.RispostaControlliAttSpec;

/**
 *
 * @author Pagani_S (iniziata)
 * @author Fioravanti_F
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "ControlliSemantici")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliSemantici {

    private static final Logger log = LoggerFactory.getLogger(ControlliSemantici.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    private ConfigurationHelper config;

    public enum TipologieComponente {

        COMPONENTE, SOTTOCOMPONENTE
    }

    public enum TipiGestioneUDAnnullate {

        CARICA, CONSIDERA_ASSENTE
    }

    public RispostaControlli caricaDefaultDaDB(String tipoPar) {
        return caricaDefaultDaDB(new String[] { tipoPar });
    }

    public RispostaControlli caricaDefaultDaDB(String[] tipoPars) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        try {
            // carico i parametri applicativi
            Map<String, String> tmpDefaults = config.getValoreParamApplicByTiParamApplicAsMap(Arrays.asList(tipoPars));

            if (!tmpDefaults.isEmpty()) {
                rispostaControlli.setrObject(tmpDefaults);
                rispostaControlli.setrBoolean(true);
            } else {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliSemantici.caricaDefaultDaDB: "
                                + "Parametri applicativi non correttamente configurati per "
                                + String.join(",", tipoPars)));
                log.error(
                        "ControlliSemantici.caricaDefaultDaDB: Parametri applicativi non correttamente configurati per {}.",
                        tipoPars.length > 0 ? String.join(",", tipoPars) : "default_params_not_configured");
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.caricaDefaultDaDB: " + e.getMessage()));
            log.error("Eccezione nella lettura  della tabella AplParamApplic ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkIdStrut(CSVersatore vers, Costanti.TipiWSPerControlli tipows, Date dataVersamento) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);

        // return -1 che è il codice di errore
        long idAmb = -1;
        long idEnte = -1;

        // prendo i paramentri dell'xml
        String amb = vers.getAmbiente();
        String ente = vers.getEnte();
        String strut = vers.getStruttura();

        // lista entity JPA ritornate dalle Query
        List<OrgStrut> orgStrutS = null;
        List<OrgEnte> orgEnteS = null;
        List<OrgAmbiente> orgAmbienteS = null;

        // lancio query di controllo
        try {
            // controllo ambiente
            String queryStr = "select amb " + "from OrgAmbiente amb " + "where amb.nmAmbiente =  :nmAmbienteIn";
            javax.persistence.Query query = entityManager.createQuery(queryStr, OrgAmbiente.class);
            query.setParameter("nmAmbienteIn", amb);
            orgAmbienteS = query.getResultList();

            // assente
            if (orgAmbienteS.isEmpty()) {
                switch (tipows) {
                case VERSAMENTO_RECUPERO:
                case AGGIORNAMENTO_VERSAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_001, amb));
                    break;
                case ANNULLAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_004);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_004));
                    break;
                case VERSAMENTO_FASCICOLO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_001_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_001_001, amb));
                    break;
                }
                return rispostaControlli;
            }
            // too many rows
            if (orgAmbienteS.size() > 1) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliSemantici.checkIdStrut: " + "Ambiente duplicato"));
                log.error("ControlliSemantici.checkIdStrut: " + "Ambiente duplicato");
                return rispostaControlli;
            }
            for (OrgAmbiente a : orgAmbienteS) {
                idAmb = a.getIdAmbiente();
            }

            // controllo ente
            queryStr = "select ente " + "from OrgEnte ente " + "where ente.orgAmbiente.idAmbiente = :idAmbienteIn "
                    + " and ente.nmEnte = :nmEnteIn ";
            query = entityManager.createQuery(queryStr, OrgEnte.class);
            query.setParameter("idAmbienteIn", idAmb);
            query.setParameter("nmEnteIn", ente);
            orgEnteS = query.getResultList();
            // assente
            if (orgEnteS.isEmpty()) {
                switch (tipows) {
                case VERSAMENTO_RECUPERO:
                case AGGIORNAMENTO_VERSAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_002, ente));
                    break;
                case ANNULLAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_005);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_005));
                    break;
                case VERSAMENTO_FASCICOLO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_001_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_001_002, ente));
                    break;
                }
                return rispostaControlli;
            }
            // too many rows
            if (orgEnteS.size() > 1) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliSemantici.checkIdStrut: " + "Ente duplicato"));
                log.error("ControlliSemantici.checkIdStrut: " + "Ente duplicato");
                return rispostaControlli;
            }
            for (OrgEnte e : orgEnteS) {
                idEnte = e.getIdEnte();
            }

            // controllo struttura
            queryStr = "select strut " + "from OrgStrut strut " + "where strut.orgEnte.idEnte = :idEnteIn "
                    + " and strut.nmStrut = :nmStrutIn ";
            query = entityManager.createQuery(queryStr, OrgStrut.class);
            query.setParameter("idEnteIn", idEnte);
            query.setParameter("nmStrutIn", strut);
            orgStrutS = query.getResultList();
            // assente
            if (orgStrutS.isEmpty()) {
                switch (tipows) {
                case VERSAMENTO_RECUPERO:
                case AGGIORNAMENTO_VERSAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_003);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_003, strut));
                    break;
                case ANNULLAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_006);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_006));
                    break;
                case VERSAMENTO_FASCICOLO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_001_003);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_001_003, strut));
                    break;
                }
                return rispostaControlli;
            }
            // too many rows
            if (orgStrutS.size() > 1) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliSemantici.checkIdStrut: " + "Struttura duplicata"));
                log.error("ControlliSemantici.checkIdStrut: " + "Struttura duplicata");
                return rispostaControlli;
            }
            // scrivo nel campo Long ausiliario (rLongExtended) l'ID della struttura trovata
            // (mi serve nella gestione fascicoli per restituire l'ID della struttura
            // quando lo trovo, indipendentemente dal fatto che questa sia una template o
            // meno)
            rispostaControlli.setrLongExtended(orgStrutS.get(0).getIdStrut());
            // verifico se è una struttura template
            if (orgStrutS.get(0).getFlTemplate().equals("1")) {
                if (tipows == Costanti.TipiWSPerControlli.VERSAMENTO_RECUPERO
                        || tipows == Costanti.TipiWSPerControlli.AGGIORNAMENTO_VERSAMENTO) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_015);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_015, strut));
                    return rispostaControlli;
                } else if (tipows == Costanti.TipiWSPerControlli.VERSAMENTO_FASCICOLO) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_001_004);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_001_004, strut));
                    return rispostaControlli;
                }
            }
            /**
             * Nota: Confronto basata solo su DATA 1) eseguire un versamento ud su una struttura con dt_ini_val > data
             * corrente : deve restituire errore 2) eseguire un versamento ud su una struttura con dt_fine_val < data
             * corrente : deve restituire errore 3) eseguire un versamento ud su una struttura con dt_ini_val < data
             * corrente e dt_fine_val > data corrente : deve eseguire versamento
             */
            boolean hasDateErr = false;
            if (orgStrutS.get(0).getDtIniValStrut() != null && orgStrutS.get(0).getDtFineValStrut() == null) {
                hasDateErr = DateUtils.truncate(orgStrutS.get(0).getDtIniValStrut(), Calendar.DATE)
                        .after(DateUtils.truncate(dataVersamento, Calendar.DATE));
            } else if (orgStrutS.get(0).getDtIniValStrut() == null && orgStrutS.get(0).getDtFineValStrut() != null) {
                hasDateErr = DateUtils.truncate(orgStrutS.get(0).getDtFineValStrut(), Calendar.DATE)
                        .before(DateUtils.truncate(dataVersamento, Calendar.DATE));
            } else if (orgStrutS.get(0).getDtIniValStrut() != null && orgStrutS.get(0).getDtFineValStrut() != null) {
                // In tutta l'applicazione viene utilizzato joda time solo per questo metodo. refactor senza usare la
                // libreria
                long dtIni = DateUtils.truncate(orgStrutS.get(0).getDtIniValStrut(), Calendar.DATE).getTime();
                long dtFine = DateUtils.truncate(orgStrutS.get(0).getDtFineValStrut(), Calendar.DATE).getTime();
                long dtVers = DateUtils.truncate(dataVersamento, Calendar.DATE).getTime();
                hasDateErr = dtVers < dtIni || dtVers > dtFine;
            }
            if (hasDateErr) {
                if (tipows == Costanti.TipiWSPerControlli.VERSAMENTO_RECUPERO
                        || tipows == Costanti.TipiWSPerControlli.AGGIORNAMENTO_VERSAMENTO) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_019);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_019, strut));
                } else if (tipows == Costanti.TipiWSPerControlli.VERSAMENTO_FASCICOLO) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_001_005);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_001_005, strut));
                }
                return rispostaControlli;
            }

            //
            rispostaControlli.setrLong(orgStrutS.get(0).getIdStrut());
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkIdStrut: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkSistemaMigraz(String sistemaMig) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        Long numSist;
        try {
            String queryStr = "select count(t) from AplSistemaMigraz t "
                    + "where t.nmSistemaMigraz = :nmSistemaMigrazIn ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("nmSistemaMigrazIn", sistemaMig);

            numSist = (Long) query.getSingleResult();
            if (numSist > 0) {
                rispostaControlli.setrBoolean(true);
            } else {
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_014);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_014, sistemaMig));
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkSistemaMigraz: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkChiave(CSChiave key, long idStruttura, TipiGestioneUDAnnullate tguda) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        // prendo i paramentri dell'xml
        String numero = key.getNumero();
        Long anno = key.getAnno();
        String tipoReg = key.getTipoRegistro();

        // lista entity JPA ritornate dalle Query
        List<AroUnitaDoc> unitaDocS = null;

        // lancio query di controllo
        try {
            // ricavo le ud presenti in base ai parametri impostati
            String queryStr = "select ud " + "from AroUnitaDoc ud " + "where ud.orgStrut.idStrut = :idStrutIn "
                    + " and ud.cdKeyUnitaDoc = :cdKeyUnitaDocIn " + " and ud.aaKeyUnitaDoc = :aaKeyUnitaDocIn "
                    + " and ud.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn " + " order by ud.dtCreazione desc";

            javax.persistence.Query query = entityManager.createQuery(queryStr, AroUnitaDoc.class);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("cdKeyUnitaDocIn", numero);
            query.setParameter("aaKeyUnitaDocIn", new BigDecimal(anno));
            query.setParameter("cdRegistroKeyUnitaDocIn", tipoReg);
            unitaDocS = query.getResultList();

            // chiave già presente (uno o più righe trovate, mi interessa solo l'ultima -
            // più recente)
            if (!unitaDocS.isEmpty()) {
                StatoConservazioneUnitaDoc scud = StatoConservazioneUnitaDoc
                        .valueOf(unitaDocS.get(0).getTiStatoConservazione());
                if (scud == StatoConservazioneUnitaDoc.ANNULLATA
                        && tguda == TipiGestioneUDAnnullate.CONSIDERA_ASSENTE) {
                    // commuto l'errore in UD annullata e rendo true come risposta: in pratica come
                    // se non
                    // avesse trovato l'UD ma con un errore diverso: è lo stesso comportamento della
                    // vecchia versione del metodo
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_012_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_012_002,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                    rispostaControlli.setrBoolean(true);
                } else {
                    // gestione normale: ho trovato l'UD e non è annullata.
                    // Oppure è annullata e voglio caricarla lo stesso (il solo caso è nel ws
                    // recupero stato UD)
                    // intanto rendo l'errore di chiave già presente
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_002_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_002_001,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));

                    rispostaControlli.setrLong(unitaDocS.get(0).getIdUnitaDoc());
                    // se la chiave è già presente, oltre all'id dell'UD trovata,
                    // recupero il tipo di salvataggio. Mi serve sia nell'aggiunta documenti
                    // che nel recupero UD
                    rispostaControlli.setrString(unitaDocS.get(0).getDecTipoUnitaDoc().getTiSaveFile());
                    //
                    rispostaControlli.setrStringExtended(unitaDocS.get(0).getDecTipoUnitaDoc().getNmTipoUnitaDoc());
                    // lo stato conservazione viene usato per l'aggiunta doc:
                    // non posso aggiungere doc se l'ud è nello stato sbagliato
                    rispostaControlli.setrObject(scud);

                    // recupero id registro
                    rispostaControlli.setrLongExtended(unitaDocS.get(0).getIdDecRegistroUnitaDoc());
                    // **************
                    // EXTENDED VALUES
                    // **************
                    // recupero id tipo ud
                    rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.ID_TIPO_UD.name(),
                            unitaDocS.get(0).getDecTipoUnitaDoc().getIdTipoUnitaDoc());
                    // recupero chiave normalizzata (se esiste)
                    rispostaControlli.getrMap().put(RispostaControlli.ValuesOnrMap.CD_KEY_NORMALIZED.name(),
                            unitaDocS.get(0).getCdKeyUnitaDocNormaliz());
                }
                return rispostaControlli;
            }

            // Chiave non trovata
            rispostaControlli.setCodErr(MessaggiWSBundle.UD_005_001);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_005_001,
                    MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
            rispostaControlli.setrBoolean(true);

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkChiave: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkTipologiaUD(String tipologiaUD, String descKey, long idStruttura) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);
        // lista entity JPA ritornate dalle Query
        List<DecTipoUnitaDoc> tipoUnitaDocS = null;
        // lancio query di controllo
        try {
            // ricavo le ud presenti in base ai parametri impostati
            String queryStr = "select tud " + "from DecTipoUnitaDoc tud " + "where tud.orgStrut.idStrut = :idStrutIn "
                    + " and UPPER(tud.nmTipoUnitaDoc) = :nmTipoUnitaDocIn " + " and tud.dtIstituz <= :dataDiOggiIn "
                    + " and tud.dtSoppres > :dataDiOggiIn "; // da notare STRETTAMENTE MAGGIORE della data di
            // riferimento!!!

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecTipoUnitaDoc.class);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("nmTipoUnitaDocIn", tipologiaUD.toUpperCase());
            query.setParameter("dataDiOggiIn", new Date());
            tipoUnitaDocS = query.getResultList();

            // tipologia assente
            if (tipoUnitaDocS.isEmpty()) {
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_003_001);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_003_001, descKey, tipologiaUD));
                return rispostaControlli;
            }

            // troppe tipologie UD trovate - la cosa ha senso perché cerco in uppercase e
            // qualche utente geniale potrebbe aver definito due tipologie differenti solo
            // nel "casing" del nome
            if (tipoUnitaDocS.size() > 1) {
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_003_005);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_003_005, descKey, tipologiaUD));
                return rispostaControlli;
            }

            DecTipoUnitaDoc tud = tipoUnitaDocS.get(0);

            rispostaControlli.setrLong(tud.getIdTipoUnitaDoc());
            // recupero il tipo di salvataggio, mi serve nel versamento nuova UD.
            rispostaControlli.setrString(tud.getTiSaveFile());
            rispostaControlli.setrStringExtended(tud.getNmTipoUnitaDoc());
            rispostaControlli.setrObject(tud);// TODO: aggiunto l'oggetto (utile per altri controlli)
            rispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkTipologiaUD: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkTipoRegistro(String nomeTipoRegistro, String descKey, long idStruttura) {
        return this.checkTipoRegistro(nomeTipoRegistro, descKey, idStruttura, null);
    }

    public RispostaControlli checkTipoRegistro(String nomeTipoRegistro, String descKey, long idStruttura,
            String descKeyUdColl) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        List<DecRegistroUnitaDoc> tipoRegistroUDs = null;

        try {
            String queryStr = "select tud " + "from DecRegistroUnitaDoc tud "
                    + "where tud.orgStrut.idStrut = :idStrutIn " + " and tud.cdRegistroUnitaDoc = :cdRegistroUnitaDoc "
                    + " and tud.dtIstituz <= :dataDiOggiIn " + " and tud.dtSoppres > :dataDiOggiIn "; // da notare
            // STRETTAMENTE
            // MAGGIORE della
            // data di
            // riferimento!!!

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecRegistroUnitaDoc.class);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("cdRegistroUnitaDoc", nomeTipoRegistro);
            query.setParameter("dataDiOggiIn", new Date());
            tipoRegistroUDs = query.getResultList();

            // tipo registro assente
            if (tipoRegistroUDs.isEmpty()) {
                if (descKeyUdColl == null) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_003_002);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_003_002, descKey, nomeTipoRegistro));
                } else {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_004_003);
                    rispostaControlli
                            .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_004_003, descKey, descKeyUdColl));
                }
                return rispostaControlli;
            }

            // se trovato, rendo l'id del registro UD relativo al cd registro fornito
            rispostaControlli.setrLong(tipoRegistroUDs.get(0).getIdRegistroUnitaDoc());
            rispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkTipoRegistro: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }
        return rispostaControlli;
    }

    public RispostaControlli checkTipoRegistroTipoUD(String nomeTipoRegistro, String descKey, String tipologiaUD,
            long idStruttura, long idTipologiaUD) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        DatiRegistroFiscale tmpDatiRegistroFiscale = new DatiRegistroFiscale();

        // lista entity JPA ritornate dalle Query
        List<DecRegistroUnitaDoc> tipoRegistroUDs = null;
        List<DecTipoUnitaDocAmmesso> tipoRegistroUDAmms = null;
        long tmpIdReg = 0;

        // lancio query di controllo
        try {
            // ricavo le righe presenti in base ai parametri impostati
            String queryStr = "select tud " + "from DecRegistroUnitaDoc tud "
                    + "where tud.orgStrut.idStrut = :idStrutIn " + " and tud.cdRegistroUnitaDoc = :cdRegistroUnitaDoc "
                    + " and tud.dtIstituz <= :dataDiOggiIn " + " and tud.dtSoppres > :dataDiOggiIn "; // da notare
            // STRETTAMENTE
            // MAGGIORE della
            // data di
            // riferimento!!!

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecRegistroUnitaDoc.class);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("cdRegistroUnitaDoc", nomeTipoRegistro);
            query.setParameter("dataDiOggiIn", new Date());
            tipoRegistroUDs = query.getResultList();

            // tipo registro assente
            if (tipoRegistroUDs.isEmpty()) {
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_003_002);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_003_002, descKey, nomeTipoRegistro));
                return rispostaControlli;
            }

            tmpIdReg = tipoRegistroUDs.get(0).getIdRegistroUnitaDoc();

            queryStr = "select tud " + "from DecTipoUnitaDocAmmesso tud "
                    + "where tud.decRegistroUnitaDoc.idRegistroUnitaDoc = :idRegistroUnitaDoc "
                    + " and tud.decTipoUnitaDoc.idTipoUnitaDoc = :idTipoUnitaDoc ";

            query = entityManager.createQuery(queryStr, DecTipoUnitaDocAmmesso.class);
            query.setParameter("idRegistroUnitaDoc", tmpIdReg);
            query.setParameter("idTipoUnitaDoc", idTipologiaUD);
            tipoRegistroUDAmms = query.getResultList();

            // tipologia non ammessa
            if (tipoRegistroUDAmms.isEmpty()) {
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_003_003);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_003_003, descKey,
                        nomeTipoRegistro, tipologiaUD));
                return rispostaControlli;
            }

            tmpDatiRegistroFiscale.setIdOrgStrut(idStruttura);
            tmpDatiRegistroFiscale.setIdRegistroUnitaDoc(tmpIdReg);
            tmpDatiRegistroFiscale.setFlRegistroFisc(tipoRegistroUDs.get(0).getFlRegistroFisc().equals("1"));

            rispostaControlli.setrLong(tmpIdReg);
            rispostaControlli.setrBoolean(true);
            rispostaControlli.setrObject(tmpDatiRegistroFiscale);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkTipoRegistroTipoUD: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli caricaRegistroAnno(String nomeTipoRegistro, String descKey, Long anno,
            long idRegistroUnitaDoc) {
        return this.caricaRegistroAnno(nomeTipoRegistro, descKey, anno, idRegistroUnitaDoc, null);
    }

    public RispostaControlli caricaRegistroAnno(String nomeTipoRegistro, String descKey, Long anno,
            long idRegistroUnitaDoc, String descKeyUdColl) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        try {
            String queryStr = "select t from DecAaRegistroUnitaDoc t "
                    + "where t.decRegistroUnitaDoc.idRegistroUnitaDoc " + "= :idRegistroUnitaDocIn "
                    + " and t.aaMinRegistroUnitaDoc <= :annoUDIn " + " and (t.aaMaxRegistroUnitaDoc >= :annoUDIn "
                    + "or (t.aaMaxRegistroUnitaDoc is null " + "and :annoCorrenteIn >= :annoUDIn))";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idRegistroUnitaDocIn", idRegistroUnitaDoc);
            query.setParameter("annoUDIn", new BigDecimal(anno));
            query.setParameter("annoCorrenteIn", new BigDecimal(new GregorianCalendar().get(Calendar.YEAR)));

            List<DecAaRegistroUnitaDoc> tmpLst = query.getResultList();
            if (tmpLst.size() == 1) {
                DecAaRegistroUnitaDoc tmpAaRegistroUnitaDoc = tmpLst.get(0);
                ConfigRegAnno tmpConfAnno = this.caricaPartiAARegistro(tmpAaRegistroUnitaDoc.getIdAaRegistroUnitaDoc());
                if (!tmpConfAnno.getParti().isEmpty()) {
                    rispostaControlli.setrObject(tmpConfAnno);
                    rispostaControlli.setrBoolean(true);
                } else {
                    if (descKeyUdColl == null) {
                        rispostaControlli.setCodErr(MessaggiWSBundle.UD_003_004);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_003_004, descKey,
                                nomeTipoRegistro + ": non è stata configurata la numerazione per il periodo",
                                anno.toString()));
                    } else {
                        rispostaControlli.setCodErr(MessaggiWSBundle.UD_004_004);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_004_004, descKey,
                                descKeyUdColl, ": non è stata configurata la numerazione per il periodo"));
                    }
                    return rispostaControlli;
                }
            } else {
                if (descKeyUdColl == null) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_003_004);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_003_004, descKey,
                            nomeTipoRegistro, anno.toString()));
                } else {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_004_004);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_004_004, descKey, descKeyUdColl, ""));
                }
                return rispostaControlli;
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkTipoRegistroAnno: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    // chiamata anche dal job, rende la configurazione per validare i numeri/chiave
    // in base all'anno del registro
    public ConfigRegAnno caricaPartiAARegistro(long idAaRegistroUnitaDoc) {
        ConfigRegAnno tmpConfAnno = new ConfigRegAnno(idAaRegistroUnitaDoc);

        String queryStr = "select t from DecParteNumeroRegistro t "
                + "where t.decAaRegistroUnitaDoc.idAaRegistroUnitaDoc " + "= :idAaRegistroUnitaDoc "
                + "order by t.niParteNumeroRegistro";
        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("idAaRegistroUnitaDoc", idAaRegistroUnitaDoc);
        List<DecParteNumeroRegistro> tmpLstP = query.getResultList();

        for (DecParteNumeroRegistro tmpParte : tmpLstP) {
            ConfigRegAnno.ParteRegAnno tmpPRanno = tmpConfAnno.aggiungiParte();
            tmpPRanno.setNumParte(tmpParte.getNiParteNumeroRegistro().intValue());
            tmpPRanno.setNomeParte(tmpParte.getNmParteNumeroRegistro());
            tmpPRanno.setMaxLen(tmpParte.getNiMaxCharParte() != null ? tmpParte.getNiMaxCharParte().longValue() : -1);
            tmpPRanno.setMinLen(tmpParte.getNiMinCharParte() != null ? tmpParte.getNiMinCharParte().longValue() : 0);

            //
            tmpPRanno.setSeparatore((tmpParte.getTiCharSep() != null && tmpParte.getTiCharSep().isEmpty()) ? " "
                    : tmpParte.getTiCharSep());
            /*
             * se il separatore è una stringa non-nulla ma vuota, il valore viene letto come uno spazio. nel DB è
             * memorizzato come CHAR(1), pad-dato -al salvataggio- da Oracle, e che al momento della lettura viene
             * trim-ato da eclipselink. Quindi con questo sistema ricostruisco il valore originale se questo era uno
             * spazio
             */
            //
            // Nota: nel DB la variabile tiParte ha tre valori mutualmente esclusivi.
            // in questo caso, vengono gestiti come 3 flag separati perché i test relativi
            // vengono effettuati in parti diverse del codice
            tmpPRanno.setMatchAnnoChiave(
                    tmpParte.getTiParte() != null && tmpParte.getTiParte().equals(ConfigRegAnno.TiParte.ANNO.name()));
            tmpPRanno.setMatchRegistro(tmpParte.getTiParte() != null
                    && tmpParte.getTiParte().equals(ConfigRegAnno.TiParte.REGISTRO.name()));
            tmpPRanno.setUsaComeProgressivo(
                    tmpParte.getTiParte() != null && tmpParte.getTiParte().equals(ConfigRegAnno.TiParte.PROGR.name()));
            tmpPRanno.setTipoCalcolo(KeyOrdUtility.TipiCalcolo.valueOf(tmpParte.getTiCharParte()));
            tmpPRanno.setTiPadding(
                    tmpParte.getTiPadSxParte() != null ? ConfigRegAnno.TipiPadding.valueOf(tmpParte.getTiPadSxParte())
                            : ConfigRegAnno.TipiPadding.NESSUNO);
            ConfigRegAnno.impostaValoriAccettabili(tmpPRanno, tmpParte.getDlValoriParte());
        }
        tmpConfAnno.ElaboraParti();
        return tmpConfAnno;
    }

    public RispostaControlli checkTipoDocumento(String nomeTipoDoc, long idStruttura, boolean documentoPrinc,
            String descChiaveDoc) {

        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        rispostaControlli.setrBoolean(false);

        // lista entity JPA ritornate dalle Query
        List<DecTipoDoc> tipoDocS = null;

        // lancio query di controllo
        try {

            // ricavo le ud presenti in base ai parametri impostati
            String queryStr = "select td " + "from DecTipoDoc td " + "where td.orgStrut.idStrut = :idStrutIn "
                    + " and UPPER(td.nmTipoDoc) = :nmTipoDocIn " + " and td.dtIstituz <= :dataDiOggiIn "
                    + " and td.dtSoppres > :dataDiOggiIn "; // da notare STRETTAMENTE MAGGIORE della data di
            // riferimento!!!

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecTipoDoc.class);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("nmTipoDocIn", nomeTipoDoc.toUpperCase());
            query.setParameter("dataDiOggiIn", new Date());
            tipoDocS = query.getResultList();

            // tipo documento assente
            if (tipoDocS.isEmpty()) {
                // "Tipo documento non presente per la determinata struttura"
                rispostaControlli.setCodErr(MessaggiWSBundle.DOC_001_001);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DOC_001_001, descChiaveDoc, nomeTipoDoc));
                return rispostaControlli;
            }

            // troppe tipologie doc trovate - la cosa ha senso perché cerco in uppercase e
            // qualche utente geniale potrebbe aver definito due tipologie differenti solo
            // nel "casing" del nome
            if (tipoDocS.size() > 1) {
                rispostaControlli.setCodErr(MessaggiWSBundle.DOC_001_003);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DOC_001_003, descChiaveDoc, nomeTipoDoc));
                return rispostaControlli;
            }

            DecTipoDoc td = tipoDocS.get(0);

            // tipo documento non ammissibile per il documento principale
            if (documentoPrinc && (td.getFlTipoDocPrincipale() == null || td.getFlTipoDocPrincipale().equals("0"))) {
                rispostaControlli.setCodErr(MessaggiWSBundle.DOC_001_002);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DOC_001_002, descChiaveDoc, nomeTipoDoc));
                return rispostaControlli;
            }
            rispostaControlli.setrLong(td.getIdTipoDoc());
            rispostaControlli.setrBoolean(true);

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkTipoDocumento: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkTipoStruttura(String nomeTipoStruttura, long idStruttura, String descChiaveDoc) {

        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        rispostaControlli.setrBoolean(false);

        // lista entity JPA ritornate dalle Query
        List<DecTipoStrutDoc> decTipoStrutDocS = null;

        // lancio query di controllo
        try {

            // ricavo i record in base ai parametri impostati
            String queryStr = "select tsd " + "from DecTipoStrutDoc tsd " + "where tsd.orgStrut.idStrut = :idStrutIn "
                    + " and tsd.nmTipoStrutDoc = :nmTipoStrutDocIn " + " and tsd.dtIstituz <= :dataDiOggiIn "
                    + " and tsd.dtSoppres > :dataDiOggiIn "; // da notare STRETTAMENTE MAGGIORE della data di
            // riferimento!!!

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecTipoStrutDoc.class);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("nmTipoStrutDocIn", nomeTipoStruttura);
            query.setParameter("dataDiOggiIn", new Date());
            decTipoStrutDocS = query.getResultList();

            // tipo struttura documento assente
            if (decTipoStrutDocS.isEmpty()) {
                // "Tipo struttura documento non presente per la determinata struttura"
                rispostaControlli.setCodErr(MessaggiWSBundle.DOC_005_001);
                rispostaControlli.setDsErr(
                        MessaggiWSBundle.getString(MessaggiWSBundle.DOC_005_001, descChiaveDoc, nomeTipoStruttura));
                return rispostaControlli;
            }

            for (DecTipoStrutDoc tsd : decTipoStrutDocS) {
                rispostaControlli.setrLong(tsd.getIdTipoStrutDoc());
                rispostaControlli.setrString(tsd.getNmTipoStrutDoc());
            }

            rispostaControlli.setrBoolean(true);

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkTipoStruttura: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkTipoComponente(String nomeTipoComponente, long idTipoStrutDoc,
            TipologieComponente tComp, String descChiaveComp) {

        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        rispostaControlli.setrBoolean(false);

        // lista entity JPA ritornate dalle Query
        List<DecTipoCompDoc> decTipoCompDocS = null;

        // lancio query di controllo
        try {
            // ricavo i record in base ai parametri impostati
            String queryStr = "select tcd " + "from DecTipoCompDoc tcd "
                    + "where tcd.decTipoStrutDoc.idTipoStrutDoc = :idTipoStrutDocIn "
                    + " and tcd.nmTipoCompDoc = :nmTipoCompDocIn " + " and tcd.dtIstituz <= :dataDiOggiIn "
                    + " and tcd.dtSoppres > :dataDiOggiIn "; // da notare STRETTAMENTE MAGGIORE della data di
            // riferimento!!!

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecTipoCompDoc.class);
            query.setParameter("idTipoStrutDocIn", idTipoStrutDoc);
            query.setParameter("nmTipoCompDocIn", nomeTipoComponente);
            query.setParameter("dataDiOggiIn", new Date());
            decTipoCompDocS = query.getResultList();

            // E' UN COMPONENTE
            if (tComp == TipologieComponente.COMPONENTE) {
                // tipo componente assente
                if (decTipoCompDocS.isEmpty()) {
                    // "Tipo componente non definito per il tipo struttura"
                    rispostaControlli.setCodErr(MessaggiWSBundle.COMP_001_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.COMP_001_001, descChiaveComp,
                            nomeTipoComponente));
                    return rispostaControlli;
                }
                // tipo componente presente
                for (DecTipoCompDoc tcd : decTipoCompDocS) {
                    rispostaControlli.setrLong(tcd.getIdTipoCompDoc());
                    rispostaControlli.setrString(tcd.getTiUsoCompDoc());
                    if (!tcd.getTiUsoCompDoc().equals(CostantiDB.TipoUsoComponente.CONTENUTO)
                            && !tcd.getTiUsoCompDoc().equals(CostantiDB.TipoUsoComponente.CONVERTITORE)) {
                        // "Il tipo componente non ha tipo di uso pari a Contenuto o Convertitore"
                        rispostaControlli.setCodErr(MessaggiWSBundle.COMP_001_002);
                        rispostaControlli
                                .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.COMP_001_002, descChiaveComp));
                        return rispostaControlli;
                    }
                }
                rispostaControlli.setrBoolean(true);
            } else { // E' UN SOTTOCOMPONENTE
                // tipo sottocomponente assente
                if (decTipoCompDocS.isEmpty()) {
                    // "Tipo componente non definito per il tipo struttura"
                    rispostaControlli.setCodErr(MessaggiWSBundle.SUBCOMP_001_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.SUBCOMP_001_001,
                            descChiaveComp, nomeTipoComponente));
                    return rispostaControlli;
                }
                // tipo sottocomponente presente
                for (DecTipoCompDoc tcd : decTipoCompDocS) {
                    rispostaControlli.setrLong(tcd.getIdTipoCompDoc());
                    rispostaControlli.setrString(tcd.getTiUsoCompDoc());
                    if (!tcd.getTiUsoCompDoc().equals(CostantiDB.TipoUsoComponente.FIRMA)
                            && !tcd.getTiUsoCompDoc().equals(CostantiDB.TipoUsoComponente.MARCA)
                            && !tcd.getTiUsoCompDoc().equals(CostantiDB.TipoUsoComponente.CONVERTITORE)
                            && !tcd.getTiUsoCompDoc().equals(CostantiDB.TipoUsoComponente.RAPPRESENTAZIONE)
                            && !tcd.getTiUsoCompDoc().equals(CostantiDB.TipoUsoComponente.SEGNATURA)
                            && !tcd.getTiUsoCompDoc().equals(CostantiDB.TipoUsoComponente.FIRMA_ELETTRONICA)) {
                        // Il tipo componente non ha tipo di uso corretto
                        rispostaControlli.setCodErr(MessaggiWSBundle.SUBCOMP_001_002);
                        rispostaControlli
                                .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.SUBCOMP_001_002, descChiaveComp));
                        return rispostaControlli;
                    }
                }
                rispostaControlli.setrBoolean(true);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkTipoComponente: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkTipoRappComponente(String nomeTipoRapprComponente, long idStruttura,
            String descChiaveComp) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        rispostaControlli.setrBoolean(false);
        // lista entity JPA ritornate dalle Query
        List<DecTipoRapprComp> decTipoRapprCompS = null;
        // lancio query di controllo
        try {
            // ricavo i record in base ai parametri impostati
            String queryStr = "select trc " + "from DecTipoRapprComp trc " + "where trc.orgStrut.idStrut = :idStrutIn "
                    + " and trc.nmTipoRapprComp = :nmTipoRapprCompIn " + " and trc.dtIstituz <= :dataDiOggiIn "
                    + " and trc.dtSoppres > :dataDiOggiIn "; // da notare STRETTAMENTE MAGGIORE della data di
            // riferimento!!!

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecTipoRapprComp.class);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("nmTipoRapprCompIn", nomeTipoRapprComponente);
            query.setParameter("dataDiOggiIn", new Date());
            decTipoRapprCompS = query.getResultList();
            // tipo rappresentazione componente assente

            if (decTipoRapprCompS.isEmpty()) {
                // "Tipo rappresentazione componente non presente per la determinata struttura"
                rispostaControlli.setCodErr(MessaggiWSBundle.COMP_003_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.COMP_003_001, descChiaveComp,
                        nomeTipoRapprComponente));
                return rispostaControlli;
            }

            rispostaControlli.setrObject(decTipoRapprCompS.get(0));
            rispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkTipoRappComponente: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkPresenzaDatiSpec(TipiUsoDatiSpec tiUsoXsd, TipiEntitaSacer tipoEntitySacer,
            String sistemaMig, long idStruttura, long idTipoEntitySacer, Date dataRiferimento) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        Long numXsd;

        try {
            if (tiUsoXsd == TipiUsoDatiSpec.MIGRAZ) {
                String queryStr = "select count(t) from DecXsdDatiSpec t " + "where t.orgStrut.idStrut = :idStrutIn "
                        + " and t.tiUsoXsd = :tiUsoXsdIn " + " and t.nmSistemaMigraz = :nmSistemaMigrazIn "
                        + " and t.tiEntitaSacer = :tiEntitaSacerIn " + " and t.dtIstituz <= :dataRifIn "
                        + " and t.dtSoppres > :dataRifIn ";
                // da notare STRETTAMENTE MAGGIORE della data di riferimento!!!

                javax.persistence.Query query = entityManager.createQuery(queryStr);
                query.setParameter("idStrutIn", idStruttura);
                query.setParameter("tiUsoXsdIn", tiUsoXsd.name());
                query.setParameter("nmSistemaMigrazIn", sistemaMig);
                query.setParameter("tiEntitaSacerIn", tipoEntitySacer.name());
                query.setParameter("dataRifIn", dataRiferimento);

                numXsd = (Long) query.getSingleResult();
                if (numXsd > 0) {
                    rispostaControlli.setrBoolean(true);
                }
                rispostaControlli.setrLong(numXsd);
            } else {
                String tmpTipoEntita = null;
                switch (tipoEntitySacer) {
                case UNI_DOC:
                    tmpTipoEntita = "t.decTipoUnitaDoc.idTipoUnitaDoc";
                    break;
                case DOC:
                    tmpTipoEntita = "t.decTipoDoc.idTipoDoc";
                    break;
                case COMP:
                case SUB_COMP:
                    tmpTipoEntita = "t.decTipoCompDoc.idTipoCompDoc";
                    break;
                }

                String queryStr = String.format(
                        "select count(t) from DecXsdDatiSpec t " + "where t.orgStrut.idStrut = :idStrutIn "
                                + " and t.tiUsoXsd = :tiUsoXsdIn " + " and %s = :idTipoEntitySacerIn "
                                + " and t.tiEntitaSacer = :tiEntitaSacerIn " + " and t.dtIstituz <= :dataRifIn "
                                + " and t.dtSoppres > :dataRifIn ",
                        // da notare STRETTAMENTE MAGGIORE della data di riferimento!!!
                        tmpTipoEntita);

                javax.persistence.Query query = entityManager.createQuery(queryStr, Long.class);
                query.setParameter("idStrutIn", idStruttura);
                query.setParameter("tiUsoXsdIn", tiUsoXsd.name());
                query.setParameter("idTipoEntitySacerIn", idTipoEntitySacer);
                query.setParameter("tiEntitaSacerIn", tipoEntitySacer.name());
                query.setParameter("dataRifIn", dataRiferimento);

                numXsd = (Long) query.getSingleResult();
                if (numXsd > 0) {
                    rispostaControlli.setrBoolean(true);
                }
                rispostaControlli.setrLong(numXsd);
            }

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkPresenzaDatiSpec: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkXSDDatiSpec(TipiUsoDatiSpec tiUsoXsd, TipiEntitaSacer tipoEntitySacer,
            String sistemaMig, long idStruttura, long idTipoEntitySacer, Date dataRiferimento, String versioneXsd) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        List<DecXsdDatiSpec> lstXsdDatiSpecs = null;

        try {
            if (tiUsoXsd == TipiUsoDatiSpec.MIGRAZ) {
                String queryStr = "select t from DecXsdDatiSpec t " + "where t.orgStrut.idStrut = :idStrutIn "
                        + " and t.tiUsoXsd = :tiUsoXsdIn " + " and t.nmSistemaMigraz = :nmSistemaMigrazIn "
                        + " and t.tiEntitaSacer = :tiEntitaSacerIn " + " and t.cdVersioneXsd = :cdVersioneXsdIn "
                        + " and t.dtIstituz <= :dataRifIn " + " and t.dtSoppres > :dataRifIn ";
                // da notare STRETTAMENTE MAGGIORE della data di riferimento!!!

                javax.persistence.Query query = entityManager.createQuery(queryStr, DecXsdDatiSpec.class);
                query.setParameter("idStrutIn", idStruttura);
                query.setParameter("tiUsoXsdIn", tiUsoXsd.name());
                query.setParameter("nmSistemaMigrazIn", sistemaMig);
                query.setParameter("tiEntitaSacerIn", tipoEntitySacer.name());
                query.setParameter("cdVersioneXsdIn", versioneXsd);
                query.setParameter("dataRifIn", dataRiferimento);
                lstXsdDatiSpecs = query.getResultList();

                for (DecXsdDatiSpec td : lstXsdDatiSpecs) {
                    rispostaControlli.setrString(td.getBlXsd());
                    rispostaControlli.setrDate(td.getDtIstituz());
                    rispostaControlli.setrLong(td.getIdXsdDatiSpec());
                }
            } else {
                String tmpTipoEntita = null;
                switch (tipoEntitySacer) {
                case UNI_DOC:
                    tmpTipoEntita = "t.decTipoUnitaDoc.idTipoUnitaDoc";
                    break;
                case DOC:
                    tmpTipoEntita = "t.decTipoDoc.idTipoDoc";
                    break;
                case COMP:
                case SUB_COMP:
                    tmpTipoEntita = "t.decTipoCompDoc.idTipoCompDoc";
                    break;
                }

                String queryStr = String.format(
                        "select t from DecXsdDatiSpec t " + "where t.orgStrut.idStrut = :idStrutIn "
                                + " and t.tiUsoXsd = :tiUsoXsdIn " + " and %s = :idTipoEntitySacerIn "
                                + " and t.tiEntitaSacer = :tiEntitaSacerIn "
                                + " and t.cdVersioneXsd = :cdVersioneXsdIn " + " and t.dtIstituz <= :dataRifIn "
                                + " and t.dtSoppres > :dataRifIn ",
                        // da notare STRETTAMENTE MAGGIORE della data di riferimento!!!
                        tmpTipoEntita);

                javax.persistence.Query query = entityManager.createQuery(queryStr, DecXsdDatiSpec.class);
                query.setParameter("idStrutIn", idStruttura);
                query.setParameter("tiUsoXsdIn", tiUsoXsd.name());
                query.setParameter("idTipoEntitySacerIn", idTipoEntitySacer);
                query.setParameter("tiEntitaSacerIn", tipoEntitySacer.name());
                query.setParameter("cdVersioneXsdIn", versioneXsd);
                query.setParameter("dataRifIn", dataRiferimento);
                lstXsdDatiSpecs = query.getResultList();

                for (DecXsdDatiSpec td : lstXsdDatiSpecs) {
                    rispostaControlli.setrString(td.getBlXsd());
                    rispostaControlli.setrDate(td.getDtIstituz());
                    rispostaControlli.setrLong(td.getIdXsdDatiSpec());
                }
            }

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkXSDDatiSpec: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkPresenzaVersioneDatiSpec(TipiUsoDatiSpec tiUsoXsd, TipiEntitaSacer tipoEntitySacer,
            String sistemaMig, long idStruttura, long idTipoEntitySacer, String versioneXsd) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        Long numXsd;

        try {
            if (tiUsoXsd == TipiUsoDatiSpec.MIGRAZ) {
                String queryStr = "select count(t) from DecXsdDatiSpec t " + "where t.orgStrut.idStrut = :idStrutIn "
                        + " and t.tiUsoXsd = :tiUsoXsdIn " + " and t.nmSistemaMigraz = :nmSistemaMigrazIn "
                        + " and t.tiEntitaSacer = :tiEntitaSacerIn " + " and t.cdVersioneXsd = :cdVersioneXsdIn ";

                javax.persistence.Query query = entityManager.createQuery(queryStr);
                query.setParameter("idStrutIn", idStruttura);
                query.setParameter("tiUsoXsdIn", tiUsoXsd.name());
                query.setParameter("nmSistemaMigrazIn", sistemaMig);
                query.setParameter("tiEntitaSacerIn", tipoEntitySacer.name());
                query.setParameter("cdVersioneXsdIn", versioneXsd);

                numXsd = (Long) query.getSingleResult();
                if (numXsd > 0) {
                    rispostaControlli.setrBoolean(true);
                }
                rispostaControlli.setrLong(numXsd);
            } else {
                String tmpTipoEntita = null;
                switch (tipoEntitySacer) {
                case UNI_DOC:
                    tmpTipoEntita = "t.decTipoUnitaDoc.idTipoUnitaDoc";
                    break;
                case DOC:
                    tmpTipoEntita = "t.decTipoDoc.idTipoDoc";
                    break;
                case COMP:
                case SUB_COMP:
                    tmpTipoEntita = "t.decTipoCompDoc.idTipoCompDoc";
                    break;
                }

                String queryStr = String.format("select count(t) from DecXsdDatiSpec t "
                        + "where t.orgStrut.idStrut = :idStrutIn " + " and t.tiUsoXsd = :tiUsoXsdIn "
                        + " and %s = :idTipoEntitySacerIn " + " and t.tiEntitaSacer = :tiEntitaSacerIn "
                        + " and t.cdVersioneXsd = :cdVersioneXsdIn ", tmpTipoEntita);

                javax.persistence.Query query = entityManager.createQuery(queryStr);
                query.setParameter("idStrutIn", idStruttura);
                query.setParameter("tiUsoXsdIn", tiUsoXsd.name());
                query.setParameter("idTipoEntitySacerIn", idTipoEntitySacer);
                query.setParameter("tiEntitaSacerIn", tipoEntitySacer.name());
                query.setParameter("cdVersioneXsdIn", versioneXsd);

                numXsd = (Long) query.getSingleResult();
                if (numXsd > 0) {
                    rispostaControlli.setrBoolean(true);
                }
                rispostaControlli.setrLong(numXsd);
            }

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkPresenzaVersioneDatiSpec: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlliAttSpec checkDatiSpecifici(TipiUsoDatiSpec tipoUsoAttr, TipiEntitaSacer tipoEntitySacer,
            String sistemaMig, long idStruttura, long idTipoEntitySacer, long idXsdDatiSpec) {
        RispostaControlliAttSpec rispostaControlli;
        rispostaControlli = new RispostaControlliAttSpec();
        rispostaControlli.setrBoolean(false);
        rispostaControlli.setDatiSpecifici(new LinkedHashMap<>());
        DatoSpecifico tmpDatoSpecifico;
        List<DecAttribDatiSpec> lstAttribDatiSpecs = null;

        try {
            if (tipoUsoAttr == TipiUsoDatiSpec.MIGRAZ) {
                String queryStr = "select t from DecAttribDatiSpec t " + "join t.decXsdAttribDatiSpecs attXsd "
                        + "where t.orgStrut.idStrut = :idStrutIn " + " and t.tiUsoAttrib = :tiUsoAttribIn "
                        + " and t.nmSistemaMigraz = :nmSistemaMigrazIn " + " and t.tiEntitaSacer = :tiEntitaSacerIn "
                        + " and attxsd.decXsdDatiSpec.idXsdDatiSpec = :idXsdDatiSpecIn ";

                javax.persistence.Query query = entityManager.createQuery(queryStr, DecAttribDatiSpec.class);
                query.setParameter("idStrutIn", idStruttura);
                query.setParameter("tiUsoAttribIn", tipoUsoAttr.name());
                query.setParameter("nmSistemaMigrazIn", sistemaMig);
                query.setParameter("tiEntitaSacerIn", tipoEntitySacer.name());
                query.setParameter("idXsdDatiSpecIn", idXsdDatiSpec);
                lstAttribDatiSpecs = query.getResultList();

                if (!lstAttribDatiSpecs.isEmpty()) {
                    rispostaControlli.setrBoolean(true);
                    for (DecAttribDatiSpec td : lstAttribDatiSpecs) {
                        tmpDatoSpecifico = new DatoSpecifico();
                        tmpDatoSpecifico.setChiave(td.getNmAttribDatiSpec().trim());
                        // uso trim() per gestire il caso in cui in tabella ci siano degli spazi
                        // all'inizio o alla fine del nome (va confrontato con un tag XML, che
                        // per definizione non ha spazi)
                        tmpDatoSpecifico.setIdDatoSpec(td.getIdAttribDatiSpec());
                        rispostaControlli.getDatiSpecifici().put(td.getNmAttribDatiSpec().trim(), tmpDatoSpecifico);
                    }
                }
            } else {
                String tmpTipoEntita = null;
                switch (tipoEntitySacer) {
                case UNI_DOC:
                    tmpTipoEntita = "t.decTipoUnitaDoc.idTipoUnitaDoc";
                    break;
                case DOC:
                    tmpTipoEntita = "t.decTipoDoc.idTipoDoc";
                    break;
                case COMP:
                case SUB_COMP:
                    tmpTipoEntita = "t.decTipoCompDoc.idTipoCompDoc";
                    break;
                }

                String queryStr = String
                        .format("select t from DecAttribDatiSpec t " + "join t.decXsdAttribDatiSpecs attXsd "
                                + "where t.orgStrut.idStrut = :idStrutIn " + " and t.tiUsoAttrib = :tiUsoAttribIn "
                                + " and %s = :idTipoEntitySacerIn " + " and t.tiEntitaSacer = :tiEntitaSacerIn "
                                + " and attxsd.decXsdDatiSpec.idXsdDatiSpec = :idXsdDatiSpecIn ", tmpTipoEntita);

                javax.persistence.Query query = entityManager.createQuery(queryStr, DecAttribDatiSpec.class);
                query.setParameter("idStrutIn", idStruttura);
                query.setParameter("tiUsoAttribIn", tipoUsoAttr.name());
                query.setParameter("idTipoEntitySacerIn", idTipoEntitySacer);
                query.setParameter("tiEntitaSacerIn", tipoEntitySacer.name());
                query.setParameter("idXsdDatiSpecIn", idXsdDatiSpec);
                lstAttribDatiSpecs = query.getResultList();

                if (!lstAttribDatiSpecs.isEmpty()) {
                    rispostaControlli.setrBoolean(true);
                    for (DecAttribDatiSpec td : lstAttribDatiSpecs) {
                        tmpDatoSpecifico = new DatoSpecifico();
                        tmpDatoSpecifico.setChiave(td.getNmAttribDatiSpec());
                        tmpDatoSpecifico.setIdDatoSpec(td.getIdAttribDatiSpec());
                        rispostaControlli.getDatiSpecifici().put(td.getNmAttribDatiSpec(), tmpDatoSpecifico);
                    }
                }
            }

        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkDatiSpecifici: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkRiferimentoUD(CSChiave key, long idStrutturaVersante, TipologieComponente tComp,
            String descChiaveComp) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        // prendo i paramentri dell'xml
        String numero = key.getNumero();
        Long anno = key.getAnno();
        String tipoReg = key.getTipoRegistro();
        List<AroCompDoc> aroCompDocs = null;

        // lancio query di controllo
        try {
            // ricavo i comp doc presenti in base ai parametri impostati
            String queryStr = "select comp_doc " + "from AroUnitaDoc ud " + "join ud.aroDocs doc "
                    + "join doc.aroStrutDocs strut_doc " + "join strut_doc.aroCompDocs comp_doc " + "where "
                    + " trim(doc.tiDoc) = 'PRINCIPALE' " + " and strut_doc.flStrutOrig = '1' "
                    + " and ud.orgStrut.idStrut = :idStrutIn " + " and ud.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
                    + " and ud.aaKeyUnitaDoc = :aaKeyUnitaDocIn "
                    + " and ud.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn " + " and ud.dtAnnul > :dataDiOggiIn"
                    + " and comp_doc.tiSupportoComp = 'FILE'";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idStrutIn", idStrutturaVersante);
            query.setParameter("cdRegistroKeyUnitaDocIn", tipoReg);
            query.setParameter("aaKeyUnitaDocIn", new BigDecimal(anno));
            query.setParameter("cdKeyUnitaDocIn", numero);
            query.setParameter("dataDiOggiIn", new Date());

            aroCompDocs = query.getResultList();
            if (aroCompDocs == null || aroCompDocs.isEmpty()) {
                if (tComp == TipologieComponente.COMPONENTE) {
                    // Nel documento PRINCIPALE dell?unitï¿½ documentaria indicata dal riferimento,
                    // non sono stati individuati componenti con tipo supporto uguale a FILE
                    rispostaControlli.setCodErr(MessaggiWSBundle.COMP_008_005);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.COMP_008_005, descChiaveComp,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                } else {
                    rispostaControlli.setCodErr(MessaggiWSBundle.SUBCOMP_006_005);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.SUBCOMP_006_005,
                            descChiaveComp, MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                }
                return rispostaControlli;
            } else if (aroCompDocs.size() > 1) {
                if (tComp == TipologieComponente.COMPONENTE) {
                    // Componente {0}: il numero di componenti del documento principale della UD di
                    // Riferimento {1} non puï¿½ essere maggiore di 1
                    rispostaControlli.setCodErr(MessaggiWSBundle.COMP_008_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.COMP_008_001, descChiaveComp,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                } else {
                    rispostaControlli.setCodErr(MessaggiWSBundle.SUBCOMP_006_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.SUBCOMP_006_001,
                            descChiaveComp, MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                }
                return rispostaControlli;
            } else {
                String tipoComp = aroCompDocs.get(0).getDecTipoCompDoc().getTiUsoCompDoc();
                if (!tipoComp.equals(CostantiDB.TipoUsoComponente.CONVERTITORE)) {
                    if (tComp == TipologieComponente.COMPONENTE) {
                        // Componente {0}: il componente del documento principale della UD di
                        // Riferimento {1} ï¿½ di tipo diverso da "CONVERTITORE"
                        rispostaControlli.setCodErr(MessaggiWSBundle.COMP_008_002);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.COMP_008_002,
                                descChiaveComp, MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                    } else {
                        rispostaControlli.setCodErr(MessaggiWSBundle.SUBCOMP_006_002);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.SUBCOMP_006_002,
                                descChiaveComp, MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                    }
                    return rispostaControlli;
                }
                String tipoSupporto = aroCompDocs.get(0).getAroStrutDoc().getAroDoc().getAroUnitaDoc()
                        .getDecTipoUnitaDoc().getTiSaveFile();
                if (!tipoSupporto.equals(CostantiDB.TipoSalvataggioFile.BLOB.name())) {
                    if (tComp == TipologieComponente.COMPONENTE) {
                        // L'unitï¿½ documentaria del convertitore indicato dal riferimento deve essere
                        // salvata su BLOB
                        rispostaControlli.setCodErr(MessaggiWSBundle.COMP_008_004);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.COMP_008_004,
                                descChiaveComp, MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                    } else {
                        rispostaControlli.setCodErr(MessaggiWSBundle.SUBCOMP_006_004);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.SUBCOMP_006_004,
                                descChiaveComp, MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                    }
                    return rispostaControlli;
                }
                rispostaControlli.setrLong(aroCompDocs.get(0).getDecFormatoFileDoc().getIdFormatoFileDoc());
                rispostaControlli.setrBoolean(true);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkRiferimentoUD: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }
        return rispostaControlli;
    }

    public RispostaControlli checkFormatoFileVersato(String nomeFormatoFile, long idStrut, long idTipoCompDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        if (nomeFormatoFile == null || nomeFormatoFile.isEmpty()) {
            return rispostaControlli;
        }

        // lista entity JPA ritornate dalle Query
        List<DecFormatoFileDoc> formatoFileDocS = null;

        // lancio query di controllo
        try {
            // ricavo le ud presenti in base ai parametri impostati
            String queryStr = "select ff " + "from DecFormatoFileDoc ff " + "join ff.decFormatoFileAmmessos fAmmessi "
                    + "where UPPER(ff.nmFormatoFileDoc) = :nmFormatoFileDocIn "
                    + "and fammessi.decTipoCompDoc.idTipoCompDoc = :idTipoCompDocIn "
                    + "and ff.orgStrut.idStrut = :idStrutIn " + " and ff.dtIstituz <= :dataDiOggiIn "
                    + " and ff.dtSoppres > :dataDiOggiIn "; // da notare STRETTAMENTE MAGGIORE della data di
            // riferimento!!!

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecFormatoFileDoc.class);
            query.setParameter("nmFormatoFileDocIn", nomeFormatoFile.toUpperCase());
            query.setParameter("idTipoCompDocIn", idTipoCompDoc);
            query.setParameter("idStrutIn", idStrut);
            query.setParameter("dataDiOggiIn", new Date());

            formatoFileDocS = query.getResultList();

            if (formatoFileDocS.isEmpty()) {
                return rispostaControlli;
            }
            for (DecFormatoFileDoc ff : formatoFileDocS) {
                rispostaControlli.setrLong(ff.getIdFormatoFileDoc());
            }

            rispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkFormatoFileVersato: " + e.getMessage()));
            log.error("Eccezione nel check checkFormatoFileVersato ", e);
        }

        return rispostaControlli;
    }

    /*
     * metodi usati dall'aggiunta allegati
     */
    public RispostaControlli checkDocumentoInUd(long idUnitaDoc, String idDocumento, String descUd) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);
        // lista entity JPA ritornate dalle Query
        List<AroDoc> aroDocs = null;

        try {
            String queryStr = "select doc " + "from AroDoc doc " + "where doc.aroUnitaDoc.idUnitaDoc = :idUnitaDocIn "
                    + "and doc.cdKeyDocVers = :idDocumento ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idUnitaDocIn", idUnitaDoc);
            query.setParameter("idDocumento", idDocumento);

            aroDocs = query.getResultList();
            if (aroDocs == null || aroDocs.isEmpty()) {
                rispostaControlli.setrLong(1);
                rispostaControlli.setrBoolean(true);
            } else {
                // Documento giï¿½ presente nell'Unitï¿½ Documentaria, recupero l'id e l'urn del
                // documento
                rispostaControlli.setrLong(aroDocs.get(0).getIdDoc());
                rispostaControlli.setrString(MessaggiWSFormat.formattaUrnPartDocumento(
                        Costanti.CategoriaDocumento.getEnum(aroDocs.get(0).getTiDoc()),
                        aroDocs.get(0).getPgDoc().intValue()));
                rispostaControlli.setCodErr(MessaggiWSBundle.DOC_008_001);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DOC_008_001, idDocumento, descUd));
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkDuplicatiInUD: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella dei documenti ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli trovaNuovoProgDocumento(long idUnitaDoc, String categoriaDocumento) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);
        BigDecimal max = null;
        // lista entity JPA ritornate dalle Query

        // lancio query di controllo
        try {
            String queryStr = "select max(doc.pgDoc) " + "from AroDoc doc "
                    + "where doc.aroUnitaDoc.idUnitaDoc = :idUnitaDocIn " + "and doc.tiDoc = :tiDocIn ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idUnitaDocIn", idUnitaDoc);
            query.setParameter("tiDocIn", categoriaDocumento);

            max = (BigDecimal) query.getSingleResult();
            if (max != null) {
                rispostaControlli.setrLong(max.longValue() + 1);
            } else {
                rispostaControlli.setrLong(1);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.trovaNuovoProgDocumento: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella dei documenti ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli trovaNuovoNiOrdDocumento(long idUnitaDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        // lancio query di controllo
        try {
            String queryStr = "select max(doc.niOrdDoc) " + "from AroDoc doc "
                    + "where doc.aroUnitaDoc.idUnitaDoc = :idUnitaDocIn ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idUnitaDocIn", idUnitaDoc);

            BigDecimal max = (BigDecimal) query.getSingleResult();
            if (max != null) {
                rispostaControlli.setrLong(max.longValue() + 1);
            } else {
                // se non esiste un niOrdDoc si calcola sulla base del totale dei documenti
                // per il calcolo dell'URN del nuovo documento
                queryStr = "select count(doc.idDoc) " + "from AroDoc doc "
                        + "where doc.aroUnitaDoc.idUnitaDoc = :idUnitaDocIn ";

                query = entityManager.createQuery(queryStr);
                query.setParameter("idUnitaDocIn", idUnitaDoc);

                Long count = (Long) query.getSingleResult();

                if (count != null) {
                    rispostaControlli.setrLong(count.longValue() + 1);
                } else {
                    rispostaControlli.setrLong(1);
                }
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.trovaNuovoNiOrdDocumento: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella dei documenti ", e);
        }

        return rispostaControlli;
    }

    /*
     * metodi usati dal versamentoMM
     */
    public RispostaControlli checkFormatoFileStandard(String nomeFormatoFile) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();

        // lista entity JPA ritornate dalle Query
        List<DecFormatoFileStandard> formatoFileStdS = null;

        // lancio query di controllo
        try {
            // ricavo le ud presenti in base ai parametri impostati
            String queryStr = "select ff " + "from DecFormatoFileStandard ff "
                    + "where UPPER(ff.nmFormatoFileStandard) = :nmFormatoFileStdIn ";

            javax.persistence.Query query = entityManager.createQuery(queryStr, DecFormatoFileStandard.class);
            query.setParameter("nmFormatoFileStdIn", nomeFormatoFile != null ? nomeFormatoFile.toUpperCase() : null);

            formatoFileStdS = query.getResultList();

            if (formatoFileStdS.isEmpty()) {
                return rispostaControlli;
            }
            for (DecFormatoFileStandard ff : formatoFileStdS) {
                rispostaControlli.setrLong(ff.getIdFormatoFileStandard());
            }

            rispostaControlli.setrBoolean(true);
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkFormatoFileStandard: " + e.getMessage()));
            log.error("Eccezione nel check checkFormatoFileStandard ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli calcAndcheckCdKeyNormalized(long idRegistro, CSChiave key, String cdKeyUnitaDocNormaliz) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        try {
            // check esistenza
            rispostaControlli = this.checkCdKeyNormalized(idRegistro, key, cdKeyUnitaDocNormaliz);
            // 666 error
            if (rispostaControlli.getCodErr() != null) {
                return rispostaControlli;
            }
            if (rispostaControlli.getrLong() == 0/* esiste */) {
                // aggiungere _ infondo e richiamare ricorsivamente lo stesso metodo fino a che
                // la condizione di uscita restituisce l'urn part normalizzato corretto
                cdKeyUnitaDocNormaliz = cdKeyUnitaDocNormaliz.concat("_");
                // chiamata su metodo ricorsiva
                rispostaControlli = this.calcAndcheckCdKeyNormalized(idRegistro, key, cdKeyUnitaDocNormaliz);
            } else {
                // condizione di uscita da chiamata ricorsiva
                // setta la chiave calcolata per utilizzarla dal chiamante
                rispostaControlli.setrString(cdKeyUnitaDocNormaliz);// default
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.calcAndcheckPresenzaUDNormalized: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    private RispostaControlli checkCdKeyNormalized(long idRegistro, CSChiave key, String cdKeyUnitaDocNormaliz) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        Long num;
        try {
            String queryStr = "select count(ud) from AroUnitaDoc ud "
                    + "where ud.decRegistroUnitaDoc.idRegistroUnitaDoc = :idRegistro "
                    + " and ud.aaKeyUnitaDoc = :aaKeyUnitaDoc " + " and ud.cdKeyUnitaDoc != :cdKeyUnitaDoc "
                    + " and ud.cdKeyUnitaDocNormaliz = :cdKeyUnitaDocNormaliz ";

            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idRegistro", idRegistro);
            query.setParameter("aaKeyUnitaDoc", HibernateUtils.bigDecimalFrom(key.getAnno()));
            query.setParameter("cdKeyUnitaDoc", key.getNumero());
            query.setParameter("cdKeyUnitaDocNormaliz", cdKeyUnitaDocNormaliz);

            num = (Long) query.getSingleResult();
            if (num > 0) {
                // esiste chiave normalized
                rispostaControlli.setrLong(0);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.checkCdKeyNormalized: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkUniqueCdKeyNormalized(CSChiave chiave, long idRegistro, long idUnitaDoc,
            String cdKeyNormalized, Date dtInizioCalcoloNewUrn) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        try {

            rispostaControlli = this.getDtMaxVersMaxByUd(idUnitaDoc);
            if (rispostaControlli.getrLong() != -1) {
                // dtVersMax ottenuto da vista
                Date dtVersMax = rispostaControlli.getrDate();
                // controllo : dtVersMax <= dataInizioCalcNuoviUrn
                if ((dtVersMax.before(dtInizioCalcoloNewUrn) || dtVersMax.equals(dtInizioCalcoloNewUrn))
                        && StringUtils.isBlank(cdKeyNormalized)) {
                    // calcola e verifica la chiave normalizzata
                    cdKeyNormalized = MessaggiWSFormat.normalizingKey(chiave.getNumero()); // base
                    rispostaControlli = this.checkCdKeyNormalized(idRegistro, chiave, cdKeyNormalized);
                    // 666 error
                    if (rispostaControlli.getCodErr() != null) {
                        return rispostaControlli;
                    }
                    if (rispostaControlli.getrLong() == 0 /* esiste chiave normalizzata */) {
                        // Esiste key normalized
                        rispostaControlli.setCodErr(MessaggiWSBundle.UD_005_005);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_005_005,
                                MessaggiWSFormat.formattaUrnPartUnitaDoc(chiave)));
                        rispostaControlli.setrBoolean(true);
                    } else {
                        rispostaControlli.setrDate(dtVersMax);
                        rispostaControlli.setrString(cdKeyNormalized);
                    }
                }
            }
        } catch (Exception e) {
            rispostaControlli.setrBoolean(false);
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Eccezione ControlliUpdVersamento.checkUrnNormalized " + e.getMessage()));
            log.error("Eccezione nella verifica esistenza URN normalizzato ", e);
        }
        return rispostaControlli;
    }

    private RispostaControlli getDtMaxVersMaxByUd(long idUnitaDoc) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        AroVDtVersMaxByUnitaDoc aroVDtVersMaxByUnitaDoc = null;

        try {
            Query query = entityManager
                    .createQuery("SELECT aro FROM AroVDtVersMaxByUnitaDoc aro WHERE aro.idUnitaDoc = :idUnitaDoc ");
            query.setParameter("idUnitaDoc", HibernateUtils.bigDecimalFrom(idUnitaDoc));
            aroVDtVersMaxByUnitaDoc = (AroVDtVersMaxByUnitaDoc) query.getSingleResult();
            if (aroVDtVersMaxByUnitaDoc == null) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
                rispostaControlli.setDsErr("ControlliSemantici.getDtMaxVersMaxByUd: non presente per id " + idUnitaDoc);
            } else {
                rispostaControlli.setrLong(0);
                rispostaControlli.setrDate(aroVDtVersMaxByUnitaDoc.getDtVersMax());
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.getDtMaxVersMaxByUd: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }
        return rispostaControlli;
    }

    public RispostaControlli getDtCalcInizioNuoviUrn() {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        try {
            // recupero parametro
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date dtInizioCalcNuoviUrn = dateFormat
                    .parse(config.getValoreParamApplicByApplic(ParametroApplDB.DATA_INIZIO_CALC_NUOVI_URN));

            rispostaControlli.setrDate(dtInizioCalcNuoviUrn);
        } catch (Exception e) {
            rispostaControlli.setrBoolean(true);
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemantici.getDtCalcInizioNuoviUrn: " + e.getMessage()));
            log.error("Eccezione nella lettura della tabella di decodifica ", e);
        }
        return rispostaControlli;
    }

}
