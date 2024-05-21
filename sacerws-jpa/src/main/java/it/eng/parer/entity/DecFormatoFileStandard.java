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
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_FORMATO_FILE_STANDARD database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_FORMATO_FILE_STANDARD")
public class DecFormatoFileStandard implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFormatoFileStandard;

    private String cdVersione;

    private String dsCopyright;

    private String dsFormatoFileStandard;

    private String flFormatoConcat;

    private String nmFormatoFileStandard;

    private String nmMimetypeFile;

    private String tiEsitoContrFormato;

    private List<AroCompDoc> aroCompDocs = new ArrayList<>();

    private List<DecEstensioneFile> decEstensioneFiles = new ArrayList<>();

    private List<DecFormatoFileBusta> decFormatoFileBustas = new ArrayList<>();

    private List<DecUsoFormatoFileStandard> decUsoFormatoFileStandards = new ArrayList<>();

    private List<AroBustaCrittog> aroBustaCrittogs = new ArrayList<>();

    private List<DecTipoRapprComp> decTipoRapprComps = new ArrayList<>();

    public DecFormatoFileStandard() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_FORMATO_FILE_STANDARD")
    @GenericGenerator(name = "SDEC_FORMATO_FILE_STANDARD_ID_FORMATO_FILE_STANDARD_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_FORMATO_FILE_STANDARD"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_FORMATO_FILE_STANDARD_ID_FORMATO_FILE_STANDARD_GENERATOR")
    public Long getIdFormatoFileStandard() {
        return this.idFormatoFileStandard;
    }

    public void setIdFormatoFileStandard(Long idFormatoFileStandard) {
        this.idFormatoFileStandard = idFormatoFileStandard;
    }

    @Column(name = "CD_VERSIONE")
    public String getCdVersione() {
        return this.cdVersione;
    }

    public void setCdVersione(String cdVersione) {
        this.cdVersione = cdVersione;
    }

    @Column(name = "DS_COPYRIGHT")
    public String getDsCopyright() {
        return this.dsCopyright;
    }

    public void setDsCopyright(String dsCopyright) {
        this.dsCopyright = dsCopyright;
    }

    @Column(name = "DS_FORMATO_FILE_STANDARD")
    public String getDsFormatoFileStandard() {
        return this.dsFormatoFileStandard;
    }

    public void setDsFormatoFileStandard(String dsFormatoFileStandard) {
        this.dsFormatoFileStandard = dsFormatoFileStandard;
    }

    @Column(name = "FL_FORMATO_CONCAT", columnDefinition = "char(1)")
    public String getFlFormatoConcat() {
        return this.flFormatoConcat;
    }

    public void setFlFormatoConcat(String flFormatoConcat) {
        this.flFormatoConcat = flFormatoConcat;
    }

    @Column(name = "NM_FORMATO_FILE_STANDARD")
    public String getNmFormatoFileStandard() {
        return this.nmFormatoFileStandard;
    }

    public void setNmFormatoFileStandard(String nmFormatoFileStandard) {
        this.nmFormatoFileStandard = nmFormatoFileStandard;
    }

    @Column(name = "NM_MIMETYPE_FILE")
    public String getNmMimetypeFile() {
        return this.nmMimetypeFile;
    }

    public void setNmMimetypeFile(String nmMimetypeFile) {
        this.nmMimetypeFile = nmMimetypeFile;
    }

    @Column(name = "TI_ESITO_CONTR_FORMATO")
    public String getTiEsitoContrFormato() {
        return this.tiEsitoContrFormato;
    }

    public void setTiEsitoContrFormato(String tiEsitoContrFormato) {
        this.tiEsitoContrFormato = tiEsitoContrFormato;
    }

    // bi-directional many-to-one association to AroCompDoc
    @OneToMany(mappedBy = "decFormatoFileStandard")
    @XmlTransient
    public List<AroCompDoc> getAroCompDocs() {
        return this.aroCompDocs;
    }

    public void setAroCompDocs(List<AroCompDoc> aroCompDocs) {
        this.aroCompDocs = aroCompDocs;
    }

    // bi-directional many-to-one association to DecEstensioneFile
    @OneToMany(mappedBy = "decFormatoFileStandard")
    public List<DecEstensioneFile> getDecEstensioneFiles() {
        return this.decEstensioneFiles;
    }

    public void setDecEstensioneFiles(List<DecEstensioneFile> decEstensioneFiles) {
        this.decEstensioneFiles = decEstensioneFiles;
    }

    // bi-directional many-to-one association to DecFormatoFileBusta
    @OneToMany(mappedBy = "decFormatoFileStandard")
    public List<DecFormatoFileBusta> getDecFormatoFileBustas() {
        return this.decFormatoFileBustas;
    }

    public void setDecFormatoFileBustas(List<DecFormatoFileBusta> decFormatoFileBustas) {
        this.decFormatoFileBustas = decFormatoFileBustas;
    }

    // bi-directional many-to-one association to DecUsoFormatoFileStandard
    @OneToMany(mappedBy = "decFormatoFileStandard", cascade = { CascadeType.ALL })
    @XmlInverseReference(mappedBy = "decFormatoFileStandard")
    public List<DecUsoFormatoFileStandard> getDecUsoFormatoFileStandards() {
        return this.decUsoFormatoFileStandards;
    }

    public void setDecUsoFormatoFileStandards(List<DecUsoFormatoFileStandard> decUsoFormatoFileStandards) {
        this.decUsoFormatoFileStandards = decUsoFormatoFileStandards;
    }

    // bi-directional many-to-one association to AroBustaCrittog
    @OneToMany(mappedBy = "decFormatoFileStandard")
    @XmlTransient
    public List<AroBustaCrittog> getAroBustaCrittogs() {
        return this.aroBustaCrittogs;
    }

    public void setAroBustaCrittogs(List<AroBustaCrittog> aroBustaCrittogs) {
        this.aroBustaCrittogs = aroBustaCrittogs;
    }

    // bi-directional many-to-one association to DecTipoRapprComp
    @OneToMany(mappedBy = "decFormatoFileStandard", cascade = CascadeType.PERSIST)
    @XmlIDREF
    public List<DecTipoRapprComp> getDecTipoRapprComps() {
        return this.decTipoRapprComps;
    }

    public void setDecTipoRapprComps(List<DecTipoRapprComp> decTipoRapprComps) {
        this.decTipoRapprComps = decTipoRapprComps;
    }

}
