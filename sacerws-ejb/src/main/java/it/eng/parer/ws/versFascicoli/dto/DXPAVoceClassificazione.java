package it.eng.parer.ws.versFascicoli.dto;

public class DXPAVoceClassificazione {

    String codiceVoce;
    String DescrizioneVoce;

    public String getCodiceVoce() {
        return codiceVoce;
    }

    public void setCodiceVoce(String codiceVoce) {
        this.codiceVoce = codiceVoce;
    }

    public String getDescrizioneVoce() {
        return DescrizioneVoce;
    }

    public void setDescrizioneVoce(String descrizioneVoce) {
        DescrizioneVoce = descrizioneVoce;
    }

    @Override
    public String toString() {
        return codiceVoce + " - " + DescrizioneVoce;
    }

}
