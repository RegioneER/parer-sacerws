/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.utils.Costanti;

/**
 *
 * @author Fioravanti_F
 */
public class WSDescVersamentoAggAll implements IWSDesc {

    @Override
    public String getVersione() {
        return Costanti.WS_AGGIUNTA_VRSN;
    }

    @Override
    public String getNomeWs() {
        return Costanti.WS_AGGIUNTA_DOC_NOME;
    }

    // @Override
    // public String[] getCompatibilitaWS() {
    // return Costanti.WS_AGGIUNTA_COMP;
    // }
}