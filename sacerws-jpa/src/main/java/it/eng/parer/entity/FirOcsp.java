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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * The persistent class for the FIR_OCSP database table.
 * 
 */
@Entity
@Cacheable(true)
@Table(name = "FIR_OCSP")
public class FirOcsp implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idOcsp;
    private String dsCertifIssuername;
    private String dsCertifSerialBase64;
    private String dsCertifSkiBase64;

    private FirCertifOcsp firCertifOcsp;
    private FirFilePerFirma firFilePerFirma;

    public FirOcsp() {/* Hibernate */
    }

    @Id
    @SequenceGenerator(name = "FIR_OCSP_IDOCSP_GENERATOR", sequenceName = "SFIR_OCSP", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FIR_OCSP_IDOCSP_GENERATOR")
    @Column(name = "ID_OCSP")
    public Long getIdOcsp() {
        return this.idOcsp;
    }

    public void setIdOcsp(Long idOcsp) {
        this.idOcsp = idOcsp;
    }

    @Column(name = "DS_CERTIF_ISSUERNAME")
    public String getDsCertifIssuername() {
        return this.dsCertifIssuername;
    }

    public void setDsCertifIssuername(String dsCertifIssuername) {
        this.dsCertifIssuername = dsCertifIssuername;
    }

    @Column(name = "DS_CERTIF_SERIAL_BASE64")
    public String getDsCertifSerialBase64() {
        return this.dsCertifSerialBase64;
    }

    public void setDsCertifSerialBase64(String dsCertifSerialBase64) {
        this.dsCertifSerialBase64 = dsCertifSerialBase64;
    }

    @Column(name = "DS_CERTIF_SKI_BASE64")
    public String getDsCertifSkiBase64() {
        return this.dsCertifSkiBase64;
    }

    public void setDsCertifSkiBase64(String dsCertifSkiBase64) {
        this.dsCertifSkiBase64 = dsCertifSkiBase64;
    }

    // bi-directional many-to-one association to FirCertifCa
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_OCSP")
    public FirCertifOcsp getFirCertifOcsp() {
        return this.firCertifOcsp;
    }

    public void setFirCertifOcsp(FirCertifOcsp firCertifOcsp) {
        this.firCertifOcsp = firCertifOcsp;
    }

    // bi-directional one-to-one association to FirFilePerFirma
    @OneToOne(mappedBy = "firOcsp", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REFRESH }, fetch = FetchType.LAZY)
    public FirFilePerFirma getFirFilePerFirma() {
        return this.firFilePerFirma;
    }

    public void setFirFilePerFirma(FirFilePerFirma firFilePerFirma) {
        this.firFilePerFirma = firFilePerFirma;
    }

}
