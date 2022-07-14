/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoTpi.ejb;

/**
 *
 * @author fioravanti_f
 */
public class StatoCreaCartelle {

    private boolean creaCartellaData;
    private boolean creaCartellaVersatore;
    private Long idDtVers = null;
    private String pathCartellaData;
    private String pathCartellaVersatore;

    public boolean isCreaCartellaData() {
        return creaCartellaData;
    }

    public void setCreaCartellaData(boolean creaCartellaData) {
        this.creaCartellaData = creaCartellaData;
    }

    public boolean isCreaCartellaVersatore() {
        return creaCartellaVersatore;
    }

    public void setCreaCartellaVersatore(boolean creaCartellaVersatore) {
        this.creaCartellaVersatore = creaCartellaVersatore;
    }

    public Long getIdDtVers() {
        return idDtVers;
    }

    public void setIdDtVers(Long idDtVers) {
        this.idDtVers = idDtVers;
    }

    public String getPathCartellaData() {
        return pathCartellaData;
    }

    public void setPathCartellaData(String pathCartellaData) {
        this.pathCartellaData = pathCartellaData;
    }

    public String getPathCartellaVersatore() {
        return pathCartellaVersatore;
    }

    public void setPathCartellaVersatore(String pathCartellaVersatore) {
        this.pathCartellaVersatore = pathCartellaVersatore;
    }

}
