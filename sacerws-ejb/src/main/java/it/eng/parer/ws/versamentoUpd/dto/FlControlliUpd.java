/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.dto;

/**
 *
 * @author sinattti_s
 */
public class FlControlliUpd {

    boolean flAbilitaUpdMeta;
    boolean flAccettaUpdMetaInark;
    boolean flForzaUpdMetaInark;

    boolean flProfiloUdObbOggetto;
    boolean flProfiloUdObbData;

    public boolean isFlAbilitaUpdMeta() {
        return flAbilitaUpdMeta;
    }

    public void setFlAbilitaUpdMeta(boolean flAbilitaUpdMeta) {
        this.flAbilitaUpdMeta = flAbilitaUpdMeta;
    }

    public boolean isFlAccettaUpdMetaInark() {
        return flAccettaUpdMetaInark;
    }

    public void setFlAccettaUpdMetaInark(boolean flAccettaUpdMetaInark) {
        this.flAccettaUpdMetaInark = flAccettaUpdMetaInark;
    }

    public boolean isFlForzaUpdMetaInark() {
        return flForzaUpdMetaInark;
    }

    public void setFlForzaUpdMetaInark(boolean flForzaUpdMetaInark) {
        this.flForzaUpdMetaInark = flForzaUpdMetaInark;
    }

    public boolean isFlProfiloUdObbOggetto() {
        return flProfiloUdObbOggetto;
    }

    public void setFlProfiloUdObbOggetto(boolean flProfiloUdObbOggetto) {
        this.flProfiloUdObbOggetto = flProfiloUdObbOggetto;
    }

    public boolean isFlProfiloUdObbData() {
        return flProfiloUdObbData;
    }

    public void setFlProfiloUdObbData(boolean flProfiloUdObbData) {
        this.flProfiloUdObbData = flProfiloUdObbData;
    }

}
