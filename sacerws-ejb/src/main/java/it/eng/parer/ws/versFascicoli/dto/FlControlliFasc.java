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
package it.eng.parer.ws.versFascicoli.dto;

/**
 *
 * @author sinattti_s
 */
public class FlControlliFasc {

    boolean flAbilitaContrClassif;
    boolean flAbilitaContrColleg;
    boolean flAbilitaContrNumero;
    boolean flAccettaContrClassifNeg;
    boolean flAccettaContrCollegNeg;
    boolean flAccettaContrNumeroNeg;
    boolean flForzaContrFlassif;
    boolean flForzaContrColleg;
    boolean flForzaContrNumero;

    public boolean isFlAbilitaContrClassif() {
        return flAbilitaContrClassif;
    }

    public void setFlAbilitaContrClassif(boolean flAbilitaContrClassif) {
        this.flAbilitaContrClassif = flAbilitaContrClassif;
    }

    public boolean isFlAbilitaContrColleg() {
        return flAbilitaContrColleg;
    }

    public void setFlAbilitaContrColleg(boolean flAbilitaContrColleg) {
        this.flAbilitaContrColleg = flAbilitaContrColleg;
    }

    public boolean isFlAbilitaContrNumero() {
        return flAbilitaContrNumero;
    }

    public void setFlAbilitaContrNumero(boolean flAbilitaContrNumero) {
        this.flAbilitaContrNumero = flAbilitaContrNumero;
    }

    public boolean isFlAccettaContrClassifNeg() {
        return flAccettaContrClassifNeg;
    }

    public void setFlAccettaContrClassifNeg(boolean flAccettaContrClassifNeg) {
        this.flAccettaContrClassifNeg = flAccettaContrClassifNeg;
    }

    public boolean isFlAccettaContrCollegNeg() {
        return flAccettaContrCollegNeg;
    }

    public void setFlAccettaContrCollegNeg(boolean flAccettaContrCollegNeg) {
        this.flAccettaContrCollegNeg = flAccettaContrCollegNeg;
    }

    public boolean isFlAccettaContrNumeroNeg() {
        return flAccettaContrNumeroNeg;
    }

    public void setFlAccettaContrNumeroNeg(boolean flAccettaContrNumeroNeg) {
        this.flAccettaContrNumeroNeg = flAccettaContrNumeroNeg;
    }

    public boolean isFlForzaContrFlassif() {
        return flForzaContrFlassif;
    }

    public void setFlForzaContrFlassif(boolean flForzaContrFlassif) {
        this.flForzaContrFlassif = flForzaContrFlassif;
    }

    public boolean isFlForzaContrColleg() {
        return flForzaContrColleg;
    }

    public void setFlForzaContrColleg(boolean flForzaContrColleg) {
        this.flForzaContrColleg = flForzaContrColleg;
    }

    public boolean isFlForzaContrNumero() {
        return flForzaContrNumero;
    }

    public void setFlForzaContrNumero(boolean flForzaContrNumero) {
        this.flForzaContrNumero = flForzaContrNumero;
    }

}
