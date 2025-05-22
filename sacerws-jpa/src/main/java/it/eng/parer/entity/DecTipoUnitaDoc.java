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
import javax.persistence.PrePersist;
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
 * The persistent class for the DEC_TIPO_UNITA_DOC database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_TIPO_UNITA_DOC")
public class DecTipoUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTipoUnitaDoc;

    private String cdSerie;

    private String cdSerieDaCreare;

    private String dlNoteTipoUd;

    private String dsSerieDaCreare;

    private String dsTipoSerieDaCreare;

    private String dsTipoUnitaDoc;

    private Date dtIstituz;

    private Date dtSoppres;

    private String flCreaTipoSerieStandard;

    private String flVersManuale;

    private String nmTipoSerieDaCreare;

    private String nmTipoUnitaDoc;

    private String tiSaveFile;

    private List<AroUnitaDoc> aroUnitaDocs = new ArrayList<>();

    private List<DecAttribDatiSpec> decAttribDatiSpecs = new ArrayList<>();

    private OrgStrut orgStrut;

    private List<DecTipoUnitaDocAmmesso> decTipoUnitaDocAmmessos = new ArrayList<>();

    private List<DecXsdDatiSpec> decXsdDatiSpecs = new ArrayList<>();

    private List<OrgRegolaValSubStrut> orgRegolaValSubStruts = new ArrayList<>();

    private List<DecUsoModelloXsdUniDoc> decUsoModelloXsdUniDocs = new ArrayList<>();

    public DecTipoUnitaDoc() {
	// hibernate
    }

    @Id
    @Column(name = "ID_TIPO_UNITA_DOC")
    @XmlID
    @GenericGenerator(name = "SDEC_TIPO_UNITA_DOC_ID_TIPO_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_TIPO_UNITA_DOC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_TIPO_UNITA_DOC_ID_TIPO_UNITA_DOC_GENERATOR")
    public Long getIdTipoUnitaDoc() {
	return this.idTipoUnitaDoc;
    }

    public void setIdTipoUnitaDoc(Long idTipoUnitaDoc) {
	this.idTipoUnitaDoc = idTipoUnitaDoc;
    }

    @Column(name = "CD_SERIE")
    public String getCdSerie() {
	return this.cdSerie;
    }

    public void setCdSerie(String cdSerie) {
	this.cdSerie = cdSerie;
    }

    @Column(name = "CD_SERIE_DA_CREARE")
    public String getCdSerieDaCreare() {
	return this.cdSerieDaCreare;
    }

    public void setCdSerieDaCreare(String cdSerieDaCreare) {
	this.cdSerieDaCreare = cdSerieDaCreare;
    }

    @Column(name = "DL_NOTE_TIPO_UD")
    public String getDlNoteTipoUd() {
	return this.dlNoteTipoUd;
    }

    public void setDlNoteTipoUd(String dlNoteTipoUd) {
	this.dlNoteTipoUd = dlNoteTipoUd;
    }

    @Column(name = "DS_SERIE_DA_CREARE")
    public String getDsSerieDaCreare() {
	return this.dsSerieDaCreare;
    }

    public void setDsSerieDaCreare(String dsSerieDaCreare) {
	this.dsSerieDaCreare = dsSerieDaCreare;
    }

    @Column(name = "DS_TIPO_SERIE_DA_CREARE")
    public String getDsTipoSerieDaCreare() {
	return this.dsTipoSerieDaCreare;
    }

    public void setDsTipoSerieDaCreare(String dsTipoSerieDaCreare) {
	this.dsTipoSerieDaCreare = dsTipoSerieDaCreare;
    }

    @Column(name = "DS_TIPO_UNITA_DOC")
    public String getDsTipoUnitaDoc() {
	return this.dsTipoUnitaDoc;
    }

    public void setDsTipoUnitaDoc(String dsTipoUnitaDoc) {
	this.dsTipoUnitaDoc = dsTipoUnitaDoc;
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

    // effettivamente mappato come varchar su DB
    @Column(name = "FL_CREA_TIPO_SERIE_STANDARD")
    public String getFlCreaTipoSerieStandard() {
	return this.flCreaTipoSerieStandard;
    }

    public void setFlCreaTipoSerieStandard(String flCreaTipoSerieStandard) {
	this.flCreaTipoSerieStandard = flCreaTipoSerieStandard;
    }

    @Column(name = "FL_VERS_MANUALE", columnDefinition = "char(1)")
    public String getFlVersManuale() {
	return this.flVersManuale;
    }

    public void setFlVersManuale(String flVersManuale) {
	this.flVersManuale = flVersManuale;
    }

    @Column(name = "NM_TIPO_SERIE_DA_CREARE")
    public String getNmTipoSerieDaCreare() {
	return this.nmTipoSerieDaCreare;
    }

    public void setNmTipoSerieDaCreare(String nmTipoSerieDaCreare) {
	this.nmTipoSerieDaCreare = nmTipoSerieDaCreare;
    }

    @Column(name = "NM_TIPO_UNITA_DOC")
    public String getNmTipoUnitaDoc() {
	return this.nmTipoUnitaDoc;
    }

    public void setNmTipoUnitaDoc(String nmTipoUnitaDoc) {
	this.nmTipoUnitaDoc = nmTipoUnitaDoc;
    }

    @Column(name = "TI_SAVE_FILE")
    public String getTiSaveFile() {
	return this.tiSaveFile;
    }

    public void setTiSaveFile(String tiSaveFile) {
	this.tiSaveFile = tiSaveFile;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @OneToMany(mappedBy = "decTipoUnitaDoc")
    @XmlTransient
    public List<AroUnitaDoc> getAroUnitaDocs() {
	return this.aroUnitaDocs;
    }

    public void setAroUnitaDocs(List<AroUnitaDoc> aroUnitaDocs) {
	this.aroUnitaDocs = aroUnitaDocs;
    }

    // bi-directional many-to-one association to DecAttribDatiSpec
    @OneToMany(mappedBy = "decTipoUnitaDoc", cascade = {
	    CascadeType.PERSIST, CascadeType.REMOVE })
    @XmlIDREF
    public List<DecAttribDatiSpec> getDecAttribDatiSpecs() {
	return this.decAttribDatiSpecs;
    }

    public void setDecAttribDatiSpecs(List<DecAttribDatiSpec> decAttribDatiSpecs) {
	this.decAttribDatiSpecs = decAttribDatiSpecs;
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

    // bi-directional many-to-one association to DecTipoUnitaDocAmmesso
    @OneToMany(mappedBy = "decTipoUnitaDoc", cascade = {
	    CascadeType.PERSIST, CascadeType.REMOVE })
    @XmlIDREF
    public List<DecTipoUnitaDocAmmesso> getDecTipoUnitaDocAmmessos() {
	return this.decTipoUnitaDocAmmessos;
    }

    public void setDecTipoUnitaDocAmmessos(List<DecTipoUnitaDocAmmesso> decTipoUnitaDocAmmessos) {
	this.decTipoUnitaDocAmmessos = decTipoUnitaDocAmmessos;
    }

    // bi-directional many-to-one association to DecXsdDatiSpec
    @OneToMany(mappedBy = "decTipoUnitaDoc", cascade = {
	    CascadeType.PERSIST, CascadeType.REMOVE })
    @XmlIDREF
    public List<DecXsdDatiSpec> getDecXsdDatiSpecs() {
	return this.decXsdDatiSpecs;
    }

    public void setDecXsdDatiSpecs(List<DecXsdDatiSpec> decXsdDatiSpecs) {
	this.decXsdDatiSpecs = decXsdDatiSpecs;
    }

    // bi-directional many-to-one association to OrgRegolaValSubStrut
    @OneToMany(mappedBy = "decTipoUnitaDoc", cascade = {
	    CascadeType.PERSIST, CascadeType.REMOVE })
    public List<OrgRegolaValSubStrut> getOrgRegolaValSubStruts() {
	return this.orgRegolaValSubStruts;
    }

    public void setOrgRegolaValSubStruts(List<OrgRegolaValSubStrut> orgRegolaValSubStruts) {
	this.orgRegolaValSubStruts = orgRegolaValSubStruts;
    }

    // bi-directional many-to-one association to DecTipoRapprAmmesso
    @OneToMany(mappedBy = "decTipoUnitaDoc", cascade = CascadeType.PERSIST)
    @XmlTransient
    public List<DecUsoModelloXsdUniDoc> getDecUsoModelloXsdUniDocs() {
	return this.decUsoModelloXsdUniDocs;
    }

    public void setDecUsoModelloXsdUniDocs(List<DecUsoModelloXsdUniDoc> decUsoModelloXsdUniDocs) {
	this.decUsoModelloXsdUniDocs = decUsoModelloXsdUniDocs;
    }

    /**
     * Gestione dei default. Risulta la migliore pratica in quanto è indipendente dal db utilizzato
     * e sfrutta diretta JPA quindi calabile sotto ogni contesto in termini di ORM
     *
     * ref. https://stackoverflow.com/a/13432234
     */
    @PrePersist
    void preInsert() {
	if (this.flVersManuale == null) {
	    this.flVersManuale = "0";
	}
    }
}
