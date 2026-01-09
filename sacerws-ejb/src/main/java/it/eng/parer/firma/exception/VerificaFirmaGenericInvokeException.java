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
package it.eng.parer.firma.exception;

/**
 *
 * @author sinatti_s
 */
public class VerificaFirmaGenericInvokeException extends Exception
        implements IVerificaFirmaInvokeException {

    /**
     *
     */
    private static final long serialVersionUID = -8607219490984095397L;

    private final String cdService;
    private final String url;

    public VerificaFirmaGenericInvokeException(final String cdService, final String url) {
        super();
        this.cdService = cdService;
        this.url = url;
    }

    public VerificaFirmaGenericInvokeException(String message, Throwable cause,
            final String cdService, final String url) {
        super(message, cause);
        this.cdService = cdService;
        this.url = url;
    }

    public VerificaFirmaGenericInvokeException(String message, final String cdService,
            final String url) {
        super(message);
        this.cdService = cdService;
        this.url = url;
    }

    public VerificaFirmaGenericInvokeException(Throwable cause, final String cdService,
            final String url) {
        super(cause);
        this.cdService = cdService;
        this.url = url;
    }

    @Override
    public String getCdService() {
        return cdService;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getMessage() {
        return "Servizio di verifica firma " + getCdService()
                + " errore generico su gestione risposta da endpoint " + getUrl();
    }

}
