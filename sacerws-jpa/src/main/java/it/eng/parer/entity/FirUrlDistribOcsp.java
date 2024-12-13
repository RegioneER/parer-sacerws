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

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * The persistent class for the FIR_URL_DISTRIB_CRL database table.
 *
 */
@Entity
@Table(name = "FIR_URL_DISTRIB_OCSP")
public class FirUrlDistribOcsp implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idUrlDistribOcsp;
    private String dlUrlDistribOcsp;
    private BigDecimal niOrdUrlDistribOcsp;
    private FirCertifCa firCertifCa;

    public FirUrlDistribOcsp() {/* Hibernate */
    }

    @Id
    @SequenceGenerator(name = "FIR_URL_DISTRIB_OCSP_IDURLDISTRIBOCSP_GENERATOR", sequenceName = "SFIR_URL_DISTRIB_OCSP", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FIR_URL_DISTRIB_OCSP_IDURLDISTRIBOCSP_GENERATOR")
    @Column(name = "ID_URL_DISTRIB_OCSP")
    public Long getIdUrlDistribOcsp() {
        return this.idUrlDistribOcsp;
    }

    public void setIdUrlDistribOcsp(Long idUrlDistribOcsp) {
        this.idUrlDistribOcsp = idUrlDistribOcsp;
    }

    @Column(name = "DL_URL_DISTRIB_OCSP")
    public String getDlUrlDistribOcsp() {
        return this.dlUrlDistribOcsp;
    }

    public void setDlUrlDistribOcsp(String dlUrlDistribOcsp) {
        this.dlUrlDistribOcsp = dlUrlDistribOcsp;
    }

    @Column(name = "NI_ORD_URL_DISTRIB_OCSP")
    public BigDecimal getNiOrdUrlDistribOcsp() {
        return this.niOrdUrlDistribOcsp;
    }

    public void setNiOrdUrlDistribOcsp(BigDecimal niOrdUrlDistribOcsp) {
        this.niOrdUrlDistribOcsp = niOrdUrlDistribOcsp;
    }

    // bi-directional many-to-one association to FirCertifCa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_CA")
    public FirCertifCa getFirCertifCa() {
        return this.firCertifCa;
    }

    public void setFirCertifCa(FirCertifCa firCertifCa) {
        this.firCertifCa = firCertifCa;
    }

}
