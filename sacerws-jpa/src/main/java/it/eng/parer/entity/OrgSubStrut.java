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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ORG_SUB_STRUT database table.
 */
@Entity
@Table(name = "ORG_SUB_STRUT")
public class OrgSubStrut implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idSubStrut;

    private String dsSubStrut;

    private String nmSubStrut;

    private List<OrgCampoValSubStrut> orgCampoValSubStruts = new ArrayList<>();

    private List<OrgPartitionSubStrut> orgPartitionSubStruts = new ArrayList<>();

    private OrgStrut orgStrut;

    private List<AroUnitaDoc> aroUnitaDocs = new ArrayList<>();

    public OrgSubStrut() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_SUB_STRUT")
    @GenericGenerator(name = "SORG_SUB_STRUT_ID_SUB_STRUT_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SORG_SUB_STRUT"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORG_SUB_STRUT_ID_SUB_STRUT_GENERATOR")
    public Long getIdSubStrut() {
	return this.idSubStrut;
    }

    public void setIdSubStrut(Long idSubStrut) {
	this.idSubStrut = idSubStrut;
    }

    @Column(name = "DS_SUB_STRUT")
    public String getDsSubStrut() {
	return this.dsSubStrut;
    }

    public void setDsSubStrut(String dsSubStrut) {
	this.dsSubStrut = dsSubStrut;
    }

    @Column(name = "NM_SUB_STRUT")
    public String getNmSubStrut() {
	return this.nmSubStrut;
    }

    public void setNmSubStrut(String nmSubStrut) {
	this.nmSubStrut = nmSubStrut;
    }

    // bi-directional many-to-one association to OrgCampoValSubStrut
    @OneToMany(mappedBy = "orgSubStrut")
    @XmlIDREF
    public List<OrgCampoValSubStrut> getOrgCampoValSubStruts() {
	return this.orgCampoValSubStruts;
    }

    public void setOrgCampoValSubStruts(List<OrgCampoValSubStrut> orgCampoValSubStruts) {
	this.orgCampoValSubStruts = orgCampoValSubStruts;
    }

    // bi-directional many-to-one association to OrgPartitionSubStrut
    @OneToMany(mappedBy = "orgSubStrut", cascade = CascadeType.REMOVE)
    @XmlTransient
    public List<OrgPartitionSubStrut> getOrgPartitionSubStruts() {
	return this.orgPartitionSubStruts;
    }

    public void setOrgPartitionSubStruts(List<OrgPartitionSubStrut> orgPartitionSubStruts) {
	this.orgPartitionSubStruts = orgPartitionSubStruts;
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

    // bi-directional many-to-one association to AroUnitaDoc
    @OneToMany(mappedBy = "orgSubStrut")
    @XmlTransient
    public List<AroUnitaDoc> getAroUnitaDocs() {
	return this.aroUnitaDocs;
    }

    public void setAroUnitaDocs(List<AroUnitaDoc> aroUnitaDocs) {
	this.aroUnitaDocs = aroUnitaDocs;
    }
}
