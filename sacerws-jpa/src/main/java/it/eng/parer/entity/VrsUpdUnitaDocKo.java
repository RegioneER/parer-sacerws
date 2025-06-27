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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.VrsUpdUnitaDocKo.TiStatoUdpUdKo;

/**
 * The persistent class for the VRS_UPD_UNITA_DOC_KO database table.
 */
@Entity
@Table(name = "VRS_UPD_UNITA_DOC_KO")
public class VrsUpdUnitaDocKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUpdUnitaDocKo;

    private BigDecimal aaKeyUnitaDoc;

    private String cdKeyUnitaDoc;

    private String cdRegistroKeyUnitaDoc;

    private String dsErrPrinc;

    private DecControlloWs decControlloWsPrinc;

    private DecErrSacer decErrSacerPrinc;

    private DecRegistroUnitaDoc decRegistroUnitaDocLast;

    private VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKoFirst;

    private VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKoLast;

    private OrgStrut orgStrut;

    private DecTipoDoc decTipoDocPrincLast;

    private DecTipoUnitaDoc decTipoUnitaDocLast;

    private TiStatoUdpUdKo tiStatoUdpUdKo;

    private Date tsIniFirstSes;

    private Date tsIniLastSes;

    private List<VrsSesUpdUnitaDocKo> vrsSesUpdUnitaDocKos = new ArrayList<>();

    public VrsUpdUnitaDocKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UPD_UNITA_DOC_KO")
    @GenericGenerator(name = "SVRS_UPD_UNITA_DOC_KO_ID_UPD_UNITA_DOC_KO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_UPD_UNITA_DOC_KO"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_UPD_UNITA_DOC_KO_ID_UPD_UNITA_DOC_KO_GENERATOR")
    public Long getIdUpdUnitaDocKo() {
	return this.idUpdUnitaDocKo;
    }

    public void setIdUpdUnitaDocKo(Long idUpdUnitaDocKo) {
	this.idUpdUnitaDocKo = idUpdUnitaDocKo;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
	return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
	this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Column(name = "CD_KEY_UNITA_DOC")
    public String getCdKeyUnitaDoc() {
	return this.cdKeyUnitaDoc;
    }

    public void setCdKeyUnitaDoc(String cdKeyUnitaDoc) {
	this.cdKeyUnitaDoc = cdKeyUnitaDoc;
    }

    @Column(name = "CD_REGISTRO_KEY_UNITA_DOC")
    public String getCdRegistroKeyUnitaDoc() {
	return this.cdRegistroKeyUnitaDoc;
    }

    public void setCdRegistroKeyUnitaDoc(String cdRegistroKeyUnitaDoc) {
	this.cdRegistroKeyUnitaDoc = cdRegistroKeyUnitaDoc;
    }

    @Column(name = "DS_ERR_PRINC")
    public String getDsErrPrinc() {
	return this.dsErrPrinc;
    }

    public void setDsErrPrinc(String dsErrPrinc) {
	this.dsErrPrinc = dsErrPrinc;
    }

    // bi-directional many-to-one association to DecErrSacer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONTROLLO_WS_PRINC")
    public DecControlloWs getDecControlloWsPrinc() {
	return this.decControlloWsPrinc;
    }

    public void setDecControlloWsPrinc(DecControlloWs decControlloWsPrinc) {
	this.decControlloWsPrinc = decControlloWsPrinc;
    }

    // bi-directional many-to-one association to DecErrSacer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ERR_SACER_PRINC")
    public DecErrSacer getDecErrSacerPrinc() {
	return this.decErrSacerPrinc;
    }

    public void setDecErrSacerPrinc(DecErrSacer decErrSacerPrinc) {
	this.decErrSacerPrinc = decErrSacerPrinc;
    }

    // bi-directional many-to-one association to DecRegistroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGISTRO_UNITA_DOC_LAST")
    public DecRegistroUnitaDoc getDecRegistroUnitaDocLast() {
	return this.decRegistroUnitaDocLast;
    }

    public void setDecRegistroUnitaDocLast(DecRegistroUnitaDoc decRegistroUnitaDocLast) {
	this.decRegistroUnitaDocLast = decRegistroUnitaDocLast;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SES_UPD_UD_KO_FIRST")
    public VrsSesUpdUnitaDocKo getVrsSesUpdUnitaDocKoFirst() {
	return this.vrsSesUpdUnitaDocKoFirst;
    }

    public void setVrsSesUpdUnitaDocKoFirst(VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKoFirst) {
	this.vrsSesUpdUnitaDocKoFirst = vrsSesUpdUnitaDocKoFirst;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SES_UPD_UD_KO_LAST")
    public VrsSesUpdUnitaDocKo getVrsSesUpdUnitaDocKoLast() {
	return this.vrsSesUpdUnitaDocKoLast;
    }

    public void setVrsSesUpdUnitaDocKoLast(VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKoLast) {
	this.vrsSesUpdUnitaDocKoLast = vrsSesUpdUnitaDocKoLast;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
	return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
	this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to DecTipoDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_DOC_PRINC_LAST")
    public DecTipoDoc getDecTipoDocPrincLast() {
	return this.decTipoDocPrincLast;
    }

    public void setDecTipoDocPrincLast(DecTipoDoc decTipoDocPrincLast) {
	this.decTipoDocPrincLast = decTipoDocPrincLast;
    }

    // bi-directional many-to-one association to DecTipoUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_UNITA_DOC_LAST")
    public DecTipoUnitaDoc getDecTipoUnitaDocLast() {
	return this.decTipoUnitaDocLast;
    }

    public void setDecTipoUnitaDocLast(DecTipoUnitaDoc decTipoUnitaDocLast) {
	this.decTipoUnitaDocLast = decTipoUnitaDocLast;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_STATO_UDP_UD_KO")
    public TiStatoUdpUdKo getTiStatoUdpUdKo() {
	return this.tiStatoUdpUdKo;
    }

    public void setTiStatoUdpUdKo(TiStatoUdpUdKo tiStatoUdpUdKo) {
	this.tiStatoUdpUdKo = tiStatoUdpUdKo;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_INI_FIRST_SES")
    public Date getTsIniFirstSes() {
	return this.tsIniFirstSes;
    }

    public void setTsIniFirstSes(Date tsIniFirstSes) {
	this.tsIniFirstSes = tsIniFirstSes;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_INI_LAST_SES")
    public Date getTsIniLastSes() {
	return this.tsIniLastSes;
    }

    public void setTsIniLastSes(Date tsIniLastSes) {
	this.tsIniLastSes = tsIniLastSes;
    }

    // bi-directional many-to-one association to VrsSesFascicoloKo
    @OneToMany(mappedBy = "vrsUpdUnitaDocKo")
    public List<VrsSesUpdUnitaDocKo> getVrsSesUpdUnitaDocKos() {
	return this.vrsSesUpdUnitaDocKos;
    }

    public void setVrsSesUpdUnitaDocKos(List<VrsSesUpdUnitaDocKo> vrsSesUpdUnitaDocKos) {
	this.vrsSesUpdUnitaDocKos = vrsSesUpdUnitaDocKos;
    }

}
