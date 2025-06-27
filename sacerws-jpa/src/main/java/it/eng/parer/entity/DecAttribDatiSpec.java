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
import java.util.List;

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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_ATTRIB_DATI_SPEC database table.
 */
@Entity
@Table(name = "DEC_ATTRIB_DATI_SPEC")
public class DecAttribDatiSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idAttribDatiSpec;

    private String dsAttribDatiSpec;

    private String nmAttribDatiSpec;

    private String nmSistemaMigraz;

    private String tiEntitaSacer;

    private String tiUsoAttrib;

    private String tiAttribDatiSpec;

    private List<AroValoreAttribDatiSpec> aroValoreAttribDatiSpecs = new ArrayList<>();

    private DecTipoCompDoc decTipoCompDoc;

    private DecTipoDoc decTipoDoc;

    private DecTipoUnitaDoc decTipoUnitaDoc;

    private OrgStrut orgStrut;

    private List<DecXsdAttribDatiSpec> decXsdAttribDatiSpecs = new ArrayList<>();

    private List<OrgCampoValSubStrut> orgCampoValSubStruts = new ArrayList<>();

    public DecAttribDatiSpec() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_ATTRIB_DATI_SPEC")
    @XmlID
    @GenericGenerator(name = "SDEC_ATTRIB_DATI_SPEC_ID_ATTRIB_DATI_SPEC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_ATTRIB_DATI_SPEC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_ATTRIB_DATI_SPEC_ID_ATTRIB_DATI_SPEC_GENERATOR")
    public Long getIdAttribDatiSpec() {
	return this.idAttribDatiSpec;
    }

    public void setIdAttribDatiSpec(Long idAttribDatiSpec) {
	this.idAttribDatiSpec = idAttribDatiSpec;
    }

    @Column(name = "DS_ATTRIB_DATI_SPEC")
    public String getDsAttribDatiSpec() {
	return this.dsAttribDatiSpec;
    }

    public void setDsAttribDatiSpec(String dsAttribDatiSpec) {
	this.dsAttribDatiSpec = dsAttribDatiSpec;
    }

    @Column(name = "NM_ATTRIB_DATI_SPEC")
    public String getNmAttribDatiSpec() {
	return this.nmAttribDatiSpec;
    }

    public void setNmAttribDatiSpec(String nmAttribDatiSpec) {
	this.nmAttribDatiSpec = nmAttribDatiSpec;
    }

    @Column(name = "NM_SISTEMA_MIGRAZ")
    public String getNmSistemaMigraz() {
	return this.nmSistemaMigraz;
    }

    public void setNmSistemaMigraz(String nmSistemaMigraz) {
	this.nmSistemaMigraz = nmSistemaMigraz;
    }

    @Column(name = "TI_ENTITA_SACER")
    public String getTiEntitaSacer() {
	return this.tiEntitaSacer;
    }

    public void setTiEntitaSacer(String tiEntitaSacer) {
	this.tiEntitaSacer = tiEntitaSacer;
    }

    @Column(name = "TI_USO_ATTRIB")
    public String getTiUsoAttrib() {
	return this.tiUsoAttrib;
    }

    public void setTiUsoAttrib(String tiUsoAttrib) {
	this.tiUsoAttrib = tiUsoAttrib;
    }

    @Column(name = "TI_ATTRIB_DATI_SPEC")
    public String getTiAttribDatiSpec() {
	return tiAttribDatiSpec;
    }

    public void setTiAttribDatiSpec(String tiAttribDatiSpec) {
	this.tiAttribDatiSpec = tiAttribDatiSpec;
    }

    // bi-directional many-to-one association to AroValoreAttribDatiSpec
    @OneToMany(mappedBy = "decAttribDatiSpec")
    @XmlTransient
    public List<AroValoreAttribDatiSpec> getAroValoreAttribDatiSpecs() {
	return this.aroValoreAttribDatiSpecs;
    }

    public void setAroValoreAttribDatiSpecs(
	    List<AroValoreAttribDatiSpec> aroValoreAttribDatiSpecs) {
	this.aroValoreAttribDatiSpecs = aroValoreAttribDatiSpecs;
    }

    // bi-directional many-to-one association to DecTipoCompDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_COMP_DOC")
    public DecTipoCompDoc getDecTipoCompDoc() {
	return this.decTipoCompDoc;
    }

    public void setDecTipoCompDoc(DecTipoCompDoc decTipoCompDoc) {
	this.decTipoCompDoc = decTipoCompDoc;
    }

    // bi-directional many-to-one association to DecTipoDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_DOC")
    public DecTipoDoc getDecTipoDoc() {
	return this.decTipoDoc;
    }

    public void setDecTipoDoc(DecTipoDoc decTipoDoc) {
	this.decTipoDoc = decTipoDoc;
    }

    // bi-directional many-to-one association to DecTipoUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_UNITA_DOC")
    public DecTipoUnitaDoc getDecTipoUnitaDoc() {
	return this.decTipoUnitaDoc;
    }

    public void setDecTipoUnitaDoc(DecTipoUnitaDoc decTipoUnitaDoc) {
	this.decTipoUnitaDoc = decTipoUnitaDoc;
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

    // bi-directional many-to-one association to DecXsdAttribDatiSpec
    @OneToMany(mappedBy = "decAttribDatiSpec", cascade = {
	    CascadeType.PERSIST, CascadeType.REMOVE })
    public List<DecXsdAttribDatiSpec> getDecXsdAttribDatiSpecs() {
	return this.decXsdAttribDatiSpecs;
    }

    public void setDecXsdAttribDatiSpecs(List<DecXsdAttribDatiSpec> decXsdAttribDatiSpecs) {
	this.decXsdAttribDatiSpecs = decXsdAttribDatiSpecs;
    }

    // bi-directional many-to-one association to OrgCampoValSubStrut
    @OneToMany(mappedBy = "decAttribDatiSpec", cascade = CascadeType.PERSIST)
    @XmlIDREF
    public List<OrgCampoValSubStrut> getOrgCampoValSubStruts() {
	return this.orgCampoValSubStruts;
    }

    public void setOrgCampoValSubStruts(List<OrgCampoValSubStrut> orgCampoValSubStruts) {
	this.orgCampoValSubStruts = orgCampoValSubStruts;
    }

}
