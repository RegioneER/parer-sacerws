/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.dto;

import java.util.HashMap;

import it.eng.parer.ws.utils.VerificaVersione;

/**
 *
 * @author Fioravanti_F
 */
public interface IWSDesc {

    String getNomeWs();

    /**
     * Deprecated : fare riferimento a metodo getVersione(String versioniWsParamVal)
     * 
     * @return versione del ws
     */
    @Deprecated
    String getVersione(); // versione standard, senza modifiche indotte dalla versione chiamata

    default String getVersione(HashMap<String, String> mapWsVersion) { // versione standard, senza modifiche indotte
                                                                       // dalla versione chiamata
        return VerificaVersione.latestVersion(getNomeWs(), mapWsVersion);
    }

    // public String[] getCompatibilitaWS(); // lista di versioni compatibili con il parser
}
