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
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the FIR_URL_DISTRIB_CRL database table.
 */
@Entity
@Table(name = "FIR_URL_DISTRIB_CRL")
public class FirUrlDistribCrl implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUrlDistribCrl;

    private String dlUrlDistribCrl;

    private BigDecimal niOrdUrlDistribCrl;

    private FirCertifCa firCertifCa;

    public FirUrlDistribCrl() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_URL_DISTRIB_CRL")
    @GenericGenerator(name = "SFIR_URL_DISTRIB_CRL_ID_URL_DISTRIB_CRL_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFIR_URL_DISTRIB_CRL"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFIR_URL_DISTRIB_CRL_ID_URL_DISTRIB_CRL_GENERATOR")
    public Long getIdUrlDistribCrl() {
        return this.idUrlDistribCrl;
    }

    public void setIdUrlDistribCrl(Long idUrlDistribCrl) {
        this.idUrlDistribCrl = idUrlDistribCrl;
    }

    @Column(name = "DL_URL_DISTRIB_CRL")
    public String getDlUrlDistribCrl() {
        return this.dlUrlDistribCrl;
    }

    public void setDlUrlDistribCrl(String dlUrlDistribCrl) {
        this.dlUrlDistribCrl = dlUrlDistribCrl;
    }

    @Column(name = "NI_ORD_URL_DISTRIB_CRL")
    public BigDecimal getNiOrdUrlDistribCrl() {
        return this.niOrdUrlDistribCrl;
    }

    public void setNiOrdUrlDistribCrl(BigDecimal niOrdUrlDistribCrl) {
        this.niOrdUrlDistribCrl = niOrdUrlDistribCrl;
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
