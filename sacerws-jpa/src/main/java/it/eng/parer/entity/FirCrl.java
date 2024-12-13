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
import java.util.Date;
import java.util.TimeZone;

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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.converter.NeverendingDateConverter;

/**
 * The persistent class for the FIR_CRL database table.
 */
@Entity
@Cacheable(true)
@Table(name = "FIR_CRL")
public class FirCrl implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCrl;

    private Date dtIniCrl;

    private Date dtScadCrl;

    private String dsSerialCrl;

    private FirCertifCa firCertifCa;

    private FirFilePerFirma firFilePerFirma;

    public FirCrl() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CRL")
    @GenericGenerator(name = "SFIR_CRL_ID_CRL_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFIR_CRL"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFIR_CRL_ID_CRL_GENERATOR")
    public Long getIdCrl() {
        return this.idCrl;
    }

    public void setIdCrl(Long idCrl) {
        this.idCrl = idCrl;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_INI_CRL")
    public Date getDtIniCrl() {
        return this.dtIniCrl;
    }

    public void setDtIniCrl(Date dtIniCrl) {
        this.dtIniCrl = dtIniCrl;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_SCAD_CRL")
    public Date getDtScadCrl() {
        return this.dtScadCrl;
    }

    public void setDtScadCrl(Date dtScadCrl) {
        this.dtScadCrl = dtScadCrl;
    }

    @Column(name = "DS_SERIAL_CRL")
    public String getDsSerialCrl() {
        return this.dsSerialCrl;
    }

    public void setDsSerialCrl(String dsSerialCrl) {
        this.dsSerialCrl = dsSerialCrl;
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

    // bi-directional one-to-one association to FirFilePerFirma
    @OneToOne(mappedBy = "firCrl", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REFRESH }, fetch = FetchType.LAZY)
    public FirFilePerFirma getFirFilePerFirma() {
        return this.firFilePerFirma;
    }

    public void setFirFilePerFirma(FirFilePerFirma firFilePerFirma) {
        this.firFilePerFirma = firFilePerFirma;
    }

    /**
     * Secondo specifica RFC5280 https://tools.ietf.org/html/rfc5280#section-5.1.2.5 le date sarebbero normalemente
     * espresse in UTC/GMT Il sistema però persistente con il TZ locale (ossia GMT+01), esiste un caso "particolare" di
     * timestamp : 9999/31/12 23:59:59 UTC che per un hard limit di ORACLE DB non può essere persisto (la sua
     * conversione in GMT+01 lo trasforma in 10000/01/01 00:59:59 GMT+01) che non può essere persistito e/o letto
     */
    @PrePersist
    void preInsert() {
        this.dtScadCrl = NeverendingDateConverter.verifyOverZoneId(this.dtScadCrl,
                TimeZone.getTimeZone("UTC").toZoneId());
    }

    @PreUpdate
    void preUpdate() {
        this.dtScadCrl = NeverendingDateConverter.verifyOverZoneId(this.dtScadCrl,
                TimeZone.getTimeZone("UTC").toZoneId());
    }

}
