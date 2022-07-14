package it.eng.parer.ws.versFascicoli.dto;

public class DXPGAmminTitolare {

    String denominazione;
    String codice;
    String tiCodice;

    public String getDenominazione() {
        return denominazione;
    }

    public void setDenominazione(String denominazione) {
        this.denominazione = denominazione;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getTiCodice() {
        return tiCodice;
    }

    public void setTiCodice(String tiCodice) {
        this.tiCodice = tiCodice;
    }

    @Override
    public String toString() {
        return denominazione + " - " + codice + " - " + tiCodice;
    }

}
