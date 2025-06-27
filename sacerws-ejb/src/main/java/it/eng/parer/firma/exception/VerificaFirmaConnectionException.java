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

import java.io.IOException;

/**
 *
 * @author sinatti_s
 */
public class VerificaFirmaConnectionException extends IOException
	implements IVerificaFirmaInvokeException {

    private static final long serialVersionUID = -8229661865982756916L;

    private final String cdService;
    private final String url;

    public VerificaFirmaConnectionException(final String cdService, final String url) {
	super();
	this.cdService = cdService;
	this.url = url;
    }

    public VerificaFirmaConnectionException(String message, Throwable cause, final String cdService,
	    final String url) {
	super(message, cause);
	this.cdService = cdService;
	this.url = url;
    }

    public VerificaFirmaConnectionException(String message, final String cdService,
	    final String url) {
	super(message);
	this.cdService = cdService;
	this.url = url;
    }

    public VerificaFirmaConnectionException(Throwable cause, final String cdService,
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
	return "Servizio di verifica firma " + getCdService() + " non attivo o non raggiungibile ( "
		+ getUrl() + ")";
    }

}
