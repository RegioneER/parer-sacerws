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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_VER_INDICE_AIP_UD database table.
 */
@Entity
@Table(name = "ARO_VER_INDICE_AIP_UD")
public class AroVerIndiceAipUd implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idVerIndiceAip;

    private String cdEncodingHashAip;

    private String cdEncodingHashIndiceAip;

    private String cdVerIndiceAip;

    private String cdVerXsdIndiceAip;

    private String dsAlgoHashAip;

    private String dsAlgoHashIndiceAip;

    private String dsCausale;

    private String dsHashIndiceAip;

    private String dsHashAip;

    private String dsUrn;

    private Date dtCreazione;

    private BigDecimal pgVerIndiceAip;

    private BigDecimal idEnteConserv;

    private AroIndiceAipUd aroIndiceAipUd;

    public AroVerIndiceAipUd() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_VER_INDICE_AIP")
    @GenericGenerator(name = "SARO_VER_INDICE_AIP_UD_ID_VER_INDICE_AIP_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_VER_INDICE_AIP_UD"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_VER_INDICE_AIP_UD_ID_VER_INDICE_AIP_GENERATOR")
    public Long getIdVerIndiceAip() {
        return this.idVerIndiceAip;
    }

    public void setIdVerIndiceAip(Long idVerIndiceAip) {
        this.idVerIndiceAip = idVerIndiceAip;
    }

    @Column(name = "CD_ENCODING_HASH_AIP")
    public String getCdEncodingHashAip() {
        return this.cdEncodingHashAip;
    }

    public void setCdEncodingHashAip(String cdEncodingHashAip) {
        this.cdEncodingHashAip = cdEncodingHashAip;
    }

    @Column(name = "CD_ENCODING_HASH_INDICE_AIP")
    public String getCdEncodingHashIndiceAip() {
        return this.cdEncodingHashIndiceAip;
    }

    public void setCdEncodingHashIndiceAip(String cdEncodingHashIndiceAip) {
        this.cdEncodingHashIndiceAip = cdEncodingHashIndiceAip;
    }

    @Column(name = "CD_VER_INDICE_AIP")
    public String getCdVerIndiceAip() {
        return this.cdVerIndiceAip;
    }

    public void setCdVerIndiceAip(String cdVerIndiceAip) {
        this.cdVerIndiceAip = cdVerIndiceAip;
    }

    @Column(name = "CD_VER_XSD_INDICE_AIP")
    public String getCdVerXsdIndiceAip() {
        return this.cdVerXsdIndiceAip;
    }

    public void setCdVerXsdIndiceAip(String cdVerXsdIndiceAip) {
        this.cdVerXsdIndiceAip = cdVerXsdIndiceAip;
    }

    @Column(name = "DS_ALGO_HASH_AIP")
    public String getDsAlgoHashAip() {
        return this.dsAlgoHashAip;
    }

    public void setDsAlgoHashAip(String dsAlgoHashAip) {
        this.dsAlgoHashAip = dsAlgoHashAip;
    }

    @Column(name = "DS_ALGO_HASH_INDICE_AIP")
    public String getDsAlgoHashIndiceAip() {
        return this.dsAlgoHashIndiceAip;
    }

    public void setDsAlgoHashIndiceAip(String dsAlgoHashIndiceAip) {
        this.dsAlgoHashIndiceAip = dsAlgoHashIndiceAip;
    }

    @Column(name = "DS_CAUSALE")
    public String getDsCausale() {
        return this.dsCausale;
    }

    public void setDsCausale(String dsCausale) {
        this.dsCausale = dsCausale;
    }

    @Column(name = "DS_HASH_INDICE_AIP")
    public String getDsHashIndiceAip() {
        return this.dsHashIndiceAip;
    }

    public void setDsHashIndiceAip(String dsHashIndiceAip) {
        this.dsHashIndiceAip = dsHashIndiceAip;
    }

    @Column(name = "DS_HASH_AIP")
    public String getDsHashAip() {
        return this.dsHashAip;
    }

    public void setDsHashAip(String dsHashAip) {
        this.dsHashAip = dsHashAip;
    }

    @Column(name = "DS_URN")
    public String getDsUrn() {
        return this.dsUrn;
    }

    public void setDsUrn(String dsUrn) {
        this.dsUrn = dsUrn;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_CREAZIONE")
    public Date getDtCreazione() {
        return this.dtCreazione;
    }

    public void setDtCreazione(Date dtCreazione) {
        this.dtCreazione = dtCreazione;
    }

    @Column(name = "PG_VER_INDICE_AIP")
    public BigDecimal getPgVerIndiceAip() {
        return this.pgVerIndiceAip;
    }

    public void setPgVerIndiceAip(BigDecimal pgVerIndiceAip) {
        this.pgVerIndiceAip = pgVerIndiceAip;
    }

    @Column(name = "ID_ENTE_CONSERV")
    public BigDecimal getIdEnteConserv() {
        return this.idEnteConserv;
    }

    public void setIdEnteConserv(BigDecimal idEnteConserv) {
        this.idEnteConserv = idEnteConserv;
    }

    // bi-directional many-to-one association to AroIndiceAipUd
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_INDICE_AIP")
    public AroIndiceAipUd getAroIndiceAipUd() {
        return this.aroIndiceAipUd;
    }

    public void setAroIndiceAipUd(AroIndiceAipUd aroIndiceAipUd) {
        this.aroIndiceAipUd = aroIndiceAipUd;
    }

}
