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

package it.eng.parer.ws.versFascicoli.dto;

public class DXPGAmminPartecipante {

    String denominazione;
    String codice;
    String tipoCodice;

    public String getDenominazione() {
	return denominazione;
    }

    public void setDenominazione(String denominazione) {
	this.denominazione = denominazione;
    }

    public String getCodice() {
	return codice;
    }

    public void setCodice(String codice) {
	this.codice = codice;
    }

    public String getTipoCodice() {
	return tipoCodice;
    }

    public void setTipoCodice(String tipoCodice) {
	this.tipoCodice = tipoCodice;
    }

    @Override
    public String toString() {

	return denominazione + " - " + codice + " - " + tipoCodice;

    }

}
