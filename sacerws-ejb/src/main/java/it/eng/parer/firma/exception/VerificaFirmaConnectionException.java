/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.exception;

import java.io.IOException;

/**
 *
 * @author sinatti_s
 */
public class VerificaFirmaConnectionException extends IOException implements IVerificaFirmaInvokeException {

    private static final long serialVersionUID = -8229661865982756916L;

    private final String cdService;
    private final String url;

    public VerificaFirmaConnectionException(final String cdService, final String url) {
        super();
        this.cdService = cdService;
        this.url = url;
    }

    public VerificaFirmaConnectionException(String message, Throwable cause, final String cdService, final String url) {
        super(message, cause);
        this.cdService = cdService;
        this.url = url;
    }

    public VerificaFirmaConnectionException(String message, final String cdService, final String url) {
        super(message);
        this.cdService = cdService;
        this.url = url;
    }

    public VerificaFirmaConnectionException(Throwable cause, final String cdService, final String url) {
        super(cause);
        this.cdService = cdService;
        this.url = url;
    }

    @Override
    public String getCdService() {
        return cdService;
    }

    public String getUrl() {
        return url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    @Override
    public String getMessage() {
        return "Servizio di verifica firma " + getCdService() + " non attivo o non raggiungibile ( " + getUrl() + ")";
    }

}
