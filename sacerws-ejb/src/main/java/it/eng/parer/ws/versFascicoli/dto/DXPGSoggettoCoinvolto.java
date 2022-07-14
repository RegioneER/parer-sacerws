package it.eng.parer.ws.versFascicoli.dto;

public class DXPGSoggettoCoinvolto {

    String nome;
    String cognome;
    String denominazione;
    String identificativo;
    String tipoIdentificativo;
    String tipoRapporto;

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

    public String getDenominazione() {
        return denominazione;
    }

    public void setDenominazione(String denominazione) {
        this.denominazione = denominazione;
    }

    public String getIdentificativo() {
        return identificativo;
    }

    public void setIdentificativo(String identificativo) {
        this.identificativo = identificativo;
    }

    public String getTipoIdentificativo() {
        return tipoIdentificativo;
    }

    public void setTipoIdentificativo(String tipoIdentificativo) {
        this.tipoIdentificativo = tipoIdentificativo;
    }

    public String getTipoRapporto() {
        return tipoRapporto;
    }

    public void setTipoRapporto(String tipoRapporto) {
        this.tipoRapporto = tipoRapporto;
    }

    @Override
    public String toString() {
        return nome + " - " + cognome + " - " + denominazione + " - " + identificativo + " - " + tipoIdentificativo
                + " - " + tipoRapporto;
    }

}
