/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versamentoMM.dto;

import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.xml.versReqMultiMedia.IndiceMM;

/**
 *
 * @author Fioravanti_F
 */
public class VersamentoMMExt extends VersamentoExt {

    private static final long serialVersionUID = 1L;
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
