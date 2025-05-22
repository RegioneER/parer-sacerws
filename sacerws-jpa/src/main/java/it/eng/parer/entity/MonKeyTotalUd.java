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

/**
 * The persistent class for the MON_KEY_TOTAL_UD database table.
 */
@Entity
@Table(name = "MON_KEY_TOTAL_UD")
public class MonKeyTotalUd implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idKeyTotalUd;

    private BigDecimal aaKeyUnitaDoc;

    private Date dtLastUpdUd;

    private OrgStrut orgStrut;

    private OrgSubStrut orgSubStrut;

    private DecTipoDoc decTipoDocPrinc;

    private DecTipoUnitaDoc decTipoUnitaDoc;

    private DecRegistroUnitaDoc decRegistroUnitaDoc;

    private List<MonContaSesUpdUd> monContaSesUpdUds = new ArrayList<>();

    public MonKeyTotalUd() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_KEY_TOTAL_UD")
    @GenericGenerator(name = "SMON_KEY_TOTAL_UD_ID_KEY_TOTAL_UD_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SMON_KEY_TOTAL_UD"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SMON_KEY_TOTAL_UD_ID_KEY_TOTAL_UD_GENERATOR")
    public Long getIdKeyTotalUd() {
	return this.idKeyTotalUd;
    }

    public void setIdKeyTotalUd(Long idKeyTotalUd) {
	this.idKeyTotalUd = idKeyTotalUd;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
	return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
	this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DT_LAST_UPD_UD")
    public Date getDtLastUpdUd() {
	return this.dtLastUpdUd;
    }

    public void setDtLastUpdUd(Date dtLastUpdUd) {
	this.dtLastUpdUd = dtLastUpdUd;
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

    // bi-directional many-to-one association to OrgSubStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SUB_STRUT")
    public OrgSubStrut getOrgSubStrut() {
	return this.orgSubStrut;
    }

    public void setOrgSubStrut(OrgSubStrut orgSubStrut) {
	this.orgSubStrut = orgSubStrut;
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

    // bi-directional many-to-one association to DecRegistroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGISTRO_UNITA_DOC")
    public DecRegistroUnitaDoc getDecRegistroUnitaDoc() {
	return this.decRegistroUnitaDoc;
    }

    public void setDecRegistroUnitaDoc(DecRegistroUnitaDoc decRegistroUnitaDoc) {
	this.decRegistroUnitaDoc = decRegistroUnitaDoc;
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

    // bi-directional many-to-one association to MonContaSesUpdUd
    @OneToMany(mappedBy = "monKeyTotalUd")
    public List<MonContaSesUpdUd> getMonContaSesUpdUds() {
	return this.monContaSesUpdUds;
    }

    public void setMonContaSesUpdUds(List<MonContaSesUpdUd> monContaSesUpdUds) {
	this.monContaSesUpdUds = monContaSesUpdUds;
    }

}
