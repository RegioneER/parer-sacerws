/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.ejb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.IamUser;
import it.eng.parer.grantedEntity.UsrUser;
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
@Stateless(mappedName = "ControlliWS")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliWS {

    @EJB
    WsIdpLogger idpLogger;

    @EJB
    ControlliSemantici controlliSemantici;

    private static final Logger log = LoggerFactory.getLogger(ControlliWS.class);
    //
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public RispostaControlli checkVersione(String versione, String versioniWsKey, HashMap<String, String> xmlDefaults,
            TipiWSPerControlli tipows) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        if (versione == null || versione.isEmpty()) {
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_010);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_010));
                break;
            case ANNULLAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_010);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_010));
                break;
            case VERSAMENTO_FASCICOLO:
                rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_003_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_003_001));
                break;
            case AGGIORNAMENTO_VERSAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_010);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_010));
                break;
            }

            return rispostaControlli;
        }

        List<String> versioniWs = VerificaVersione.getWsVersionList(versioniWsKey, xmlDefaults);
        if (versioniWs.isEmpty()) {
            rispostaControlli.setCodErr(MessaggiWSBundle.UD_018_001);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_018_001, versioniWsKey));
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
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_011);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_011, versione));
                break;
            case ANNULLAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_003);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_003,
                        StringUtils.join(versioniWs, ",")));
                break;
            case VERSAMENTO_FASCICOLO:
                rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_003_002);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_003_002, versione));
                break;
            case AGGIORNAMENTO_VERSAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_011);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_011, versione));
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

        log.info("Indirizzo IP del chiamante - access: ws - IP: " + indirizzoIP);
        // log.debug("Indirizzo IP del chiamante: " + indirizzoIP);

        if (loginName == null || loginName.isEmpty()) {
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_004);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_004));
                break;
            case ANNULLAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_004);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_004));
                break;
            case VERSAMENTO_FASCICOLO:
                rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_007);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_007));
                break;
            case AGGIORNAMENTO_VERSAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_004);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_004));
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
            javax.persistence.Query query = entityManager.createQuery(queryStr, IamUser.class);
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
            log.warn("ERRORE DI AUTENTICAZIONE WS." + " Funzionalità: " + tipows.name() + " Utente: " + loginName
                    + " Tipo errore: " + e.getCodiceErrore().name() + " Indirizzo IP: " + indirizzoIP + " Descrizione: "
                    + e.getDescrizioneErrore());
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
                if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_006);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_006, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_007);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_007, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_012);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_012, e.getDescrizioneErrore()));
                }
                break;
            case ANNULLAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_001);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_001));
                break;
            case VERSAMENTO_FASCICOLO:
                if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_002);
                    rispostaControlli
                            .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_002, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_004);
                    rispostaControlli
                            .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_004, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_003);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_003, e.getDescrizioneErrore()));
                }
                break;
            case AGGIORNAMENTO_VERSAMENTO:
                if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_SCADUTO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_006);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_006, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.UTENTE_NON_ATTIVO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_007);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_007, loginName));
                } else if (e.getCodiceErrore().equals(AuthWSException.CodiceErrore.LOGIN_FALLITO)) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_012);
                    rispostaControlli.setDsErr(
                            MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_012, e.getDescrizioneErrore()));
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
                long tmpNumUtenti = (Long) query.getSingleResult();
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
                    "Eccezione nella fase di autenticazione del EJB " + e.getMessage()));
            log.error("Eccezione nella fase di autenticazione del EJB ", e);
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
            List<IamUser> tmpUsers = (List<IamUser>) query.getResultList();
            if (tmpUsers != null && tmpUsers.size() > 0) {
                iamUser = tmpUsers.get(0);

                if (!iamUser.getFlAttivo().equals("1")) {
                    // UTENTE_NON_ATTIVO
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_007);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_007, loginName));
                    return rispostaControlli;
                }
                if (iamUser.getDtScadPsw().before(new Date())) {
                    // UTENTE_SCADUTO
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_006);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_006, loginName));
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
                    "Eccezione nella fase di autenticazione del EJB " + e.getMessage()));
            log.error("Eccezione nella fase di autenticazione del EJB ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli checkAuthWS(User utente, IWSDesc descrizione, TipiWSPerControlli tipows) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);
        boolean checkOrgVersAuth = false;
        long numAbil = 0;
        Integer tmpIdOrganizz = utente.getIdOrganizzazioneFoglia() != null
                ? utente.getIdOrganizzazioneFoglia().intValue() : null;
        try {
            WSLoginHandler.checkAuthz(utente.getUsername(), tmpIdOrganizz, descrizione.getNomeWs(), entityManager);
            rispostaControlli.setrBoolean(true);
        } catch (AuthWSException ex) {
            checkOrgVersAuth = true;
            switch (tipows) {
            case VERSAMENTO_RECUPERO:
                // L''utente {0} non è abilitato entro la struttura versante
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_009);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_009, utente.getUsername()));
                break;
            case ANNULLAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_008);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_008));
                break;
            case VERSAMENTO_FASCICOLO:
                rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_006);
                rispostaControlli.setDsErr(
                        MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_006, utente.getUsername()));
                break;
            case AGGIORNAMENTO_VERSAMENTO:
                rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_009);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_009, utente.getUsername()));
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
                String queryStr = "select count(t) from IamAbilOrganiz t where " + "t.iamUser.idUserIam = :idUserIamIn "
                        + "and t.idOrganizApplic = :idOrganizApplicIn";
                javax.persistence.Query query = entityManager.createQuery(queryStr, IamUser.class);
                query.setParameter("idUserIamIn", utente.getIdUtente());
                query.setParameter("idOrganizApplicIn", tmpIdOrganizz);
                numAbil = (long) query.getSingleResult();
                if (numAbil > 0) {
                    switch (tipows) {
                    case VERSAMENTO_RECUPERO:
                        // L''utente {0} non è autorizzato alla funzione {1}
                        rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_008);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_008,
                                utente.getUsername(), descrizione.getNomeWs()));
                        break;
                    case ANNULLAMENTO:
                        rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_008);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_008));
                        break;
                    case VERSAMENTO_FASCICOLO:
                        // L''utente {0} non è autorizzato alla funzione {1}
                        rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_005);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_005,
                                utente.getUsername(), descrizione.getNomeWs()));
                        break;
                    case AGGIORNAMENTO_VERSAMENTO:
                        rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_008);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_008,
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

    public RispostaControlli checkAuthWSNoOrg(User utente, IWSDesc descrizione, TipiWSPerControlli tipows) {
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
                    // L''utente {0} non è autorizzato alla funzione {1}
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_008);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_008,
                            utente.getUsername(), descrizione.getNomeWs()));
                    break;
                case ANNULLAMENTO:
                    rispostaControlli.setCodErr(MessaggiWSBundle.RICH_ANN_VERS_008);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.RICH_ANN_VERS_008));
                    break;
                case VERSAMENTO_FASCICOLO:
                    // L''utente {0} non è autorizzato alla funzione {1}
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_CONFIG_002_005);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_002_005,
                            utente.getUsername(), descrizione.getNomeWs()));
                    break;
                case AGGIORNAMENTO_VERSAMENTO:
                    // L''utente {0} non è autorizzato alla funzione {1}
                    rispostaControlli.setCodErr(MessaggiWSBundle.UD_001_008);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_008,
                            utente.getUsername(), descrizione.getNomeWs()));
                    break;
                }
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "Eccezione nella fase di autenticazione del EJB " + e.getMessage()));
            log.error("Eccezione nella fase di autenticazione del EJB ", e);
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
}
