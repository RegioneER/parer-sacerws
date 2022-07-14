/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import java.math.BigDecimal;

/**
 *
 * @author Fioravanti_F
 */
public class DatiRegistroFiscale {

    private long idOrgStrut;
    private long idRegistroUnitaDoc;
    private boolean flRegistroFisc;

    public long getIdOrgStrut() {
        return idOrgStrut;
    }

    public void setIdOrgStrut(long idOrgStrut) {
        this.idOrgStrut = idOrgStrut;
    }

    public long getIdRegistroUnitaDoc() {
        return idRegistroUnitaDoc;
    }

    public void setIdRegistroUnitaDoc(long idRegistroUnitaDoc) {
        this.idRegistroUnitaDoc = idRegistroUnitaDoc;
    }

    public boolean isFlRegistroFisc() {
        return flRegistroFisc;
    }

    public void setFlRegistroFisc(boolean flRegistroFisc) {
        this.flRegistroFisc = flRegistroFisc;
    }

}
