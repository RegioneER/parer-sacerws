/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.versFascicoli.dto;

public class DXPGRespFascicolo {

    String nome;
    String cognome;
    String cdIdentificativo;
    String tiCdIdentificativo;
    String responsabilita;

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

    public String getCdIdentificativo() {
        return cdIdentificativo;
    }

    public void setCdIdentificativo(String cdIdentificativo) {
        this.cdIdentificativo = cdIdentificativo;
    }

    public String getTiCdIdentificativo() {
        return tiCdIdentificativo;
    }

    public void setTiCdIdentificativo(String tiCdIdentificativo) {
        this.tiCdIdentificativo = tiCdIdentificativo;
    }

    public String getResponsabilita() {
        return responsabilita;
    }

    public void setResponsabilita(String responsabilita) {
        this.responsabilita = responsabilita;
    }

    @Override
    public String toString() {
        return nome + " - " + cognome + " - " + cdIdentificativo + " - " + tiCdIdentificativo + " - " + responsabilita;
    }

}
