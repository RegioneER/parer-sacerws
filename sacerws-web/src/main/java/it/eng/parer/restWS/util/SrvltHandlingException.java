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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.restWS.util;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.restWS.util.Response405.NomeWebServiceRest;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.utils.MessaggiWSBundle;

/**
 *
 * @author sinatti_s
 */
public class SrvltHandlingException {

    /**
     * 
     * @param ws
     *            nome del webservice {@link NomeWebServiceRest}
     * @param e1
     *            errore generico
     * @param rispostaWs
     *            interfaccia risposta ws {@link IRispostaWS}
     * 
     *            //All Socket Exceptions problems like java.net.SocketException, java.net.SocketTimeoutException, etc.
     */

    public static void handlingSocketErrors(NomeWebServiceRest ws, Exception e1, IRispostaWS rispostaWs) {
        //
        if (e1 instanceof SocketTimeoutException || e1 instanceof SocketException || e1 instanceof ConnectException
                || e1 instanceof InterruptedIOException) {

            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666N, ws.toString(), e1.getClass().getName()
                    + (StringUtils.isNotBlank(e1.getMessage()) ? " (" + e1.getMessage() + ")" : ""));

        }
    }

}
