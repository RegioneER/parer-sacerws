/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoMM.dto;

import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.xml.versReqMultiMedia.IndiceMM;

/**
 *
 * @author Fioravanti_F
 */
public class VersamentoMMExt extends VersamentoExt {

    private String datiXmlIndice;
    private IndiceMM indiceMM;
    private StrutturaIndiceMM strutturaIndiceMM;
    //
    private boolean containerZip;
    private String pathContainerZip;
    //
    private String pathLocaleContenutoZip;
    private String prefissoPathPerApp;
    //

    public String getDatiXmlIndice() {
        return datiXmlIndice;
    }

    public void setDatiXmlIndice(String datiXmlIndice) {
        this.datiXmlIndice = datiXmlIndice;
    }

    public IndiceMM getIndiceMM() {
        return indiceMM;
    }

    public void setIndiceMM(IndiceMM indiceMM) {
        this.indiceMM = indiceMM;
    }

    public StrutturaIndiceMM getStrutturaIndiceMM() {
        return strutturaIndiceMM;
    }

    public void setStrutturaIndiceMM(StrutturaIndiceMM strutturaIndiceMM) {
        this.strutturaIndiceMM = strutturaIndiceMM;
    }

    public boolean isContainerZip() {
        return containerZip;
    }

    public void setContainerZip(boolean containerZip) {
        this.containerZip = containerZip;
    }

    public String getPathContainerZip() {
        return pathContainerZip;
    }

    public void setPathContainerZip(String pathContainerZip) {
        this.pathContainerZip = pathContainerZip;
    }

    public String getPathLocaleContenutoZip() {
        return pathLocaleContenutoZip;
    }

    public void setPathLocaleContenutoZip(String pathLocaleContenutoZip) {
        this.pathLocaleContenutoZip = pathLocaleContenutoZip;
    }

    public String getPrefissoPathPerApp() {
        return prefissoPathPerApp;
    }

    public void setPrefissoPathPerApp(String prefissoPathPerApp) {
        this.prefissoPathPerApp = prefissoPathPerApp;
    }
}
