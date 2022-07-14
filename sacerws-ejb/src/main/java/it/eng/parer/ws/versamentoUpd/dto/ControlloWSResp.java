/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ControlloWSResp implements Serializable {

    private String cdCategoria;
    private String cdFamiglia;
    private String dsControllo;
    private String cdControllo;
    private BigDecimal niOrd;

    public ControlloWSResp() {
        super();
    }

    public ControlloWSResp(String cdCategoria, String cdControllo, String cdFamiglia, String dsControllo,
            BigDecimal niOrd) {
        super();
        this.cdCategoria = cdCategoria;
        this.cdControllo = cdControllo;
        this.cdFamiglia = cdFamiglia;
        this.dsControllo = dsControllo;
        this.niOrd = niOrd;
    }

    public String getCdFamiglia() {
        return cdFamiglia;
    }

    public void setCdFamiglia(String cdFamiglia) {
        this.cdFamiglia = cdFamiglia;
    }

    public String getDsControllo() {
        return dsControllo;
    }

    public void setDsControllo(String dsControllo) {
        this.dsControllo = dsControllo;
    }

    public BigDecimal getNiOrd() {
        return niOrd;
    }

    public void setNiOrd(BigDecimal niOrd) {
        this.niOrd = niOrd;
    }

    public String getCdControllo() {
        return cdControllo;
    }

    public void setCdControllo(String cdControllo) {
        this.cdControllo = cdControllo;
    }

    public String getCdCategoria() {
        return cdCategoria;
    }

    public void setCdCategoria(String cdCategoria) {
        this.cdCategoria = cdCategoria;
    }

    @Override
    public String toString() {
        return cdCategoria + " - " + dsControllo;
    }

}
