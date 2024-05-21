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
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the VRS_XML_DATI_SESSIONE_VERS_KO database table.
 */
@Entity
@Table(name = "VRS_XML_DATI_SESSIONE_VERS_KO")
public class VrsXmlDatiSessioneVersKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXmlDatiSessioneVersKo;

    private String blXml;

    private String cdEncodingHashXmlVers;

    private String cdVersioneXml;

    private String dsAlgoHashXmlVers;

    private String dsHashXmlVers;

    private String dsUrnXmlVers;

    private BigDecimal idStrut;

    private String tiXmlDati;
    private String flCanonicalized;
    private VrsDatiSessioneVersKo vrsDatiSessioneVersKo;
    private List<VrsUrnXmlSessioneVersKo> vrsUrnXmlSessioneVerKos = new ArrayList<>();

    public VrsXmlDatiSessioneVersKo() {
        // hibernate
    }

    @Id
    @Column(name = "ID_XML_DATI_SESSIONE_VERS_KO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getIdXmlDatiSessioneVersKo() {
        return this.idXmlDatiSessioneVersKo;
    }

    public void setIdXmlDatiSessioneVersKo(Long idXmlDatiSessioneVersKo) {
        this.idXmlDatiSessioneVersKo = idXmlDatiSessioneVersKo;
    }

    @Lob()
    @Column(name = "BL_XML")
    public String getBlXml() {
        return this.blXml;
    }

    public void setBlXml(String blXml) {
        this.blXml = blXml;
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

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "TI_XML_DATI")
    public String getTiXmlDati() {
        return this.tiXmlDati;
    }

    public void setTiXmlDati(String tiXmlDati) {
        this.tiXmlDati = tiXmlDati;
    }

    // bi-directional many-to-one association to VrsDatiSessioneVersKo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DATI_SESSIONE_VERS_KO")
    public VrsDatiSessioneVersKo getVrsDatiSessioneVersKo() {
        return this.vrsDatiSessioneVersKo;
    }

    public void setVrsDatiSessioneVersKo(VrsDatiSessioneVersKo vrsDatiSessioneVersKo) {
        this.vrsDatiSessioneVersKo = vrsDatiSessioneVersKo;
    }

    @Column(name = "FL_CANONICALIZED", columnDefinition = "CHAR")
    public String getFlCanonicalized() {
        return this.flCanonicalized;
    }

    public void setFlCanonicalized(String flCanonicalized) {
        this.flCanonicalized = flCanonicalized;
    }

    // bi-directional many-to-one association to VrsXmlDatiSessioneVers
    @OneToMany(mappedBy = "vrsXmlDatiSessioneVersKo")
    public List<VrsUrnXmlSessioneVersKo> getVrsUrnXmlSessioneVersKo() {
        return this.vrsUrnXmlSessioneVerKos;
    }

    //
    public void setVrsUrnXmlSessioneVersKo(List<VrsUrnXmlSessioneVersKo> vrsUrnXmlSessioneVerKos) {
        this.vrsUrnXmlSessioneVerKos = vrsUrnXmlSessioneVerKos;
    }
}
