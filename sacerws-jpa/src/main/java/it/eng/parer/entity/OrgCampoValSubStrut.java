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
import javax.xml.bind.annotation.XmlID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ORG_CAMPO_VAL_SUB_STRUT database table.
 */
@Entity
@Table(name = "ORG_CAMPO_VAL_SUB_STRUT")
public class OrgCampoValSubStrut implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCampoValSubStrut;

    private String nmCampo;

    private String tiCampo;

    private DecAttribDatiSpec decAttribDatiSpec;

    private OrgRegolaValSubStrut orgRegolaValSubStrut;

    private OrgSubStrut orgSubStrut;

    public OrgCampoValSubStrut() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CAMPO_VAL_SUB_STRUT")
    @XmlID
    @GenericGenerator(name = "SORG_CAMPO_VAL_SUB_STRUT_ID_CAMPO_VAL_SUB_STRUT_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SORG_CAMPO_VAL_SUB_STRUT"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SORG_CAMPO_VAL_SUB_STRUT_ID_CAMPO_VAL_SUB_STRUT_GENERATOR")
    public Long getIdCampoValSubStrut() {
	return this.idCampoValSubStrut;
    }

    public void setIdCampoValSubStrut(Long idCampoValSubStrut) {
	this.idCampoValSubStrut = idCampoValSubStrut;
    }

    @Column(name = "NM_CAMPO")
    public String getNmCampo() {
	return this.nmCampo;
    }

    public void setNmCampo(String nmCampo) {
	this.nmCampo = nmCampo;
    }

    @Column(name = "TI_CAMPO")
    public String getTiCampo() {
	return this.tiCampo;
    }

    public void setTiCampo(String tiCampo) {
	this.tiCampo = tiCampo;
    }

    // bi-directional many-to-one association to DecAttribDatiSpec
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ATTRIB_DATI_SPEC")
    public DecAttribDatiSpec getDecAttribDatiSpec() {
	return this.decAttribDatiSpec;
    }

    public void setDecAttribDatiSpec(DecAttribDatiSpec decAttribDatiSpec) {
	this.decAttribDatiSpec = decAttribDatiSpec;
    }

    // bi-directional many-to-one association to OrgRegolaValSubStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGOLA_VAL_SUB_STRUT")
    public OrgRegolaValSubStrut getOrgRegolaValSubStrut() {
	return this.orgRegolaValSubStrut;
    }

    public void setOrgRegolaValSubStrut(OrgRegolaValSubStrut orgRegolaValSubStrut) {
	this.orgRegolaValSubStrut = orgRegolaValSubStrut;
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
