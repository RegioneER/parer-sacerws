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
import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiEntitaAroUpdDatiSpecUnitaDoc;
import it.eng.parer.entity.constraint.AroUpdDatiSpecUnitaDoc.TiUsoXsdAroUpdDatiSpecUnitaDoc;

/**
 * The persistent class for the ARO_UPD_DATI_SPEC_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_UPD_DATI_SPEC_UNITA_DOC")
public class AroUpdDatiSpecUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUpdDatiSpecUnitaDoc;

    private String blXmlDatiSpec;

    private LocalDate dtIniSes;

    private OrgStrut orgStrut;

    private AroUpdCompUnitaDoc aroUpdCompUnitaDoc;

    private AroUpdDocUnitaDoc aroUpdDocUnitaDoc;

    private AroUpdUnitaDoc aroUpdUnitaDoc;

    private DecXsdDatiSpec decXsdDatiSpec;

    private TiEntitaAroUpdDatiSpecUnitaDoc tiEntitaSacer;

    private TiUsoXsdAroUpdDatiSpecUnitaDoc tiUsoXsd;

    private Integer aaIniSes;

    public AroUpdDatiSpecUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UPD_DATI_SPEC_UNITA_DOC")
    @GenericGenerator(name = "SARO_UPD_DATI_SPEC_UNITA_DOC_ID_UPD_DATI_SPEC_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_UPD_DATI_SPEC_UNITA_DOC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_UPD_DATI_SPEC_UNITA_DOC_ID_UPD_DATI_SPEC_UNITA_DOC_GENERATOR")
    public Long getIdUpdDatiSpecUnitaDoc() {
	return this.idUpdDatiSpecUnitaDoc;
    }

    public void setIdUpdDatiSpecUnitaDoc(Long idUpdDatiSpecUnitaDoc) {
	this.idUpdDatiSpecUnitaDoc = idUpdDatiSpecUnitaDoc;
    }

    @Lob
    @Column(name = "BL_XML_DATI_SPEC")
    public String getBlXmlDatiSpec() {
	return this.blXmlDatiSpec;
    }

    public void setBlXmlDatiSpec(String blXmlDatiSpec) {
	this.blXmlDatiSpec = blXmlDatiSpec;
    }

    @Column(name = "DT_INI_SES")
    public LocalDate getDtIniSes() {
	return this.dtIniSes;
    }

    public void setDtIniSes(LocalDate dtIniSes) {
	this.dtIniSes = dtIniSes;
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

    // bi-directional many-to-one association to AroUpdCompUnitaDoc
    @ManyToOne(cascade = {
	    CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_COMP_UNITA_DOC")
    public AroUpdCompUnitaDoc getAroUpdCompUnitaDoc() {
	return this.aroUpdCompUnitaDoc;
    }

    public void setAroUpdCompUnitaDoc(AroUpdCompUnitaDoc aroUpdCompUnitaDoc) {
	this.aroUpdCompUnitaDoc = aroUpdCompUnitaDoc;
    }

    // bi-directional many-to-one association to AroUpdDocUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_DOC_UNITA_DOC")
    public AroUpdDocUnitaDoc getAroUpdDocUnitaDoc() {
	return this.aroUpdDocUnitaDoc;
    }

    public void setAroUpdDocUnitaDoc(AroUpdDocUnitaDoc aroUpdDocUnitaDoc) {
	this.aroUpdDocUnitaDoc = aroUpdDocUnitaDoc;
    }

    // bi-directional many-to-one association to AroUpdUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_UNITA_DOC")
    public AroUpdUnitaDoc getAroUpdUnitaDoc() {
	return this.aroUpdUnitaDoc;
    }

    public void setAroUpdUnitaDoc(AroUpdUnitaDoc aroUpdUnitaDoc) {
	this.aroUpdUnitaDoc = aroUpdUnitaDoc;
    }

    // bi-directional many-to-one association to DecXsdDatiSpec
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_XSD_DATI_SPEC")
    public DecXsdDatiSpec getDecXsdDatiSpec() {
	return this.decXsdDatiSpec;
    }

    public void setDecXsdDatiSpec(DecXsdDatiSpec decXsdDatiSpec) {
	this.decXsdDatiSpec = decXsdDatiSpec;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_ENTITA_SACER")
    public TiEntitaAroUpdDatiSpecUnitaDoc getTiEntitaSacer() {
	return this.tiEntitaSacer;
    }

    public void setTiEntitaSacer(TiEntitaAroUpdDatiSpecUnitaDoc tiEntitaSacer) {
	this.tiEntitaSacer = tiEntitaSacer;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_USO_XSD")
    public TiUsoXsdAroUpdDatiSpecUnitaDoc getTiUsoXsd() {
	return this.tiUsoXsd;
    }

    public void setTiUsoXsd(TiUsoXsdAroUpdDatiSpecUnitaDoc tiUsoXsd) {
	this.tiUsoXsd = tiUsoXsd;
    }

    @Column(name = "AA_INI_SES")
    public Integer getAaIniSes() {
	return aaIniSes;
    }

    public void setAaIniSes(Integer aaIniSes) {
	this.aaIniSes = aaIniSes;
    }

}
