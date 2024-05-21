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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.AroUpdUnitaDoc.AroUpdUDTiStatoUpdElencoVers;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

/**
 * The persistent class for the ARO_UPD_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_UPD_UNITA_DOC")
public class AroUpdUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUpdUnitaDoc;

    private String cdFascicPrinc;

    private String cdIndIpClient;

    private String cdIndServer;

    private String cdSottofascicPrinc;

    private String dlOggettoUnitaDoc;

    private String dsClassifPrinc;

    private String dsOggettoFascicPrinc;

    private String dsOggettoSottofascicPrinc;

    private Date dtAnnul;

    private Date dtRegUnitaDoc;

    private String flForzaUpd;

    private AroUnitaDoc aroUnitaDoc;

    private IamUser iamUser;

    private String ntUpd;

    private BigDecimal pgUpdUnitaDoc;

    private AroUpdUDTiStatoUpdElencoVers tiStatoUpdElencoVers;

    private Date tsFineSes;

    private Date tsIniSes;

    private Date tsStatoElencoVers;

    private Date tsLastResetStato;

    private List<AroUpdDatiSpecUnitaDoc> aroUpdDatiSpecUnitaDocs = new ArrayList<>();

    private List<AroUpdDocUnitaDoc> aroUpdDocUnitaDocs = new ArrayList<>();

    private List<AroUpdLinkUnitaDoc> aroUpdLinkUnitaDocs = new ArrayList<>();

    private List<AroWarnUpdUnitaDoc> aroWarnUpdUnitaDocs = new ArrayList<>();

    private List<AroXmlUpdUnitaDoc> aroXmlUpdUnitaDocs = new ArrayList<>();

    private List<AroUpdArchivSec> aroUpdArchivSecs = new ArrayList<>();

    private List<ElvUpdUdDaElabElenco> elvUpdUdDaElabElencos = new ArrayList<>();

    private OrgStrut orgStrut;

    private BigDecimal aaKeyUnitaDoc;

    private DecRegistroUnitaDoc decRegistroUnitaDoc;

    private DecTipoUnitaDoc decTipoUnitaDoc;

    private DecTipoDoc decTipoDocPrinc;

    private String flSesUpdKoRisolti;

    private String flUpdProfiloArchiv;

    private String flUpdFascicoloPrinc;

    private String flUpdFascicoliSec;

    private String flUpdProfiloUnitaDoc;

    private String flUpdLinkUnitaDoc;

    private String flUpdDatiSpec;

    private String flUpdDatiSpecMigraz;

    private String flUpdProfiloNormativo;

    private String ntAnnul;

    private String tipoUpdUnitaDoc;

    private BigDecimal niResetStato;

    private AroXmlUpdUdObjectStorage aroXmlUpdUdObjectStorage;

    public AroUpdUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UPD_UNITA_DOC")
    @GenericGenerator(name = "SARO_UPD_UNITA_DOC_ID_UPD_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_UPD_UNITA_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_UPD_UNITA_DOC_ID_UPD_UNITA_DOC_GENERATOR")
    public Long getIdUpdUnitaDoc() {
        return this.idUpdUnitaDoc;
    }

    public void setIdUpdUnitaDoc(Long idUpdUnitaDoc) {
        this.idUpdUnitaDoc = idUpdUnitaDoc;
    }

    @Column(name = "CD_FASCIC_PRINC")
    public String getCdFascicPrinc() {
        return this.cdFascicPrinc;
    }

    public void setCdFascicPrinc(String cdFascicPrinc) {
        this.cdFascicPrinc = cdFascicPrinc;
    }

    @Column(name = "CD_IND_IP_CLIENT")
    public String getCdIndIpClient() {
        return this.cdIndIpClient;
    }

    public void setCdIndIpClient(String cdIndIpClient) {
        this.cdIndIpClient = cdIndIpClient;
    }

    @Column(name = "CD_IND_SERVER")
    public String getCdIndServer() {
        return this.cdIndServer;
    }

    public void setCdIndServer(String cdIndServer) {
        this.cdIndServer = cdIndServer;
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
    @Column(name = "DT_ANNUL")
    public Date getDtAnnul() {
        return this.dtAnnul;
    }

    public void setDtAnnul(Date dtAnnul) {
        this.dtAnnul = dtAnnul;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DT_REG_UNITA_DOC")
    public Date getDtRegUnitaDoc() {
        return this.dtRegUnitaDoc;
    }

    public void setDtRegUnitaDoc(Date dtRegUnitaDoc) {
        this.dtRegUnitaDoc = dtRegUnitaDoc;
    }

    @Column(name = "FL_FORZA_UPD", columnDefinition = "char(1)")
    public String getFlForzaUpd() {
        return this.flForzaUpd;
    }

    public void setFlForzaUpd(String flForzaUpd) {
        this.flForzaUpd = flForzaUpd;
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

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER_IAM_VERS")
    public IamUser getIamUser() {
        return this.iamUser;
    }

    public void setIamUser(IamUser iamUser) {
        this.iamUser = iamUser;
    }

    @Column(name = "NT_UPD")
    public String getNtUpd() {
        return this.ntUpd;
    }

    public void setNtUpd(String ntUpd) {
        this.ntUpd = ntUpd;
    }

    @Column(name = "PG_UPD_UNITA_DOC")
    public BigDecimal getPgUpdUnitaDoc() {
        return this.pgUpdUnitaDoc;
    }

    public void setPgUpdUnitaDoc(BigDecimal pgUpdUnitaDoc) {
        this.pgUpdUnitaDoc = pgUpdUnitaDoc;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_STATO_UPD_ELENCO_VERS")
    public AroUpdUDTiStatoUpdElencoVers getTiStatoUpdElencoVers() {
        return this.tiStatoUpdElencoVers;
    }

    public void setTiStatoUpdElencoVers(AroUpdUDTiStatoUpdElencoVers tiStatoUpdElencoVers) {
        this.tiStatoUpdElencoVers = tiStatoUpdElencoVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_FINE_SES")
    public Date getTsFineSes() {
        return this.tsFineSes;
    }

    public void setTsFineSes(Date tsFineSes) {
        this.tsFineSes = tsFineSes;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_INI_SES")
    public Date getTsIniSes() {
        return this.tsIniSes;
    }

    public void setTsIniSes(Date tsIniSes) {
        this.tsIniSes = tsIniSes;
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

    // bi-directional many-to-one association to AroUpdDatiSpecUnitaDoc
    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    @XmlTransient
    public List<AroUpdDatiSpecUnitaDoc> getAroUpdDatiSpecUnitaDocs() {
        return this.aroUpdDatiSpecUnitaDocs;
    }

    public void setAroUpdDatiSpecUnitaDocs(List<AroUpdDatiSpecUnitaDoc> aroUpdDatiSpecUnitaDocs) {
        this.aroUpdDatiSpecUnitaDocs = aroUpdDatiSpecUnitaDocs;
    }

    // bi-directional many-to-one association to AroUpdDocUnitaDoc
    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    @XmlTransient
    public List<AroUpdDocUnitaDoc> getAroUpdDocUnitaDocs() {
        return this.aroUpdDocUnitaDocs;
    }

    public void setAroUpdDocUnitaDocs(List<AroUpdDocUnitaDoc> aroUpdDocUnitaDocs) {
        this.aroUpdDocUnitaDocs = aroUpdDocUnitaDocs;
    }

    // bi-directional many-to-one association to AroUpdLinkUnitaDoc
    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    public List<AroUpdLinkUnitaDoc> getAroUpdLinkUnitaDocs() {
        return this.aroUpdLinkUnitaDocs;
    }

    public void setAroUpdLinkUnitaDocs(List<AroUpdLinkUnitaDoc> aroUpdLinkUnitaDocs) {
        this.aroUpdLinkUnitaDocs = aroUpdLinkUnitaDocs;
    }

    // bi-directional many-to-one association to AroWarnUpdUnitaDoc
    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    public List<AroWarnUpdUnitaDoc> getAroWarnUpdUnitaDocs() {
        return this.aroWarnUpdUnitaDocs;
    }

    public void setAroWarnUpdUnitaDocs(List<AroWarnUpdUnitaDoc> aroWarnUpdUnitaDocs) {
        this.aroWarnUpdUnitaDocs = aroWarnUpdUnitaDocs;
    }

    // bi-directional many-to-one association to AroXmlUpdUnitaDoc
    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    public List<AroXmlUpdUnitaDoc> getAroXmlUpdUnitaDocs() {
        return this.aroXmlUpdUnitaDocs;
    }

    public void setAroXmlUpdUnitaDocs(List<AroXmlUpdUnitaDoc> aroXmlUpdUnitaDocs) {
        this.aroXmlUpdUnitaDocs = aroXmlUpdUnitaDocs;
    }

    // bi-directional many-to-one association to AroUpdArchivSec
    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    public List<AroUpdArchivSec> getAroUpdArchivSecs() {
        return this.aroUpdArchivSecs;
    }

    public void setAroUpdArchivSecs(List<AroUpdArchivSec> aroUpdArchivSecs) {
        this.aroUpdArchivSecs = aroUpdArchivSecs;
    }

    // bi-directional many-to-one association to AroVersIniLinkUnitaDoc
    @OneToMany(mappedBy = "aroUpdUnitaDoc", cascade = CascadeType.PERSIST)
    public List<ElvUpdUdDaElabElenco> getElvUpdUdDaElabElencos() {
        return this.elvUpdUdDaElabElencos;
    }

    public void setElvUpdUdDaElabElencos(List<ElvUpdUdDaElabElenco> elvUpdUdDaElabElencos) {
        this.elvUpdUdDaElabElencos = elvUpdUdDaElabElencos;
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

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
        return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    // bi-directional many-to-one association to DecRegistroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGISTRO_UNITA_DOC")
    public DecRegistroUnitaDoc getDecRegistroUnitaDoc() {
        return this.decRegistroUnitaDoc;
    }

    public void setDecRegistroUnitaDoc(DecRegistroUnitaDoc decRegistroUnitaDoc) {
        this.decRegistroUnitaDoc = decRegistroUnitaDoc;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_UNITA_DOC")
    public DecTipoUnitaDoc getDecTipoUnitaDoc() {
        return this.decTipoUnitaDoc;
    }

    public void setDecTipoUnitaDoc(DecTipoUnitaDoc decTipoUnitaDoc) {
        this.decTipoUnitaDoc = decTipoUnitaDoc;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_DOC_PRINC")
    public DecTipoDoc getDecTipoDocPrinc() {
        return this.decTipoDocPrinc;
    }

    public void setDecTipoDocPrinc(DecTipoDoc decTipoDocPrinc) {
        this.decTipoDocPrinc = decTipoDocPrinc;
    }

    @Column(name = "FL_SES_UPD_KO_RISOLTI", columnDefinition = "char(1)")
    public String getFlSesUpdKoRisolti() {
        return this.flSesUpdKoRisolti;
    }

    public void setFlSesUpdKoRisolti(String flSesUpdKoRisolti) {
        this.flSesUpdKoRisolti = flSesUpdKoRisolti;
    }

    @Column(name = "FL_UPD_PROFILO_ARCHIV", columnDefinition = "char(1)")
    public String getFlUpdProfiloArchiv() {
        return this.flUpdProfiloArchiv;
    }

    public void setFlUpdProfiloArchiv(String flUpdProfiloArchiv) {
        this.flUpdProfiloArchiv = flUpdProfiloArchiv;
    }

    @Column(name = "FL_UPD_FASCICOLO_PRINC", columnDefinition = "char(1)")
    public String getFlUpdFascicoloPrinc() {
        return this.flUpdFascicoloPrinc;
    }

    public void setFlUpdFascicoloPrinc(String flUpdFascicoloPrinc) {
        this.flUpdFascicoloPrinc = flUpdFascicoloPrinc;
    }

    @Column(name = "FL_UPD_FASCICOLI_SEC", columnDefinition = "char(1)")
    public String getFlUpdFascicoliSec() {
        return this.flUpdFascicoliSec;
    }

    public void setFlUpdFascicoliSec(String flUpdFascicoliSec) {
        this.flUpdFascicoliSec = flUpdFascicoliSec;
    }

    @Column(name = "FL_UPD_PROFILO_UNITA_DOC", columnDefinition = "char(1)")
    public String getFlUpdProfiloUnitaDoc() {
        return this.flUpdProfiloUnitaDoc;
    }

    public void setFlUpdProfiloUnitaDoc(String flUpdProfiloUnitaDoc) {
        this.flUpdProfiloUnitaDoc = flUpdProfiloUnitaDoc;
    }

    @Column(name = "FL_UPD_LINK_UNITA_DOC", columnDefinition = "char(1)")
    public String getFlUpdLinkUnitaDoc() {
        return this.flUpdLinkUnitaDoc;
    }

    public void setFlUpdLinkUnitaDoc(String flUpdLinkUnitaDoc) {
        this.flUpdLinkUnitaDoc = flUpdLinkUnitaDoc;
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

    @Column(name = "NT_ANNUL")
    public String getNtAnnul() {
        return this.ntAnnul;
    }

    public void setNtAnnul(String ntAnnul) {
        this.ntAnnul = ntAnnul;
    }

    @Column(name = "TIPO_UPD_UNITA_DOC")
    public String getTipoUpdUnitaDoc() {
        return this.tipoUpdUnitaDoc;
    }

    public void setTipoUpdUnitaDoc(String tipoUpdUnitaDoc) {
        this.tipoUpdUnitaDoc = tipoUpdUnitaDoc;
    }

    @Column(name = "NI_RESET_STATO")
    public BigDecimal getNiResetStato() {
        return this.niResetStato;
    }

    public void setNiResetStato(BigDecimal niResetStato) {
        this.niResetStato = niResetStato;
    }

    @Column(name = "FL_UPD_PROFILO_NORMATIVO", columnDefinition = "char(1)")
    public String getFlUpdProfiloNormativo() {
        return flUpdProfiloNormativo;
    }

    public void setFlUpdProfiloNormativo(String flUpdProfiloNormativo) {
        this.flUpdProfiloNormativo = flUpdProfiloNormativo;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    public AroXmlUpdUdObjectStorage getAroXmlUpdUdObjectStorage() {
        return aroXmlUpdUdObjectStorage;
    }

    public void setAroXmlUpdUdObjectStorage(AroXmlUpdUdObjectStorage aroXmlUpdUdObjectStorage) {
        this.aroXmlUpdUdObjectStorage = aroXmlUpdUdObjectStorage;
    }

}
