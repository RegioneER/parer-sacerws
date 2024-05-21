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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The persistent class for the ORG_V_CHK_PARTITION_FASC_ERR database table.
 *
 */
@Entity
@Table(name = "ORG_V_CHK_PARTITION_FASC_ERR")
public class OrgVChkPartitionFascErr implements Serializable {

    private static final long serialVersionUID = 1L;
    private String cdPartitionSesfascerr;

    private String flPartSesfascerrOk;

    public OrgVChkPartitionFascErr() {/* Hibernate */
    }

    @Column(name = "CD_PARTITION_SESFASCERR")
    public String getCdPartitionSesfascerr() {
        return cdPartitionSesfascerr;
    }

    public void setCdPartitionSesfascerr(String cdPartitionSesfascerr) {
        this.cdPartitionSesfascerr = cdPartitionSesfascerr;
    }

    @Id
    @Column(name = "FL_PART_SESFASCERR_OK", columnDefinition = "char(1)")
    public String getFlPartSesfascerrOk() {
        return flPartSesfascerrOk;
    }

    public void setFlPartSesfascerrOk(String flPartSesfascerrOk) {
        this.flPartSesfascerrOk = flPartSesfascerrOk;
    }

}
