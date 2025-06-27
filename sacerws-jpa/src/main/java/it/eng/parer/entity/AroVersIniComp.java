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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_VERS_INI_COMP database table.
 */
@Entity
@Table(name = "ARO_VERS_INI_COMP")
public class AroVersIniComp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idVersIniComp;

    private String dlUrnCompVers;

    private String dsIdCompVers;

    private String dsNomeCompVers;

    private AroCompDoc aroCompDoc;

    private AroVersIniDoc aroVersIniDoc;

    private List<AroVersIniDatiSpec> aroVersIniDatiSpecs = new ArrayList<>();

    public AroVersIniComp() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_VERS_INI_COMP")
    @GenericGenerator(name = "SARO_VERS_INI_COMP_ID_VERS_INI_COMP_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_VERS_INI_COMP"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_VERS_INI_COMP_ID_VERS_INI_COMP_GENERATOR")
    public Long getIdVersIniComp() {
	return this.idVersIniComp;
    }

    public void setIdVersIniComp(Long idVersIniComp) {
	this.idVersIniComp = idVersIniComp;
    }

    @Column(name = "DL_URN_COMP_VERS")
    public String getDlUrnCompVers() {
	return this.dlUrnCompVers;
    }

    public void setDlUrnCompVers(String dlUrnCompVers) {
	this.dlUrnCompVers = dlUrnCompVers;
    }

    @Column(name = "DS_ID_COMP_VERS")
    public String getDsIdCompVers() {
	return this.dsIdCompVers;
    }

    public void setDsIdCompVers(String dsIdCompVers) {
	this.dsIdCompVers = dsIdCompVers;
    }

    @Column(name = "DS_NOME_COMP_VERS")
    public String getDsNomeCompVers() {
	return this.dsNomeCompVers;
    }

    public void setDsNomeCompVers(String dsNomeCompVers) {
	this.dsNomeCompVers = dsNomeCompVers;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    public AroCompDoc getAroCompDoc() {
	return this.aroCompDoc;
    }

    public void setAroCompDoc(AroCompDoc aroCompDoc) {
	this.aroCompDoc = aroCompDoc;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VERS_INI_DOC")
    public AroVersIniDoc getAroVersIniDoc() {
	return this.aroVersIniDoc;
    }

    public void setAroVersIniDoc(AroVersIniDoc aroVersIniDoc) {
	this.aroVersIniDoc = aroVersIniDoc;
    }

    // bi-directional many-to-one association to AroVersIniComp
    @OneToMany(mappedBy = "aroVersIniComp", cascade = {
	    CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<AroVersIniDatiSpec> getAroVersIniDatiSpecs() {
	return this.aroVersIniDatiSpecs;
    }

    public void setAroVersIniDatiSpecs(List<AroVersIniDatiSpec> aroVersIniDatiSpecs) {
	this.aroVersIniDatiSpecs = aroVersIniDatiSpecs;
    }

}
