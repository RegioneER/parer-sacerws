package it.eng.parer.ws.versFascicoli.dto;

public class DXPGRespFascicolo {

    String nome;
    String cognome;
    String cdIdentificativo;
    String tiCdIdentificativo;
    String responsabilita;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getCdIdentificativo() {
        return cdIdentificativo;
    }

    public void setCdIdentificativo(String cdIdentificativo) {
        this.cdIdentificativo = cdIdentificativo;
    }

    public String getTiCdIdentificativo() {
        return tiCdIdentificativo;
    }

    public void setTiCdIdentificativo(String tiCdIdentificativo) {
        this.tiCdIdentificativo = tiCdIdentificativo;
    }

    public String getResponsabilita() {
        return responsabilita;
    }

    public void setResponsabilita(String responsabilita) {
        this.responsabilita = responsabilita;
    }

    @Override
    public String toString() {
        return nome + " - " + cognome + " - " + cdIdentificativo + " - " + tiCdIdentificativo + " - " + responsabilita;
    }

}
