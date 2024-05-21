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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the FIR_FILE_PER_FIRMA database table.
 */
@Entity
@Table(name = "FIR_FILE_PER_FIRMA")
public class FirFilePerFirma implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFilePerFirma;

    private byte[] blFilePerFirma;

    private String nmFileDownload;

    private String tiFilePerFirma;

    private FirCertifCa firCertifCa;

    private FirCertifFirmatario firCertifFirmatario;

    private FirCertifOcsp firCertifOcsp;

    private FirCrl firCrl;

    private FirOcsp firOcsp;

    public FirFilePerFirma() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_FILE_PER_FIRMA")
    @GenericGenerator(name = "SFIR_FILE_PER_FIRMA_ID_FILE_PER_FIRMA_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFIR_FILE_PER_FIRMA"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFIR_FILE_PER_FIRMA_ID_FILE_PER_FIRMA_GENERATOR")
    public Long getIdFilePerFirma() {
        return this.idFilePerFirma;
    }

    public void setIdFilePerFirma(Long idFilePerFirma) {
        this.idFilePerFirma = idFilePerFirma;
    }

    @Lob()
    @Column(name = "BL_FILE_PER_FIRMA")
    public byte[] getBlFilePerFirma() {
        return this.blFilePerFirma;
    }

    public void setBlFilePerFirma(byte[] blFilePerFirma) {
        this.blFilePerFirma = blFilePerFirma;
    }

    @Column(name = "NM_FILE_DOWNLOAD")
    public String getNmFileDownload() {
        return this.nmFileDownload;
    }

    public void setNmFileDownload(String nmFileDownload) {
        this.nmFileDownload = nmFileDownload;
    }

    @Column(name = "TI_FILE_PER_FIRMA")
    public String getTiFilePerFirma() {
        return this.tiFilePerFirma;
    }

    public void setTiFilePerFirma(String tiFilePerFirma) {
        this.tiFilePerFirma = tiFilePerFirma;
    }

    // bi-directional one-to-one association to FirCertifCa
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_CA")
    public FirCertifCa getFirCertifCa() {
        return this.firCertifCa;
    }

    public void setFirCertifCa(FirCertifCa firCertifCa) {
        this.firCertifCa = firCertifCa;
    }

    // bi-directional one-to-one association to FirCertifFirmatario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_FIRMATARIO")
    public FirCertifFirmatario getFirCertifFirmatario() {
        return this.firCertifFirmatario;
    }

    public void setFirCertifFirmatario(FirCertifFirmatario firCertifFirmatario) {
        this.firCertifFirmatario = firCertifFirmatario;
    }

    // bi-directional one-to-one association to FirCertifFirmatario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_OCSP")
    public FirCertifOcsp getFirCertifOcsp() {
        return this.firCertifOcsp;
    }

    public void setFirCertifOcsp(FirCertifOcsp firCertifOcsp) {
        this.firCertifOcsp = firCertifOcsp;
    }

    // bi-directional one-to-one association to FirCrl
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CRL")
    public FirCrl getFirCrl() {
        return this.firCrl;
    }

    public void setFirCrl(FirCrl firCrl) {
        this.firCrl = firCrl;
    }

    // bi-directional one-to-one association to FirCrl
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_OCSP")
    public FirOcsp getFirOcsp() {
        return this.firOcsp;
    }

    public void setFirOcsp(FirOcsp firOcsp) {
        this.firOcsp = firOcsp;
    }

}
