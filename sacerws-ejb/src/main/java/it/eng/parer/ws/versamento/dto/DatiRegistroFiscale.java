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
package it.eng.parer.ws.versamento.dto;

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
