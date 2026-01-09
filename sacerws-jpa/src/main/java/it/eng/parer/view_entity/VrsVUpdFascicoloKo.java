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
 * The persistent class for the VRS_V_UPD_FASCICOLO_KO database table.
 *
 */
@Entity
@Table(name = "VRS_V_UPD_FASCICOLO_KO")
public class VrsVUpdFascicoloKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFascicoloKo;
    private Date tsIniFirstSes;
    private Date tsIniLastSes;
    private BigDecimal idErrSacerPrinc;
    private String dsErrPrinc;
    private BigDecimal idTipoFascicoloLast;
    private String tiStatoFascicoloKo;
    private BigDecimal idSesFascicoloKoFirst;
    private BigDecimal idSesFascicoloKoLast;

    public VrsVUpdFascicoloKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_FASCICOLO_KO")
    public Long getIdFascicoloKo() {
        return idFascicoloKo;
    }

    public void setIdFascicoloKo(Long idFascicoloKo) {
        this.idFascicoloKo = idFascicoloKo;
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

    @Column(name = "ID_ERR_SACER_PRINC")
    public BigDecimal getIdErrSacerPrinc() {
        return idErrSacerPrinc;
    }

    public void setIdErrSacerPrinc(BigDecimal idErrSacerPrinc) {
        this.idErrSacerPrinc = idErrSacerPrinc;
    }

    @Column(name = "DS_ERR_PRINC")
    public String getDsErrPrinc() {
        return dsErrPrinc;
    }

    public void setDsErrPrinc(String dsErrPrinc) {
        this.dsErrPrinc = dsErrPrinc;
    }

    @Column(name = "ID_TIPO_FASCICOLO_LAST")
    public BigDecimal getIdTipoFascicoloLast() {
        return idTipoFascicoloLast;
    }

    public void setIdTipoFascicoloLast(BigDecimal idTipoFascicoloLast) {
        this.idTipoFascicoloLast = idTipoFascicoloLast;
    }

    @Column(name = "TI_STATO_FASCICOLO_KO")
    public String getTiStatoFascicoloKo() {
        return tiStatoFascicoloKo;
    }

    public void setTiStatoFascicoloKo(String tiStatoFascicoloKo) {
        this.tiStatoFascicoloKo = tiStatoFascicoloKo;
    }

    @Column(name = "ID_SES_FASCICOLO_KO_FIRST")
    public BigDecimal getIdSesFascicoloKoFirst() {
        return idSesFascicoloKoFirst;
    }

    public void setIdSesFascicoloKoFirst(BigDecimal idSesFascicoloKoFirst) {
        this.idSesFascicoloKoFirst = idSesFascicoloKoFirst;
    }

    @Column(name = "ID_SES_FASCICOLO_KO_LAST")
    public BigDecimal getIdSesFascicoloKoLast() {
        return idSesFascicoloKoLast;
    }

    public void setIdSesFascicoloKoLast(BigDecimal idSesFascicoloKoLast) {
        this.idSesFascicoloKoLast = idSesFascicoloKoLast;
    }
}
