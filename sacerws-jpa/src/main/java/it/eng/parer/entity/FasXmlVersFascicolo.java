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
import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the FAS_XML_VERS_FASCICOLO database table.
 */
@Entity
@Table(name = "FAS_XML_VERS_FASCICOLO")
public class FasXmlVersFascicolo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXmlVersFascicolo;

    private String blXmlVers;

    private String cdEncodingHashXmlVers;

    private String cdVersioneXml;

    private String dsAlgoHashXmlVers;

    private String dsHashXmlVers;

    private String dsUrnXmlVers;

    private String dsUrnNormalizXmlVers;

    private Date dtVersFascicolo;

    private BigDecimal idStrut;

    private String tiXmlVers;

    private String flCanonicalized;

    private FasFascicolo fasFascicolo;

    public FasXmlVersFascicolo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_XML_VERS_FASCICOLO")
    @GenericGenerator(name = "SFAS_XML_VERS_FASCICOLO_ID_XML_VERS_FASCICOLO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFAS_XML_VERS_FASCICOLO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFAS_XML_VERS_FASCICOLO_ID_XML_VERS_FASCICOLO_GENERATOR")
    public Long getIdXmlVersFascicolo() {
        return this.idXmlVersFascicolo;
    }

    public void setIdXmlVersFascicolo(Long idXmlVersFascicolo) {
        this.idXmlVersFascicolo = idXmlVersFascicolo;
    }

    @Lob
    @Column(name = "BL_XML_VERS")
    public String getBlXmlVers() {
        return this.blXmlVers;
    }

    public void setBlXmlVers(String blXmlVers) {
        this.blXmlVers = blXmlVers;
    }

    @Column(name = "CD_ENCODING_HASH_XML_VERS")
    public String getCdEncodingHashXmlVers() {
        return this.cdEncodingHashXmlVers;
    }

    public void setCdEncodingHashXmlVers(String cdEncodingHashXmlVers) {
        this.cdEncodingHashXmlVers = cdEncodingHashXmlVers;
    }

    @Column(name = "CD_VERSIONE_XML")
    public String getCdVersioneXml() {
        return this.cdVersioneXml;
    }

    public void setCdVersioneXml(String cdVersioneXml) {
        this.cdVersioneXml = cdVersioneXml;
    }

    @Column(name = "DS_ALGO_HASH_XML_VERS")
    public String getDsAlgoHashXmlVers() {
        return this.dsAlgoHashXmlVers;
    }

    public void setDsAlgoHashXmlVers(String dsAlgoHashXmlVers) {
        this.dsAlgoHashXmlVers = dsAlgoHashXmlVers;
    }

    @Column(name = "DS_HASH_XML_VERS")
    public String getDsHashXmlVers() {
        return this.dsHashXmlVers;
    }

    public void setDsHashXmlVers(String dsHashXmlVers) {
        this.dsHashXmlVers = dsHashXmlVers;
    }

    @Column(name = "DS_URN_XML_VERS")
    public String getDsUrnXmlVers() {
        return this.dsUrnXmlVers;
    }

    public void setDsUrnXmlVers(String dsUrnXmlVers) {
        this.dsUrnXmlVers = dsUrnXmlVers;
    }

    @Column(name = "DS_URN_NORMALIZ_XML_VERS")
    public String getDsUrnNormalizXmlVers() {
        return this.dsUrnNormalizXmlVers;
    }

    public void setDsUrnNormalizXmlVers(String dsUrnNormalizXmlVers) {
        this.dsUrnNormalizXmlVers = dsUrnNormalizXmlVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_VERS_FASCICOLO")
    public Date getDtVersFascicolo() {
        return this.dtVersFascicolo;
    }

    public void setDtVersFascicolo(Date dtVersFascicolo) {
        this.dtVersFascicolo = dtVersFascicolo;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "TI_XML_VERS")
    public String getTiXmlVers() {
        return this.tiXmlVers;
    }

    public void setTiXmlVers(String tiXmlVers) {
        this.tiXmlVers = tiXmlVers;
    }

    // bi-directional many-to-one association to FasFascicolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO")
    public FasFascicolo getFasFascicolo() {
        return this.fasFascicolo;
    }

    public void setFasFascicolo(FasFascicolo fasFascicolo) {
        this.fasFascicolo = fasFascicolo;
    }

    @Column(name = "FL_CANONICALIZED", columnDefinition = "CHAR")
    public String getFlCanonicalized() {
        return this.flCanonicalized;
    }

    public void setFlCanonicalized(String flCanonicalized) {
        this.flCanonicalized = flCanonicalized;
    }

}
