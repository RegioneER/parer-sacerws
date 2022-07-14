package it.eng.parer.ws.versFascicoli.dto;

public class FascicoloLink extends DXPAFascicoloCollegato {

    //
    Long idLinkFasc;

    public Long getIdLinkFasc() {
        return idLinkFasc;
    }

    public void setIdLinkFasc(Long idLinkFasc) {
        this.idLinkFasc = idLinkFasc;
    }

    @Override
    public String toString() {
        return idLinkFasc + " - " + descCollegamento + " - " + csChiaveFasc;
    }

}
