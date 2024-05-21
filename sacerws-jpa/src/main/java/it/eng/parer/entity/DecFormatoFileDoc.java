/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
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

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_FORMATO_FILE_DOC database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_FORMATO_FILE_DOC")
public class DecFormatoFileDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFormatoFileDoc;

    private String cdVersione;

    private String dsFormatoFileDoc;

    private Date dtIstituz;

    private Date dtSoppres;

    private String nmFormatoFileDoc;

    private List<AroCompDoc> aroCompDocs = new ArrayList<>();

    private List<DecFormatoFileAmmesso> decFormatoFileAmmessos = new ArrayList<>();

    private OrgStrut orgStrut;

    private List<DecUsoFormatoFileStandard> decUsoFormatoFileStandards = new ArrayList<>();

    private List<DecTipoRapprComp> decTipoRapprCompConts = new ArrayList<>();

    private List<DecTipoRapprComp> decTipoRapprCompConvs = new ArrayList<>();

    public DecFormatoFileDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_FORMATO_FILE_DOC")
    @GenericGenerator(name = "SDEC_FORMATO_FILE_DOC_ID_FORMATO_FILE_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_FORMATO_FILE_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_FORMATO_FILE_DOC_ID_FORMATO_FILE_DOC_GENERATOR")
    public Long getIdFormatoFileDoc() {
        return this.idFormatoFileDoc;
    }

    public void setIdFormatoFileDoc(Long idFormatoFileDoc) {
        this.idFormatoFileDoc = idFormatoFileDoc;
    }

    @Column(name = "CD_VERSIONE")
    public String getCdVersione() {
        return this.cdVersione;
    }

    public void setCdVersione(String cdVersione) {
        this.cdVersione = cdVersione;
    }

    @Column(name = "DS_FORMATO_FILE_DOC")
    public String getDsFormatoFileDoc() {
        return this.dsFormatoFileDoc;
    }

    public void setDsFormatoFileDoc(String dsFormatoFileDoc) {
        this.dsFormatoFileDoc = dsFormatoFileDoc;
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

    @Column(name = "NM_FORMATO_FILE_DOC")
    public String getNmFormatoFileDoc() {
        return this.nmFormatoFileDoc;
    }

    public void setNmFormatoFileDoc(String nmFormatoFileDoc) {
        this.nmFormatoFileDoc = nmFormatoFileDoc;
    }

    // bi-directional many-to-one association to AroCompDoc
    @OneToMany(mappedBy = "decFormatoFileDoc")
    @XmlTransient
    public List<AroCompDoc> getAroCompDocs() {
        return this.aroCompDocs;
    }

    public void setAroCompDocs(List<AroCompDoc> aroCompDocs) {
        this.aroCompDocs = aroCompDocs;
    }

    // bi-directional many-to-one association to DecFormatoFileAmmesso
    @OneToMany(mappedBy = "decFormatoFileDoc", cascade = CascadeType.PERSIST)
    @XmlIDREF
    public List<DecFormatoFileAmmesso> getDecFormatoFileAmmessos() {
        return this.decFormatoFileAmmessos;
    }

    public void setDecFormatoFileAmmessos(List<DecFormatoFileAmmesso> decFormatoFileAmmessos) {
        this.decFormatoFileAmmessos = decFormatoFileAmmessos;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    @XmlInverseReference(mappedBy = "decFormatoFileDocs")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to DecUsoFormatoFileStandard
    @OneToMany(mappedBy = "decFormatoFileDoc", cascade = CascadeType.PERSIST)
    public List<DecUsoFormatoFileStandard> getDecUsoFormatoFileStandards() {
        return this.decUsoFormatoFileStandards;
    }

    public void setDecUsoFormatoFileStandards(List<DecUsoFormatoFileStandard> decUsoFormatoFileStandards) {
        this.decUsoFormatoFileStandards = decUsoFormatoFileStandards;
    }

    // bi-directional many-to-one association to DecTipoRapprComp
    @OneToMany(mappedBy = "decFormatoFileDocCont", cascade = CascadeType.PERSIST)
    @XmlIDREF
    public List<DecTipoRapprComp> getDecTipoRapprCompConts() {
        return this.decTipoRapprCompConts;
    }

    public void setDecTipoRapprCompConts(List<DecTipoRapprComp> decTipoRapprCompConts) {
        this.decTipoRapprCompConts = decTipoRapprCompConts;
    }

    // bi-directional many-to-one association to DecTipoRapprComp
    @OneToMany(mappedBy = "decFormatoFileDocConv", cascade = CascadeType.PERSIST)
    @XmlIDREF
    public List<DecTipoRapprComp> getDecTipoRapprCompConvs() {
        return this.decTipoRapprCompConvs;
    }

    public void setDecTipoRapprCompConvs(List<DecTipoRapprComp> decTipoRapprCompConvs) {
        this.decTipoRapprCompConvs = decTipoRapprCompConvs;
    }

}
