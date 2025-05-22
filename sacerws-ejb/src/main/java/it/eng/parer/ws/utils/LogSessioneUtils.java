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
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;

/**
 *
 * @author fioravanti_f
 */
public class LogSessioneUtils {

    //
    private static final int DS_ERR_MAX_LEN = 1024;

    private LogSessioneUtils() {
	throw new IllegalStateException("Utility class");
    }

    public static Date getDatePart(Date date) {
	Calendar cal = Calendar.getInstance();
	cal.setTime(date);
	cal.set(Calendar.HOUR_OF_DAY, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MILLISECOND, 0);

	return cal.getTime();
    }

    public static String getDsErrAtMaxLen(String dsErrore) {
	String tmpErrMess;
	if (dsErrore.isEmpty()) {
	    tmpErrMess = "(vuoto)";
	} else {
	    tmpErrMess = getStringAtMaxLen(dsErrore, DS_ERR_MAX_LEN);
	}
	return tmpErrMess;
    }

    public static String getStringAtMaxLen(String string, int maxLen) {
	String tmpReturn = string;

	if (tmpReturn.length() > maxLen) {
	    tmpReturn = tmpReturn.substring(0, maxLen);
	}
	return tmpReturn;
    }

    public static void logSimulazioni(String ambiente, String ente, String struttura,
	    String registro, int anno, String numero, String user, Logger logger) {
	logger.info(
		"Chiamata al WS con SimulaSalvataggioDatiInDB=true versatore=[{}-{}-{}] key=[{}-{}-{}] user=[{}] ",
		ambiente, ente, struttura, registro, anno, numero, user);
    }
}
