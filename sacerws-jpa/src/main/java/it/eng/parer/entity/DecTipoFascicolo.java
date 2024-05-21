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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_TIPO_FASCICOLO database table.
 */
@Entity
@Table(name = "DEC_TIPO_FASCICOLO")
public class DecTipoFascicolo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTipoFascicolo;

    private String dsTipoFascicolo;

    private Date dtIstituz;

    private Date dtSoppres;

    private String nmTipoFascicolo;

    private List<DecAaTipoFascicolo> decAaTipoFascicolos = new ArrayList<>();

    private OrgStrut orgStrut;

    private List<FasFascicolo> fasFascicolos = new ArrayList<>();

    public DecTipoFascicolo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_TIPO_FASCICOLO")
    @GenericGenerator(name = "SDEC_TIPO_FASCICOLO_ID_TIPO_FASCICOLO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_TIPO_FASCICOLO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_TIPO_FASCICOLO_ID_TIPO_FASCICOLO_GENERATOR")
    public Long getIdTipoFascicolo() {
        return this.idTipoFascicolo;
    }

    public void setIdTipoFascicolo(Long idTipoFascicolo) {
        this.idTipoFascicolo = idTipoFascicolo;
    }

    @Column(name = "DS_TIPO_FASCICOLO")
    public String getDsTipoFascicolo() {
        return this.dsTipoFascicolo;
    }

    public void setDsTipoFascicolo(String dsTipoFascicolo) {
        this.dsTipoFascicolo = dsTipoFascicolo;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_ISTITUZ")
    public Date getDtIstituz() {
        return this.dtIstituz;
    }

    public void setDtIstituz(Date dtIstituz) {
        this.dtIstituz = dtIstituz;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_SOPPRES")
    public Date getDtSoppres() {
        return this.dtSoppres;
    }

    public void setDtSoppres(Date dtSoppres) {
        this.dtSoppres = dtSoppres;
    }

    @Column(name = "NM_TIPO_FASCICOLO")
    public String getNmTipoFascicolo() {
        return this.nmTipoFascicolo;
    }

    public void setNmTipoFascicolo(String nmTipoFascicolo) {
        this.nmTipoFascicolo = nmTipoFascicolo;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    @XmlTransient
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to DecAaTipoFascicolo
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "decTipoFascicolo", fetch = FetchType.LAZY)
    public List<DecAaTipoFascicolo> getDecAaTipoFascicolos() {
        return this.decAaTipoFascicolos;
    }

    public void setDecAaTipoFascicolos(List<DecAaTipoFascicolo> decAaTipoFascicolos) {
        this.decAaTipoFascicolos = decAaTipoFascicolos;
    }

    // bi-directional many-to-one association to FasFascicolo
    @OneToMany(mappedBy = "decTipoFascicolo")
    @XmlTransient
    public List<FasFascicolo> getFasFascicolos() {
        return this.fasFascicolos;
    }

    public void setFasFascicolos(List<FasFascicolo> fasFascicolos) {
        this.fasFascicolos = fasFascicolos;
    }
}
