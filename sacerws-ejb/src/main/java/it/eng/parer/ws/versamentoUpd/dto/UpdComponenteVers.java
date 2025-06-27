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

package it.eng.parer.ws.versamentoUpd.dto;

import it.eng.parer.ws.versamento.dto.ComponenteVers;

/**
 *
 * @author sinatti_s
 */
public class UpdComponenteVers extends ComponenteVers {

    private static final long serialVersionUID = 1L;
    private it.eng.parer.ws.xml.versUpdReq.ComponenteType myUpdComponente;
    private it.eng.parer.ws.xml.versUpdReq.SottoComponenteType myUpdSottoComponente;
    private UpdDocumentoVers rifAggDocumentoVers;

    private String keyCtrl;

    private String tipoComponenteNonVerificato;

    public it.eng.parer.ws.xml.versUpdReq.ComponenteType getMyUpdComponente() {
	return myUpdComponente;
    }

    public void setMyUpdComponente(it.eng.parer.ws.xml.versUpdReq.ComponenteType myAggComponente) {
	this.myUpdComponente = myAggComponente;
    }

    public it.eng.parer.ws.xml.versUpdReq.SottoComponenteType getMyUpdSottoComponente() {
	return myUpdSottoComponente;
    }

    public void setMyUpdSottoComponente(
	    it.eng.parer.ws.xml.versUpdReq.SottoComponenteType myAggSottoComponente) {
	this.myUpdSottoComponente = myAggSottoComponente;
    }

    public UpdDocumentoVers getRifUpdDocumentoVers() {
	return rifAggDocumentoVers;
    }

    public void setRifUpdDocumentoVers(UpdDocumentoVers rifAggDocumentoVers) {
	this.rifAggDocumentoVers = rifAggDocumentoVers;
    }

    public String getTipoComponenteNonVerificato() {
	return tipoComponenteNonVerificato;
    }

    public void setTipoComponenteNonVerificato(String tipoComponenteNonVerificato) {
	this.tipoComponenteNonVerificato = tipoComponenteNonVerificato;
    }

    public String getKeyCtrl() {
	return keyCtrl;
    }

    public void setKeyCtrl(String keyCtrl) {
	this.keyCtrl = keyCtrl;
    }

}
