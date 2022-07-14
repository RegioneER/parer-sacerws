/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.ejb;

import it.eng.parer.idpjaas.logutils.IdpConfigLog;
import it.eng.parer.idpjaas.logutils.IdpLogger;
import it.eng.parer.idpjaas.logutils.LogDto;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.ParametroApplDB;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fioravanti_f
 */
@Stateless(mappedName = "WsIdpLogger")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class WsIdpLogger {

    @EJB
    private ControlliSemantici controlliSemantici;

    @EJB
    private AppServerInstance asi;

    private static final Logger log = LoggerFactory.getLogger(WsIdpLogger.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;
    //

    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    public void scriviLog(LogDto logDto) {
        HashMap<String, String> iamDefaults = null;

        RispostaControlli rispostaControlli = controlliSemantici
                .caricaDefaultDaDB(ParametroApplDB.TipoParametroAppl.IAM);
        if (rispostaControlli.isrBoolean() == false) {
            throw new RuntimeException("WsIdpLogger: Impossibile accedere alla tabella parametri");
        } else {
            iamDefaults = (HashMap<String, String>) rispostaControlli.getrObject();
        }
        if (iamDefaults.get(ParametroApplDB.IDP_QRY_REGISTRA_EVENTO_UTENTE) != null
                && !iamDefaults.get(ParametroApplDB.IDP_QRY_REGISTRA_EVENTO_UTENTE).isEmpty()
                && iamDefaults.get(ParametroApplDB.IDP_QRY_VERIFICA_DISATTIVAZIONE_UTENTE) != null
                && !iamDefaults.get(ParametroApplDB.IDP_QRY_VERIFICA_DISATTIVAZIONE_UTENTE).isEmpty()
                && iamDefaults.get(ParametroApplDB.IDP_QRY_DISABLE_USER) != null
                && !iamDefaults.get(ParametroApplDB.IDP_QRY_DISABLE_USER).isEmpty()
                && iamDefaults.get(ParametroApplDB.IDP_MAX_GIORNI) != null
                && !iamDefaults.get(ParametroApplDB.IDP_MAX_GIORNI).isEmpty()
                && iamDefaults.get(ParametroApplDB.IDP_MAX_TENTATIVI_FALLITI) != null
                && !iamDefaults.get(ParametroApplDB.IDP_MAX_TENTATIVI_FALLITI).isEmpty()) {
            try {
                IdpConfigLog icl = new IdpConfigLog();
                icl.setQryRegistraEventoUtente(iamDefaults.get(ParametroApplDB.IDP_QRY_REGISTRA_EVENTO_UTENTE));
                icl.setQryVerificaDisattivazioneUtente(
                        iamDefaults.get(ParametroApplDB.IDP_QRY_VERIFICA_DISATTIVAZIONE_UTENTE));
                icl.setQryDisabilitaUtente(iamDefaults.get(ParametroApplDB.IDP_QRY_DISABLE_USER));
                icl.setMaxTentativi(Integer.parseInt(iamDefaults.get(ParametroApplDB.IDP_MAX_TENTATIVI_FALLITI)));
                icl.setMaxGiorni(Integer.parseInt(iamDefaults.get(ParametroApplDB.IDP_MAX_GIORNI)));

                logDto.setServername(asi.getName());

                java.sql.Connection connection = entityManager.unwrap(java.sql.Connection.class);

                IdpLogger.EsitiLog risposta = (new IdpLogger(icl).scriviLog(logDto, connection));

                if (risposta == IdpLogger.EsitiLog.UTENTE_DISATTIVATO) {
                    String queryStr = "update IamUser iu " + "set iu.flAttivo = :flAttivoIn "
                            + "where iu.nmUserid = :nmUseridIn ";

                    // l'operazione di log dell'evento BAD_PASS ha causato la disattivazione
                    // dell'utente nella tabella USR_USER di IAM; questa situazione verrà recepita
                    // tra circa 5 minuti, durante i qali l'utente risulta ancora attivo per SACER.
                    // Per accelerare la risposta del sistema, disattivo l'utente anche nella
                    // tabella locale. Tra 5 minuti il job di aggiornamento utenti ripeterà
                    // la stessa situazione.
                    javax.persistence.Query query = entityManager.createQuery(queryStr);
                    query.setParameter("flAttivoIn", "0");
                    query.setParameter("nmUseridIn", logDto.getNmUser());
                    query.executeUpdate();

                    log.warn("ERRORE DI AUTENTICAZIONE WS." + " DISATTIVAZIONE UTENTE: " + logDto.getNmUser());
                }

            } catch (UnknownHostException ex) {
                throw new RuntimeException(
                        "WsIdpLogger: Errore nel determinare il nome host per il server: " + ex.getMessage());
            } catch (SQLException ex) {
                throw new RuntimeException("WsIdpLogger: Errore nell'accesso ai dati di log: " + ex.getMessage());
            } catch (Exception ex) {
                throw new RuntimeException("WsIdpLogger: Errore: " + ex.getMessage());
            }
        }

    }

}
