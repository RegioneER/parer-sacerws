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

import it.eng.parer.entity.*;

import java.math.BigDecimal;

public final class VrsFileSesObjectStorageBuilder {
    private DecBackend decBackend;
    private VrsFileSessioneKo fileSessioneKo;
    private String nmTenant;
    private String nmBucket;
    private String nmKeyFile;
    private BigDecimal idStrut;

    private VrsFileSesObjectStorageBuilder() {
    }

    public static VrsFileSesObjectStorageBuilder builder() {
        return new VrsFileSesObjectStorageBuilder();
    }

    public VrsFileSesObjectStorageBuilder decBackend(DecBackend decBackend) {
        this.decBackend = decBackend;
        return this;
    }

    public VrsFileSesObjectStorageBuilder fileSessioneKo(VrsFileSessioneKo fileSessioneKo) {
        this.fileSessioneKo = fileSessioneKo;
        return this;
    }

    public VrsFileSesObjectStorageBuilder nmTenant(String nmTenant) {
        this.nmTenant = nmTenant;
        return this;
    }

    public VrsFileSesObjectStorageBuilder nmBucket(String nmBucket) {
        this.nmBucket = nmBucket;
        return this;
    }

    public VrsFileSesObjectStorageBuilder nmKeyFile(String nmKeyFile) {
        this.nmKeyFile = nmKeyFile;
        return this;
    }

    public VrsFileSesObjectStorageBuilder idStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
        return this;
    }

    public VrsFileSesObjectStorageKo buildVrsFileSesObjectStorageKo() {
        VrsFileSesObjectStorageKo vrsFileSesObjectStorageKo = new VrsFileSesObjectStorageKo();
        vrsFileSesObjectStorageKo.setDecBackend(decBackend);
        vrsFileSesObjectStorageKo.setFileSessioneKo(fileSessioneKo);
        vrsFileSesObjectStorageKo.setNmTenant(nmTenant);
        vrsFileSesObjectStorageKo.setNmBucket(nmBucket);
        vrsFileSesObjectStorageKo.setNmKeyFile(nmKeyFile);
        vrsFileSesObjectStorageKo.setIdStrut(idStrut);
        return vrsFileSesObjectStorageKo;
    }
}
