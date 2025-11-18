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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_TIPO_DOC database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_TIPO_DOC")
public class DecTipoDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTipoDoc;

    private String dlNoteTipoDoc;

    private String dsPeriodicitaVers;

    private String dsTipoDoc;

    private Date dtIstituz;

    private Date dtSoppres;

    private String flTipoDocPrincipale;

    private String nmTipoDoc;

    private List<AroDoc> aroDocs = new ArrayList<>();

    private List<DecAttribDatiSpec> decAttribDatiSpecs = new ArrayList<>();

    private OrgStrut orgStrut;
    private List<VrsSesUpdUnitaDocKo> vrsSesUpdUnitaDocKos;
    private List<VrsUpdUnitaDocKo> vrsUpdUnitaDocKos;

    private List<DecXsdDatiSpec> decXsdDatiSpecs = new ArrayList<>();

    private List<OrgRegolaValSubStrut> orgRegolaValSubStruts = new ArrayList<>();

    private List<DecUsoModelloXsdDoc> decUsoModelloXsdDocs = new ArrayList<>();

    public DecTipoDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_TIPO_DOC")
    @XmlID
    @GenericGenerator(name = "SDEC_TIPO_DOC_ID_TIPO_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_TIPO_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_TIPO_DOC_ID_TIPO_DOC_GENERATOR")
    public Long getIdTipoDoc() {
        return this.idTipoDoc;
    }

    public void setIdTipoDoc(Long idTipoDoc) {
        this.idTipoDoc = idTipoDoc;
    }

    @Column(name = "DL_NOTE_TIPO_DOC")
    public String getDlNoteTipoDoc() {
        return this.dlNoteTipoDoc;
    }

    public void setDlNoteTipoDoc(String dlNoteTipoDoc) {
        this.dlNoteTipoDoc = dlNoteTipoDoc;
    }

    @Column(name = "DS_PERIODICITA_VERS")
    public String getDsPeriodicitaVers() {
        return this.dsPeriodicitaVers;
    }

    public void setDsPeriodicitaVers(String dsPeriodicitaVers) {
        this.dsPeriodicitaVers = dsPeriodicitaVers;
    }

    @Column(name = "DS_TIPO_DOC")
    public String getDsTipoDoc() {
        return this.dsTipoDoc;
    }

    public void setDsTipoDoc(String dsTipoDoc) {
        this.dsTipoDoc = dsTipoDoc;
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

    @Column(name = "FL_TIPO_DOC_PRINCIPALE", columnDefinition = "char(1)")
    public String getFlTipoDocPrincipale() {
        return this.flTipoDocPrincipale;
    }

    public void setFlTipoDocPrincipale(String flTipoDocPrincipale) {
        this.flTipoDocPrincipale = flTipoDocPrincipale;
    }

    @Column(name = "NM_TIPO_DOC")
    public String getNmTipoDoc() {
        return this.nmTipoDoc;
    }

    public void setNmTipoDoc(String nmTipoDoc) {
        this.nmTipoDoc = nmTipoDoc;
    }

    // bi-directional many-to-one association to AroDoc
    @OneToMany(mappedBy = "decTipoDoc")
    @XmlTransient
    public List<AroDoc> getAroDocs() {
        return this.aroDocs;
    }

    public void setAroDocs(List<AroDoc> aroDocs) {
        this.aroDocs = aroDocs;
    }

    // bi-directional many-to-one association to DecAttribDatiSpec
    @OneToMany(mappedBy = "decTipoDoc", cascade = {
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

    // bi-directional many-to-one association to DecXsdDatiSpec
    @OneToMany(mappedBy = "decTipoDoc", cascade = {
            CascadeType.PERSIST, CascadeType.REMOVE })
    @XmlIDREF
    public List<DecXsdDatiSpec> getDecXsdDatiSpecs() {
        return this.decXsdDatiSpecs;
    }

    public void setDecXsdDatiSpecs(List<DecXsdDatiSpec> decXsdDatiSpecs) {
        this.decXsdDatiSpecs = decXsdDatiSpecs;
    }

    // bi-directional many-to-one association to OrgRegolaValSubStrut
    @OneToMany(mappedBy = "decTipoDoc", cascade = {
            CascadeType.PERSIST, CascadeType.REMOVE })
    @XmlIDREF
    public List<OrgRegolaValSubStrut> getOrgRegolaValSubStruts() {
        return this.orgRegolaValSubStruts;
    }

    public void setOrgRegolaValSubStruts(List<OrgRegolaValSubStrut> orgRegolaValSubStruts) {
        this.orgRegolaValSubStruts = orgRegolaValSubStruts;
    }

    // bi-directional many-to-one association to DecTipoRapprAmmesso
    @OneToMany(mappedBy = "decTipoDoc", cascade = CascadeType.PERSIST)
    public List<DecUsoModelloXsdDoc> getDecUsoModelloXsdDocs() {
        return this.decUsoModelloXsdDocs;
    }

    public void setDecUsoModelloXsdDocs(List<DecUsoModelloXsdDoc> decUsoModelloXsdDocs) {
        this.decUsoModelloXsdDocs = decUsoModelloXsdDocs;
    }

    // bi-directional many-to-one association to VrsSesUpdUnitaDocKo
    @OneToMany(mappedBy = "decTipoDocPrinc")
    @XmlTransient
    public List<VrsSesUpdUnitaDocKo> getVrsSesUpdUnitaDocKos() {
        return this.vrsSesUpdUnitaDocKos;
    }

    public void setVrsSesUpdUnitaDocKos(List<VrsSesUpdUnitaDocKo> vrsSesUpdUnitaDocKos) {
        this.vrsSesUpdUnitaDocKos = vrsSesUpdUnitaDocKos;
    }

    // bi-directional many-to-one association to VrsUpdUnitaDocKo
    @OneToMany(mappedBy = "decTipoDocPrincLast")
    @XmlTransient
    public List<VrsUpdUnitaDocKo> getVrsUpdUnitaDocKos() {
        return this.vrsUpdUnitaDocKos;
    }

    public void setVrsUpdUnitaDocKos(List<VrsUpdUnitaDocKo> vrsUpdUnitaDocKos) {
        this.vrsUpdUnitaDocKos = vrsUpdUnitaDocKos;
    }

}
