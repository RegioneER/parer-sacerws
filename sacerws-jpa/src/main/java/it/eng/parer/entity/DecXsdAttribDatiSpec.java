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
import java.math.BigDecimal;

import javax.persistence.Cacheable;
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

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_XSD_ATTRIB_DATI_SPEC database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_XSD_ATTRIB_DATI_SPEC")
public class DecXsdAttribDatiSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXsdAttribDatiSpec;

    private BigDecimal niOrdAttrib;

    private DecAttribDatiSpec decAttribDatiSpec;

    private DecXsdDatiSpec decXsdDatiSpec;

    public DecXsdAttribDatiSpec() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_XSD_ATTRIB_DATI_SPEC")
    @XmlID
    @GenericGenerator(name = "SDEC_XSD_ATTRIB_DATI_SPEC_ID_XSD_ATTRIB_DATI_SPEC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_XSD_ATTRIB_DATI_SPEC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_XSD_ATTRIB_DATI_SPEC_ID_XSD_ATTRIB_DATI_SPEC_GENERATOR")
    public Long getIdXsdAttribDatiSpec() {
        return this.idXsdAttribDatiSpec;
    }

    public void setIdXsdAttribDatiSpec(Long idXsdAttribDatiSpec) {
        this.idXsdAttribDatiSpec = idXsdAttribDatiSpec;
    }

    @Column(name = "NI_ORD_ATTRIB")
    public BigDecimal getNiOrdAttrib() {
        return this.niOrdAttrib;
    }

    public void setNiOrdAttrib(BigDecimal niOrdAttrib) {
        this.niOrdAttrib = niOrdAttrib;
    }

    // bi-directional many-to-one association to DecAttribDatiSpec
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ATTRIB_DATI_SPEC")
    @XmlInverseReference(mappedBy = "decXsdAttribDatiSpecs")
    public DecAttribDatiSpec getDecAttribDatiSpec() {
        return this.decAttribDatiSpec;
    }

    public void setDecAttribDatiSpec(DecAttribDatiSpec decAttribDatiSpec) {
        this.decAttribDatiSpec = decAttribDatiSpec;
    }

    // bi-directional many-to-one association to DecXsdDatiSpec
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_XSD_DATI_SPEC", nullable = false)
    @XmlInverseReference(mappedBy = "decXsdAttribDatiSpecs")
    public DecXsdDatiSpec getDecXsdDatiSpec() {
        return this.decXsdDatiSpec;
    }

    public void setDecXsdDatiSpec(DecXsdDatiSpec decXsdDatiSpec) {
        this.decXsdDatiSpec = decXsdDatiSpec;
    }

}
