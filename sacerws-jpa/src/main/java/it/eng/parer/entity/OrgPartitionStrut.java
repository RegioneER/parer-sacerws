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

import javax.persistence.Column;
import javax.persistence.Entity;
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

/**
 * The persistent class for the ORG_PARTITION_STRUT database table.
 */
@Entity
@Table(name = "ORG_PARTITION_STRUT")
public class OrgPartitionStrut implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idPartitionStrut;

    private String tiPartition;

    private OrgPartition orgPartition;

    private OrgStrut orgStrut;

    public OrgPartitionStrut() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_PARTITION_STRUT")
    @GenericGenerator(name = "SORG_PARTITION_STRUT_ID_PARTITION_STRUT_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SORG_PARTITION_STRUT"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORG_PARTITION_STRUT_ID_PARTITION_STRUT_GENERATOR")
    public Long getIdPartitionStrut() {
        return this.idPartitionStrut;
    }

    public void setIdPartitionStrut(Long idPartitionStrut) {
        this.idPartitionStrut = idPartitionStrut;
    }

    @Column(name = "TI_PARTITION")
    public String getTiPartition() {
        return this.tiPartition;
    }

    public void setTiPartition(String tiPartition) {
        this.tiPartition = tiPartition;
    }

    // bi-directional many-to-one association to OrgPartition
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PARTITION")
    public OrgPartition getOrgPartition() {
        return this.orgPartition;
    }

    public void setOrgPartition(OrgPartition orgPartition) {
        this.orgPartition = orgPartition;
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

}
