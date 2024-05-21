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

import javax.persistence.CascadeType;
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
 * The persistent class for the ARO_USO_CERTIF_CA_CONTR_COMP database table.
 */
@Entity
@Table(name = "ARO_USO_CERTIF_CA_CONTR_COMP")
public class AroUsoCertifCaContrComp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUsoCertifCaContrComp;

    private BigDecimal pgCertifCa;

    private AroContrFirmaComp aroContrFirmaComp;

    private FirCertifCa firCertifCa;

    private FirCrl firCrl;

    private FirOcsp firOcsp;

    public AroUsoCertifCaContrComp() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_USO_CERTIF_CA_CONTR_COMP")
    @GenericGenerator(name = "SARO_USO_CERTIF_CA_CONTR_COMP_ID_USO_CERTIF_CA_CONTR_COMP_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_USO_CERTIF_CA_CONTR_COMP"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_USO_CERTIF_CA_CONTR_COMP_ID_USO_CERTIF_CA_CONTR_COMP_GENERATOR")
    public Long getIdUsoCertifCaContrComp() {
        return this.idUsoCertifCaContrComp;
    }

    public void setIdUsoCertifCaContrComp(Long idUsoCertifCaContrComp) {
        this.idUsoCertifCaContrComp = idUsoCertifCaContrComp;
    }

    @Column(name = "PG_CERTIF_CA")
    public BigDecimal getPgCertifCa() {
        return this.pgCertifCa;
    }

    public void setPgCertifCa(BigDecimal pgCertifCa) {
        this.pgCertifCa = pgCertifCa;
    }

    // bi-directional many-to-one association to AroContrFirmaComp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONTR_FIRMA_COMP", nullable = false)
    public AroContrFirmaComp getAroContrFirmaComp() {
        return this.aroContrFirmaComp;
    }

    public void setAroContrFirmaComp(AroContrFirmaComp aroContrFirmaComp) {
        this.aroContrFirmaComp = aroContrFirmaComp;
    }

    // uni-directional many-to-one association to FirCertifCa
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_CA")
    public FirCertifCa getFirCertifCa() {
        return this.firCertifCa;
    }

    public void setFirCertifCa(FirCertifCa firCertifCa) {
        this.firCertifCa = firCertifCa;
    }

    // uni-directional many-to-one association to FirCrl
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CRL_USATA")
    public FirCrl getFirCrl() {
        return this.firCrl;
    }

    public void setFirCrl(FirCrl firCrl) {
        this.firCrl = firCrl;
    }

    // uni-directional many-to-one association to FirCrl
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_OCSP_USATA")
    public FirOcsp getFirOcsp() {
        return this.firOcsp;
    }

    public void setFirOcsp(FirOcsp firOcsp) {
        this.firOcsp = firOcsp;
    }
}
