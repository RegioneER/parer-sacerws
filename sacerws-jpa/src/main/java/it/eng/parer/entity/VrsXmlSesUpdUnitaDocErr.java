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

import it.eng.parer.entity.constraint.VrsXmlSesUpdUnitaDocErr.TiXmlVrsXmlSesUpdUnitaDocErr;

/**
 * The persistent class for the VRS_XML_SES_UPD_UNITA_DOC_ERR database table.
 */
@Entity
@Table(name = "VRS_XML_SES_UPD_UNITA_DOC_ERR")
public class VrsXmlSesUpdUnitaDocErr implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXmlSesUpdUnitaDocErr;

    private String blXml;

    private String cdVersioneXml;

    private VrsSesUpdUnitaDocErr vrsSesUpdUnitaDocErr;

    private TiXmlVrsXmlSesUpdUnitaDocErr tiXml;

    public VrsXmlSesUpdUnitaDocErr() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_XML_SES_UPD_UNITA_DOC_ERR")
    @GenericGenerator(name = "SVRS_XML_SES_UPD_UNITA_DOC_ERR_ID_XML_SES_UPD_UNITA_DOC_ERR_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_XML_SES_UPD_UNITA_DOC_ERR"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_XML_SES_UPD_UNITA_DOC_ERR_ID_XML_SES_UPD_UNITA_DOC_ERR_GENERATOR")
    public Long getIdXmlSesUpdUnitaDocErr() {
        return this.idXmlSesUpdUnitaDocErr;
    }

    public void setIdXmlSesUpdUnitaDocErr(Long idXmlSesUpdUnitaDocErr) {
        this.idXmlSesUpdUnitaDocErr = idXmlSesUpdUnitaDocErr;
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

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SES_UPD_UD_ERR")
    public VrsSesUpdUnitaDocErr getVrsSesUpdUnitaDocErr() {
        return this.vrsSesUpdUnitaDocErr;
    }

    public void setVrsSesUpdUnitaDocErr(VrsSesUpdUnitaDocErr vrsSesUpdUnitaDocErr) {
        this.vrsSesUpdUnitaDocErr = vrsSesUpdUnitaDocErr;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_XML")
    public TiXmlVrsXmlSesUpdUnitaDocErr getTiXml() {
        return this.tiXml;
    }

    public void setTiXml(TiXmlVrsXmlSesUpdUnitaDocErr tiXml) {
        this.tiXml = tiXml;
    }

}
