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
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
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
