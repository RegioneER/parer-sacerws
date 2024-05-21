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
public class UpdFascPrincipale {

    private String classifica;
    private String identificativo;
    private String oggetto;
    private String sottoIdentificativo;
    private String sottoOggetto;

    public String getClassifica() {
        return classifica;
    }

    public void setClassifica(String classifica) {
        this.classifica = classifica;
    }

    public String getIdentificativo() {
        return identificativo;
    }

    public void setIdentificativo(String identificativo) {
        this.identificativo = identificativo;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public String getSottoIdentificativo() {
        return sottoIdentificativo;
    }

    public void setSottoIdentificativo(String sottoIdentificativo) {
        this.sottoIdentificativo = sottoIdentificativo;
    }

    public String getSottoOggetto() {
        return sottoOggetto;
    }

    public void setSottoOggetto(String sottoOggetto) {
        this.sottoOggetto = sottoOggetto;
    }

}
