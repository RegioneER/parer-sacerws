/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.exception;

/**
 * ... perché odio RispostaControlli!
 *
 * @author Snidero_L
 */
public class VerificaFirmaException extends Exception {

    private static final long serialVersionUID = -5880705094719132871L;

    private final String codiceErrore;
    private final String descrizioneErrore;

    public VerificaFirmaException(final String codiceErrore, final String descrizioneErrore, Throwable cause) {
        super(cause);
        this.codiceErrore = codiceErrore;
        this.descrizioneErrore = descrizioneErrore;
    }

    public VerificaFirmaException(final String codiceErrore, final String descrizioneErrore) {
        this.codiceErrore = codiceErrore;
        this.descrizioneErrore = descrizioneErrore;
    }

    public String getCodiceErrore() {
        return codiceErrore;
    }

    public String getDescrizioneErrore() {
        return descrizioneErrore;
    }

}
