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
 * The persistent class for the VRS_SES_FASCICOLO_KO database table.
 */
@Entity
@Table(name = "VRS_SES_FASCICOLO_KO")
public class VrsSesFascicoloKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idSesFascicoloKo;

    private BigDecimal aaFascicolo;

    private String cdVersioneWs;

    private String dsErrPrinc;

    private String tiStatoSes;

    private Date tsFineSes;

    private Date tsIniSes;

    private String cdIndIpClient;

    private String cdIndServer;

    private List<VrsErrSesFascicoloKo> vrsErrSesFascicoloKos = new ArrayList<>();

    private DecErrSacer decErrSacer;

    private DecTipoFascicolo decTipoFascicolo;

    private FasFascicolo fasFascicolo;

    private IamUser iamUser;

    private VrsFascicoloKo vrsFascicoloKo;

    private OrgStrut orgStrut;

    private List<VrsXmlSesFascicoloKo> vrsXmlSesFascicoloKos = new ArrayList<>();

    public VrsSesFascicoloKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_SES_FASCICOLO_KO")
    @GenericGenerator(name = "SVRS_SES_FASCICOLO_KO_ID_SES_FASCICOLO_KO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_SES_FASCICOLO_KO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_SES_FASCICOLO_KO_ID_SES_FASCICOLO_KO_GENERATOR")
    public Long getIdSesFascicoloKo() {
        return this.idSesFascicoloKo;
    }

    public void setIdSesFascicoloKo(Long idSesFascicoloKo) {
        this.idSesFascicoloKo = idSesFascicoloKo;
    }

    @Column(name = "AA_FASCICOLO")
    public BigDecimal getAaFascicolo() {
        return this.aaFascicolo;
    }

    public void setAaFascicolo(BigDecimal aaFascicolo) {
        this.aaFascicolo = aaFascicolo;
    }

    @Column(name = "CD_VERSIONE_WS")
    public String getCdVersioneWs() {
        return this.cdVersioneWs;
    }

    public void setCdVersioneWs(String cdVersioneWs) {
        this.cdVersioneWs = cdVersioneWs;
    }

    @Column(name = "DS_ERR_PRINC")
    public String getDsErrPrinc() {
        return this.dsErrPrinc;
    }

    public void setDsErrPrinc(String dsErrPrinc) {
        this.dsErrPrinc = dsErrPrinc;
    }

    @Column(name = "TI_STATO_SES")
    public String getTiStatoSes() {
        return this.tiStatoSes;
    }

    public void setTiStatoSes(String tiStatoSes) {
        this.tiStatoSes = tiStatoSes;
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

    @Column(name = "CD_IND_IP_CLIENT")
    public String getCdIndIpClient() {
        return cdIndIpClient;
    }

    public void setCdIndIpClient(String cdIndIpClient) {
        this.cdIndIpClient = cdIndIpClient;
    }

    @Column(name = "CD_IND_SERVER")
    public String getCdIndServer() {
        return cdIndServer;
    }

    public void setCdIndServer(String cdIndServer) {
        this.cdIndServer = cdIndServer;
    }

    // bi-directional many-to-one association to VrsErrSesFascicoloKo
    @OneToMany(mappedBy = "vrsSesFascicoloKo")
    public List<VrsErrSesFascicoloKo> getVrsErrSesFascicoloKos() {
        return this.vrsErrSesFascicoloKos;
    }

    public void setVrsErrSesFascicoloKos(List<VrsErrSesFascicoloKo> vrsErrSesFascicoloKos) {
        this.vrsErrSesFascicoloKos = vrsErrSesFascicoloKos;
    }

    // bi-directional many-to-one association to DecErrSacer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ERR_SACER_PRINC")
    public DecErrSacer getDecErrSacer() {
        return this.decErrSacer;
    }

    public void setDecErrSacer(DecErrSacer decErrSacer) {
        this.decErrSacer = decErrSacer;
    }

    // bi-directional many-to-one association to DecTipoFascicolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_FASCICOLO")
    public DecTipoFascicolo getDecTipoFascicolo() {
        return this.decTipoFascicolo;
    }

    public void setDecTipoFascicolo(DecTipoFascicolo decTipoFascicolo) {
        this.decTipoFascicolo = decTipoFascicolo;
    }

    // bi-directional many-to-one association to FasFascicolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO")
    public FasFascicolo getFasFascicolo() {
        return this.fasFascicolo;
    }

    public void setFasFascicolo(FasFascicolo fasFascicolo) {
        this.fasFascicolo = fasFascicolo;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER_IAM")
    public IamUser getIamUser() {
        return this.iamUser;
    }

    public void setIamUser(IamUser iamUser) {
        this.iamUser = iamUser;
    }

    // bi-directional many-to-one association to VrsFascicoloKo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO_KO")
    public VrsFascicoloKo getVrsFascicoloKo() {
        return this.vrsFascicoloKo;
    }

    public void setVrsFascicoloKo(VrsFascicoloKo vrsFascicoloKo) {
        this.vrsFascicoloKo = vrsFascicoloKo;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to VrsXmlSesFascicoloKo
    @OneToMany(mappedBy = "vrsSesFascicoloKo")
    public List<VrsXmlSesFascicoloKo> getVrsXmlSesFascicoloKos() {
        return this.vrsXmlSesFascicoloKos;
    }

    public void setVrsXmlSesFascicoloKos(List<VrsXmlSesFascicoloKo> vrsXmlSesFascicoloKos) {
        this.vrsXmlSesFascicoloKos = vrsXmlSesFascicoloKos;
    }
}
