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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * The persistent class for the ORG_V_VAL_SUB_PARTITION database table.
 *
 */
@Entity
@Table(name = "ORG_V_VAL_SUB_PARTITION")
public class OrgVValSubPartition implements Serializable {
    private static final long serialVersionUID = 1L;
    private Date cdValSubPartition;
    private BigDecimal idPartition;
    private BigDecimal idSubPartition;
    private Long idValSubPartition;

    public OrgVValSubPartition() {/* Hibernate */
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CD_VAL_SUB_PARTITION")
    public Date getCdValSubPartition() {
        return this.cdValSubPartition;
    }

    public void setCdValSubPartition(Date cdValSubPartition) {
        this.cdValSubPartition = cdValSubPartition;
    }

    @Column(name = "ID_PARTITION")
    public BigDecimal getIdPartition() {
        return this.idPartition;
    }

    public void setIdPartition(BigDecimal idPartition) {
        this.idPartition = idPartition;
    }

    @Column(name = "ID_SUB_PARTITION")
    public BigDecimal getIdSubPartition() {
        return this.idSubPartition;
    }

    public void setIdSubPartition(BigDecimal idSubPartition) {
        this.idSubPartition = idSubPartition;
    }

    @Id
    @Column(name = "ID_VAL_SUB_PARTITION")
    public Long getIdValSubPartition() {
        return this.idValSubPartition;
    }

    public void setIdValSubPartition(Long idValSubPartition) {
        this.idValSubPartition = idValSubPartition;
    }

}
