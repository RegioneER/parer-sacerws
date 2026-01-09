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
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.VrsErrUpdUnitaDocKo.TiErrVrsErrUpdUnitaDocKo;

/**
 * The persistent class for the VRS_ERR_SES_UPD_UNITA_DOC_KO database table.
 */
@Entity
@Table(name = "VRS_ERR_SES_UPD_UNITA_DOC_KO")
public class VrsErrSesUpdUnitaDocKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idErrSesUpdUnitaDocKo;

    private String dsErr;

    private String flErrPrinc;

    private DecControlloWs decControlloWs;

    private DecErrSacer decErrSacer;

    private VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKo;

    private BigDecimal pgErr;

    private TiErrVrsErrUpdUnitaDocKo tiErr;

    private OrgStrut orgStrut;

    private Integer aaKeyUnitaDoc;

    public VrsErrSesUpdUnitaDocKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_ERR_SES_UPD_UNITA_DOC_KO")
    @GenericGenerator(name = "SVRS_ERR_SES_UPD_UNITA_DOC_KO_ID_ERR_SES_UPD_UNITA_DOC_KO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_ERR_SES_UPD_UNITA_DOC_KO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_ERR_SES_UPD_UNITA_DOC_KO_ID_ERR_SES_UPD_UNITA_DOC_KO_GENERATOR")
    public Long getIdErrSesUpdUnitaDocKo() {
        return this.idErrSesUpdUnitaDocKo;
    }

    public void setIdErrSesUpdUnitaDocKo(Long idErrSesUpdUnitaDocKo) {
        this.idErrSesUpdUnitaDocKo = idErrSesUpdUnitaDocKo;
    }

    @Column(name = "DS_ERR")
    public String getDsErr() {
        return this.dsErr;
    }

    public void setDsErr(String dsErr) {
        this.dsErr = dsErr;
    }

    @Column(name = "FL_ERR_PRINC", columnDefinition = "char(1)")
    public String getFlErrPrinc() {
        return this.flErrPrinc;
    }

    public void setFlErrPrinc(String flErrPrinc) {
        this.flErrPrinc = flErrPrinc;
    }

    // bi-directional many-to-one association to DecErrSacer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONTROLLO_WS")
    public DecControlloWs getDecControlloWs() {
        return this.decControlloWs;
    }

    public void setDecControlloWs(DecControlloWs decControlloWs) {
        this.decControlloWs = decControlloWs;
    }

    // bi-directional many-to-one association to DecErrSacer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ERR_SACER")
    public DecErrSacer getDecErrSacer() {
        return this.decErrSacer;
    }

    public void setDecErrSacer(DecErrSacer decErrSacer) {
        this.decErrSacer = decErrSacer;
    }

    // bi-directional many-to-one association to DecTipoUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SES_UPD_UNITA_DOC_KO")
    public VrsSesUpdUnitaDocKo getVrsSesUpdUnitaDocKo() {
        return this.vrsSesUpdUnitaDocKo;
    }

    public void setVrsSesUpdUnitaDocKo(VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKo) {
        this.vrsSesUpdUnitaDocKo = vrsSesUpdUnitaDocKo;
    }

    @Column(name = "PG_ERR")
    public BigDecimal getPgErr() {
        return this.pgErr;
    }

    public void setPgErr(BigDecimal pgErr) {
        this.pgErr = pgErr;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_ERR")
    public TiErrVrsErrUpdUnitaDocKo getTiErr() {
        return this.tiErr;
    }

    public void setTiErr(TiErrVrsErrUpdUnitaDocKo tiErr) {
        this.tiErr = tiErr;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public Integer getAaKeyUnitaDoc() {
        return aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(Integer aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

}
