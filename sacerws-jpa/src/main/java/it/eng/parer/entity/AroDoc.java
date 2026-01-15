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
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_DOC database table.
 *
 */
@Entity
@Table(name = "ARO_DOC")
public class AroDoc implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idDoc;
    private String cdKeyDocVers;
    private String dlDoc;
    private String dsAutoreDoc;
    private String dsMsgEsitoVerifFirme;
    private Date dtAnnul;
    private Date dtCreazione;
    private String flDocFirmato;
    private String flDocFisc;
    private String flForzaAccettazione;
    private String flForzaConservazione;
    private BigDecimal idStrut;
    private String nmSistemaMigraz;
    private String ntAnnul;
    private String ntDoc;
    private BigDecimal pgDoc;
    private BigDecimal niOrdDoc;
    private BigDecimal niResetStato;
    private String tiAnnul;
    private String tiConservazione;
    private String tiCreazione;
    private String tiDoc;
    private String tiEsitoVerifFirme;
    private String tiStatoDoc;
    private String tiStatoDocElencoVers;
    private Date tsStatoElencoVers;
    private Date tsLastResetStato;
    private AroUnitaDoc aroUnitaDoc;
    private DecTipoDoc decTipoDoc;
    private Long idDecTipoDoc;
    private AroXmlDocObjectStorage aroXmlDocObjectStorage;

    private List<AroStrutDoc> aroStrutDocs = new ArrayList<>();
    private List<AroUsoXsdDatiSpec> aroUsoXsdDatiSpecs = new ArrayList<>();
    private List<AroWarnUnitaDoc> aroWarnUnitaDocs = new ArrayList<>();
    private List<VrsSessioneVers> vrsSessioneVers = new ArrayList<>();
    private List<ElvDocAggDaElabElenco> elvDocAggDaElabElencos = new ArrayList<>();
    private List<AroVersIniDoc> aroVersIniDocs = new ArrayList<>();
    private List<AroUpdDocUnitaDoc> aroUpdDocUnitaDocs = new ArrayList<>();

    public AroDoc() {
        // hibernate
    }

    @Id
    @Column(name = "ID_DOC")
    @GenericGenerator(name = "ARO_DOC_ID_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ARO_DOC_ID_DOC_GENERATOR")
    public Long getIdDoc() {
        return this.idDoc;
    }

    public void setIdDoc(Long idDoc) {
        this.idDoc = idDoc;
    }

    @Column(name = "CD_KEY_DOC_VERS")
    public String getCdKeyDocVers() {
        return this.cdKeyDocVers;
    }

    public void setCdKeyDocVers(String cdKeyDocVers) {
        this.cdKeyDocVers = cdKeyDocVers;
    }

    @Column(name = "DL_DOC")
    public String getDlDoc() {
        return this.dlDoc;
    }

    public void setDlDoc(String dlDoc) {
        this.dlDoc = dlDoc;
    }

    @Column(name = "DS_AUTORE_DOC")
    public String getDsAutoreDoc() {
        return this.dsAutoreDoc;
    }

    public void setDsAutoreDoc(String dsAutoreDoc) {
        this.dsAutoreDoc = dsAutoreDoc;
    }

    @Column(name = "DS_MSG_ESITO_VERIF_FIRME")
    public String getDsMsgEsitoVerifFirme() {
        return this.dsMsgEsitoVerifFirme;
    }

    public void setDsMsgEsitoVerifFirme(String dsMsgEsitoVerifFirme) {
        this.dsMsgEsitoVerifFirme = dsMsgEsitoVerifFirme;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_ANNUL")
    public Date getDtAnnul() {
        return this.dtAnnul;
    }

    public void setDtAnnul(Date dtAnnul) {
        this.dtAnnul = dtAnnul;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_CREAZIONE")
    public Date getDtCreazione() {
        return this.dtCreazione;
    }

    public void setDtCreazione(Date dtCreazione) {
        this.dtCreazione = dtCreazione;
    }

    @Column(name = "FL_DOC_FIRMATO", columnDefinition = "char(1)")
    public String getFlDocFirmato() {
        return this.flDocFirmato;
    }

    public void setFlDocFirmato(String flDocFirmato) {
        this.flDocFirmato = flDocFirmato;
    }

    @Column(name = "FL_DOC_FISC", columnDefinition = "char(1)")
    public String getFlDocFisc() {
        return this.flDocFisc;
    }

    public void setFlDocFisc(String flDocFisc) {
        this.flDocFisc = flDocFisc;
    }

    @Column(name = "FL_FORZA_ACCETTAZIONE", columnDefinition = "char(1)")
    public String getFlForzaAccettazione() {
        return this.flForzaAccettazione;
    }

    public void setFlForzaAccettazione(String flForzaAccettazione) {
        this.flForzaAccettazione = flForzaAccettazione;
    }

    @Column(name = "FL_FORZA_CONSERVAZIONE", columnDefinition = "char(1)")
    public String getFlForzaConservazione() {
        return this.flForzaConservazione;
    }

    public void setFlForzaConservazione(String flForzaConservazione) {
        this.flForzaConservazione = flForzaConservazione;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "NM_SISTEMA_MIGRAZ")
    public String getNmSistemaMigraz() {
        return this.nmSistemaMigraz;
    }

    public void setNmSistemaMigraz(String nmSistemaMigraz) {
        this.nmSistemaMigraz = nmSistemaMigraz;
    }

    @Column(name = "NT_ANNUL")
    public String getNtAnnul() {
        return this.ntAnnul;
    }

    public void setNtAnnul(String ntAnnul) {
        this.ntAnnul = ntAnnul;
    }

    @Column(name = "NT_DOC")
    public String getNtDoc() {
        return this.ntDoc;
    }

    public void setNtDoc(String ntDoc) {
        this.ntDoc = ntDoc;
    }

    @Column(name = "PG_DOC")
    public BigDecimal getPgDoc() {
        return this.pgDoc;
    }

    public void setPgDoc(BigDecimal pgDoc) {
        this.pgDoc = pgDoc;
    }

    @Column(name = "NI_ORD_DOC")
    public BigDecimal getNiOrdDoc() {
        return this.niOrdDoc;
    }

    public void setNiOrdDoc(BigDecimal niOrdDoc) {
        this.niOrdDoc = niOrdDoc;
    }

    @Column(name = "NI_RESET_STATO")
    public BigDecimal getNiResetStato() {
        return this.niResetStato;
    }

    public void setNiResetStato(BigDecimal niResetStato) {
        this.niResetStato = niResetStato;
    }

    @Column(name = "TI_ANNUL")
    public String getTiAnnul() {
        return this.tiAnnul;
    }

    public void setTiAnnul(String tiAnnul) {
        this.tiAnnul = tiAnnul;
    }

    @Column(name = "TI_CONSERVAZIONE")
    public String getTiConservazione() {
        return this.tiConservazione;
    }

    public void setTiConservazione(String tiConservazione) {
        this.tiConservazione = tiConservazione;
    }

    @Column(name = "TI_CREAZIONE")
    public String getTiCreazione() {
        return this.tiCreazione;
    }

    public void setTiCreazione(String tiCreazione) {
        this.tiCreazione = tiCreazione;
    }

    @Column(name = "TI_DOC")
    public String getTiDoc() {
        return this.tiDoc;
    }

    public void setTiDoc(String tiDoc) {
        this.tiDoc = tiDoc;
    }

    @Column(name = "TI_ESITO_VERIF_FIRME")
    public String getTiEsitoVerifFirme() {
        return this.tiEsitoVerifFirme;
    }

    public void setTiEsitoVerifFirme(String tiEsitoVerifFirme) {
        this.tiEsitoVerifFirme = tiEsitoVerifFirme;
    }

    @Column(name = "TI_STATO_DOC")
    public String getTiStatoDoc() {
        return this.tiStatoDoc;
    }

    public void setTiStatoDoc(String tiStatoDoc) {
        this.tiStatoDoc = tiStatoDoc;
    }

    @Column(name = "TI_STATO_DOC_ELENCO_VERS")
    public String getTiStatoDocElencoVers() {
        return this.tiStatoDocElencoVers;
    }

    public void setTiStatoDocElencoVers(String tiStatoDocElencoVers) {
        this.tiStatoDocElencoVers = tiStatoDocElencoVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_STATO_ELENCO_VERS")
    public Date getTsStatoElencoVers() {
        return this.tsStatoElencoVers;
    }

    public void setTsStatoElencoVers(Date tsStatoElencoVers) {
        this.tsStatoElencoVers = tsStatoElencoVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_LAST_RESET_STATO")
    public Date getTsLastResetStato() {
        return this.tsLastResetStato;
    }

    public void setTsLastResetStato(Date tsLastResetStato) {
        this.tsLastResetStato = tsLastResetStato;
    }

    @Column(name = "ID_TIPO_DOC", insertable = false, updatable = false)
    public Long getIdDecTipoDoc() {
        return idDecTipoDoc;
    }

    public void setIdDecTipoDoc(Long idDecTipoDoc) {
        this.idDecTipoDoc = idDecTipoDoc;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

    public void setAroUnitaDoc(AroUnitaDoc aroUnitaDoc) {
        this.aroUnitaDoc = aroUnitaDoc;
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

    // bi-directional many-to-one association to AroStrutDoc
    @OneToMany(mappedBy = "aroDoc")
    public List<AroStrutDoc> getAroStrutDocs() {
        return this.aroStrutDocs;
    }

    public void setAroStrutDocs(List<AroStrutDoc> aroStrutDocs) {
        this.aroStrutDocs = aroStrutDocs;
    }

    // bi-directional many-to-one association to AroUsoXsdDatiSpec
    @OneToMany(mappedBy = "aroDoc")
    public List<AroUsoXsdDatiSpec> getAroUsoXsdDatiSpecs() {
        return this.aroUsoXsdDatiSpecs;
    }

    public void setAroUsoXsdDatiSpecs(List<AroUsoXsdDatiSpec> aroUsoXsdDatiSpecs) {
        this.aroUsoXsdDatiSpecs = aroUsoXsdDatiSpecs;
    }

    // bi-directional many-to-one association to AroWarnUnitaDoc
    @OneToMany(mappedBy = "aroDoc")
    public List<AroWarnUnitaDoc> getAroWarnUnitaDocs() {
        return this.aroWarnUnitaDocs;
    }

    public void setAroWarnUnitaDocs(List<AroWarnUnitaDoc> aroWarnUnitaDocs) {
        this.aroWarnUnitaDocs = aroWarnUnitaDocs;
    }

    // bi-directional many-to-one association to VrsSessioneVers
    @OneToMany(mappedBy = "aroDoc")
    public List<VrsSessioneVers> getVrsSessioneVers() {
        return this.vrsSessioneVers;
    }

    public void setVrsSessioneVers(List<VrsSessioneVers> vrsSessioneVers) {
        this.vrsSessioneVers = vrsSessioneVers;
    }

    // bi-directional many-to-one association to ElvDocAggDaElabElenco
    @OneToMany(mappedBy = "aroDoc")
    public List<ElvDocAggDaElabElenco> getElvDocAggDaElabElencos() {
        return this.elvDocAggDaElabElencos;
    }

    public void setElvDocAggDaElabElencos(List<ElvDocAggDaElabElenco> elvDocAggDaElabElencos) {
        this.elvDocAggDaElabElencos = elvDocAggDaElabElencos;
    }

    // bi-directional many-to-one association to AroVersIniDoc
    @OneToMany(mappedBy = "aroDoc")
    public List<AroVersIniDoc> getAroVersIniDocs() {
        return this.aroVersIniDocs;
    }

    public void setAroVersIniDocs(List<AroVersIniDoc> aroVersIniDocs) {
        this.aroVersIniDocs = aroVersIniDocs;
    }

    // bi-directional many-to-one association to AroVersIniDoc
    @OneToMany(mappedBy = "aroDoc")
    public List<AroUpdDocUnitaDoc> getAroUpdDocUnitaDocs() {
        return this.aroUpdDocUnitaDocs;
    }

    public void setAroUpdDocUnitaDocs(List<AroUpdDocUnitaDoc> aroUpdDocUnitaDocs) {
        this.aroUpdDocUnitaDocs = aroUpdDocUnitaDocs;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    public AroXmlDocObjectStorage getAroXmlDocObjectStorage() {
        return aroXmlDocObjectStorage;
    }

    public void setAroXmlDocObjectStorage(AroXmlDocObjectStorage aroXmlDocObjectStorage) {
        this.aroXmlDocObjectStorage = aroXmlDocObjectStorage;
    }

}
