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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import it.eng.parer.entity.constraint.VrsUrnXmlSessioneVers.TiUrnXmlSessioneVers;

/**
 * The persistent class for the VRS_URN_XML_SESSIONE_VERS database table.
 */
@Entity
@Table(name = "VRS_URN_XML_SESSIONE_VERS")
public class VrsUrnXmlSessioneVers implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUrnXmlSessionVers;

    private String dsUrn;

    private TiUrnXmlSessioneVers tiUrn;

    private VrsXmlDatiSessioneVers vrsXmlDatiSessioneVers;

    public VrsUrnXmlSessioneVers() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_URN_XML_SESSIONE_VERS")
    @GenericGenerator(name = "SVRS_URN_XML_SESSIONE_VERS_ID_URN_XML_SESSIONE_VERS_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_URN_XML_SESSIONE_VERS"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_URN_XML_SESSIONE_VERS_ID_URN_XML_SESSIONE_VERS_GENERATOR")
    public Long getIdUrnXmlSessionVers() {
        return this.idUrnXmlSessionVers;
    }

    public void setIdUrnXmlSessionVers(Long idUrnXmlSessionVers) {
        this.idUrnXmlSessionVers = idUrnXmlSessionVers;
    }

    @Column(name = "DS_URN")
    public String getDsUrn() {
        return this.dsUrn;
    }

    public void setDsUrn(String dsUrn) {
        this.dsUrn = dsUrn;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_URN")
    public TiUrnXmlSessioneVers getTiUrn() {
        return this.tiUrn;
    }

    public void setTiUrn(TiUrnXmlSessioneVers tiUrnXmlSessioneVers) {
        this.tiUrn = tiUrnXmlSessioneVers;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_XML_DATI_SESSIONE_VERS")
    public VrsXmlDatiSessioneVers getVrsXmlDatiSessioneVers() {
        return this.vrsXmlDatiSessioneVers;
    }

    public void setVrsXmlDatiSessioneVers(VrsXmlDatiSessioneVers vrsXmlDatiSessioneVers) {
        this.vrsXmlDatiSessioneVers = vrsXmlDatiSessioneVers;
    }

}
