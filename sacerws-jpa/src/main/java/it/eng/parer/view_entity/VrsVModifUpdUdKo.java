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

package it.eng.parer.view_entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the VRS_V_MODIF_UPD_UD_KO database table.
 * 
 */
@Entity
@Table(name = "VRS_V_MODIF_UPD_UD_KO")
public class VrsVModifUpdUdKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUpdUnitaDocKo;
    private BigDecimal idSesUpdUdKoLast;
    private BigDecimal idSesUpdUdKoFirst;
    private BigDecimal idTipoUnitaDocLast;
    private BigDecimal idErrSacerPrinc;
    private BigDecimal idControlloWsPrinc;
    private BigDecimal idTipoDocPrincLast;
    private BigDecimal idRegistroUnitaDocLast;
    private Date tsIniFirstSes;
    private Date tsIniLastSes;
    private String dsErrPrinc;
    private String tiStatoUpdUdKo;

    public VrsVModifUpdUdKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UPD_UNITA_DOC_KO")
    public Long getIdUpdUnitaDocKo() {
        return this.idUpdUnitaDocKo;
    }

    public void setIdUpdUnitaDocKo(Long idUpdUnitaDocKo) {
        this.idUpdUnitaDocKo = idUpdUnitaDocKo;
    }

    @Column(name = "ID_SES_UPD_UD_KO_LAST")
    public BigDecimal getIdSesUpdUdKoLast() {
        return this.idSesUpdUdKoLast;
    }

    public void setIdSesUpdUdKoLast(BigDecimal idSesUpdUdKoLast) {
        this.idSesUpdUdKoLast = idSesUpdUdKoLast;
    }

    @Column(name = "ID_SES_UPD_UD_KO_FIRST")
    public BigDecimal getIdSesUpdUdKoFirst() {
        return this.idSesUpdUdKoFirst;
    }

    public void setIdSesUpdUdKoFirst(BigDecimal idSesUpdUdKoFirst) {
        this.idSesUpdUdKoFirst = idSesUpdUdKoFirst;
    }

    @Column(name = "ID_TIPO_UNITA_DOC_LAST")
    public BigDecimal getIdTipoUnitaDocLast() {
        return this.idTipoUnitaDocLast;
    }

    public void setIdTipoUnitaDocLast(BigDecimal idTipoUnitaDocLast) {
        this.idTipoUnitaDocLast = idTipoUnitaDocLast;
    }

    @Column(name = "ID_ERR_SACER_PRINC")
    public BigDecimal getIdErrSacerPrinc() {
        return this.idErrSacerPrinc;
    }

    public void setIdErrSacerPrinc(BigDecimal idErrSacerPrinc) {
        this.idErrSacerPrinc = idErrSacerPrinc;
    }

    @Column(name = "ID_CONTROLLO_WS_PRINC")
    public BigDecimal getIdControlloWsPrinc() {
        return this.idControlloWsPrinc;
    }

    public void setIdControlloWsPrinc(BigDecimal idControlloWsPrinc) {
        this.idControlloWsPrinc = idControlloWsPrinc;
    }

    @Column(name = "ID_TIPO_DOC_PRINC_LAST")
    public BigDecimal getIdTipoDocPrincLast() {
        return this.idTipoDocPrincLast;
    }

    public void setIdTipoDocPrincLast(BigDecimal idTipoDocPrincLast) {
        this.idTipoDocPrincLast = idTipoDocPrincLast;
    }

    @Column(name = "ID_REGISTRO_UNITA_DOC_LAST")
    public BigDecimal getIdRegistroUnitaDocLast() {
        return this.idRegistroUnitaDocLast;
    }

    public void setIdRegistroUnitaDocLast(BigDecimal idRegistroUnitaDocLast) {
        this.idRegistroUnitaDocLast = idRegistroUnitaDocLast;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_INI_FIRST_SES")
    public Date getTsIniFirstSes() {
        return tsIniFirstSes;
    }

    public void setTsIniFirstSes(Date tsIniFirstSes) {
        this.tsIniFirstSes = tsIniFirstSes;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_INI_LAST_SES")
    public Date getTsIniLastSes() {
        return tsIniLastSes;
    }

    public void setTsIniLastSes(Date tsIniLastSes) {
        this.tsIniLastSes = tsIniLastSes;
    }

    @Column(name = "DS_ERR_PRINC")
    public String getDsErrPrinc() {
        return dsErrPrinc;
    }

    public void setDsErrPrinc(String dsErrPrinc) {
        this.dsErrPrinc = dsErrPrinc;
    }

    @Column(name = "TI_STATO_UPD_UD_KO")
    public String getTiStatoUpdUdKo() {
        return tiStatoUpdUdKo;
    }

    public void setTiStatoUpdUdKo(String tiStatoUpdUdKo) {
        this.tiStatoUpdUdKo = tiStatoUpdUdKo;
    }

}
