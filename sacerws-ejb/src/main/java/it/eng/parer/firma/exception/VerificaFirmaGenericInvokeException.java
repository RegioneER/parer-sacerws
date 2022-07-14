/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.exception;

/**
 *
 * @author sinatti_s
 */
public class VerificaFirmaGenericInvokeException extends Exception implements IVerificaFirmaInvokeException {

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

    public VerificaFirmaGenericInvokeException(String message, Throwable cause, final String cdService,
            final String url) {
        super(message, cause);
        this.cdService = cdService;
        this.url = url;
    }

    public VerificaFirmaGenericInvokeException(String message, final String cdService, final String url) {
        super(message);
        this.cdService = cdService;
        this.url = url;
    }

    public VerificaFirmaGenericInvokeException(Throwable cause, final String cdService, final String url) {
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
        return "Servizio di verifica firma " + getCdService() + " errore generico su gestione risposta da endpoint "
                + getUrl();
    }

}
