/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.dto;

/**
 *
 * @author sinattti_s
 */
public class UpdFascPrincipale {

    private String classifica;
    private String identificativo;
    private String oggetto;
    private String sottoIdentificativo;
    private String sottoOggetto;

    public String getClassifica() {
        return classifica;
    }

    public void setClassifica(String classifica) {
        this.classifica = classifica;
    }

    public String getIdentificativo() {
        return identificativo;
    }

    public void setIdentificativo(String identificativo) {
        this.identificativo = identificativo;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public String getSottoIdentificativo() {
        return sottoIdentificativo;
    }

    public void setSottoIdentificativo(String sottoIdentificativo) {
        this.sottoIdentificativo = sottoIdentificativo;
    }

    public String getSottoOggetto() {
        return sottoOggetto;
    }

    public void setSottoOggetto(String sottoOggetto) {
        this.sottoOggetto = sottoOggetto;
    }

}
