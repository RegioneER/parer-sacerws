package it.eng.parer.exception;

import java.io.Serializable;

/**
 * La classe astratta <code>EMFAbstractError</code> dev'essere estesa da tutte quelle classi che rappresentano un errore
 * gestito da <code>EMFErrorHandler</code>.
 * 
 * Exception viene lanciata quando non è possibile gestire gli attributi dell'errore.
 * 
 * @author Luigi Bellio
 * 
 *         Verifica EMFErrorHandler
 */
public abstract class ParerAbstractError extends Exception implements Serializable {
    public static final String ABSTRACT_ERROR_ELEMENT = "ABSTRACT_ERROR";
    public static final String ERROR_SEVERITY = "SEVERITY";
    public static final String ERROR_DESCRIPTION = "DESCRIPTION";
    public static final String ERROR_ADDITIONAL_INFO = "ADDITIONAL_INFO";
    private String _severity = null;
    private String _description = null;
    private Object _additionalInfo = null;

    /**
     * In questo costruttore vengono definiti alcuni attributi di classe.
     */
    protected ParerAbstractError() {
        super();
        _severity = ParerErrorSeverity.ERROR;
        _description = "NOT DEFINED";
        _additionalInfo = null;
    }

    /**
     * Ritorna il messaggio di errore composto dal severity e dalla descrizione dell'errore.
     * 
     * @return <code>String</code> composta da severity e descrizione dell'errore.
     */
    public String getMessage() {
        String message = "severity [" + _severity + "] description [" + _description + "]";
        return message;
    } // public String getMessage()

    /**
     * Ritorna il severity dell'errore.
     * 
     * @return <code>String</code> il severity dell'errore.
     */
    public String getSeverity() {
        return _severity;
    } // public String getSeverity()

    /**
     * Permette di impostare il severity dell'errore. Ad uso esclusivo della classe figlia.
     * 
     * @param severity
     *            l'attributo di severity.
     */
    protected void setSeverity(String severity) {
        if (!ParerErrorSeverity.isSeverityValid(severity))
            _severity = ParerErrorSeverity.ERROR;
        else
            _severity = severity;
    } // protected void setSeverity(String severity)

    /**
     * Ritorna la descrizione dell'errore.
     * 
     * @return <code>String</code> la descrizione dell'errore.
     */
    public String getDescription() {
        return _description;
    } // public String getDescription()

    /**
     * Permette di impostare la descrizione dell'errore. Ad uso esclusivo della classe figlia.
     * 
     * @param description
     *            l'attributo descrizione.
     */
    protected void setDescription(String description) {
        if (description == null)
            _description = "NOT DEFINED";
        else
            _description = description;
    } // protected void setDescription(String description)

    /**
     * Ritorna un oggetto rappresentante un 'informazione aggiuntiva dell'errore.
     * 
     * @return <code>Object</code> un 'informazione aggiuntiva dell'errore.
     */
    public Object getAdditionalInfo() {
        return _additionalInfo;
    } // public Object getAdditionalInfo()

    /**
     * Permette di aggiungere all'errore un'informazione espressa con qualsiasi oggetto.
     * 
     * @param additionalInfo
     *            l'iformazione aggiuntiva.
     */
    protected void setAdditionalInfo(Object additionalInfo) {
        _additionalInfo = additionalInfo;
    } // protected void setAdditionalInfo(Object additionalInfo)

    /**
     * Ritorna la categoria dell'errore.
     * 
     * @return <code>String</code> la categoria dell'errore.
     */
    public abstract String getCategory();

} // public abstract class EMFAbstractError extends Exception implements XMLObject, Serializable
