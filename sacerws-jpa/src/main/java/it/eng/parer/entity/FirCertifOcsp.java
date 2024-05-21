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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the FIR_CERTIF_OCSP database table.
 * 
 */
@Entity
@Cacheable(true)
@Table(name = "FIR_CERTIF_OCSP")
public class FirCertifOcsp implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idCertifOcsp;
    private Date dtFinValCertifOcsp;
    private Date dtIniValCertifOcsp;
    private BigDecimal niSerialCertifOcsp;
    private String dlDnSubjectCertifOcsp;

    private FirCertifCa firCertifCa;
    private FirFilePerFirma firFilePerFirma;
    private List<FirOcsp> firOcsps = new ArrayList<>();

    public FirCertifOcsp() {
        // hibernate
    }

    @Id
    @SequenceGenerator(name = "FIR_CERTIF_OCSP_IDCERTIFFIRMATARIO_GENERATOR", sequenceName = "SFIR_CERTIF_OCSP", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FIR_CERTIF_OCSP_IDCERTIFFIRMATARIO_GENERATOR")
    @Column(name = "ID_CERTIF_OCSP")
    public Long getIdCertifOcsp() {
        return this.idCertifOcsp;
    }

    public void setIdCertifOcsp(Long idCertifOcsp) {
        this.idCertifOcsp = idCertifOcsp;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_FIN_VAL_CERTIF_OCSP")
    public Date getDtFinValCertifOcsp() {
        return this.dtFinValCertifOcsp;
    }

    public void setDtFinValCertifOcsp(Date dtFinValCertifOcsp) {
        this.dtFinValCertifOcsp = dtFinValCertifOcsp;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_INI_VAL_CERTIF_OCSP")
    public Date getDtIniValCertifOcsp() {
        return this.dtIniValCertifOcsp;
    }

    public void setDtIniValCertifOcsp(Date dtIniValCertifOcsp) {
        this.dtIniValCertifOcsp = dtIniValCertifOcsp;
    }

    @Column(name = "NI_SERIAL_CERTIF_OCSP")
    public BigDecimal getNiSerialCertifOcsp() {
        return this.niSerialCertifOcsp;
    }

    public void setNiSerialCertifOcsp(BigDecimal niSerialCertifOcsp) {
        this.niSerialCertifOcsp = niSerialCertifOcsp;
    }

    // bi-directional many-to-one association to FirCertifCa
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_CA")
    public FirCertifCa getFirCertifCa() {
        return this.firCertifCa;
    }

    public void setFirCertifCa(FirCertifCa firCertifCa) {
        this.firCertifCa = firCertifCa;
    }

    @Column(name = "DL_DN_SUBJECT_CERTIF_OCSP")
    public String getDlDnSubjectCertifOcsp() {
        return dlDnSubjectCertifOcsp;
    }

    public void setDlDnSubjectCertifOcsp(String dlDnSubjectCertifOcsp) {
        this.dlDnSubjectCertifOcsp = dlDnSubjectCertifOcsp;
    }

    // bi-directional one-to-one association to FirFilePerFirma
    @OneToOne(mappedBy = "firCertifOcsp", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REFRESH }, fetch = FetchType.LAZY)
    public FirFilePerFirma getFirFilePerFirma() {
        return this.firFilePerFirma;
    }

    public void setFirFilePerFirma(FirFilePerFirma firFilePerFirma) {
        this.firFilePerFirma = firFilePerFirma;
    }

    // bi-directional many-to-one association to FirCrl
    @OneToMany(mappedBy = "firCertifOcsp", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<FirOcsp> getFirOcsps() {
        return this.firOcsps;
    }

    public void setFirOcsps(List<FirOcsp> firOcsps) {
        this.firOcsps = firOcsps;
    }

}
