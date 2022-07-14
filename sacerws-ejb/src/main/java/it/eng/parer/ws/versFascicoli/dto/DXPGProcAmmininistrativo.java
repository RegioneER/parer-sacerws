package it.eng.parer.ws.versFascicoli.dto;

public class DXPGProcAmmininistrativo {

    String codice;
    String denominazione;

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getDenominazione() {
        return denominazione;
    }

    public void setDenominazione(String denominazione) {
        this.denominazione = denominazione;
    }

    @Override
    public String toString() {
        return codice + " - " + denominazione;
    }

}
