/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.utils;

import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.utils.Costanti;

/**
 *
 * @author sinatti_s
 */
public class WSDescUpdVers implements IWSDesc {

    @Override
    public String getVersione() {
        return Costanti.WS_AGGIORNAMENTO_VERS_VRSN;
    }

    @Override
    public String getNomeWs() {
        return Costanti.WS_AGGIORNAMENTO_VERS_NOME;
    }

    // @Override
    // public String[] getCompatibilitaWS() {
    // return Costanti.WS_AGGIORNAMENTO_VERS_COMP;
    // }
}
