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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import it.eng.parer.entity.constraint.FiUrnReport.TiUrnReport;

/**
 * The persistent class for the ELV_URN_ELENCO_VERS database table.
 *
 */
@Entity
@Table(name = "FIR_URN_REPORT")
public class FirUrnReport implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idUrnReport;
    private String dsUrn;
    private TiUrnReport tiUrn;
    private FirReport firReport;

    public FirUrnReport() {/* Hibernate */
    }

    @Id
    @SequenceGenerator(name = "FIR_URN_REPORT_IDURNREPORT_GENERATOR", sequenceName = "SFIR_URN_REPORT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FIR_URN_REPORT_IDURNREPORT_GENERATOR")
    @Column(name = "ID_URN_REPORT")
    public Long getIdUrnReport() {
	return this.idUrnReport;
    }

    public void setIdUrnReport(Long idUrnReport) {
	this.idUrnReport = idUrnReport;
    }

    @Column(name = "DS_URN")
    public String getDsUrn() {
	return this.dsUrn;
    }

    public void setDsUrn(String dsUrn) {
	this.dsUrn = dsUrn;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_URN")
    public TiUrnReport getTiUrn() {
	return this.tiUrn;
    }

    public void setTiUrn(TiUrnReport tiUrn) {
	this.tiUrn = tiUrn;
    }

    // bi-directional many-to-one association to FirReport
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REPORT")
    public FirReport getFirReport() {
	return this.firReport;
    }

    public void setFirReport(FirReport firReport) {
	this.firReport = firReport;
    }

}
