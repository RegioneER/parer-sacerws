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
import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
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

/**
 * The persistent class for the VRS_XML_SES_FASCICOLO_KO database table.
 */
@Entity
@Table(name = "VRS_XML_SES_FASCICOLO_KO")
public class VrsXmlSesFascicoloKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXmlSesFascicoloKo;

    private String blXml;
    private String cdVersioneXml;
    private LocalDate dtRegXmlSesKo;

    private BigDecimal idStrut;

    private String tiXml;

    private VrsSesFascicoloKo vrsSesFascicoloKo;

    public VrsXmlSesFascicoloKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_XML_SES_FASCICOLO_KO")
    @GenericGenerator(name = "SVRS_XML_SES_FASCICOLO_KO_ID_XML_SES_FASCICOLO_KO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_XML_SES_FASCICOLO_KO"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_XML_SES_FASCICOLO_KO_ID_XML_SES_FASCICOLO_KO_GENERATOR")
    public Long getIdXmlSesFascicoloKo() {
	return this.idXmlSesFascicoloKo;
    }

    public void setIdXmlSesFascicoloKo(Long idXmlSesFascicoloKo) {
	this.idXmlSesFascicoloKo = idXmlSesFascicoloKo;
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

    @Column(name = "DT_REG_XML_SES_KO")
    public LocalDate getDtRegXmlSesKo() {
	return this.dtRegXmlSesKo;
    }

    public void setDtRegXmlSesKo(LocalDate dtRegXmlSesKo) {
	this.dtRegXmlSesKo = dtRegXmlSesKo;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
	return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
	this.idStrut = idStrut;
    }

    @Column(name = "TI_XML")
    public String getTiXml() {
	return this.tiXml;
    }

    public void setTiXml(String tiXml) {
	this.tiXml = tiXml;
    }

    // bi-directional many-to-one association to VrsSesFascicoloKo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SES_FASCICOLO_KO")
    public VrsSesFascicoloKo getVrsSesFascicoloKo() {
	return this.vrsSesFascicoloKo;
    }

    public void setVrsSesFascicoloKo(VrsSesFascicoloKo vrsSesFascicoloKo) {
	this.vrsSesFascicoloKo = vrsSesFascicoloKo;
    }
}
