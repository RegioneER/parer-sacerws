/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.crypto.helper;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper per il WS di verifica firme sync
 *
 * @author Snidero_L
 */
@Stateless
public class VerificaFirmeSyncHelper {

    Logger log = LoggerFactory.getLogger(VerificaFirmeSyncHelper.class);

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager em;

    /**
     * Effettua i controlli sull'utente del WS SOAP VerificaFirmeSync. TODO: da implementare appena c'è l'utente.
     * 
     * @param userName
     *            nome utente
     * @param password
     *            password
     * 
     * @return autorizzato o meno
     */
    public boolean checkAuthorization(String userName, String password) {
        return true;
    }
}
