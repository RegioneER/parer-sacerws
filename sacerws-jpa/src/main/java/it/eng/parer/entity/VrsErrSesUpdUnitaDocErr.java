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

import it.eng.parer.entity.constraint.VrsErrSesUpdUnitaDocErr.TiErrVrsErrSesUpdUnitaDocErr;

/**
 * The persistent class for the VRS_ERR_SES_UPD_UNITA_DOC_ERR database table.
 */
@Entity
@Table(name = "VRS_ERR_SES_UPD_UNITA_DOC_ERR")
public class VrsErrSesUpdUnitaDocErr implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idErrSesUpdUnitaDocErr;

    private String dsErr;

    private String flErrPrinc;

    private DecControlloWs decControlloWs;

    private DecErrSacer decErrSacer;

    private VrsSesUpdUnitaDocErr vrsSesUpdUnitaDocErr;

    private BigDecimal pgErr;

    private TiErrVrsErrSesUpdUnitaDocErr tiErr;

    public VrsErrSesUpdUnitaDocErr() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_ERR_SES_UPD_UNITA_DOC_ERR")
    @GenericGenerator(name = "SVRS_ERR_SES_UPD_UNITA_DOC_ERR_ID_ERR_SES_UPD_UNITA_DOC_ERR_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_ERR_SES_UPD_UNITA_DOC_ERR"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_ERR_SES_UPD_UNITA_DOC_ERR_ID_ERR_SES_UPD_UNITA_DOC_ERR_GENERATOR")
    public Long getIdErrSesUpdUnitaDocErr() {
        return this.idErrSesUpdUnitaDocErr;
    }

    public void setIdErrSesUpdUnitaDocErr(Long idErrSesUpdUnitaDocErr) {
        this.idErrSesUpdUnitaDocErr = idErrSesUpdUnitaDocErr;
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

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ERR_SACER")
    public DecErrSacer getDecErrSacer() {
        return this.decErrSacer;
    }

    public void setDecErrSacer(DecErrSacer decErrSacer) {
        this.decErrSacer = decErrSacer;
    }

    // bi-directional many-to-one association to DecErrSacer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SES_UPD_UNITA_DOC_ERR")
    public VrsSesUpdUnitaDocErr getVrsSesUpdUnitaDocErr() {
        return this.vrsSesUpdUnitaDocErr;
    }

    public void setVrsSesUpdUnitaDocErr(VrsSesUpdUnitaDocErr vrsSesUpdUnitaDocErr) {
        this.vrsSesUpdUnitaDocErr = vrsSesUpdUnitaDocErr;
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
    public TiErrVrsErrSesUpdUnitaDocErr getTiErr() {
        return this.tiErr;
    }

    public void setTiErr(TiErrVrsErrSesUpdUnitaDocErr tiErr) {
        this.tiErr = tiErr;
    }

}
