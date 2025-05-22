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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_XSD_DATI_SPEC database table.
 */
@Entity
// @Cacheable(true)
@Table(name = "DEC_XSD_DATI_SPEC")
public class DecXsdDatiSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXsdDatiSpec;

    private String blXsd;

    private String cdVersioneXsd;

    private Date dtIstituz;

    private Date dtSoppres;

    private String nmSistemaMigraz;

    private String tiEntitaSacer;

    private String tiUsoXsd;

    private String dsVersioneXsd;

    private List<AroUsoXsdDatiSpec> aroUsoXsdDatiSpecs = new ArrayList<>();

    private List<DecXsdAttribDatiSpec> decXsdAttribDatiSpecs = new ArrayList<>();

    private DecTipoCompDoc decTipoCompDoc;

    private DecTipoDoc decTipoDoc;

    private DecTipoUnitaDoc decTipoUnitaDoc;

    private OrgStrut orgStrut;

    public DecXsdDatiSpec() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_XSD_DATI_SPEC")
    @XmlID
    @GenericGenerator(name = "SDEC_XSD_DATI_SPEC_ID_XSD_DATI_SPEC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_XSD_DATI_SPEC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_XSD_DATI_SPEC_ID_XSD_DATI_SPEC_GENERATOR")
    public Long getIdXsdDatiSpec() {
	return this.idXsdDatiSpec;
    }

    public void setIdXsdDatiSpec(Long idXsdDatiSpec) {
	this.idXsdDatiSpec = idXsdDatiSpec;
    }

    @Lob()
    @Column(name = "BL_XSD")
    public String getBlXsd() {
	return this.blXsd;
    }

    public void setBlXsd(String blXsd) {
	this.blXsd = blXsd;
    }

    @Column(name = "CD_VERSIONE_XSD")
    public String getCdVersioneXsd() {
	return this.cdVersioneXsd;
    }

    public void setCdVersioneXsd(String cdVersioneXsd) {
	this.cdVersioneXsd = cdVersioneXsd;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_ISTITUZ")
    public Date getDtIstituz() {
	return dtIstituz;
    }

    public void setDtIstituz(Date dtIstituz) {
	this.dtIstituz = dtIstituz;
    }

    @Column(name = "DT_SOPPRES")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDtSoppres() {
	return dtSoppres;
    }

    public void setDtSoppres(Date dtSoppres) {
	this.dtSoppres = dtSoppres;
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

    @Column(name = "TI_USO_XSD")
    public String getTiUsoXsd() {
	return this.tiUsoXsd;
    }

    public void setTiUsoXsd(String tiUsoXsd) {
	this.tiUsoXsd = tiUsoXsd;
    }

    @Column(name = "DS_VERSIONE_XSD")
    public String getDsVersioneXsd() {
	return dsVersioneXsd;
    }

    public void setDsVersioneXsd(String dsVersioneXsd) {
	this.dsVersioneXsd = dsVersioneXsd;
    }

    // bi-directional many-to-one association to AroUsoXsdDatiSpec
    @OneToMany(mappedBy = "decXsdDatiSpec")
    @XmlTransient
    public List<AroUsoXsdDatiSpec> getAroUsoXsdDatiSpecs() {
	return this.aroUsoXsdDatiSpecs;
    }

    public void setAroUsoXsdDatiSpecs(List<AroUsoXsdDatiSpec> aroUsoXsdDatiSpecs) {
	this.aroUsoXsdDatiSpecs = aroUsoXsdDatiSpecs;
    }

    // bi-directional many-to-one association to DecXsdAttribDatiSpec
    @OneToMany(mappedBy = "decXsdDatiSpec", cascade = CascadeType.PERSIST)
    @OrderBy("niOrdAttrib ASC")
    @XmlIDREF
    public List<DecXsdAttribDatiSpec> getDecXsdAttribDatiSpecs() {
	return this.decXsdAttribDatiSpecs;
    }

    public void setDecXsdAttribDatiSpecs(List<DecXsdAttribDatiSpec> decXsdAttribDatiSpecs) {
	this.decXsdAttribDatiSpecs = decXsdAttribDatiSpecs;
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

}
