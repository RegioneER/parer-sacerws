package it.eng.parer.ws.versFascicoli.dto;

public class DXPGAmminPartecipante {

    String denominazione;
    String codice;
    String tipoCodice;

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

    public String getTipoCodice() {
        return tipoCodice;
    }

    public void setTipoCodice(String tipoCodice) {
        this.tipoCodice = tipoCodice;
    }

    @Override
    public String toString() {

        return denominazione + " - " + codice + " - " + tipoCodice;

    }

}
