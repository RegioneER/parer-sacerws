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

package it.eng.parer.entity.builder;

import it.eng.parer.entity.VrsDatiSessioneVersKo;
import it.eng.parer.entity.VrsFileSessioneKo;

import java.math.BigDecimal;

public final class VrsFileSessioneBuilder {
    private BigDecimal idStrut;
    private String nmFileSessione;
    private BigDecimal pgFileSessione;
    private String tiStatoFileSessione;
    private VrsDatiSessioneVersKo vrsDatiSessioneVersKo;

    private VrsFileSessioneBuilder() {
    }

    public static VrsFileSessioneBuilder builder() {
        return new VrsFileSessioneBuilder();
    }

    public VrsFileSessioneBuilder idStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
        return this;
    }

    public VrsFileSessioneBuilder nmFileSessione(String nmFileSessione) {
        this.nmFileSessione = nmFileSessione;
        return this;
    }

    public VrsFileSessioneBuilder pgFileSessione(BigDecimal pgFileSessione) {
        this.pgFileSessione = pgFileSessione;
        return this;
    }

    public VrsFileSessioneBuilder tiStatoFileSessione(String tiStatoFileSessione) {
        this.tiStatoFileSessione = tiStatoFileSessione;
        return this;
    }

    public VrsFileSessioneBuilder vrsDatiSessioneVersKo(VrsDatiSessioneVersKo vrsDatiSessioneVersKo) {
        this.vrsDatiSessioneVersKo = vrsDatiSessioneVersKo;
        return this;
    }

    public VrsFileSessioneKo buildVrsFileSessioneKo() {
        VrsFileSessioneKo vrsFileSessione = new VrsFileSessioneKo();
        vrsFileSessione.setIdStrut(idStrut);
        vrsFileSessione.setNmFileSessione(nmFileSessione);
        vrsFileSessione.setPgFileSessione(pgFileSessione);
        vrsFileSessione.setTiStatoFileSessione(tiStatoFileSessione);
        vrsFileSessione.setVrsDatiSessioneVersKo(vrsDatiSessioneVersKo);
        return vrsFileSessione;
    }
}
