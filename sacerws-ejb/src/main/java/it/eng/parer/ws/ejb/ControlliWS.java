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
package it.eng.parer.ws.ejb;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.IamAbilTipoDato;
import it.eng.parer.entity.IamUser;
import it.eng.parer.granted_entity.UsrUser;
import it.eng.parer.idpjaas.logutils.LogDto;
import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.ParametroApplDB.TipoParametroAppl;
import it.eng.parer.ws.utils.VerificaVersione;
import it.eng.spagoLite.security.User;
import it.eng.spagoLite.security.auth.WSLoginHandler;
import it.eng.spagoLite.security.exception.AuthWSException;

/**
 *
 * @author Fioravanti_F
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "ControlliWS")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliWS {

    private static final String ERRORE_AUTENTICAZIONE = "Eccezione nella fase di autenticazione del EJB ";
    private static final String ERRORE_TABELLA_DECODIFICA = "Eccezione nella lettura della tabella di decodifica ";
    @EJB
    private WsIdpLogger idpLogger;

    @EJB
    private ControlliSemantici controlliSemantici;

    private static final Logger log = LoggerFactory.getLogger(ControlliWS.class);
    //
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public RispostaControlli checkVersione(String versione, String versioniWsKey,
            Map<String, String> xmlDefaults, TipiWSPerControlli tipows) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        if (versione == null || versione.isEmpty()) {
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
            case ANNULLAMENTO:
            case AGGIORNAMENTO_VERSAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_010);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_010));
                break;
            case VERSAMENTO_FASCICOLO:
                rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_003_001);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_003_001));
                break;
            }

            return rispostaControlli;
        }

        List<String> versioniWs = VerificaVersione.getWsVersionList(versioniWsKey, xmlDefaults);
        if (versioniWs.isEmpty()) {
            rispostaControlli.setCodErr(MessaggiWSBundle.UD_018_001);
            rispostaControlli.setDsErr(
                    MessaggiWSBundle.getString(MessaggiWSBundle.UD_018_001, versioniWsKey));
            return rispostaControlli;
        }

        for (String tmpString : versioniWs) {
            if (versione.equals(tmpString)) {
                rispostaControlli.setrBoolean(true);
            }
        }

        if (!rispostaControlli.isrBoolean()) {
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
            case AGGIORNAMENTO_VERSAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_011);
                rispostaControlli.setDsErr(
                        MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_011, versione));
                break;
            case ANNULLAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_003);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(
                        MessaggiWSBundle.RICH_ANN_VERS_003, StringUtils.join(versioniWs, ",")));
                break;
            case VERSAMENTO_FASCICOLO:
                rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_003_002);
                rispostaControlli.setDsErr(
                        MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_003_002, versione));
                break;
            }
        }

        return rispostaControlli;
    }

    public RispostaControlli checkCredenziali(String loginName, String password, String indirizzoIP,
            TipiWSPerControlli tipows) {
        User utente = null;
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        log.info("Indirizzo IP del chiamante - access: ws - IP: {}", indirizzoIP);

        if (loginName == null || loginName.isEmpty()) {
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
            case ANNULLAMENTO:
            case AGGIORNAMENTO_VERSAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_004);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_004));
                break;
            case VERSAMENTO_FASCICOLO:
                rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_007);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_007));
                break;
            }
            return rispostaControlli;
        }

        // preparazione del log del login
        LogDto tmpLogDto = new LogDto();
        tmpLogDto.setNmAttore("Sacer WS");
        tmpLogDto.setNmUser(loginName);
        tmpLogDto.setCdIndIpClient(indirizzoIP);
        tmpLogDto.setTsEvento(new Date());
        // nota, non imposto l'indirizzo del server, verrà letto dal singleton da WsIdpLogger

        try {
            WSLoginHandler.login(loginName, password, indirizzoIP, entityManager);
            // se l'autenticazione riesce, non va in eccezione.
            // passo quindi a leggere i dati dell'utente dal db
            IamUser iamUser;
            UsrUser usrUser;
            String queryStr = "select iu, usr from IamUser iu, UsrUser usr "
                    + "where iu.nmUserid = :nmUseridIn and iu.idUserIam = usr.idUserIam";
            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("nmUseridIn", loginName);
            Object[] result = (Object[]) query.getSingleResult();
            iamUser = (IamUser) result[0]; // IamUser
            usrUser = (UsrUser) result[1]; // UsrUser
            //
            String sistemaVersante = null;
            if (usrUser.getAplSistemaVersante() != null) {
                sistemaVersante = usrUser.getAplSistemaVersante().getNmSistemaVersante();
            }
            //
            utente = new User();
            utente.setUsername(loginName);
            utente.setIdUtente(iamUser.getIdUserIam());
            utente.setSistemaVersante(sistemaVersante);
            // log della corretta autenticazione
            tmpLogDto.setTipoEvento(LogDto.TipiEvento.LOGIN_OK);
            tmpLogDto.setDsEvento("WS, login OK");
            //
            rispostaControlli.setrObject(utente);
            rispostaControlli.setrBoolean(true);
        } catch (AuthWSException e) {
            log.warn(
                    "ERRORE DI AUTENTICAZIONE WS. Funzionalità: {} Utente: {} Tipo errore: {} Indirizzo IP: {} Descrizione: {}",
                    tipows.name(), loginName, e.getCodiceErrore().name(), indirizzoIP,
                    e.getDescrizioneErrore());
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
                if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_006);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_006, loginName));
                } else if (e.getCodiceErrore()
                        .equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_007);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_007, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_012);
                    rispostaControlli.setDsErr(MessaggiWSBundle
                            .getString(MessaggiWSBundle.UD_001_012, e.getDescrizioneErrore()));
                }
                break;
            case ANNULLAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_001);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_001));
                break;
            case VERSAMENTO_FASCICOLO:
                if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle
                            .getString(MessaggiWSBundle.FAS_CONFIG_002_002, loginName));
                } else if (e.getCodiceErrore()
                        .equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_004);
                    rispostaControlli.setDsErr(MessaggiWSBundle
                            .getString(MessaggiWSBundle.FAS_CONFIG_002_004, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_003);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(
                            MessaggiWSBundle.FAS_CONFIG_002_003, e.getDescrizioneErrore()));
                }
                break;
            case AGGIORNAMENTO_VERSAMENTO:
                if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_006);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_006, loginName));
                } else if (e.getCodiceErrore()
                        .equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_007);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_007, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_012);
                    rispostaControlli.setDsErr(MessaggiWSBundle
                            .getString(MessaggiWSBundle.UD_001_012, e.getDescrizioneErrore()));
                }
                break;

            }
            //
            // log dell'errore di autenticazione; ripeto la sequenza di if per chiarezza.
            // Per altro nel caso sia stato invocato il ws di annullamento, la distnizione
            // del tipo di errore non l'ho ancora eseguita.
            //
            if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                tmpLogDto.setTipoEvento(LogDto.TipiEvento.EXPIRED);
                tmpLogDto.setDsEvento("WS, " + e.getDescrizioneErrore());
            } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                tmpLogDto.setTipoEvento(LogDto.TipiEvento.LOCKED);
                tmpLogDto.setDsEvento("WS, " + e.getDescrizioneErrore());
            } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                // se l'autenticazione fallisce, devo capire se è stato sbagliata la password oppure
                // non esiste l'utente. Provo a caricarlo e verifico la cosa.
                String queryStr = "select count(iu) from IamUser iu where iu.nmUserid = :nmUseridIn";
                javax.persistence.Query query = entityManager.createQuery(queryStr);
                query.setParameter("nmUseridIn", loginName);
                long tmpNumUtenti = (long) query.getSingleResult();
                if (tmpNumUtenti > 0) {
                    tmpLogDto.setTipoEvento(LogDto.TipiEvento.BAD_PASS);
                    tmpLogDto.setDsEvento("WS, bad password");
                } else {
                    tmpLogDto.setTipoEvento(LogDto.TipiEvento.BAD_USER);
                    tmpLogDto.setDsEvento("WS, utente sconosciuto");
                }
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    ERRORE_AUTENTICAZIONE + e.getMessage()));
            log.error(ERRORE_AUTENTICAZIONE, e);
        }

        // scrittura log
        idpLogger.scriviLog(tmpLogDto);
        //
        return rispostaControlli;
    }

    public RispostaControlli checkUtente(String loginName) {
        User utente = null;
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        try {
            IamUser iamUser;
            String queryStr = "select iu from IamUser iu where iu.nmUserid = :nmUseridIn";
            javax.persistence.Query query = entityManager.createQuery(queryStr, IamUser.class);
            query.setParameter("nmUseridIn", loginName);
            List<IamUser> tmpUsers = query.getResultList();
            if (tmpUsers != null && !tmpUsers.isEmpty()) {
                iamUser = tmpUsers.get(0);

                if (!iamUser.getFlAttivo().equals("1")) {
                    // UTENTE_NON_ATTIVO
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_007);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_007, loginName));
                    return rispostaControlli;
                }
                if (iamUser.getDtScadPsw().before(new Date())) {
                    // UTENTE_SCADUTO
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_006);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_006, loginName));
                    return rispostaControlli;
                }
                //
                utente = new User();
                utente.setUsername(loginName);
                utente.setIdUtente(iamUser.getIdUserIam());
                rispostaControlli.setrObject(utente);
                rispostaControlli.setrBoolean(true);
            } else {
                // LOGIN_FALLITO
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_012);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_012,
                        String.format("l'utente %s non è censito nel sistema", loginName)));
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    ERRORE_AUTENTICAZIONE + e.getMessage()));
            log.error(ERRORE_AUTENTICAZIONE, e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkAuthWS(User utente, IWSDesc descrizione,
            TipiWSPerControlli tipows) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        boolean checkOrgVersAuth = false;
        Integer tmpIdOrganizz = utente.getIdOrganizzazioneFoglia() != null
                ? utente.getIdOrganizzazioneFoglia().intValue()
                : null;
        try {
            WSLoginHandler.checkAuthz(utente.getUsername(), tmpIdOrganizz, descrizione.getNomeWs(),
                    entityManager);
            rispostaControlli.setrBoolean(true);
        } catch (AuthWSException ex) {
            checkOrgVersAuth = true;
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
            case AGGIORNAMENTO_VERSAMENTO:
                // L''utente {0} non è abilitato entro la struttura versante
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_009);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_009,
                        utente.getUsername()));
                break;
            case ANNULLAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_008);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_008));
                break;
            case VERSAMENTO_FASCICOLO:
                rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_006);
                rispostaControlli.setDsErr(MessaggiWSBundle
                        .getString(MessaggiWSBundle.FAS_CONFIG_002_006, utente.getUsername()));
                break;
            }
        } catch (Exception ex) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Errore nella verifica delle autorizzazioni utente  " + ex.getMessage()));
            log.error("Errore nella verifica delle autorizzazioni utente ", ex);
        }

        if (checkOrgVersAuth) {
            try {
                String queryStr = "select count(t) from IamAbilOrganiz t where "
                        + "t.iamUser.idUserIam = :idUserIamIn "
                        + "and t.idOrganizApplic = :idOrganizApplicIn";
                TypedQuery<Long> query = entityManager.createQuery(queryStr, Long.class);
                query.setParameter("idUserIamIn", utente.getIdUtente());
                query.setParameter("idOrganizApplicIn", new BigDecimal(tmpIdOrganizz));
                Long numAbil = query.getSingleResult();
                if (numAbil != null && numAbil > 0) {
                    switch (tipows) {
                    case VERSAMENTO_RECUPERO:
                    case AGGIORNAMENTO_VERSAMENTO:
                        rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_008);
                        rispostaControlli
                                .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_008,
                                        utente.getUsername(), descrizione.getNomeWs()));
                        break;
                    case ANNULLAMENTO:
                        rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_008);
                        rispostaControlli.setDsErr(
                                MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_008));
                        break;
                    case VERSAMENTO_FASCICOLO:
                        rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_005);
                        rispostaControlli.setDsErr(
                                MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_005,
                                        utente.getUsername(), descrizione.getNomeWs()));
                        break;
                    }
                }
            } catch (Exception ex) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "Errore nella verifica delle autorizzazioni utente  " + ex.getMessage()));
                log.error("Errore nella verifica delle autorizzazioni utente ", ex);
            }
        }
        return rispostaControlli;
    }

    public RispostaControlli checkAuthWSNoOrg(User utente, IWSDesc descrizione,
            TipiWSPerControlli tipows) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        try {
            String querString = "select count(iu) from IamUser iu " + "JOIN iu.iamAbilOrganizs iao "
                    + "JOIN iao.iamAutorServs ias  " + "WHERE iu.nmUserid = :nmUserid  "
                    + "AND ias.nmServizioWeb = :servizioWeb";
            javax.persistence.Query query = entityManager.createQuery(querString);
            query.setParameter("nmUserid", utente.getUsername());
            query.setParameter("servizioWeb", descrizione.getNomeWs());
            long num = (long) query.getSingleResult();
            if (num > 0) {
                rispostaControlli.setrBoolean(true);
            } else {
                switch (tipows) {
                case VERSAMENTO_RECUPERO:
                case AGGIORNAMENTO_VERSAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_008);
                    rispostaControlli
                            .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_008,
                                    utente.getUsername(), descrizione.getNomeWs()));
                    break;
                case ANNULLAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_008);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_008));
                    break;
                case VERSAMENTO_FASCICOLO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_005);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_005,
                                    utente.getUsername(), descrizione.getNomeWs()));
                    break;
                }
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    ERRORE_AUTENTICAZIONE + e.getMessage()));
            log.error(ERRORE_AUTENTICAZIONE, e);
        }

        return rispostaControlli;
    }

    public RispostaControlli loadWsVersions(IWSDesc desc) {
        RispostaControlli rs = controlliSemantici.caricaDefaultDaDB(TipoParametroAppl.VERSIONI_WS);
        // if positive ...
        if (rs.isrBoolean()) {
            HashMap<String, String> wsVersions = (HashMap<String, String>) rs.getrObject();
            // verify if my version exits
            if (VerificaVersione.getWsVersionList(desc.getNomeWs(), wsVersions).isEmpty()) {
                rs.setrBoolean(false);
                rs.setCodErr(MessaggiWSBundle.UD_018_001);
                rs.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_018_001,
                        VerificaVersione.elabWsKey(desc.getNomeWs())));
            }
        }
        return rs;
    }

    public RispostaControlli checkAbilitazioniUtenteIamAbilTipoDato(String descKey,
            long idStruttura, long idUser, long idTipoDatoApplic, String nmClasseTipoDato) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        List<IamAbilTipoDato> iamAbilTipoDatos = null;

        try {
            String queryStr = "select t from IamAbilTipoDato t "
                    + "where t.iamAbilOrganiz.iamUser.idUserIam = :idUserIam "
                    + "and t.iamAbilOrganiz.idOrganizApplic = :idOrganizApplic  "
                    + "and t.idTipoDatoApplic = :idTipoDatoApplic  "
                    + "and t.nmClasseTipoDato = :nmClasseTipoDato  ";
            javax.persistence.Query query = entityManager.createQuery(queryStr,
                    IamAbilTipoDato.class);
            query.setParameter("idOrganizApplic", new BigDecimal(idStruttura));
            query.setParameter("idUserIam", idUser);
            query.setParameter("idTipoDatoApplic", new BigDecimal(idTipoDatoApplic));
            query.setParameter("nmClasseTipoDato", nmClasseTipoDato);

            iamAbilTipoDatos = query.getResultList();

            // ottengo un risultato -> abilitato al tipo dato
            if (iamAbilTipoDatos.size() == 1) {
                rispostaControlli.setrLong(iamAbilTipoDatos.get(0).getIdAbilTipoDato());
                rispostaControlli.setrBoolean(true);
            } else {
                rispostaControlli.setCodErr(MessaggiWSBundle.IAM_ABIL_TIPO_DATO_001_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(
                        MessaggiWSBundle.IAM_ABIL_TIPO_DATO_001_001, descKey, nmClasseTipoDato));
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliWS.checkAbilitazioniUtenteIamAbilTipoDato: " + e.getMessage()));
            log.error(ERRORE_TABELLA_DECODIFICA, e);
        }

        return rispostaControlli;
    }
}
