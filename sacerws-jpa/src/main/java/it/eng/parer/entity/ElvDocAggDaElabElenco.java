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

import javax.persistence.AssociationOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
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

import it.eng.parer.entity.inheritance.oop.ElvUdDocUpdDaElabElenco;

/**
 * The persistent class for the ELV_DOC_AGG_DA_ELAB_ELENCO database table.
 */
@Entity
@Table(name = "ELV_DOC_AGG_DA_ELAB_ELENCO")
@AssociationOverride(name = "udDocUpdObj", joinColumns = @JoinColumn(name = "ID_DOC", insertable = false, updatable = false))
public class ElvDocAggDaElabElenco extends ElvUdDocUpdDaElabElenco<AroDoc> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idDocAggDaElabElenco;

    private BigDecimal idStrut;

    private String tiStatoDocDaElab;

    private AroDoc aroDoc;

    public ElvDocAggDaElabElenco() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_DOC_AGG_DA_ELAB_ELENCO")
    @GenericGenerator(name = "SELV_DOC_AGG_DA_ELAB_ELENCO_ID_DOC_AGG_DA_ELAB_ELENCO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SELV_DOC_AGG_DA_ELAB_ELENCO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SELV_DOC_AGG_DA_ELAB_ELENCO_ID_DOC_AGG_DA_ELAB_ELENCO_GENERATOR")
    public Long getIdDocAggDaElabElenco() {
        return this.idDocAggDaElabElenco;
    }

    public void setIdDocAggDaElabElenco(Long idDocAggDaElabElenco) {
        this.idDocAggDaElabElenco = idDocAggDaElabElenco;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "TI_STATO_DOC_DA_ELAB")
    public String getTiStatoDocDaElab() {
        return this.tiStatoDocDaElab;
    }

    public void setTiStatoDocDaElab(String tiStatoDocDaElab) {
        this.tiStatoDocDaElab = tiStatoDocDaElab;
    }

    // bi-directional many-to-one association to AroDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOC")
    public AroDoc getAroDoc() {
        return this.aroDoc;
    }

    public void setAroDoc(AroDoc aroDoc) {
        this.aroDoc = aroDoc;
    }
}
