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
 * The persistent class for the ARO_USO_CERTIF_CA_CONTR_MARCA database table.
 */
@Entity
@Table(name = "ARO_USO_CERTIF_CA_CONTR_MARCA")
public class AroUsoCertifCaContrMarca implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUsoCertifCaContrMarca;

    private BigDecimal pgCertifCa;

    private AroContrMarcaComp aroContrMarcaComp;

    private FirCertifCa firCertifCa;

    private FirCrl firCrl;

    private FirOcsp firOcsp;

    public AroUsoCertifCaContrMarca() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_USO_CERTIF_CA_CONTR_MARCA")
    @GenericGenerator(name = "SARO_USO_CERTIF_CA_CONTR_MARCA_ID_USO_CERTIF_CA_CONTR_MARCA_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_USO_CERTIF_CA_CONTR_MARCA"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_USO_CERTIF_CA_CONTR_MARCA_ID_USO_CERTIF_CA_CONTR_MARCA_GENERATOR")
    public Long getIdUsoCertifCaContrMarca() {
        return this.idUsoCertifCaContrMarca;
    }

    public void setIdUsoCertifCaContrMarca(Long idUsoCertifCaContrMarca) {
        this.idUsoCertifCaContrMarca = idUsoCertifCaContrMarca;
    }

    @Column(name = "PG_CERTIF_CA")
    public BigDecimal getPgCertifCa() {
        return this.pgCertifCa;
    }

    public void setPgCertifCa(BigDecimal pgCertifCa) {
        this.pgCertifCa = pgCertifCa;
    }

    // bi-directional many-to-one association to AroContrMarcaComp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONTR_MARCA_COMP", nullable = false)
    public AroContrMarcaComp getAroContrMarcaComp() {
        return this.aroContrMarcaComp;
    }

    public void setAroContrMarcaComp(AroContrMarcaComp aroContrMarcaComp) {
        this.aroContrMarcaComp = aroContrMarcaComp;
    }

    // uni-directional many-to-one association to FirCertifCa
    @ManyToOne(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_CA")
    public FirCertifCa getFirCertifCa() {
        return this.firCertifCa;
    }

    public void setFirCertifCa(FirCertifCa firCertifCa) {
        this.firCertifCa = firCertifCa;
    }

    // uni-directional many-to-one association to FirCrl
    @ManyToOne(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CRL_USATA")
    public FirCrl getFirCrl() {
        return this.firCrl;
    }

    public void setFirCrl(FirCrl firCrl) {
        this.firCrl = firCrl;
    }

    // uni-directional many-to-one association to FirCrl
    @ManyToOne(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_OCSP_USATA")
    public FirOcsp getFirOcsp() {
        return this.firOcsp;
    }

    public void setFirOcsp(FirOcsp firOcsp) {
        this.firOcsp = firOcsp;
    }

}
