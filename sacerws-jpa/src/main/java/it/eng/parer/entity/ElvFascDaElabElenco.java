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
import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.ElvFascDaElabElenco.TiStatoFascDaElab;

/**
 * The persistent class for the ELV_FASC_DA_ELAB_ELENCO database table.
 */
@Entity
@Table(name = "ELV_FASC_DA_ELAB_ELENCO")
public class ElvFascDaElabElenco implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFascDaElabElenco;

    private BigDecimal aaFascicolo;

    private Date tsVersFascicolo;

    private BigDecimal idStrut;

    private TiStatoFascDaElab tiStatoFascDaElab;

    private FasFascicolo fasFascicolo;

    private BigDecimal idTipoFascicolo;

    public ElvFascDaElabElenco() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_FASC_DA_ELAB_ELENCO")
    @GenericGenerator(name = "SELV_FASC_DA_ELAB_ELENCO_ID_FASC_DA_ELAB_ELENCO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SELV_FASC_DA_ELAB_ELENCO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SELV_FASC_DA_ELAB_ELENCO_ID_FASC_DA_ELAB_ELENCO_GENERATOR")
    public Long getIdFascDaElabElenco() {
        return this.idFascDaElabElenco;
    }

    public void setIdFascDaElabElenco(Long idFascDaElabElenco) {
        this.idFascDaElabElenco = idFascDaElabElenco;
    }

    @Column(name = "AA_FASCICOLO")
    public BigDecimal getAaFascicolo() {
        return this.aaFascicolo;
    }

    public void setAaFascicolo(BigDecimal aaFascicolo) {
        this.aaFascicolo = aaFascicolo;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TS_VERS_FASCICOLO")
    public Date getTsVersFascicolo() {
        return this.tsVersFascicolo;
    }

    public void setTsVersFascicolo(Date tsVersFascicolo) {
        this.tsVersFascicolo = tsVersFascicolo;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_STATO_FASC_DA_ELAB")
    public TiStatoFascDaElab getTiStatoFascDaElab() {
        return this.tiStatoFascDaElab;
    }

    public void setTiStatoFascDaElab(TiStatoFascDaElab tiStatoFascDaElab) {
        this.tiStatoFascDaElab = tiStatoFascDaElab;
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

    @Column(name = "ID_TIPO_FASCICOLO")
    public BigDecimal getIdTipoFascicolo() {
        return this.idTipoFascicolo;
    }

    public void setIdTipoFascicolo(BigDecimal idTipoFascicolo) {
        this.idTipoFascicolo = idTipoFascicolo;
    }
}
