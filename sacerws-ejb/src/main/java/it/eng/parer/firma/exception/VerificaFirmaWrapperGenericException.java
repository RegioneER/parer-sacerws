/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.exception;

import it.eng.parer.firma.xml.VerificaFirmaWrapper;

/**
 *
 * @author sinatti_s
 */
public class VerificaFirmaWrapperGenericException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4578580893175157354L;

    private final transient VerificaFirmaWrapper wrapper;

    public VerificaFirmaWrapperGenericException(final VerificaFirmaWrapper wrapper) {
        super();
        this.wrapper = wrapper;
    }

    public VerificaFirmaWrapperGenericException(Throwable ex, final VerificaFirmaWrapper wrapper) {
        super(ex);
        this.wrapper = wrapper;
    }

    public VerificaFirmaWrapperGenericException(String message, final VerificaFirmaWrapper wrapper) {
        super(message);
        this.wrapper = wrapper;
    }

    public VerificaFirmaWrapper getVerificaFirmaWrapper() {
        return wrapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    @Override
    public String getMessage() {
        return "Versione servizio v." + wrapper.getServiceVersion() + " libreria "
                + wrapper.getAdditionalInfo().getServiceCode() + " v. " + wrapper.getLibraryVersion();
    }

}
