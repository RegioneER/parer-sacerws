/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.dto;

import java.util.ArrayList;
import java.util.List;

import it.eng.parer.ws.dto.CSChiaveSottFasc;

/**
 *
 * @author fioravanti_f, sinatti_s
 */
public class DatiXmlProfiloArchivistico {

    private String indiceClassificazione;
    private String descIndiceClassificazione;
    private CSChiaveSottFasc chiaveFascicoloDiAppartenenza;
    private List<DXPAFascicoloCollegato> fascCollegati;
    private List<DXPAVoceClassificazione> vociClassificazione;

    public String getIndiceClassificazione() {
        return indiceClassificazione;
    }

    public void setIndiceClassificazione(String indiceClassificazione) {
        this.indiceClassificazione = indiceClassificazione;
    }

    public String getDescIndiceClassificazione() {
        return descIndiceClassificazione;
    }

    public void setDescIndiceClassificazione(String descIndiceClassificazione) {
        this.descIndiceClassificazione = descIndiceClassificazione;
    }

    public CSChiaveSottFasc getChiaveFascicoloDiAppartenenza() {
        return chiaveFascicoloDiAppartenenza;
    }

    public void setChiaveFascicoloDiAppartenenza(CSChiaveSottFasc chiaveFascicoloDiAppartenenza) {
        this.chiaveFascicoloDiAppartenenza = chiaveFascicoloDiAppartenenza;
    }

    public List<DXPAFascicoloCollegato> getFascCollegati() {
        if (fascCollegati == null) {
            fascCollegati = new ArrayList<DXPAFascicoloCollegato>(0);
        }
        return fascCollegati;
    }

    public void setFascCollegati(List<DXPAFascicoloCollegato> fascCollegati) {
        this.fascCollegati = fascCollegati;
    }

    public DXPAFascicoloCollegato addFascCollegato(DXPAFascicoloCollegato fascCollegato) {
        getFascCollegati().add(fascCollegato);
        return fascCollegato;
    }

    public DXPAFascicoloCollegato removeFascCollegato(DXPAFascicoloCollegato fascCollegato) {
        getFascCollegati().remove(fascCollegato);
        return fascCollegato;
    }

    public List<DXPAVoceClassificazione> getVociClassificazione() {
        if (vociClassificazione == null) {
            vociClassificazione = new ArrayList<DXPAVoceClassificazione>(0);
        }
        return vociClassificazione;
    }

    public void setVociClassificazione(List<DXPAVoceClassificazione> vociClassificazione) {
        this.vociClassificazione = vociClassificazione;
    }

    public DXPAVoceClassificazione addVoceClassificazione(DXPAVoceClassificazione voceClassificazione) {
        getVociClassificazione().add(voceClassificazione);
        return voceClassificazione;
    }

    public DXPAVoceClassificazione removeVoceClassificazione(DXPAVoceClassificazione voceClassificazione) {
        getVociClassificazione().remove(voceClassificazione);
        return voceClassificazione;
    }
}
