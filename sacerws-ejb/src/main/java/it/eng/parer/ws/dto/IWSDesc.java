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
