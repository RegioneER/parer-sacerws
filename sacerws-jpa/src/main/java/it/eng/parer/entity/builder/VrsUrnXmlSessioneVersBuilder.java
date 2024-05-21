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

import it.eng.parer.entity.VrsUrnXmlSessioneVers;
import it.eng.parer.entity.VrsUrnXmlSessioneVersKo;
import it.eng.parer.entity.VrsXmlDatiSessioneVers;
import it.eng.parer.entity.VrsXmlDatiSessioneVersKo;
import it.eng.parer.entity.constraint.VrsUrnXmlSessioneVers.TiUrnXmlSessioneVers;

import java.math.BigDecimal;
import java.util.Optional;

public final class VrsUrnXmlSessioneVersBuilder {
    private String dsUrn;
    private TiUrnXmlSessioneVers tiUrn;
    private Optional<VrsXmlDatiSessioneVers> vrsXmlDatiSessioneVers;
    private Optional<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKo;
    private BigDecimal idStrut;

    private VrsUrnXmlSessioneVersBuilder() {
    }

    public static VrsUrnXmlSessioneVersBuilder builder() {
        return new VrsUrnXmlSessioneVersBuilder();
    }

    public VrsUrnXmlSessioneVersBuilder dsUrn(String dsUrn) {
        this.dsUrn = dsUrn;
        return this;
    }

    public VrsUrnXmlSessioneVersBuilder tiUrn(TiUrnXmlSessioneVers tiUrn) {
        this.tiUrn = tiUrn;
        return this;
    }

    public VrsUrnXmlSessioneVersBuilder vrsXmlDatiSessioneVers(
            Optional<VrsXmlDatiSessioneVers> vrsXmlDatiSessioneVers) {
        this.vrsXmlDatiSessioneVers = vrsXmlDatiSessioneVers;
        return this;
    }

    public VrsUrnXmlSessioneVersBuilder vrsXmlDatiSessioneVersKo(
            Optional<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKo) {
        this.vrsXmlDatiSessioneVersKo = vrsXmlDatiSessioneVersKo;
        return this;
    }

    public VrsUrnXmlSessioneVersBuilder idStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
        return this;
    }

    public VrsUrnXmlSessioneVers buildVrsUrnXmlSessioneVers() {
        VrsUrnXmlSessioneVers vrsUrnXmlSessioneVers = new VrsUrnXmlSessioneVers();
        vrsUrnXmlSessioneVers.setDsUrn(dsUrn);
        vrsUrnXmlSessioneVers.setTiUrn(tiUrn);
        vrsUrnXmlSessioneVers.setVrsXmlDatiSessioneVers(vrsXmlDatiSessioneVers.get());
        return vrsUrnXmlSessioneVers;
    }

    public VrsUrnXmlSessioneVersKo buildVrsUrnXmlSessioneVersKo() {
        VrsUrnXmlSessioneVersKo vrsUrnXmlSessioneVers = new VrsUrnXmlSessioneVersKo();
        vrsUrnXmlSessioneVers.setDsUrn(dsUrn);
        vrsUrnXmlSessioneVers.setTiUrn(tiUrn);
        vrsUrnXmlSessioneVers.setIdStrut(idStrut);
        vrsUrnXmlSessioneVers.setVrsXmlDatiSessioneVersKo(vrsXmlDatiSessioneVersKo.get());
        return vrsUrnXmlSessioneVers;
    }
}
