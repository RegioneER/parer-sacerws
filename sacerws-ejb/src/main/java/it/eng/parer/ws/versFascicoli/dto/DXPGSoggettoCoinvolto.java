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

public class DXPGSoggettoCoinvolto {

    String nome;
    String cognome;
    String denominazione;
    String identificativo;
    String tipoIdentificativo;
    String tipoRapporto;

    public String getNome() {
	return nome;
    }

    public void setNome(String nome) {
	this.nome = nome;
    }

    public String getCognome() {
	return cognome;
    }

    public void setCognome(String cognome) {
	this.cognome = cognome;
    }

    public String getDenominazione() {
	return denominazione;
    }

    public void setDenominazione(String denominazione) {
	this.denominazione = denominazione;
    }

    public String getIdentificativo() {
	return identificativo;
    }

    public void setIdentificativo(String identificativo) {
	this.identificativo = identificativo;
    }

    public String getTipoIdentificativo() {
	return tipoIdentificativo;
    }

    public void setTipoIdentificativo(String tipoIdentificativo) {
	this.tipoIdentificativo = tipoIdentificativo;
    }

    public String getTipoRapporto() {
	return tipoRapporto;
    }

    public void setTipoRapporto(String tipoRapporto) {
	this.tipoRapporto = tipoRapporto;
    }

    @Override
    public String toString() {
	return nome + " - " + cognome + " - " + denominazione + " - " + identificativo + " - "
		+ tipoIdentificativo + " - " + tipoRapporto;
    }

}
