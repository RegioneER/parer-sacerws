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

public final class VrsXmlDatiSesObjectStorageBuilder {
    private DecBackend decBackend;
    private VrsDatiSessioneVersKo datiSessioneVersKo;
    private String nmTenant;
    private String nmBucket;
    private String nmKeyFile;
    private BigDecimal idStrut;

    private VrsXmlDatiSesObjectStorageBuilder() {
    }

    public static VrsXmlDatiSesObjectStorageBuilder builder() {
        return new VrsXmlDatiSesObjectStorageBuilder();
    }

    public VrsXmlDatiSesObjectStorageBuilder decBackend(DecBackend decBackend) {
        this.decBackend = decBackend;
        return this;
    }

    public VrsXmlDatiSesObjectStorageBuilder datiSessioneVersKo(VrsDatiSessioneVersKo datiSessioneVersKo) {
        this.datiSessioneVersKo = datiSessioneVersKo;
        return this;
    }

    public VrsXmlDatiSesObjectStorageBuilder nmTenant(String nmTenant) {
        this.nmTenant = nmTenant;
        return this;
    }

    public VrsXmlDatiSesObjectStorageBuilder nmBucket(String nmBucket) {
        this.nmBucket = nmBucket;
        return this;
    }

    public VrsXmlDatiSesObjectStorageBuilder nmKeyFile(String nmKeyFile) {
        this.nmKeyFile = nmKeyFile;
        return this;
    }

    public VrsXmlDatiSesObjectStorageBuilder idStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
        return this;
    }

    public VrsXmlDatiSesObjectStorageKo buildVrsXmlDatiSesObjectStorageKo() {
        VrsXmlDatiSesObjectStorageKo vrsXmlDatiSesObjectStorage = new VrsXmlDatiSesObjectStorageKo();
        vrsXmlDatiSesObjectStorage.setDecBackend(decBackend);
        vrsXmlDatiSesObjectStorage.setDatiSessioneVersKo(datiSessioneVersKo);
        vrsXmlDatiSesObjectStorage.setNmTenant(nmTenant);
        vrsXmlDatiSesObjectStorage.setNmBucket(nmBucket);
        vrsXmlDatiSesObjectStorage.setNmKeyFile(nmKeyFile);
        vrsXmlDatiSesObjectStorage.setIdStrut(idStrut);
        return vrsXmlDatiSesObjectStorage;
    }
}
