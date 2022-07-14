/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.exception;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;

/**
 *
 * @author sinatti_s
 */
public class SacerWsRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 5015771412184277789L;

    private final SacerWsErrorCategory category;

    public SacerWsRuntimeException() {
        super();
        this.category = SacerWsErrorCategory.INTERNAL_ERROR; // default
    }

    public SacerWsRuntimeException(SacerWsErrorCategory category) {
        super();
        this.category = category;
    }

    public SacerWsRuntimeException(String message, Throwable throwable, SacerWsErrorCategory category) {
        super(message, throwable);
        this.category = category;
    }

    public SacerWsRuntimeException(Throwable throwable, SacerWsErrorCategory category) {
        super(throwable);
        this.category = category;
    }

    public SacerWsRuntimeException(String message, SacerWsErrorCategory category) {
        super(message);
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
