/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import java.io.Serializable;

/**
 *
 * @author DiLorenzo_F
 */
public class PayLoad implements Serializable {

    private long id;

    private long idStrut;

    private String tipoEntitaSacer;

    private String stato;

    private long aaKeyUnitaDoc;

    private long dtCreazione;

    public PayLoad() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdStrut() {
        return idStrut;
    }

    public void setIdStrut(long idStrut) {
        this.idStrut = idStrut;
    }

    public String getTipoEntitaSacer() {
        return tipoEntitaSacer;
    }

    public void setTipoEntitaSacer(String tipoEntitaSacer) {
        this.tipoEntitaSacer = tipoEntitaSacer;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public long getAaKeyUnitaDoc() {
        return aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(long aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    public long getDtCreazione() {
        return dtCreazione;
    }

    public void setDtCreazione(long dtCreazione) {
        this.dtCreazione = dtCreazione;
    }

}
