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
 * The persistent class for the MON_KEY_TOTAL_UD_KO database table.
 */
@Entity
@Table(name = "MON_KEY_TOTAL_UD_KO")
public class MonKeyTotalUdKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idKeyTotalUdKo;

    private BigDecimal aaKeyUnitaDoc;

    private Date dtLastUpdUdKo;

    private OrgStrut orgStrut;

    private DecTipoDoc decTipoDocPrinc;

    private DecTipoUnitaDoc decTipoUnitaDoc;

    private DecRegistroUnitaDoc decRegistroUnitaDoc;

    private List<MonContaSesUpdUdKo> monContaSesUpdUdKos = new ArrayList<>();

    public MonKeyTotalUdKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_KEY_TOTAL_UD_KO")
    @GenericGenerator(name = "SMON_KEY_TOTAL_UD_KO_ID_KEY_TOTAL_UD_KO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SMON_KEY_TOTAL_UD_KO"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SMON_KEY_TOTAL_UD_KO_ID_KEY_TOTAL_UD_KO_GENERATOR")
    public Long getIdKeyTotalUdKo() {
	return this.idKeyTotalUdKo;
    }

    public void setIdKeyTotalUdKo(Long idKeyTotalUdKo) {
	this.idKeyTotalUdKo = idKeyTotalUdKo;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
	return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
	this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DT_LAST_UPD_UD_KO")
    public Date getDtLastUpdUdKo() {
	return this.dtLastUpdUdKo;
    }

    public void setDtLastUpdUdKo(Date dtLastUpdUdKo) {
	this.dtLastUpdUdKo = dtLastUpdUdKo;
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

    // bi-directional many-to-one association to MonContaSesUpdUdKo
    @OneToMany(mappedBy = "monKeyTotalUdKo")
    public List<MonContaSesUpdUdKo> getMonContaSesUpdUdKos() {
	return this.monContaSesUpdUdKos;
    }

    public void setMonContaSesUpdUdKos(List<MonContaSesUpdUdKo> monContaSesUpdUdKos) {
	this.monContaSesUpdUdKos = monContaSesUpdUdKos;
    }

}
