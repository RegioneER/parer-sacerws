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
import java.math.BigDecimal;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_MARCA_COMP database table.
 */
@Entity
@Table(name = "ARO_MARCA_COMP")
public class AroMarcaComp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idMarcaComp;

    private String dsAlgoMarca;

    private String dsMarcaBase64;

    private String dsMsgEsitoContrConforme;

    private String dsMsgEsitoVerifMarca;

    private Date dtScadMarca;

    private BigDecimal pgBusta;

    private BigDecimal pgMarca;

    private String tiEsitoContrConforme;

    private String tiEsitoVerifMarca;

    private String tiFormatoMarca;

    private String tiMarcaTemp;

    private String dsNote;

    private Date tmMarcaTemp;

    private List<AroContrMarcaComp> aroContrMarcaComps = new ArrayList<>();

    private List<AroFirmaComp> aroFirmaComps = new ArrayList<>();

    private AroBustaCrittog aroBustaCrittog;

    private AroCompDoc aroCompDoc;

    private FirCertifCa firCertifCa;

    public AroMarcaComp() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_MARCA_COMP")
    @GenericGenerator(name = "SARO_MARCA_COMP_ID_MARCA_COMP_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_MARCA_COMP"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_MARCA_COMP_ID_MARCA_COMP_GENERATOR")
    public Long getIdMarcaComp() {
	return this.idMarcaComp;
    }

    public void setIdMarcaComp(Long idMarcaComp) {
	this.idMarcaComp = idMarcaComp;
    }

    @Column(name = "DS_ALGO_MARCA")
    public String getDsAlgoMarca() {
	return this.dsAlgoMarca;
    }

    public void setDsAlgoMarca(String dsAlgoMarca) {
	this.dsAlgoMarca = dsAlgoMarca;
    }

    @Column(name = "DS_MARCA_BASE64")
    public String getDsMarcaBase64() {
	return this.dsMarcaBase64;
    }

    public void setDsMarcaBase64(String dsMarcaBase64) {
	this.dsMarcaBase64 = dsMarcaBase64;
    }

    @Column(name = "DS_MSG_ESITO_CONTR_CONFORME")
    public String getDsMsgEsitoContrConforme() {
	return this.dsMsgEsitoContrConforme;
    }

    public void setDsMsgEsitoContrConforme(String dsMsgEsitoContrConforme) {
	this.dsMsgEsitoContrConforme = dsMsgEsitoContrConforme;
    }

    @Column(name = "DS_MSG_ESITO_VERIF_MARCA")
    public String getDsMsgEsitoVerifMarca() {
	return this.dsMsgEsitoVerifMarca;
    }

    public void setDsMsgEsitoVerifMarca(String dsMsgEsitoVerifMarca) {
	this.dsMsgEsitoVerifMarca = dsMsgEsitoVerifMarca;
    }

    @Column(name = "DS_NOTE")
    public String getDsNote() {
	return this.dsNote;
    }

    public void setDsNote(String dsNote) {
	this.dsNote = dsNote;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_SCAD_MARCA")
    public Date getDtScadMarca() {
	return this.dtScadMarca;
    }

    public void setDtScadMarca(Date dtScadMarca) {
	this.dtScadMarca = dtScadMarca;
    }

    @Column(name = "PG_BUSTA")
    public BigDecimal getPgBusta() {
	return this.pgBusta;
    }

    public void setPgBusta(BigDecimal pgBusta) {
	this.pgBusta = pgBusta;
    }

    @Column(name = "PG_MARCA")
    public BigDecimal getPgMarca() {
	return this.pgMarca;
    }

    public void setPgMarca(BigDecimal pgMarca) {
	this.pgMarca = pgMarca;
    }

    @Column(name = "TI_ESITO_CONTR_CONFORME")
    public String getTiEsitoContrConforme() {
	return this.tiEsitoContrConforme;
    }

    public void setTiEsitoContrConforme(String tiEsitoContrConforme) {
	this.tiEsitoContrConforme = tiEsitoContrConforme;
    }

    @Column(name = "TI_ESITO_VERIF_MARCA")
    public String getTiEsitoVerifMarca() {
	return this.tiEsitoVerifMarca;
    }

    public void setTiEsitoVerifMarca(String tiEsitoVerifMarca) {
	this.tiEsitoVerifMarca = tiEsitoVerifMarca;
    }

    @Column(name = "TI_FORMATO_MARCA")
    public String getTiFormatoMarca() {
	return this.tiFormatoMarca;
    }

    public void setTiFormatoMarca(String tiFormatoMarca) {
	this.tiFormatoMarca = tiFormatoMarca;
    }

    @Column(name = "TI_MARCA_TEMP")
    public String getTiMarcaTemp() {
	return this.tiMarcaTemp;
    }

    public void setTiMarcaTemp(String tiMarcaTemp) {
	this.tiMarcaTemp = tiMarcaTemp;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TM_MARCA_TEMP")
    public Date getTmMarcaTemp() {
	return this.tmMarcaTemp;
    }

    public void setTmMarcaTemp(Date tmMarcaTemp) {
	this.tmMarcaTemp = tmMarcaTemp;
    }

    // bi-directional many-to-one association to AroContrMarcaComp
    @OneToMany(mappedBy = "aroMarcaComp", cascade = {
	    CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<AroContrMarcaComp> getAroContrMarcaComps() {
	return this.aroContrMarcaComps;
    }

    public void setAroContrMarcaComps(List<AroContrMarcaComp> aroContrMarcaComps) {
	this.aroContrMarcaComps = aroContrMarcaComps;
    }

    // bi-directional many-to-one association to AroFirmaComp
    @OneToMany(mappedBy = "aroMarcaComp")
    public List<AroFirmaComp> getAroFirmaComps() {
	return this.aroFirmaComps;
    }

    public void setAroFirmaComps(List<AroFirmaComp> aroFirmaComps) {
	this.aroFirmaComps = aroFirmaComps;
    }

    // bi-directional many-to-one association to AroBustaCrittog
    @ManyToOne(fetch = FetchType.LAZY, cascade = {
	    CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "ID_BUSTA_CRITTOG", nullable = false)
    public AroBustaCrittog getAroBustaCrittog() {
	return this.aroBustaCrittog;
    }

    public void setAroBustaCrittog(AroBustaCrittog aroBustaCrittog) {
	this.aroBustaCrittog = aroBustaCrittog;
    }

    // bi-directional many-to-one association to AroCompDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    public AroCompDoc getAroCompDoc() {
	return this.aroCompDoc;
    }

    public void setAroCompDoc(AroCompDoc aroCompDoc) {
	this.aroCompDoc = aroCompDoc;
    }

    // uni-directional many-to-one association to FirCertifCa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_CA")
    public FirCertifCa getFirCertifCa() {
	return this.firCertifCa;
    }

    public void setFirCertifCa(FirCertifCa firCertifCa) {
	this.firCertifCa = firCertifCa;
    }

}
