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

import it.eng.parer.entity.constraint.AroXmlUpdUnitaDoc.TiXmlUpdUnitaDoc;

/**
 * The persistent class for the ARO_XML_UPD_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_XML_UPD_UNITA_DOC")
public class AroXmlUpdUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXmlUpdUnitaDoc;

    private String blXml;

    private String cdEncodingHashXml;

    private String cdVersioneXml;

    private String dsAlgoHashXml;

    private String dsHashXml;

    private String dsUrnXml;

    private String dsUrnNormalizXml;

    private LocalDate dtIniSes;

    private OrgStrut orgStrut;

    private AroUpdUnitaDoc aroUpdUnitaDoc;

    private TiXmlUpdUnitaDoc tiXmlUpdUnitaDoc;

    private Integer aaIniSes;

    public AroXmlUpdUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_XML_UPD_UNITA_DOC")
    @GenericGenerator(name = "SARO_XML_UPD_UNITA_DOC_ID_XML_UPD_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_XML_UPD_UNITA_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_XML_UPD_UNITA_DOC_ID_XML_UPD_UNITA_DOC_GENERATOR")
    public Long getIdXmlUpdUnitaDoc() {
        return this.idXmlUpdUnitaDoc;
    }

    public void setIdXmlUpdUnitaDoc(Long idXmlUpdUnitaDoc) {
        this.idXmlUpdUnitaDoc = idXmlUpdUnitaDoc;
    }

    @Lob
    @Column(name = "BL_XML")
    public String getBlXml() {
        return this.blXml;
    }

    public void setBlXml(String blXml) {
        this.blXml = blXml;
    }

    @Column(name = "CD_ENCODING_HASH_XML")
    public String getCdEncodingHashXml() {
        return this.cdEncodingHashXml;
    }

    public void setCdEncodingHashXml(String cdEncodingHashXml) {
        this.cdEncodingHashXml = cdEncodingHashXml;
    }

    @Column(name = "CD_VERSIONE_XML")
    public String getCdVersioneXml() {
        return this.cdVersioneXml;
    }

    public void setCdVersioneXml(String cdVersioneXml) {
        this.cdVersioneXml = cdVersioneXml;
    }

    @Column(name = "DS_ALGO_HASH_XML")
    public String getDsAlgoHashXml() {
        return this.dsAlgoHashXml;
    }

    public void setDsAlgoHashXml(String dsAlgoHashXml) {
        this.dsAlgoHashXml = dsAlgoHashXml;
    }

    @Column(name = "DS_HASH_XML")
    public String getDsHashXml() {
        return this.dsHashXml;
    }

    public void setDsHashXml(String dsHashXml) {
        this.dsHashXml = dsHashXml;
    }

    @Column(name = "DS_URN_XML")
    public String getDsUrnXml() {
        return this.dsUrnXml;
    }

    public void setDsUrnXml(String dsUrnXml) {
        this.dsUrnXml = dsUrnXml;
    }

    @Column(name = "DS_URN_NORMALIZ_XML")
    public String getDsUrnNormalizXml() {
        return this.dsUrnNormalizXml;
    }

    public void setDsUrnNormalizXml(String dsUrnNormalizXml) {
        this.dsUrnNormalizXml = dsUrnNormalizXml;
    }

    @Column(name = "DT_INI_SES")
    public LocalDate getDtIniSes() {
        return this.dtIniSes;
    }

    public void setDtIniSes(LocalDate dtIniSes) {
        this.dtIniSes = dtIniSes;
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

    // bi-directional many-to-one association to AroUpdUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_UNITA_DOC")
    public AroUpdUnitaDoc getAroUpdUnitaDoc() {
        return this.aroUpdUnitaDoc;
    }

    public void setAroUpdUnitaDoc(AroUpdUnitaDoc aroUpdUnitaDoc) {
        this.aroUpdUnitaDoc = aroUpdUnitaDoc;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_XML_UPD_UNITA_DOC")
    public TiXmlUpdUnitaDoc getTiXmlUpdUnitaDoc() {
        return this.tiXmlUpdUnitaDoc;
    }

    public void setTiXmlUpdUnitaDoc(TiXmlUpdUnitaDoc tiXmlUpdUnitaDoc) {
        this.tiXmlUpdUnitaDoc = tiXmlUpdUnitaDoc;
    }

    @Column(name = "AA_INI_SES")
    public Integer getAaIniSes() {
        return aaIniSes;
    }

    public void setAaIniSes(Integer aaIniSes) {
        this.aaIniSes = aaIniSes;
    }

}
