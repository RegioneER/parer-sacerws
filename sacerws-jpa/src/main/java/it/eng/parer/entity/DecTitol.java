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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_TITOL database table.
 */
@Entity
@Table(name = "DEC_TITOL")
public class DecTitol implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTitol;

    private String cdSepFascicolo;

    private String dlNote;

    private Date dtIstituz;

    private Date dtSoppres;

    private BigDecimal niLivelli;

    private String nmTitol;

    private String tiStatoTitol;

    private List<DecLivelloTitol> decLivelloTitols = new ArrayList<>();

    private OrgStrut orgStrut;

    private List<DecVoceTitol> decVoceTitols = new ArrayList<>();

    public DecTitol() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_TITOL")
    @GenericGenerator(name = "SDEC_TITOL_ID_TITOL_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_TITOL"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_TITOL_ID_TITOL_GENERATOR")
    public Long getIdTitol() {
        return this.idTitol;
    }

    public void setIdTitol(Long idTitol) {
        this.idTitol = idTitol;
    }

    @Column(name = "CD_SEP_FASCICOLO", columnDefinition = "char")
    public String getCdSepFascicolo() {
        return this.cdSepFascicolo;
    }

    public void setCdSepFascicolo(String cdSepFascicolo) {
        this.cdSepFascicolo = cdSepFascicolo;
    }

    @Column(name = "DL_NOTE")
    public String getDlNote() {
        return this.dlNote;
    }

    public void setDlNote(String dlNote) {
        this.dlNote = dlNote;
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

    @Column(name = "NI_LIVELLI")
    public BigDecimal getNiLivelli() {
        return this.niLivelli;
    }

    public void setNiLivelli(BigDecimal niLivelli) {
        this.niLivelli = niLivelli;
    }

    @Column(name = "NM_TITOL")
    public String getNmTitol() {
        return this.nmTitol;
    }

    public void setNmTitol(String nmTitol) {
        this.nmTitol = nmTitol;
    }

    @Column(name = "TI_STATO_TITOL")
    public String getTiStatoTitol() {
        return this.tiStatoTitol;
    }

    public void setTiStatoTitol(String tiStatoTitol) {
        this.tiStatoTitol = tiStatoTitol;
    }

    // bi-directional many-to-one association to DecLivelloTitol
    @OneToMany(mappedBy = "decTitol", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    public List<DecLivelloTitol> getDecLivelloTitols() {
        return this.decLivelloTitols;
    }

    public void setDecLivelloTitols(List<DecLivelloTitol> decLivelloTitols) {
        this.decLivelloTitols = decLivelloTitols;
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

    // bi-directional many-to-one association to DecVoceTitol
    @OneToMany(mappedBy = "decTitol", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    public List<DecVoceTitol> getDecVoceTitols() {
        return this.decVoceTitols;
    }

    public void setDecVoceTitols(List<DecVoceTitol> decVoceTitols) {
        this.decVoceTitols = decVoceTitols;
    }

}
