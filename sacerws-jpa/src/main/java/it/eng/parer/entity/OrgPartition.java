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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ORG_PARTITION database table.
 */
@Entity
@Table(name = "ORG_PARTITION")
public class OrgPartition implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idPartition;

    private String cdPartition;

    private String tiPartition;

    private List<OrgPartitionStrut> orgPartitionStruts = new ArrayList<>();

    private List<OrgValSubPartition> orgValSubPartitions = new ArrayList<>();

    private List<OrgPartitionSubStrut> orgPartitionSubStruts = new ArrayList<>();

    public OrgPartition() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_PARTITION")
    @GenericGenerator(name = "SORG_PARTITION_ID_PARTITION_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SORG_PARTITION"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORG_PARTITION_ID_PARTITION_GENERATOR")
    public Long getIdPartition() {
        return this.idPartition;
    }

    public void setIdPartition(Long idPartition) {
        this.idPartition = idPartition;
    }

    @Column(name = "CD_PARTITION")
    public String getCdPartition() {
        return this.cdPartition;
    }

    public void setCdPartition(String cdPartition) {
        this.cdPartition = cdPartition;
    }

    @Column(name = "TI_PARTITION")
    public String getTiPartition() {
        return this.tiPartition;
    }

    public void setTiPartition(String tiPartition) {
        this.tiPartition = tiPartition;
    }

    // bi-directional many-to-one association to OrgPartitionStrut
    @OneToMany(mappedBy = "orgPartition")
    public List<OrgPartitionStrut> getOrgPartitionStruts() {
        return this.orgPartitionStruts;
    }

    public void setOrgPartitionStruts(List<OrgPartitionStrut> orgPartitionStruts) {
        this.orgPartitionStruts = orgPartitionStruts;
    }

    // bi-directional many-to-one association to OrgValSubPartition
    @OneToMany(mappedBy = "orgPartition")
    public List<OrgValSubPartition> getOrgValSubPartitions() {
        return this.orgValSubPartitions;
    }

    public void setOrgValSubPartitions(List<OrgValSubPartition> orgValSubPartitions) {
        this.orgValSubPartitions = orgValSubPartitions;
    }

    // bi-directional many-to-one association to OrgPartitionSubStrut
    @OneToMany(mappedBy = "orgPartition")
    public List<OrgPartitionSubStrut> getOrgPartitionSubStruts() {
        return this.orgPartitionSubStruts;
    }

    public void setOrgPartitionSubStruts(List<OrgPartitionSubStrut> orgPartitionSubStruts) {
        this.orgPartitionSubStruts = orgPartitionSubStruts;
    }
}
