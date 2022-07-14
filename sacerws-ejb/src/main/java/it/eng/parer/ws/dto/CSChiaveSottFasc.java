/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.dto;

/**
 *
 * @author sinatti_s
 */
public class CSChiaveSottFasc extends CSChiaveFasc {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String oggetto;

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    @Override
    public String toString() {
        return super.toString() + " - " + oggetto;
    }

}
