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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_TIPO_STRUT_DOC database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_TIPO_STRUT_DOC")
public class DecTipoStrutDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTipoStrutDoc;

    private String dsTipoStrutDoc;

    private Date dtIstituz;

    private Date dtSoppres;

    private String nmTipoStrutDoc;

    private List<AroStrutDoc> aroStrutDocs = new ArrayList<>();

    private List<DecTipoCompDoc> decTipoCompDocs = new ArrayList<>();

    private OrgStrut orgStrut;

    public DecTipoStrutDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_TIPO_STRUT_DOC")
    @GenericGenerator(name = "SDEC_TIPO_STRUT_DOC_ID_TIPO_STRUT_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_TIPO_STRUT_DOC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_TIPO_STRUT_DOC_ID_TIPO_STRUT_DOC_GENERATOR")
    public Long getIdTipoStrutDoc() {
	return this.idTipoStrutDoc;
    }

    public void setIdTipoStrutDoc(Long idTipoStrutDoc) {
	this.idTipoStrutDoc = idTipoStrutDoc;
    }

    @Column(name = "DS_TIPO_STRUT_DOC")
    public String getDsTipoStrutDoc() {
	return this.dsTipoStrutDoc;
    }

    public void setDsTipoStrutDoc(String dsTipoStrutDoc) {
	this.dsTipoStrutDoc = dsTipoStrutDoc;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_ISTITUZ")
    public Date getDtIstituz() {
	return this.dtIstituz;
    }

    public void setDtIstituz(Date dtIstituz) {
	this.dtIstituz = dtIstituz;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_SOPPRES")
    public Date getDtSoppres() {
	return this.dtSoppres;
    }

    public void setDtSoppres(Date dtSoppres) {
	this.dtSoppres = dtSoppres;
    }

    @Column(name = "NM_TIPO_STRUT_DOC")
    public String getNmTipoStrutDoc() {
	return this.nmTipoStrutDoc;
    }

    public void setNmTipoStrutDoc(String nmTipoStrutDoc) {
	this.nmTipoStrutDoc = nmTipoStrutDoc;
    }

    // bi-directional many-to-one association to AroStrutDoc
    @OneToMany(mappedBy = "decTipoStrutDoc")
    @XmlTransient
    public List<AroStrutDoc> getAroStrutDocs() {
	return this.aroStrutDocs;
    }

    public void setAroStrutDocs(List<AroStrutDoc> aroStrutDocs) {
	this.aroStrutDocs = aroStrutDocs;
    }

    // bi-directional many-to-one association to DecTipoCompDoc
    @OneToMany(mappedBy = "decTipoStrutDoc", cascade = {
	    CascadeType.PERSIST, CascadeType.REMOVE })
    public List<DecTipoCompDoc> getDecTipoCompDocs() {
	return this.decTipoCompDocs;
    }

    public void setDecTipoCompDocs(List<DecTipoCompDoc> decTipoCompDocs) {
	this.decTipoCompDocs = decTipoCompDocs;
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
