/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.dto;

import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.utils.Costanti;

/**
 *
 * @author fioravanti_f
 */
public class WSDescVersFascicolo implements IWSDesc {

    @Override
    public String getVersione() {
        return Costanti.WS_VERS_FASCICOLO_VRSN;
    }

    @Override
    public String getNomeWs() {
        return Costanti.WS_VERS_FASCICOLO_NOME;
    }

    // @Override
    // public String[] getCompatibilitaWS() {
    // return Costanti.WS_VERS_FASCICOLO_COMP;
    // }
}
