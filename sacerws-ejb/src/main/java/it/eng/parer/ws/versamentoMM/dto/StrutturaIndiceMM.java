/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoMM.dto;

import java.util.HashMap;

/**
 *
 * @author Fioravanti_F
 */
public class StrutturaIndiceMM {

    private HashMap<String, ComponenteMM> componenti;

    public HashMap<String, ComponenteMM> getComponenti() {
        return componenti;
    }

    public void setComponenti(HashMap<String, ComponenteMM> componenti) {
        this.componenti = componenti;
    }
}
