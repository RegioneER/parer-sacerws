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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_TIPO_COMP_DOC database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_TIPO_COMP_DOC")
public class DecTipoCompDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTipoCompDoc;

    private String dsTipoCompDoc;

    private String flGestiti;

    private String flIdonei;

    private String flDeprecati;

    private Date dtIstituz;

    private Date dtSoppres;

    private String nmTipoCompDoc;

    private String tiUsoCompDoc;

    private List<AroCompDoc> aroCompDocs = new ArrayList<>();

    private List<DecAttribDatiSpec> decAttribDatiSpecs = new ArrayList<>();

    private List<DecFormatoFileAmmesso> decFormatoFileAmmessos = new ArrayList<>();

    private DecTipoStrutDoc decTipoStrutDoc;

    private List<DecXsdDatiSpec> decXsdDatiSpecs = new ArrayList<>();
    private List<DecUsoModelloXsdCompDoc> decUsoModelloXsdCompDocs = new ArrayList<>();

    public DecTipoCompDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_TIPO_COMP_DOC")
    @GenericGenerator(name = "SDEC_TIPO_COMP_DOC_ID_TIPO_COMP_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_TIPO_COMP_DOC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_TIPO_COMP_DOC_ID_TIPO_COMP_DOC_GENERATOR")
    public Long getIdTipoCompDoc() {
	return this.idTipoCompDoc;
    }

    public void setIdTipoCompDoc(Long idTipoCompDoc) {
	this.idTipoCompDoc = idTipoCompDoc;
    }

    @Column(name = "DS_TIPO_COMP_DOC")
    public String getDsTipoCompDoc() {
	return this.dsTipoCompDoc;
    }

    public void setDsTipoCompDoc(String dsTipoCompDoc) {
	this.dsTipoCompDoc = dsTipoCompDoc;
    }

    @Column(name = "FL_GESTITI", columnDefinition = "char(1)")
    public String getFlGestiti() {
	return this.flGestiti;
    }

    public void setFlGestiti(String flGestiti) {
	this.flGestiti = flGestiti;
    }

    @Column(name = "FL_IDONEI", columnDefinition = "char(1)")
    public String getFlIdonei() {
	return this.flIdonei;
    }

    public void setFlIdonei(String flIdonei) {
	this.flIdonei = flIdonei;
    }

    @Column(name = "FL_DEPRECATI", columnDefinition = "char(1)")
    public String getFlDeprecati() {
	return this.flDeprecati;
    }

    public void setFlDeprecati(String flDeprecati) {
	this.flDeprecati = flDeprecati;
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

    @Column(name = "NM_TIPO_COMP_DOC")
    public String getNmTipoCompDoc() {
	return this.nmTipoCompDoc;
    }

    public void setNmTipoCompDoc(String nmTipoCompDoc) {
	this.nmTipoCompDoc = nmTipoCompDoc;
    }

    @Column(name = "TI_USO_COMP_DOC")
    public String getTiUsoCompDoc() {
	return this.tiUsoCompDoc;
    }

    public void setTiUsoCompDoc(String tiUsoCompDoc) {
	this.tiUsoCompDoc = tiUsoCompDoc;
    }

    // bi-directional many-to-one association to AroCompDoc
    @OneToMany(mappedBy = "decTipoCompDoc")
    @XmlTransient
    public List<AroCompDoc> getAroCompDocs() {
	return this.aroCompDocs;
    }

    public void setAroCompDocs(List<AroCompDoc> aroCompDocs) {
	this.aroCompDocs = aroCompDocs;
    }

    // bi-directional many-to-one association to DecFormatoFileAmmesso
    @OneToMany(mappedBy = "decTipoCompDoc", cascade = CascadeType.PERSIST)
    public List<DecFormatoFileAmmesso> getDecFormatoFileAmmessos() {
	return this.decFormatoFileAmmessos;
    }

    public void setDecFormatoFileAmmessos(List<DecFormatoFileAmmesso> decFormatoFileAmmessos) {
	this.decFormatoFileAmmessos = decFormatoFileAmmessos;
    }

    // bi-directional many-to-one association to DecTipoStrutDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_STRUT_DOC")
    public DecTipoStrutDoc getDecTipoStrutDoc() {
	return this.decTipoStrutDoc;
    }

    public void setDecTipoStrutDoc(DecTipoStrutDoc decTipoStrutDoc) {
	this.decTipoStrutDoc = decTipoStrutDoc;
    }

    // bi-directional many-to-one association to DecAttribDatiSpec
    @OneToMany(mappedBy = "decTipoCompDoc", cascade = CascadeType.PERSIST)
    @XmlIDREF
    public List<DecAttribDatiSpec> getDecAttribDatiSpecs() {
	return this.decAttribDatiSpecs;
    }

    public void setDecAttribDatiSpecs(List<DecAttribDatiSpec> decAttribDatiSpecs) {
	this.decAttribDatiSpecs = decAttribDatiSpecs;
    }

    // bi-directional many-to-one association to DecXsdDatiSpec
    @OneToMany(mappedBy = "decTipoCompDoc", cascade = CascadeType.PERSIST)
    @XmlIDREF
    public List<DecXsdDatiSpec> getDecXsdDatiSpecs() {
	return this.decXsdDatiSpecs;
    }

    public void setDecXsdDatiSpecs(List<DecXsdDatiSpec> decXsdDatiSpecs) {
	this.decXsdDatiSpecs = decXsdDatiSpecs;
    }

    // bi-directional many-to-one association to DecTipoRapprAmmesso
    @OneToMany(mappedBy = "decTipoCompDoc", cascade = CascadeType.PERSIST)
    public List<DecUsoModelloXsdCompDoc> getDecUsoModelloXsdCompDocs() {
	return this.decUsoModelloXsdCompDocs;
    }

    public void setDecUsoModelloXsdCompDocs(
	    List<DecUsoModelloXsdCompDoc> decUsoModelloXsdCompDocs) {
	this.decUsoModelloXsdCompDocs = decUsoModelloXsdCompDocs;
    }
}
