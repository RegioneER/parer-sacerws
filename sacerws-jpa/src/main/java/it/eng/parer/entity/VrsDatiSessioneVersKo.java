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

package it.eng.parer.entity;

import java.util.ArrayList;
import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * The persistent class for the VRS_DATI_SESSIONE_VERS_KO database table.
 */
@Entity
@Table(name = "VRS_DATI_SESSIONE_VERS_KO")
public class VrsDatiSessioneVersKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idDatiSessioneVersKo;

    private String cdKeyAlleg;

    private BigDecimal idStrut;

    private BigDecimal niFile;

    private BigDecimal pgDatiSessioneVers;

    private String tiDatiSessioneVers;

    private VrsSessioneVersKo vrsSessioneVersKo;

    private List<VrsErrSessioneVersKo> vrsErrSessioneVersKos = new ArrayList<>();

    private List<VrsFileSessioneKo> vrsFileSessioneKos = new ArrayList<>();

    private List<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKos = new ArrayList<>();

    private List<VrsXmlDatiSesObjectStorageKo> xmlDatiSesObjectStorageKos = new ArrayList<>();

    public VrsDatiSessioneVersKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_DATI_SESSIONE_VERS_KO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getIdDatiSessioneVersKo() {
        return this.idDatiSessioneVersKo;
    }

    public void setIdDatiSessioneVersKo(Long idDatiSessioneVersKo) {
        this.idDatiSessioneVersKo = idDatiSessioneVersKo;
    }

    @Column(name = "CD_KEY_ALLEG")
    public String getCdKeyAlleg() {
        return this.cdKeyAlleg;
    }

    public void setCdKeyAlleg(String cdKeyAlleg) {
        this.cdKeyAlleg = cdKeyAlleg;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "NI_FILE")
    public BigDecimal getNiFile() {
        return this.niFile;
    }

    public void setNiFile(BigDecimal niFile) {
        this.niFile = niFile;
    }

    @Column(name = "PG_DATI_SESSIONE_VERS")
    public BigDecimal getPgDatiSessioneVers() {
        return this.pgDatiSessioneVers;
    }

    public void setPgDatiSessioneVers(BigDecimal pgDatiSessioneVers) {
        this.pgDatiSessioneVers = pgDatiSessioneVers;
    }

    @Column(name = "TI_DATI_SESSIONE_VERS")
    public String getTiDatiSessioneVers() {
        return this.tiDatiSessioneVers;
    }

    public void setTiDatiSessioneVers(String tiDatiSessioneVers) {
        this.tiDatiSessioneVers = tiDatiSessioneVers;
    }

    // bi-directional many-to-one association to VrsSessioneVersKo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SESSIONE_VERS_KO")
    public VrsSessioneVersKo getVrsSessioneVersKo() {
        return this.vrsSessioneVersKo;
    }

    public void setVrsSessioneVersKo(VrsSessioneVersKo vrsSessioneVersKo) {
        this.vrsSessioneVersKo = vrsSessioneVersKo;
    }

    // bi-directional many-to-one association to VrsErrSessioneVersKo
    @OneToMany(mappedBy = "vrsDatiSessioneVersKo")
    public List<VrsErrSessioneVersKo> getVrsErrSessioneVersKos() {
        return this.vrsErrSessioneVersKos;
    }

    public void setVrsErrSessioneVersKos(List<VrsErrSessioneVersKo> vrsErrSessioneVersKos) {
        this.vrsErrSessioneVersKos = vrsErrSessioneVersKos;
    }

    // bi-directional many-to-one association to VrsFileSessioneKo
    @OneToMany(mappedBy = "vrsDatiSessioneVersKo")
    public List<VrsFileSessioneKo> getVrsFileSessioneKos() {
        return this.vrsFileSessioneKos;
    }

    public void setVrsFileSessioneKos(List<VrsFileSessioneKo> vrsFileSessioneKos) {
        this.vrsFileSessioneKos = vrsFileSessioneKos;
    }

    // bi-directional many-to-one association to VrsXmlDatiSessioneVersKo
    @OneToMany(mappedBy = "vrsDatiSessioneVersKo")
    public List<VrsXmlDatiSessioneVersKo> getVrsXmlDatiSessioneVersKos() {
        return this.vrsXmlDatiSessioneVersKos;
    }

    public void setVrsXmlDatiSessioneVersKos(List<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKos) {
        this.vrsXmlDatiSessioneVersKos = vrsXmlDatiSessioneVersKos;
    }

    @OneToMany(mappedBy = "datiSessioneVersKo")
    public List<VrsXmlDatiSesObjectStorageKo> getXmlDatiSesObjectStorageKos() {
        return xmlDatiSesObjectStorageKos;
    }

    public void setXmlDatiSesObjectStorageKos(List<VrsXmlDatiSesObjectStorageKo> xmlDatiSesObjectStorageKos) {
        this.xmlDatiSesObjectStorageKos = xmlDatiSesObjectStorageKos;
    }

}
