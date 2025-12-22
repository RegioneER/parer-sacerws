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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_UPD_COMP_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_UPD_COMP_UNITA_DOC")
public class AroUpdCompUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUpdCompUnitaDoc;

    private String dlUrnCompVers;

    private String dsIdCompVers;

    private String dsNomeCompVers;

    private AroCompDoc aroCompDoc;

    private AroUpdDocUnitaDoc aroUpdDocUnitaDoc;

    private List<AroUpdDatiSpecUnitaDoc> aroUpdDatiSpecUnitaDocs = new ArrayList<>();

    private String flUpdDatiSpec;

    private String flUpdDatiSpecMigraz;

    public AroUpdCompUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UPD_COMP_UNITA_DOC")
    @GenericGenerator(name = "SARO_UPD_COMP_UNITA_DOC_ID_UPD_COMP_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_UPD_COMP_UNITA_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_UPD_COMP_UNITA_DOC_ID_UPD_COMP_UNITA_DOC_GENERATOR")
    public Long getIdUpdCompUnitaDoc() {
        return this.idUpdCompUnitaDoc;
    }

    public void setIdUpdCompUnitaDoc(Long idUpdCompUnitaDoc) {
        this.idUpdCompUnitaDoc = idUpdCompUnitaDoc;
    }

    @Column(name = "DL_URN_COMP_VERS")
    public String getDlUrnCompVers() {
        return this.dlUrnCompVers;
    }

    public void setDlUrnCompVers(String dlUrnCompVers) {
        this.dlUrnCompVers = dlUrnCompVers;
    }

    @Column(name = "DS_ID_COMP_VERS")
    public String getDsIdCompVers() {
        return this.dsIdCompVers;
    }

    public void setDsIdCompVers(String dsIdCompVers) {
        this.dsIdCompVers = dsIdCompVers;
    }

    @Column(name = "DS_NOME_COMP_VERS")
    public String getDsNomeCompVers() {
        return this.dsNomeCompVers;
    }

    public void setDsNomeCompVers(String dsNomeCompVers) {
        this.dsNomeCompVers = dsNomeCompVers;
    }

    // bi-directional many-to-one association to AroDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public void setAroCompDoc(AroCompDoc aroCompDoc) {
        this.aroCompDoc = aroCompDoc;
    }

    // bi-directional many-to-one association to AroDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_DOC_UNITA_DOC")
    public AroUpdDocUnitaDoc getAroUpdDocUnitaDoc() {
        return this.aroUpdDocUnitaDoc;
    }

    public void setAroUpdDocUnitaDoc(AroUpdDocUnitaDoc aroUpdDocUnitaDoc) {
        this.aroUpdDocUnitaDoc = aroUpdDocUnitaDoc;
    }

    // bi-directional many-to-one association to AroUpdDatiSpecUnitaDoc
    @OneToMany(mappedBy = "aroUpdCompUnitaDoc", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<AroUpdDatiSpecUnitaDoc> getAroUpdDatiSpecUnitaDocs() {
        return this.aroUpdDatiSpecUnitaDocs;
    }

    public void setAroUpdDatiSpecUnitaDocs(List<AroUpdDatiSpecUnitaDoc> aroUpdDatiSpecUnitaDocs) {
        this.aroUpdDatiSpecUnitaDocs = aroUpdDatiSpecUnitaDocs;
    }

    @Column(name = "FL_UPD_DATI_SPEC", columnDefinition = "char(1)")
    public String getFlUpdDatiSpec() {
        return this.flUpdDatiSpec;
    }

    public void setFlUpdDatiSpec(String flUpdDatiSpec) {
        this.flUpdDatiSpec = flUpdDatiSpec;
    }

    @Column(name = "FL_UPD_DATI_SPEC_MIGRAZ", columnDefinition = "char(1)")
    public String getFlUpdDatiSpecMigraz() {
        return this.flUpdDatiSpecMigraz;
    }

    public void setFlUpdDatiSpecMigraz(String flUpdDatiSpecMigraz) {
        this.flUpdDatiSpecMigraz = flUpdDatiSpecMigraz;
    }
}
