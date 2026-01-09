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

package it.eng.parer.exception;

/**
 * Questa classe gestisce le possibili severity utilizzabili all'atto della costruzione di un
 * errore.
 *
 * @author Luigi Bellio
 */
public class ParerErrorSeverity {
    /**
     * Ritorna un <code>boolean</code> con il seguente significato: <em>true</em> il parametro
     * severity in input è valido. <em>false</em> il parametro severity in input non è valido.
     *
     * @param severity livello verifica
     *
     * @return <code>boolean</code> la validità della severity passata in input.
     */
    public static boolean isSeverityValid(String severity) {
        if ((severity == null)
                || ((severity != null) && !severity.equals(ParerErrorSeverity.INFORMATION)
                        && !severity.equals(ParerErrorSeverity.WARNING)
                        && !severity.equals(ParerErrorSeverity.ERROR)
                        && !severity.equals(ParerErrorSeverity.BLOCKING)))
            return false;
        return true;
    } // public static boolean isSeverityValid(String severity)

    public static final String INFORMATION = "INFORMATION";
    public static final String WARNING = "WARNING";
    public static final String ERROR = "ERROR";
    public static final String BLOCKING = "BLOCKING";
} // public class EMFErrorSeverity
