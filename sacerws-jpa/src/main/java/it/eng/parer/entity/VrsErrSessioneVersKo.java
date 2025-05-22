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

/**
 * The persistent class for the VRS_ERR_SESSIONE_VERS_KO database table.
 */
@Entity
@Table(name = "VRS_ERR_SESSIONE_VERS_KO")
public class VrsErrSessioneVersKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idErrSessioneVersKo;

    private String cdErr;

    private String dsErr;

    private String flErrPrinc;

    private BigDecimal idStrut;

    private BigDecimal pgErrSessioneVers;

    private String tiErr;

    private VrsDatiSessioneVersKo vrsDatiSessioneVersKo;

    public VrsErrSessioneVersKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_ERR_SESSIONE_VERS_KO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getIdErrSessioneVersKo() {
	return this.idErrSessioneVersKo;
    }

    public void setIdErrSessioneVersKo(Long idErrSessioneVersKo) {
	this.idErrSessioneVersKo = idErrSessioneVersKo;
    }

    @Column(name = "CD_ERR")
    public String getCdErr() {
	return this.cdErr;
    }

    public void setCdErr(String cdErr) {
	this.cdErr = cdErr;
    }

    @Column(name = "DS_ERR")
    public String getDsErr() {
	return this.dsErr;
    }

    public void setDsErr(String dsErr) {
	this.dsErr = dsErr;
    }

    @Column(name = "FL_ERR_PRINC", columnDefinition = "char(1)")
    public String getFlErrPrinc() {
	return this.flErrPrinc;
    }

    public void setFlErrPrinc(String flErrPrinc) {
	this.flErrPrinc = flErrPrinc;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
	return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
	this.idStrut = idStrut;
    }

    @Column(name = "PG_ERR_SESSIONE_VERS")
    public BigDecimal getPgErrSessioneVers() {
	return this.pgErrSessioneVers;
    }

    public void setPgErrSessioneVers(BigDecimal pgErrSessioneVers) {
	this.pgErrSessioneVers = pgErrSessioneVers;
    }

    @Column(name = "TI_ERR")
    public String getTiErr() {
	return this.tiErr;
    }

    public void setTiErr(String tiErr) {
	this.tiErr = tiErr;
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
}
