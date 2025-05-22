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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.VrsSesUpdUnitaDocErr.TiStatoSesVrsSesUpdUnitaDocErr;

/**
 * The persistent class for the VRS_SES_UPD_UNITA_DOC_ERR database table.
 */
@Entity
@Table(name = "VRS_SES_UPD_UNITA_DOC_ERR")
public class VrsSesUpdUnitaDocErr implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idSesUpdUnitaDocErr;

    private BigDecimal aaKeyUnitaDoc;

    private String cdKeyUnitaDoc;

    private String cdRegistroKeyUnitaDoc;

    private String cdVersioneWs;

    private String dsErrPrinc;

    private DecControlloWs decControlloWsPrinc;

    private DecErrSacer decErrSacerPrinc;

    private DecRegistroUnitaDoc decRegistroUnitaDoc;

    private OrgStrut orgStrut;

    private DecTipoDoc decTipoDocPrinc;

    private DecTipoUnitaDoc decTipoUnitaDoc;

    private String nmAmbiente;

    private String nmEnte;

    private String nmStrut;

    private String nmTipoDocPrinc;

    private String nmTipoUnitaDoc;

    private String nmUseridWs;

    private TiStatoSesVrsSesUpdUnitaDocErr tiStatoSes;

    private Date tsFineSes;

    private Date tsIniSes;

    private List<VrsErrSesUpdUnitaDocErr> vrsErrSesUpdUnitaDocErrs = new ArrayList<>();

    private List<VrsXmlSesUpdUnitaDocErr> vrsXmlSesUpdUnitaDocErrs = new ArrayList<>();

    public VrsSesUpdUnitaDocErr() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_SES_UPD_UNITA_DOC_ERR")
    @GenericGenerator(name = "SVRS_SES_UPD_UNITA_DOC_ERR_ID_SES_UPD_UNITA_DOC_ERR_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_SES_UPD_UNITA_DOC_ERR"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_SES_UPD_UNITA_DOC_ERR_ID_SES_UPD_UNITA_DOC_ERR_GENERATOR")
    public Long getIdSesUpdUnitaDocErr() {
	return this.idSesUpdUnitaDocErr;
    }

    public void setIdSesUpdUnitaDocErr(Long idSesUpdUnitaDocErr) {
	this.idSesUpdUnitaDocErr = idSesUpdUnitaDocErr;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
	return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
	this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Column(name = "CD_KEY_UNITA_DOC")
    public String getCdKeyUnitaDoc() {
	return this.cdKeyUnitaDoc;
    }

    public void setCdKeyUnitaDoc(String cdKeyUnitaDoc) {
	this.cdKeyUnitaDoc = cdKeyUnitaDoc;
    }

    @Column(name = "CD_REGISTRO_KEY_UNITA_DOC")
    public String getCdRegistroKeyUnitaDoc() {
	return this.cdRegistroKeyUnitaDoc;
    }

    public void setCdRegistroKeyUnitaDoc(String cdRegistroKeyUnitaDoc) {
	this.cdRegistroKeyUnitaDoc = cdRegistroKeyUnitaDoc;
    }

    @Column(name = "CD_VERSIONE_WS")
    public String getCdVersioneWs() {
	return this.cdVersioneWs;
    }

    public void setCdVersioneWs(String cdVersioneWs) {
	this.cdVersioneWs = cdVersioneWs;
    }

    @Column(name = "DS_ERR_PRINC")
    public String getDsErrPrinc() {
	return this.dsErrPrinc;
    }

    public void setDsErrPrinc(String dsErrPrinc) {
	this.dsErrPrinc = dsErrPrinc;
    }

    // bi-directional many-to-one association to DecErrSacer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONTROLLO_WS_PRINC")
    public DecControlloWs getDecControlloWsPrinc() {
	return this.decControlloWsPrinc;
    }

    public void setDecControlloWsPrinc(DecControlloWs decControlloWsPrinc) {
	this.decControlloWsPrinc = decControlloWsPrinc;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ERR_SACER_PRINC")
    public DecErrSacer getDecErrSacerPrinc() {
	return this.decErrSacerPrinc;
    }

    public void setDecErrSacerPrinc(DecErrSacer decErrSacerPrinc) {
	this.decErrSacerPrinc = decErrSacerPrinc;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGISTRO_UNITA_DOC")
    public DecRegistroUnitaDoc getDecRegistroUnitaDoc() {
	return this.decRegistroUnitaDoc;
    }

    public void setDecRegistroUnitaDoc(DecRegistroUnitaDoc decRegistroUnitaDoc) {
	this.decRegistroUnitaDoc = decRegistroUnitaDoc;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
	return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
	this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_DOC_PRINC")
    public DecTipoDoc getDecTipoDocPrinc() {
	return this.decTipoDocPrinc;
    }

    public void setDecTipoDocPrinc(DecTipoDoc decTipoDocPrinc) {
	this.decTipoDocPrinc = decTipoDocPrinc;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_UNITA_DOC")
    public DecTipoUnitaDoc getDecTipoUnitaDoc() {
	return this.decTipoUnitaDoc;
    }

    public void setDecTipoUnitaDoc(DecTipoUnitaDoc decTipoUnitaDoc) {
	this.decTipoUnitaDoc = decTipoUnitaDoc;
    }

    @Column(name = "NM_AMBIENTE")
    public String getNmAmbiente() {
	return this.nmAmbiente;
    }

    public void setNmAmbiente(String nmAmbiente) {
	this.nmAmbiente = nmAmbiente;
    }

    @Column(name = "NM_ENTE")
    public String getNmEnte() {
	return this.nmEnte;
    }

    public void setNmEnte(String nmEnte) {
	this.nmEnte = nmEnte;
    }

    @Column(name = "NM_STRUT")
    public String getNmStrut() {
	return this.nmStrut;
    }

    public void setNmStrut(String nmStrut) {
	this.nmStrut = nmStrut;
    }

    @Column(name = "NM_TIPO_DOC_PRINC")
    public String getNmTipoDocPrinc() {
	return this.nmTipoDocPrinc;
    }

    public void setNmTipoDocPrinc(String nmTipoDocPrinc) {
	this.nmTipoDocPrinc = nmTipoDocPrinc;
    }

    @Column(name = "NM_TIPO_UNITA_DOC")
    public String getNmTipoUnitaDoc() {
	return this.nmTipoUnitaDoc;
    }

    public void setNmTipoUnitaDoc(String nmTipoUnitaDoc) {
	this.nmTipoUnitaDoc = nmTipoUnitaDoc;
    }

    @Column(name = "NM_USERID_WS")
    public String getNmUseridWs() {
	return this.nmUseridWs;
    }

    public void setNmUseridWs(String nmUseridWs) {
	this.nmUseridWs = nmUseridWs;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_STATO_SES")
    public TiStatoSesVrsSesUpdUnitaDocErr getTiStatoSes() {
	return this.tiStatoSes;
    }

    public void setTiStatoSes(TiStatoSesVrsSesUpdUnitaDocErr tiStatoSes) {
	this.tiStatoSes = tiStatoSes;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_FINE_SES")
    public Date getTsFineSes() {
	return this.tsFineSes;
    }

    public void setTsFineSes(Date tsFineSes) {
	this.tsFineSes = tsFineSes;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_INI_SES")
    public Date getTsIniSes() {
	return this.tsIniSes;
    }

    public void setTsIniSes(Date tsIniSes) {
	this.tsIniSes = tsIniSes;
    }

    // bi-directional many-to-one association to VrsSesFascicoloKo
    @OneToMany(mappedBy = "vrsSesUpdUnitaDocErr")
    public List<VrsErrSesUpdUnitaDocErr> getVrsErrSesUpdUnitaDocErrs() {
	return this.vrsErrSesUpdUnitaDocErrs;
    }

    public void setVrsErrSesUpdUnitaDocErrs(
	    List<VrsErrSesUpdUnitaDocErr> vrsErrSesUpdUnitaDocErrs) {
	this.vrsErrSesUpdUnitaDocErrs = vrsErrSesUpdUnitaDocErrs;
    }

    // bi-directional many-to-one association to VrsSesFascicoloKo
    @OneToMany(mappedBy = "vrsSesUpdUnitaDocErr")
    public List<VrsXmlSesUpdUnitaDocErr> getVrsXmlSesUpdUnitaDocErrs() {
	return this.vrsXmlSesUpdUnitaDocErrs;
    }

    public void setVrsXmlSesUpdUnitaDocErrs(
	    List<VrsXmlSesUpdUnitaDocErr> vrsXmlSesUpdUnitaDocErrs) {
	this.vrsXmlSesUpdUnitaDocErrs = vrsXmlSesUpdUnitaDocErrs;
    }

}
