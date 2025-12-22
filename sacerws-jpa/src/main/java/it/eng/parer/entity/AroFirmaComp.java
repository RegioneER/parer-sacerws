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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_FIRMA_COMP database table.
 */
@Entity
@Table(name = "ARO_FIRMA_COMP")
public class AroFirmaComp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFirmaComp;

    private String cdFirmatario;

    private String dlDnFirmatario;

    private String dsAlgoFirma;

    private String dsFirmaBase64;

    private String blFirmaBase64;

    private String dsMsgEsitoContrConforme;

    private String dsMsgEsitoVerifFirma;

    private Date dtFirma;

    private BigDecimal idStrut;

    private String nmCognomeFirmatario;

    private String nmFirmatario;

    private BigDecimal pgBusta;

    private BigDecimal pgFirma;

    private String tiEsitoContrConforme;

    private String tiEsitoVerifFirma;

    private String tiFirma;

    private String tiFormatoFirma;

    private String tiRifTempUsato;

    private String dsNote;

    private Date tmRifTempUsato;

    private List<AroControfirmaFirma> aroControfirmaFirmaFiglios = new ArrayList<>();

    private List<AroControfirmaFirma> aroControfirmaFirmaPadres = new ArrayList<>();

    private List<AroContrFirmaComp> aroContrFirmaComps = new ArrayList<>();

    private AroBustaCrittog aroBustaCrittog;

    private AroCompDoc aroCompDoc;

    private AroMarcaComp aroMarcaComp;

    private FirCertifFirmatario firCertifFirmatario;

    private List<AroVerifFirmaDtVer> aroVerifFirmaDtVers = new ArrayList<>();

    public AroFirmaComp() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_FIRMA_COMP")
    @GenericGenerator(name = "SARO_FIRMA_COMP_ID_FIRMA_COMP_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_FIRMA_COMP"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_FIRMA_COMP_ID_FIRMA_COMP_GENERATOR")
    public Long getIdFirmaComp() {
        return this.idFirmaComp;
    }

    public void setIdFirmaComp(Long idFirmaComp) {
        this.idFirmaComp = idFirmaComp;
    }

    @Column(name = "CD_FIRMATARIO")
    public String getCdFirmatario() {
        return this.cdFirmatario;
    }

    public void setCdFirmatario(String cdFirmatario) {
        this.cdFirmatario = cdFirmatario;
    }

    @Column(name = "DL_DN_FIRMATARIO")
    public String getDlDnFirmatario() {
        return this.dlDnFirmatario;
    }

    public void setDlDnFirmatario(String dlDnFirmatario) {
        this.dlDnFirmatario = dlDnFirmatario;
    }

    @Column(name = "DS_ALGO_FIRMA")
    public String getDsAlgoFirma() {
        return this.dsAlgoFirma;
    }

    public void setDsAlgoFirma(String dsAlgoFirma) {
        this.dsAlgoFirma = dsAlgoFirma;
    }

    @Column(name = "DS_FIRMA_BASE64")
    public String getDsFirmaBase64() {
        return this.dsFirmaBase64;
    }

    public void setDsFirmaBase64(String dsFirmaBase64) {
        this.dsFirmaBase64 = dsFirmaBase64;
    }

    @Column(name = "DS_MSG_ESITO_CONTR_CONFORME")
    public String getDsMsgEsitoContrConforme() {
        return this.dsMsgEsitoContrConforme;
    }

    public void setDsMsgEsitoContrConforme(String dsMsgEsitoContrConforme) {
        this.dsMsgEsitoContrConforme = dsMsgEsitoContrConforme;
    }

    @Column(name = "DS_MSG_ESITO_VERIF_FIRMA")
    public String getDsMsgEsitoVerifFirma() {
        return this.dsMsgEsitoVerifFirma;
    }

    public void setDsMsgEsitoVerifFirma(String dsMsgEsitoVerifFirma) {
        this.dsMsgEsitoVerifFirma = dsMsgEsitoVerifFirma;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_FIRMA")
    public Date getDtFirma() {
        return this.dtFirma;
    }

    public void setDtFirma(Date dtFirma) {
        this.dtFirma = dtFirma;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "NM_COGNOME_FIRMATARIO")
    public String getNmCognomeFirmatario() {
        return this.nmCognomeFirmatario;
    }

    public void setNmCognomeFirmatario(String nmCognomeFirmatario) {
        this.nmCognomeFirmatario = nmCognomeFirmatario;
    }

    @Column(name = "NM_FIRMATARIO")
    public String getNmFirmatario() {
        return this.nmFirmatario;
    }

    public void setNmFirmatario(String nmFirmatario) {
        this.nmFirmatario = nmFirmatario;
    }

    @Column(name = "PG_BUSTA")
    public BigDecimal getPgBusta() {
        return this.pgBusta;
    }

    public void setPgBusta(BigDecimal pgBusta) {
        this.pgBusta = pgBusta;
    }

    @Column(name = "PG_FIRMA")
    public BigDecimal getPgFirma() {
        return this.pgFirma;
    }

    public void setPgFirma(BigDecimal pgFirma) {
        this.pgFirma = pgFirma;
    }

    @Column(name = "TI_ESITO_CONTR_CONFORME")
    public String getTiEsitoContrConforme() {
        return this.tiEsitoContrConforme;
    }

    public void setTiEsitoContrConforme(String tiEsitoContrConforme) {
        this.tiEsitoContrConforme = tiEsitoContrConforme;
    }

    @Column(name = "TI_ESITO_VERIF_FIRMA")
    public String getTiEsitoVerifFirma() {
        return this.tiEsitoVerifFirma;
    }

    public void setTiEsitoVerifFirma(String tiEsitoVerifFirma) {
        this.tiEsitoVerifFirma = tiEsitoVerifFirma;
    }

    @Column(name = "TI_FIRMA")
    public String getTiFirma() {
        return this.tiFirma;
    }

    public void setTiFirma(String tiFirma) {
        this.tiFirma = tiFirma;
    }

    @Column(name = "TI_FORMATO_FIRMA")
    public String getTiFormatoFirma() {
        return this.tiFormatoFirma;
    }

    public void setTiFormatoFirma(String tiFormatoFirma) {
        this.tiFormatoFirma = tiFormatoFirma;
    }

    @Column(name = "TI_RIF_TEMP_USATO")
    public String getTiRifTempUsato() {
        return this.tiRifTempUsato;
    }

    public void setTiRifTempUsato(String tiRifTempUsato) {
        this.tiRifTempUsato = tiRifTempUsato;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TM_RIF_TEMP_USATO")
    public Date getTmRifTempUsato() {
        return this.tmRifTempUsato;
    }

    public void setTmRifTempUsato(Date tmRifTempUsato) {
        this.tmRifTempUsato = tmRifTempUsato;
    }

    @Lob()
    @Column(name = "BL_FIRMA_BASE64")
    public String getBlFirmaBase64() {
        return blFirmaBase64;
    }

    public void setBlFirmaBase64(String blFirmaBase64) {
        this.blFirmaBase64 = blFirmaBase64;
    }

    @Column(name = "DS_NOTE")
    public String getDsNote() {
        return this.dsNote;
    }

    public void setDsNote(String dsNote) {
        this.dsNote = dsNote;
    }

    // bi-directional many-to-one association to AroControfirmaFirma
    @OneToMany(mappedBy = "aroFirmaPadre", cascade = {
            CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.REFRESH })
    public List<AroControfirmaFirma> getAroControfirmaFirmaFiglios() {
        return this.aroControfirmaFirmaFiglios;
    }

    public void setAroControfirmaFirmaFiglios(
            List<AroControfirmaFirma> aroControfirmaFirmaFiglios) {
        this.aroControfirmaFirmaFiglios = aroControfirmaFirmaFiglios;
    }

    // bi-directional many-to-one association to AroControfirmaFirma
    @OneToMany(mappedBy = "aroFirmaFiglio")
    public List<AroControfirmaFirma> getAroControfirmaFirmaPadres() {
        return this.aroControfirmaFirmaPadres;
    }

    public void setAroControfirmaFirmaPadres(List<AroControfirmaFirma> aroControfirmaFirmaPadres) {
        this.aroControfirmaFirmaPadres = aroControfirmaFirmaPadres;
    }

    // bi-directional many-to-one association to AroContrFirmaComp
    @OneToMany(mappedBy = "aroFirmaComp", cascade = {
            CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH })
    public List<AroContrFirmaComp> getAroContrFirmaComps() {
        return this.aroContrFirmaComps;
    }

    public void setAroContrFirmaComps(List<AroContrFirmaComp> aroContrFirmaComps) {
        this.aroContrFirmaComps = aroContrFirmaComps;
    }

    // bi-directional many-to-one association to AroBustaCrittog
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_BUSTA_CRITTOG")
    public AroBustaCrittog getAroBustaCrittog() {
        return this.aroBustaCrittog;
    }

    public void setAroBustaCrittog(AroBustaCrittog aroBustaCrittog) {
        this.aroBustaCrittog = aroBustaCrittog;
    }

    // bi-directional many-to-one association to AroCompDoc
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID_COMP_DOC", nullable = false)
    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public void setAroCompDoc(AroCompDoc aroCompDoc) {
        this.aroCompDoc = aroCompDoc;
    }

    // bi-directional many-to-one association to AroMarcaComp
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID_MARCA_COMP")
    public AroMarcaComp getAroMarcaComp() {
        return this.aroMarcaComp;
    }

    public void setAroMarcaComp(AroMarcaComp aroMarcaComp) {
        this.aroMarcaComp = aroMarcaComp;
    }

    // uni-directional many-to-one association to FirCertifFirmatario
    @ManyToOne(cascade = {
            CascadeType.PERSIST, // CascadeType.MERGE, CascadeType.REFRESH,
            CascadeType.DETACH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_FIRMATARIO")
    public FirCertifFirmatario getFirCertifFirmatario() {
        return this.firCertifFirmatario;
    }

    public void setFirCertifFirmatario(FirCertifFirmatario firCertifFirmatario) {
        this.firCertifFirmatario = firCertifFirmatario;
    }

    // bi-directional many-to-one association to AroVerifFirmaDtVer
    @OneToMany(mappedBy = "aroFirmaComp", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH })
    public List<AroVerifFirmaDtVer> getAroVerifFirmaDtVers() {
        return this.aroVerifFirmaDtVers;
    }

    public void setAroVerifFirmaDtVers(List<AroVerifFirmaDtVer> aroVerifFirmaDtVers) {
        this.aroVerifFirmaDtVers = aroVerifFirmaDtVers;
    }
}
