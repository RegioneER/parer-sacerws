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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the VRS_DOC_NON_VERS database table.
 */
@Entity
@Table(name = "VRS_DOC_NON_VERS")
public class VrsDocNonVer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idDocNonVers;

    private BigDecimal aaKeyUnitaDoc;

    private String cdErrPrinc;

    private String cdKeyDocVers;

    private String cdKeyUnitaDoc;

    private String cdRegistroKeyUnitaDoc;

    private String dsErrPrinc;

    private Date dtFirstSesErr;

    private Date dtLastSesErr;

    private OrgStrut orgStrut;

    public VrsDocNonVer() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_DOC_NON_VERS")
    @GenericGenerator(name = "SVRS_DOC_NON_VERS_ID_DOC_NON_VERS_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_DOC_NON_VERS"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_DOC_NON_VERS_ID_DOC_NON_VERS_GENERATOR")
    public Long getIdDocNonVers() {
	return this.idDocNonVers;
    }

    public void setIdDocNonVers(Long idDocNonVers) {
	this.idDocNonVers = idDocNonVers;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
	return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
	this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Column(name = "CD_ERR_PRINC")
    public String getCdErrPrinc() {
	return this.cdErrPrinc;
    }

    public void setCdErrPrinc(String cdErrPrinc) {
	this.cdErrPrinc = cdErrPrinc;
    }

    @Column(name = "CD_KEY_DOC_VERS")
    public String getCdKeyDocVers() {
	return this.cdKeyDocVers;
    }

    public void setCdKeyDocVers(String cdKeyDocVers) {
	this.cdKeyDocVers = cdKeyDocVers;
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

    @Column(name = "DS_ERR_PRINC")
    public String getDsErrPrinc() {
	return this.dsErrPrinc;
    }

    public void setDsErrPrinc(String dsErrPrinc) {
	this.dsErrPrinc = dsErrPrinc;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_FIRST_SES_ERR")
    public Date getDtFirstSesErr() {
	return this.dtFirstSesErr;
    }

    public void setDtFirstSesErr(Date dtFirstSesErr) {
	this.dtFirstSesErr = dtFirstSesErr;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_LAST_SES_ERR")
    public Date getDtLastSesErr() {
	return this.dtLastSesErr;
    }

    public void setDtLastSesErr(Date dtLastSesErr) {
	this.dtLastSesErr = dtLastSesErr;
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

}
