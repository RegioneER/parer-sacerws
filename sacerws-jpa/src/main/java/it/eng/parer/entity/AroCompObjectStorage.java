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
 * The persistent class for the ARO_COMP_OBJECT_STORAGE database table.
 */
@Entity
@Table(name = "ARO_COMP_OBJECT_STORAGE")
public class AroCompObjectStorage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCompObjectStorage;

    private String cdKeyFile;

    private String nmBucket;

    private String nmTenant;

    private AroCompDoc aroCompDoc;

    private DecBackend decBackend;

    public AroCompObjectStorage() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_COMP_OBJECT_STORAGE")
    @GenericGenerator(name = "SARO_COMP_OBJECT_STORAGE_ID_COMP_OBJECT_STORAGE_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_COMP_OBJECT_STORAGE"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_COMP_OBJECT_STORAGE_ID_COMP_OBJECT_STORAGE_GENERATOR")
    public Long getIdCompObjectStorage() {
	return this.idCompObjectStorage;
    }

    public void setIdCompObjectStorage(Long idCompObjectStorage) {
	this.idCompObjectStorage = idCompObjectStorage;
    }

    @Column(name = "CD_KEY_FILE")
    public String getCdKeyFile() {
	return this.cdKeyFile;
    }

    public void setCdKeyFile(String cdKeyFile) {
	this.cdKeyFile = cdKeyFile;
    }

    @Column(name = "NM_BUCKET")
    public String getNmBucket() {
	return this.nmBucket;
    }

    public void setNmBucket(String nmBucket) {
	this.nmBucket = nmBucket;
    }

    @Column(name = "NM_TENANT")
    public String getNmTenant() {
	return this.nmTenant;
    }

    public void setNmTenant(String nmTenant) {
	this.nmTenant = nmTenant;
    }

    // bi-directional many-to-one association to AroCompDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    public AroCompDoc getAroCompDoc() {
	return this.aroCompDoc;
    }

    public void setAroCompDoc(AroCompDoc aroCompDoc) {
	this.aroCompDoc = aroCompDoc;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DEC_BACKEND")
    public DecBackend getDecBackend() {
	return decBackend;
    }

    public void setDecBackend(DecBackend decBackend) {
	this.decBackend = decBackend;
    }

}
