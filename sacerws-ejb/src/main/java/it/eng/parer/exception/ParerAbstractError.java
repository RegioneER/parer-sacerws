/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.exception;

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
public abstract class ParerAbstractError extends Exception {
    private static final long serialVersionUID = 1L;
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
