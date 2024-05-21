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

import it.eng.parer.entity.VrsDatiSessioneVers;
import it.eng.parer.entity.VrsDatiSessioneVersKo;
import it.eng.parer.entity.VrsErrSessioneVers;
import it.eng.parer.entity.VrsErrSessioneVersKo;

import java.math.BigDecimal;

public final class VrsErrSessioneVersBuilder {
    private String cdErr;
    private String dsErr;
    private String flErrPrinc;
    private BigDecimal idStrut;
    private BigDecimal pgErrSessioneVers;
    private String tiErr;
    private VrsDatiSessioneVersKo vrsDatiSessioneVersKo;
    private VrsDatiSessioneVers vrsDatiSessioneVers;

    private VrsErrSessioneVersBuilder() {
    }

    public static VrsErrSessioneVersBuilder builder() {
        return new VrsErrSessioneVersBuilder();
    }

    public VrsErrSessioneVersBuilder cdErr(String cdErr) {
        this.cdErr = cdErr;
        return this;
    }

    public VrsErrSessioneVersBuilder dsErr(String dsErr) {
        this.dsErr = dsErr;
        return this;
    }

    public VrsErrSessioneVersBuilder flErrPrinc(String flErrPrinc) {
        this.flErrPrinc = flErrPrinc;
        return this;
    }

    public VrsErrSessioneVersBuilder idStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
        return this;
    }

    public VrsErrSessioneVersBuilder pgErrSessioneVers(BigDecimal pgErrSessioneVers) {
        this.pgErrSessioneVers = pgErrSessioneVers;
        return this;
    }

    public VrsErrSessioneVersBuilder tiErr(String tiErr) {
        this.tiErr = tiErr;
        return this;
    }

    public VrsErrSessioneVersBuilder vrsDatiSessioneVersKo(VrsDatiSessioneVersKo vrsDatiSessioneVersKo) {
        this.vrsDatiSessioneVersKo = vrsDatiSessioneVersKo;
        return this;
    }

    public VrsErrSessioneVersBuilder vrsDatiSessioneVers(VrsDatiSessioneVers vrsDatiSessioneVers) {
        this.vrsDatiSessioneVers = vrsDatiSessioneVers;
        return this;
    }

    public VrsErrSessioneVersKo buildVrsErrSessioneVersKo() {
        VrsErrSessioneVersKo vrsErrSessioneVers = new VrsErrSessioneVersKo();
        vrsErrSessioneVers.setCdErr(cdErr);
        vrsErrSessioneVers.setDsErr(dsErr);
        vrsErrSessioneVers.setFlErrPrinc(flErrPrinc);
        vrsErrSessioneVers.setIdStrut(idStrut);
        vrsErrSessioneVers.setPgErrSessioneVers(pgErrSessioneVers);
        vrsErrSessioneVers.setTiErr(tiErr);
        vrsErrSessioneVers.setVrsDatiSessioneVersKo(vrsDatiSessioneVersKo);
        return vrsErrSessioneVers;
    }

    public VrsErrSessioneVers buildVrsErrSessioneVers() {
        VrsErrSessioneVers vrsErrSessioneVers = new VrsErrSessioneVers();
        vrsErrSessioneVers.setCdErr(cdErr);
        vrsErrSessioneVers.setDsErr(dsErr);
        vrsErrSessioneVers.setFlErrPrinc(flErrPrinc);
        vrsErrSessioneVers.setIdStrut(idStrut);
        vrsErrSessioneVers.setPgErrSessioneVers(pgErrSessioneVers);
        vrsErrSessioneVers.setTiErr(tiErr);
        vrsErrSessioneVers.setVrsDatiSessioneVers(vrsDatiSessioneVers);
        return vrsErrSessioneVers;
    }
}
