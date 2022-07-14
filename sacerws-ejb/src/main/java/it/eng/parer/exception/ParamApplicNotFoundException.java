/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.exception;

/**
 *
 * @author sinatti_s
 */
public class ParamApplicNotFoundException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 4519094509856840927L;
    /**
     * 
     */

    private final String nmParamApplic;

    public ParamApplicNotFoundException(String message, String nmParamApplic) {
        super(message);
        this.nmParamApplic = nmParamApplic;
    }

    /**
     * @return the nmParamApplic
     */
    public String getNmParamApplic() {
        return nmParamApplic;
    }

}
