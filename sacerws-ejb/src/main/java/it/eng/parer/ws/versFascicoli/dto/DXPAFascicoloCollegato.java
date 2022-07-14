package it.eng.parer.ws.versFascicoli.dto;

import it.eng.parer.ws.dto.CSChiaveFasc;

public class DXPAFascicoloCollegato {

    String descCollegamento;
    CSChiaveFasc csChiaveFasc;

    public String getDescCollegamento() {
        return descCollegamento;
    }

    public void setDescCollegamento(String descCollegamento) {
        this.descCollegamento = descCollegamento;
    }

    public CSChiaveFasc getCsChiaveFasc() {
        return csChiaveFasc;
    }

    public void setCsChiaveFasc(CSChiaveFasc csChiaveFasc) {
        this.csChiaveFasc = csChiaveFasc;
    }

    @Override
    public String toString() {
        return descCollegamento + " - " + csChiaveFasc;
    }

}
