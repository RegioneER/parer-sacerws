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
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.VrsXmlSesUpdUnitaDocKo.TiXmlVrsXmlSesUpdUnitaDocKo;

/**
 * The persistent class for the VRS_XML_SES_UPD_UNITA_DOC_KO database table.
 */
@Entity
@Table(name = "VRS_XML_SES_UPD_UNITA_DOC_KO")
public class VrsXmlSesUpdUnitaDocKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXmlSesUpdUnitaDocKo;

    private String blXml;

    private String cdVersioneXml;

    private LocalDate dtRegXml;

    private VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKo;

    private OrgStrut orgStrut;

    private TiXmlVrsXmlSesUpdUnitaDocKo tiXml;

    private Integer aaRegXml;

    public VrsXmlSesUpdUnitaDocKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_XML_SES_UPD_UNITA_DOC_KO")
    @GenericGenerator(name = "SVRS_XML_SES_UPD_UNITA_DOC_KO_ID_XML_SES_UPD_UNITA_DOC_KO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_XML_SES_UPD_UNITA_DOC_KO"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_XML_SES_UPD_UNITA_DOC_KO_ID_XML_SES_UPD_UNITA_DOC_KO_GENERATOR")
    public Long getIdXmlSesUpdUnitaDocKo() {
	return this.idXmlSesUpdUnitaDocKo;
    }

    public void setIdXmlSesUpdUnitaDocKo(Long idXmlSesUpdUnitaDocKo) {
	this.idXmlSesUpdUnitaDocKo = idXmlSesUpdUnitaDocKo;
    }

    @Lob
    @Column(name = "BL_XML")
    public String getBlXml() {
	return this.blXml;
    }

    public void setBlXml(String blXml) {
	this.blXml = blXml;
    }

    @Column(name = "CD_VERSIONE_XML")
    public String getCdVersioneXml() {
	return this.cdVersioneXml;
    }

    public void setCdVersioneXml(String cdVersioneXml) {
	this.cdVersioneXml = cdVersioneXml;
    }

    @Column(name = "DT_REG_XML")
    public LocalDate getDtRegXml() {
	return this.dtRegXml;
    }

    public void setDtRegXml(LocalDate dtRegXml) {
	this.dtRegXml = dtRegXml;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SES_UPD_UD_KO")
    public VrsSesUpdUnitaDocKo getVrsSesUpdUnitaDocKo() {
	return this.vrsSesUpdUnitaDocKo;
    }

    public void setVrsSesUpdUnitaDocKo(VrsSesUpdUnitaDocKo vrsSesUpdUnitaDocKo) {
	this.vrsSesUpdUnitaDocKo = vrsSesUpdUnitaDocKo;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_XML")
    public TiXmlVrsXmlSesUpdUnitaDocKo getTiXml() {
	return this.tiXml;
    }

    public void setTiXml(TiXmlVrsXmlSesUpdUnitaDocKo tiXml) {
	this.tiXml = tiXml;
    }

    @Column(name = "AA_REG_XML")
    public Integer getAaRegXml() {
	return aaRegXml;
    }

    public void setAaRegXml(Integer aaRegXml) {
	this.aaRegXml = aaRegXml;
    }

}
