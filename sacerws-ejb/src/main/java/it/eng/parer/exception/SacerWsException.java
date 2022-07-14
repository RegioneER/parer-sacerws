/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.exception;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;

public class SacerWsException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -354822225586846363L;

    private final SacerWsErrorCategory category;

    public SacerWsException() {
        super();
        this.category = SacerWsErrorCategory.INTERNAL_ERROR; // default
    }

    public SacerWsException(SacerWsErrorCategory category) {
        super();
        this.category = category;
    }

    public SacerWsException(String message, Throwable cause, SacerWsErrorCategory category) {
        super(message, cause);
        this.category = category;
    }

    public SacerWsException(String message, SacerWsErrorCategory category) {
        super(message);
        this.category = category;
    }

    public SacerWsException(Throwable cause, SacerWsErrorCategory category) {
        super(cause);
        this.category = category;
    }

    public SacerWsErrorCategory getCategory() {
        return category;
    }

    @Override
    public String getLocalizedMessage() {
        return "[" + getCategory().toString() + "]" + "  " + super.getLocalizedMessage();
    }

    @Override
    public String getMessage() {
        return "[" + getCategory().toString() + "]" + "  " + super.getMessage();
    }

}
