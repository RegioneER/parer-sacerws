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

package it.eng.parer.util.ejb.help.dto;

import java.io.Serializable;

public class AplVGetValParamDto implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5996789178422769294L;
    private String dsValoreParamApplic;
    private String tiAppart;

    public AplVGetValParamDto() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AplVGetValParamDto(String dsValoreParamApplic, String tiAppart) {
        super();
        this.dsValoreParamApplic = dsValoreParamApplic;
        this.tiAppart = tiAppart;
    }

    public AplVGetValParamDto(String dsValoreParamApplic) {
        super();
        this.dsValoreParamApplic = dsValoreParamApplic;
    }

    public String getDsValoreParamApplic() {
        return dsValoreParamApplic;
    }

    public void setDsValoreParamApplic(String dsValoreParamApplic) {
        this.dsValoreParamApplic = dsValoreParamApplic;
    }

    public String getTiAppart() {
        return tiAppart;
    }

    public void setTiAppart(String tiAppart) {
        this.tiAppart = tiAppart;
    }
}
