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
import java.util.TimeZone;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
 * The persistent class for the FIR_CERTIF_CA database table.
 *
 */
@Entity
@Cacheable(true)
@Table(name = "FIR_CERTIF_CA")
public class FirCertifCa implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCertifCa;

    private List<FirUrlDistribCrl> firUrlDistribCrls = new ArrayList<>();

    private List<FirUrlDistribOcsp> firUrlDistribOcsps = new ArrayList<>();

    private Date dtFinValCertifCa;

    private Date dtIniValCertifCa;

    private BigDecimal niSerialCertifCa;

    private List<FirCertifFirmatario> firCertifFirmatarios = new ArrayList<>();

    private List<FirCrl> firCrls = new ArrayList<>();

    private List<FirCertifOcsp> firCertifOcsps = new ArrayList<>();

    private FirFilePerFirma firFilePerFirma;

    private String dsSubjectKeyId;

    private String dlDnIssuerCertifCa;
    private String dlDnSubjectCertifCa;

    public FirCertifCa() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CERTIF_CA")
    @GenericGenerator(name = "SFIR_CERTIF_CA_ID_CERTIF_CA_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFIR_CERTIF_CA"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFIR_CERTIF_CA_ID_CERTIF_CA_GENERATOR")
    public Long getIdCertifCa() {
        return this.idCertifCa;
    }

    public void setIdCertifCa(Long idCertifCa) {
        this.idCertifCa = idCertifCa;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_FIN_VAL_CERTIF_CA")
    public Date getDtFinValCertifCa() {
        return this.dtFinValCertifCa;
    }

    public void setDtFinValCertifCa(Date dtFinValCertifCa) {
        this.dtFinValCertifCa = dtFinValCertifCa;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_INI_VAL_CERTIF_CA")
    public Date getDtIniValCertifCa() {
        return this.dtIniValCertifCa;
    }

    public void setDtIniValCertifCa(Date dtIniValCertifCa) {
        this.dtIniValCertifCa = dtIniValCertifCa;
    }

    @Column(name = "NI_SERIAL_CERTIF_CA")
    public BigDecimal getNiSerialCertifCa() {
        return this.niSerialCertifCa;
    }

    public void setNiSerialCertifCa(BigDecimal niSerialCertifCa) {
        this.niSerialCertifCa = niSerialCertifCa;
    }

    @Column(name = "DS_SUBJECT_KEY_ID")
    public String getDsSubjectKeyId() {
        return dsSubjectKeyId;
    }

    public void setDsSubjectKeyId(String dsSubjectKeyId) {
        this.dsSubjectKeyId = dsSubjectKeyId;
    }

    @Column(name = "DL_DN_ISSUER_CERTIF_CA")
    public String getDlDnIssuerCertifCa() {
        return dlDnIssuerCertifCa;
    }

    public void setDlDnIssuerCertifCa(String dlDnIssuerCertifCa) {
        this.dlDnIssuerCertifCa = dlDnIssuerCertifCa;
    }

    @Column(name = "DL_DN_SUBJECT_CERTIF_CA")
    public String getDlDnSubjectCertifCa() {
        return dlDnSubjectCertifCa;
    }

    public void setDlDnSubjectCertifCa(String dlDnSubjectCertifCa) {
        this.dlDnSubjectCertifCa = dlDnSubjectCertifCa;
    }

    // bi-directional many-to-one association to FirCertifFirmatario
    @OneToMany(mappedBy = "firCertifCa", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<FirCertifFirmatario> getFirCertifFirmatarios() {
        return this.firCertifFirmatarios;
    }

    public void setFirCertifFirmatarios(List<FirCertifFirmatario> firCertifFirmatarios) {
        this.firCertifFirmatarios = firCertifFirmatarios;
    }

    // bi-directional many-to-one association to FirCrl
    @OneToMany(mappedBy = "firCertifCa", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<FirCrl> getFirCrls() {
        return this.firCrls;
    }

    public void setFirCrls(List<FirCrl> firCrls) {
        this.firCrls = firCrls;
    }

    // bi-directional many-to-one association to FirCrl
    @OneToMany(mappedBy = "firCertifCa", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<FirCertifOcsp> getFirCertifOcsps() {
        return this.firCertifOcsps;
    }

    public void setFirCertifOcsps(List<FirCertifOcsp> firCertifOcsps) {
        this.firCertifOcsps = firCertifOcsps;
    }

    // bi-directional one-to-one association to FirFilePerFirma
    @OneToOne(mappedBy = "firCertifCa", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REFRESH }, fetch = FetchType.LAZY)
    public FirFilePerFirma getFirFilePerFirma() {
        return this.firFilePerFirma;
    }

    public void setFirFilePerFirma(FirFilePerFirma firFilePerFirma) {
        this.firFilePerFirma = firFilePerFirma;
    }

    // bi-directional many-to-one association to FirUrlDistribCrl
    @OneToMany(mappedBy = "firCertifCa", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<FirUrlDistribCrl> getFirUrlDistribCrls() {
        return this.firUrlDistribCrls;
    }

    public void setFirUrlDistribCrls(List<FirUrlDistribCrl> firUrlDistribCrls) {
        this.firUrlDistribCrls = firUrlDistribCrls;
    }

    // bi-directional many-to-one association to FirUrlDistribCrl
    @OneToMany(mappedBy = "firCertifCa", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<FirUrlDistribOcsp> getFirUrlDistribOcsps() {
        return this.firUrlDistribOcsps;
    }

    public void setFirUrlDistribOcsps(List<FirUrlDistribOcsp> firUrlDistribOcsps) {
        this.firUrlDistribOcsps = firUrlDistribOcsps;
    }

    /**
     * Secondo specifica RFC5280 https://tools.ietf.org/html/rfc5280#section-5.1.2.5 le date sarebbero normalemente
     * espresse in UTC/GMT Il sistema però persistente con il TZ locale (ossia GMT+01), esiste un caso "particolare" di
     * timestamp : 9999/31/12 23:59:59 UTC che per un hard limit di ORACLE DB non può essere persisto (la sua
     * conversione in GMT+01 lo trasforma in 10000/01/01 00:59:59 GMT+01) che non può essere persistito e/o letto
     */
    @PrePersist
    void preInsert() {
        this.dtFinValCertifCa = NeverendingDateConverter.verifyOverZoneId(this.dtFinValCertifCa,
                TimeZone.getTimeZone("UTC").toZoneId());
    }

    @PreUpdate
    void preUpdate() {
        this.dtFinValCertifCa = NeverendingDateConverter.verifyOverZoneId(this.dtFinValCertifCa,
                TimeZone.getTimeZone("UTC").toZoneId());
    }
}
