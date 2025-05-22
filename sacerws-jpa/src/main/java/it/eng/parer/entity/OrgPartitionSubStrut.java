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
 * The persistent class for the ORG_PARTITION_SUB_STRUT database table.
 */
@Entity
@Table(name = "ORG_PARTITION_SUB_STRUT")
public class OrgPartitionSubStrut implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idPartitionSubStrut;

    private String tiPartition;

    private OrgPartition orgPartition;

    private OrgSubStrut orgSubStrut;

    public OrgPartitionSubStrut() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_PARTITION_SUB_STRUT")
    @GenericGenerator(name = "SORG_PARTITION_SUB_STRUT_ID_PARTITION_SUB_STRUT_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SORG_PARTITION_SUB_STRUT"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORG_PARTITION_SUB_STRUT_ID_PARTITION_SUB_STRUT_GENERATOR")
    public Long getIdPartitionSubStrut() {
	return this.idPartitionSubStrut;
    }

    public void setIdPartitionSubStrut(Long idPartitionSubStrut) {
	this.idPartitionSubStrut = idPartitionSubStrut;
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

    // bi-directional many-to-one association to OrgSubStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SUB_STRUT")
    public OrgSubStrut getOrgSubStrut() {
	return this.orgSubStrut;
    }

    public void setOrgSubStrut(OrgSubStrut orgSubStrut) {
	this.orgSubStrut = orgSubStrut;
    }

}
