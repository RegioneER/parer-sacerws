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

package it.eng.parer.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the VRS_FILE_SESSIONE_KO database table.
 */
@Entity
@Table(name = "VRS_FILE_SESSIONE_KO")
public class VrsFileSessioneKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFileSessioneKo;

    private BigDecimal idStrut;

    private String nmFileSessione;

    private BigDecimal pgFileSessione;

    private String tiStatoFileSessione;

    private VrsDatiSessioneVersKo vrsDatiSessioneVersKo;

    private List<VrsFileSesObjectStorageKo> fileSesObjectStorageKos = new ArrayList<>();

    public VrsFileSessioneKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_FILE_SESSIONE_KO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getIdFileSessioneKo() {
        return this.idFileSessioneKo;
    }

    public void setIdFileSessioneKo(Long idFileSessioneKo) {
        this.idFileSessioneKo = idFileSessioneKo;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "NM_FILE_SESSIONE")
    public String getNmFileSessione() {
        return this.nmFileSessione;
    }

    public void setNmFileSessione(String nmFileSessione) {
        this.nmFileSessione = nmFileSessione;
    }

    @Column(name = "PG_FILE_SESSIONE")
    public BigDecimal getPgFileSessione() {
        return this.pgFileSessione;
    }

    public void setPgFileSessione(BigDecimal pgFileSessione) {
        this.pgFileSessione = pgFileSessione;
    }

    @Column(name = "TI_STATO_FILE_SESSIONE")
    public String getTiStatoFileSessione() {
        return this.tiStatoFileSessione;
    }

    public void setTiStatoFileSessione(String tiStatoFileSessione) {
        this.tiStatoFileSessione = tiStatoFileSessione;
    }

    // bi-directional many-to-one association to VrsDatiSessioneVersKo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DATI_SESSIONE_VERS_KO")
    public VrsDatiSessioneVersKo getVrsDatiSessioneVersKo() {
        return this.vrsDatiSessioneVersKo;
    }

    public void setVrsDatiSessioneVersKo(VrsDatiSessioneVersKo vrsDatiSessioneVersKo) {
        this.vrsDatiSessioneVersKo = vrsDatiSessioneVersKo;
    }

    @OneToMany(mappedBy = "fileSessioneKo")
    public List<VrsFileSesObjectStorageKo> getFileSesObjectStorageKos() {
        return fileSesObjectStorageKos;
    }

    public void setFileSesObjectStorageKos(
            List<VrsFileSesObjectStorageKo> fileSesObjectStorageKos) {
        this.fileSesObjectStorageKos = fileSesObjectStorageKos;
    }

}
