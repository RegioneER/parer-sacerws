/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.exception;

/**
 *
 * @author sinatti_s
 */
public class VerificaFirmaWrapperResNotFoundException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8471637049988191811L;
    private final String wichOne;

    public VerificaFirmaWrapperResNotFoundException(String message, String wichOne) {
        super(message);
        this.wichOne = wichOne;
    }

    /**
     * @return the wichOne
     */
    public String getWichOne() {
        return wichOne;
    }

}
