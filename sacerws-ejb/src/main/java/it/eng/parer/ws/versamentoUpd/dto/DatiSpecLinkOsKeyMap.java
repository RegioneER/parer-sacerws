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

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author DiLorenzo_F
 */
public class DatiSpecLinkOsKeyMap implements Serializable {

    private static final long serialVersionUID = 4124405579586404634L;

    private Long idEntitySacer;
    private String tipiEntitaSacer;

    public DatiSpecLinkOsKeyMap(Long idEntitySacer, String tipiEntitaSacer) {
        this.idEntitySacer = idEntitySacer;
        this.tipiEntitaSacer = tipiEntitaSacer;
    }

    public Long getIdEntitySacer() {
        return idEntitySacer;
    }

    public void setIdEntitySacer(Long idEntitySacer) {
        this.idEntitySacer = idEntitySacer;
    }

    public String getTipiEntitaSacer() {
        return tipiEntitaSacer;
    }

    public void setTipiEntitaSacer(String tipiEntitaSacer) {
        this.tipiEntitaSacer = tipiEntitaSacer;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.idEntitySacer);
        hash = 67 * hash + Objects.hashCode(this.tipiEntitaSacer);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DatiSpecLinkOsKeyMap other = (DatiSpecLinkOsKeyMap) obj;
        if (!Objects.equals(this.tipiEntitaSacer, other.tipiEntitaSacer)) {
            return false;
        }
        if (!Objects.equals(this.idEntitySacer, other.idEntitySacer)) {
            return false;
        }
        return true;
    }

}
