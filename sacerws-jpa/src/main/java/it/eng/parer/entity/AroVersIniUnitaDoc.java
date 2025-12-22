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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_VERS_INI_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_VERS_INI_UNITA_DOC")
public class AroVersIniUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idVersIniUnitaDoc;

    private String cdFascicPrinc;

    private String cdSottofascicPrinc;

    private String dlOggettoUnitaDoc;

    private String dsClassifPrinc;

    private String dsOggettoFascicPrinc;

    private String dsOggettoSottofascicPrinc;

    private Date dtRegUnitaDoc;

    private AroUnitaDoc aroUnitaDoc;

    private List<AroVersIniArchivSec> aroVersIniArchivSecs = new ArrayList<>();

    private List<AroVersIniLinkUnitaDoc> aroVersIniLinkUnitaDocs = new ArrayList<>();

    private List<AroVersIniDoc> aroVersIniDocs = new ArrayList<>();

    private List<AroVersIniDatiSpec> aroVersIniDatiSpecs = new ArrayList<>();

    public AroVersIniUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_VERS_INI_UNITA_DOC")
    @GenericGenerator(name = "SARO_VERS_INI_UNITA_DOC_ID_VERS_INI_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_VERS_INI_UNITA_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_VERS_INI_UNITA_DOC_ID_VERS_INI_UNITA_DOC_GENERATOR")
    public Long getIdVersIniUnitaDoc() {
        return this.idVersIniUnitaDoc;
    }

    public void setIdVersIniUnitaDoc(Long idVersIniUnitaDoc) {
        this.idVersIniUnitaDoc = idVersIniUnitaDoc;
    }

    @Column(name = "CD_FASCIC_PRINC")
    public String getCdFascicPrinc() {
        return this.cdFascicPrinc;
    }

    public void setCdFascicPrinc(String cdFascicPrinc) {
        this.cdFascicPrinc = cdFascicPrinc;
    }

    @Column(name = "CD_SOTTOFASCIC_PRINC")
    public String getCdSottofascicPrinc() {
        return this.cdSottofascicPrinc;
    }

    public void setCdSottofascicPrinc(String cdSottofascicPrinc) {
        this.cdSottofascicPrinc = cdSottofascicPrinc;
    }

    @Column(name = "DL_OGGETTO_UNITA_DOC")
    public String getDlOggettoUnitaDoc() {
        return this.dlOggettoUnitaDoc;
    }

    public void setDlOggettoUnitaDoc(String dlOggettoUnitaDoc) {
        this.dlOggettoUnitaDoc = dlOggettoUnitaDoc;
    }

    @Column(name = "DS_CLASSIF_PRINC")
    public String getDsClassifPrinc() {
        return this.dsClassifPrinc;
    }

    public void setDsClassifPrinc(String dsClassifPrinc) {
        this.dsClassifPrinc = dsClassifPrinc;
    }

    @Column(name = "DS_OGGETTO_FASCIC_PRINC")
    public String getDsOggettoFascicPrinc() {
        return this.dsOggettoFascicPrinc;
    }

    public void setDsOggettoFascicPrinc(String dsOggettoFascicPrinc) {
        this.dsOggettoFascicPrinc = dsOggettoFascicPrinc;
    }

    @Column(name = "DS_OGGETTO_SOTTOFASCIC_PRINC")
    public String getDsOggettoSottofascicPrinc() {
        return this.dsOggettoSottofascicPrinc;
    }

    public void setDsOggettoSottofascicPrinc(String dsOggettoSottofascicPrinc) {
        this.dsOggettoSottofascicPrinc = dsOggettoSottofascicPrinc;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DT_REG_UNITA_DOC")
    public Date getDtRegUnitaDoc() {
        return this.dtRegUnitaDoc;
    }

    public void setDtRegUnitaDoc(Date dtRegUnitaDoc) {
        this.dtRegUnitaDoc = dtRegUnitaDoc;
    }

    // bi-directional one-to-one association to AroUnitaDoc
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

    public void setAroUnitaDoc(AroUnitaDoc aroUnitaDoc) {
        this.aroUnitaDoc = aroUnitaDoc;
    }

    // bi-directional many-to-one association to AroVersIniArchivSec
    @OneToMany(mappedBy = "aroVersIniUnitaDoc", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<AroVersIniArchivSec> getAroVersIniArchivSecs() {
        return this.aroVersIniArchivSecs;
    }

    public void setAroVersIniArchivSecs(List<AroVersIniArchivSec> aroVersIniArchivSecs) {
        this.aroVersIniArchivSecs = aroVersIniArchivSecs;
    }

    // bi-directional many-to-one association to AroVersIniLinkUnitaDoc
    @OneToMany(mappedBy = "aroVersIniUnitaDoc")
    public List<AroVersIniLinkUnitaDoc> getAroVersIniLinkUnitaDocs() {
        return this.aroVersIniLinkUnitaDocs;
    }

    public void setAroVersIniLinkUnitaDocs(List<AroVersIniLinkUnitaDoc> aroVersIniLinkUnitaDocs) {
        this.aroVersIniLinkUnitaDocs = aroVersIniLinkUnitaDocs;
    }

    // bi-directional many-to-one association to AroVersIniDoc
    @OneToMany(mappedBy = "aroVersIniUnitaDoc")
    public List<AroVersIniDoc> getAroVersIniDocs() {
        return this.aroVersIniDocs;
    }

    public void setAroVersIniDocs(List<AroVersIniDoc> aroVersIniDocs) {
        this.aroVersIniDocs = aroVersIniDocs;
    }

    // bi-directional many-to-one association to AroVersIniDoc
    @OneToMany(mappedBy = "aroVersIniUnitaDoc")
    public List<AroVersIniDatiSpec> getAroVersIniDatiSpecs() {
        return this.aroVersIniDatiSpecs;
    }

    public void setAroVersIniDatiSpecs(List<AroVersIniDatiSpec> aroVersIniDatiSpecs) {
        this.aroVersIniDatiSpecs = aroVersIniDatiSpecs;
    }

}
