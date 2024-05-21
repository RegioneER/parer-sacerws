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
import java.math.BigDecimal;
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
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_REGISTRO_UNITA_DOC database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_REGISTRO_UNITA_DOC")
public class DecRegistroUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idRegistroUnitaDoc;

    private String cdRegistroNormaliz;

    private String cdRegistroUnitaDoc;

    private String cdSerieDaCreare;

    private String dsRegistroUnitaDoc;

    private String dsSerieDaCreare;

    private String dsTipoSerieDaCreare;

    private Date dtIstituz;

    private Date dtSoppres;

    private String flCreaSerie;

    private String flCreaTipoSerieStandard;

    private String flRegistroFisc;

    private String flTipoSerieMult;

    private BigDecimal niAnniConserv;

    private String nmTipoSerieDaCreare;

    private List<AroUnitaDoc> aroUnitaDocs = new ArrayList<>();

    private List<DecAaRegistroUnitaDoc> decAaRegistroUnitaDocs = new ArrayList<>();

    private OrgStrut orgStrut;

    private List<DecTipoUnitaDocAmmesso> decTipoUnitaDocAmmessos = new ArrayList<>();

    private List<MonAaUnitaDocRegistro> monAaUnitaDocRegistros = new ArrayList<>();

    public DecRegistroUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_REGISTRO_UNITA_DOC")
    @XmlID
    @GenericGenerator(name = "SDEC_REGISTRO_UNITA_DOC_ID_REGISTRO_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_REGISTRO_UNITA_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_REGISTRO_UNITA_DOC_ID_REGISTRO_UNITA_DOC_GENERATOR")
    public Long getIdRegistroUnitaDoc() {
        return this.idRegistroUnitaDoc;
    }

    public void setIdRegistroUnitaDoc(Long idRegistroUnitaDoc) {
        this.idRegistroUnitaDoc = idRegistroUnitaDoc;
    }

    @Column(name = "CD_REGISTRO_UNITA_DOC")
    public String getCdRegistroUnitaDoc() {
        return this.cdRegistroUnitaDoc;
    }

    public void setCdRegistroUnitaDoc(String cdRegistroUnitaDoc) {
        this.cdRegistroUnitaDoc = cdRegistroUnitaDoc;
    }

    @Column(name = "CD_REGISTRO_NORMALIZ")
    public String getCdRegistroNormaliz() {
        return this.cdRegistroNormaliz;
    }

    public void setCdRegistroNormaliz(String cdRegistroNormaliz) {
        this.cdRegistroNormaliz = cdRegistroNormaliz;
    }

    @Column(name = "CD_SERIE_DA_CREARE")
    public String getCdSerieDaCreare() {
        return this.cdSerieDaCreare;
    }

    public void setCdSerieDaCreare(String cdSerieDaCreare) {
        this.cdSerieDaCreare = cdSerieDaCreare;
    }

    @Column(name = "DS_REGISTRO_UNITA_DOC")
    public String getDsRegistroUnitaDoc() {
        return this.dsRegistroUnitaDoc;
    }

    public void setDsRegistroUnitaDoc(String dsRegistroUnitaDoc) {
        this.dsRegistroUnitaDoc = dsRegistroUnitaDoc;
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

    @Column(name = "FL_CREA_SERIE", columnDefinition = "char(1)")
    public String getFlCreaSerie() {
        return this.flCreaSerie;
    }

    public void setFlCreaSerie(String flCreaSerie) {
        this.flCreaSerie = flCreaSerie;
    }

    @Column(name = "FL_CREA_TIPO_SERIE_STANDARD", columnDefinition = "char(1)")
    public String getFlCreaTipoSerieStandard() {
        return this.flCreaTipoSerieStandard;
    }

    public void setFlCreaTipoSerieStandard(String flCreaTipoSerieStandard) {
        this.flCreaTipoSerieStandard = flCreaTipoSerieStandard;
    }

    @Column(name = "FL_REGISTRO_FISC", columnDefinition = "char(1)")
    public String getFlRegistroFisc() {
        return this.flRegistroFisc;
    }

    public void setFlRegistroFisc(String flRegistroFisc) {
        this.flRegistroFisc = flRegistroFisc;
    }

    @Column(name = "FL_TIPO_SERIE_MULT", columnDefinition = "char(1)")
    public String getFlTipoSerieMult() {
        return this.flTipoSerieMult;
    }

    public void setFlTipoSerieMult(String flTipoSerieMult) {
        this.flTipoSerieMult = flTipoSerieMult;
    }

    @Column(name = "NI_ANNI_CONSERV")
    public BigDecimal getNiAnniConserv() {
        return this.niAnniConserv;
    }

    public void setNiAnniConserv(BigDecimal niAnniConserv) {
        this.niAnniConserv = niAnniConserv;
    }

    @Column(name = "NM_TIPO_SERIE_DA_CREARE")
    public String getNmTipoSerieDaCreare() {
        return this.nmTipoSerieDaCreare;
    }

    public void setNmTipoSerieDaCreare(String nmTipoSerieDaCreare) {
        this.nmTipoSerieDaCreare = nmTipoSerieDaCreare;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @OneToMany(mappedBy = "decRegistroUnitaDoc")
    @XmlTransient
    public List<AroUnitaDoc> getAroUnitaDocs() {
        return this.aroUnitaDocs;
    }

    public void setAroUnitaDocs(List<AroUnitaDoc> aroUnitaDocs) {
        this.aroUnitaDocs = aroUnitaDocs;
    }

    // bi-directional many-to-one association to DecAaRegistroUnitaDoc
    @OneToMany(mappedBy = "decRegistroUnitaDoc", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    public List<DecAaRegistroUnitaDoc> getDecAaRegistroUnitaDocs() {
        return this.decAaRegistroUnitaDocs;
    }

    public void setDecAaRegistroUnitaDocs(List<DecAaRegistroUnitaDoc> decAaRegistroUnitaDocs) {
        this.decAaRegistroUnitaDocs = decAaRegistroUnitaDocs;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    @XmlInverseReference(mappedBy = "decRegistroUnitaDocs")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to DecTipoUnitaDocAmmesso
    @OneToMany(mappedBy = "decRegistroUnitaDoc", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    public List<DecTipoUnitaDocAmmesso> getDecTipoUnitaDocAmmessos() {
        return this.decTipoUnitaDocAmmessos;
    }

    public void setDecTipoUnitaDocAmmessos(List<DecTipoUnitaDocAmmesso> decTipoUnitaDocAmmessos) {
        this.decTipoUnitaDocAmmessos = decTipoUnitaDocAmmessos;
    }

    // bi-directional many-to-one association to MonAaUnitaDocRegistro
    @OneToMany(mappedBy = "decRegistroUnitaDoc")
    @XmlTransient
    public List<MonAaUnitaDocRegistro> getMonAaUnitaDocRegistros() {
        return this.monAaUnitaDocRegistros;
    }

    public void setMonAaUnitaDocRegistros(List<MonAaUnitaDocRegistro> monAaUnitaDocRegistros) {
        this.monAaUnitaDocRegistros = monAaUnitaDocRegistros;
    }
}
