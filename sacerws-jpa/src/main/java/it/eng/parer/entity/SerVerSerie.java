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
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the SER_VER_SERIE database table.
 */
@Entity
@Table(name = "SER_VER_SERIE")
public class SerVerSerie implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idVerSerie;

    private String cdVerSerie;

    private String dsListaAnniSelSerie;

    private Date dtCreazioneUnitaDocA;

    private Date dtCreazioneUnitaDocDa;

    private Date dtFineSelSerie;

    private Date dtInizioSelSerie;

    private Date dtRegUnitaDocA;

    private Date dtRegUnitaDocDa;

    private String flUpdAnnulUnitaDoc;

    private BigDecimal idStatoVerSerieCor;

    private BigDecimal niPeriodoSelSerie;

    private BigDecimal pgVerSerie;

    private String tiPeriodoSelSerie;

    private SerVerSerie serVerSerie;

    private List<SerVerSerie> serVerSeries = new ArrayList<>();

    private String flUpdModifUnitaDoc;

    public SerVerSerie() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_VER_SERIE")
    @GenericGenerator(name = "SSER_VER_SERIE_ID_VER_SERIE_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SSER_VER_SERIE"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SSER_VER_SERIE_ID_VER_SERIE_GENERATOR")
    public Long getIdVerSerie() {
	return this.idVerSerie;
    }

    public void setIdVerSerie(Long idVerSerie) {
	this.idVerSerie = idVerSerie;
    }

    @Column(name = "CD_VER_SERIE")
    public String getCdVerSerie() {
	return this.cdVerSerie;
    }

    public void setCdVerSerie(String cdVerSerie) {
	this.cdVerSerie = cdVerSerie;
    }

    @Column(name = "DS_LISTA_ANNI_SEL_SERIE")
    public String getDsListaAnniSelSerie() {
	return this.dsListaAnniSelSerie;
    }

    public void setDsListaAnniSelSerie(String dsListaAnniSelSerie) {
	this.dsListaAnniSelSerie = dsListaAnniSelSerie;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_CREAZIONE_UNITA_DOC_A")
    public Date getDtCreazioneUnitaDocA() {
	return this.dtCreazioneUnitaDocA;
    }

    public void setDtCreazioneUnitaDocA(Date dtCreazioneUnitaDocA) {
	this.dtCreazioneUnitaDocA = dtCreazioneUnitaDocA;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_CREAZIONE_UNITA_DOC_DA")
    public Date getDtCreazioneUnitaDocDa() {
	return this.dtCreazioneUnitaDocDa;
    }

    public void setDtCreazioneUnitaDocDa(Date dtCreazioneUnitaDocDa) {
	this.dtCreazioneUnitaDocDa = dtCreazioneUnitaDocDa;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_FINE_SEL_SERIE")
    public Date getDtFineSelSerie() {
	return this.dtFineSelSerie;
    }

    public void setDtFineSelSerie(Date dtFineSelSerie) {
	this.dtFineSelSerie = dtFineSelSerie;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_INIZIO_SEL_SERIE")
    public Date getDtInizioSelSerie() {
	return this.dtInizioSelSerie;
    }

    public void setDtInizioSelSerie(Date dtInizioSelSerie) {
	this.dtInizioSelSerie = dtInizioSelSerie;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_REG_UNITA_DOC_A")
    public Date getDtRegUnitaDocA() {
	return this.dtRegUnitaDocA;
    }

    public void setDtRegUnitaDocA(Date dtRegUnitaDocA) {
	this.dtRegUnitaDocA = dtRegUnitaDocA;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_REG_UNITA_DOC_DA")
    public Date getDtRegUnitaDocDa() {
	return this.dtRegUnitaDocDa;
    }

    public void setDtRegUnitaDocDa(Date dtRegUnitaDocDa) {
	this.dtRegUnitaDocDa = dtRegUnitaDocDa;
    }

    @Column(name = "FL_UPD_ANNUL_UNITA_DOC", columnDefinition = "char(1)")
    public String getFlUpdAnnulUnitaDoc() {
	return this.flUpdAnnulUnitaDoc;
    }

    public void setFlUpdAnnulUnitaDoc(String flUpdAnnulUnitaDoc) {
	this.flUpdAnnulUnitaDoc = flUpdAnnulUnitaDoc;
    }

    @Column(name = "ID_STATO_VER_SERIE_COR")
    public BigDecimal getIdStatoVerSerieCor() {
	return this.idStatoVerSerieCor;
    }

    public void setIdStatoVerSerieCor(BigDecimal idStatoVerSerieCor) {
	this.idStatoVerSerieCor = idStatoVerSerieCor;
    }

    @Column(name = "NI_PERIODO_SEL_SERIE")
    public BigDecimal getNiPeriodoSelSerie() {
	return this.niPeriodoSelSerie;
    }

    public void setNiPeriodoSelSerie(BigDecimal niPeriodoSelSerie) {
	this.niPeriodoSelSerie = niPeriodoSelSerie;
    }

    @Column(name = "PG_VER_SERIE")
    public BigDecimal getPgVerSerie() {
	return this.pgVerSerie;
    }

    public void setPgVerSerie(BigDecimal pgVerSerie) {
	this.pgVerSerie = pgVerSerie;
    }

    @Column(name = "TI_PERIODO_SEL_SERIE")
    public String getTiPeriodoSelSerie() {
	return this.tiPeriodoSelSerie;
    }

    public void setTiPeriodoSelSerie(String tiPeriodoSelSerie) {
	this.tiPeriodoSelSerie = tiPeriodoSelSerie;
    }

    // bi-directional many-to-one association to SerVerSerie
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VER_SERIE_PADRE")
    public SerVerSerie getSerVerSerie() {
	return this.serVerSerie;
    }

    public void setSerVerSerie(SerVerSerie serVerSerie) {
	this.serVerSerie = serVerSerie;
    }

    // bi-directional many-to-one association to SerVerSerie
    @OneToMany(mappedBy = "serVerSerie", cascade = CascadeType.PERSIST)
    public List<SerVerSerie> getSerVerSeries() {
	return this.serVerSeries;
    }

    public void setSerVerSeries(List<SerVerSerie> serVerSeries) {
	this.serVerSeries = serVerSeries;
    }

    @Column(name = "FL_UPD_MODIF_UNITA_DOC", columnDefinition = "char(1)")
    public String getFlUpdModifUnitaDoc() {
	return this.flUpdModifUnitaDoc;
    }

    public void setFlUpdModifUnitaDoc(String flUpdModifUnitaDoc) {
	this.flUpdModifUnitaDoc = flUpdModifUnitaDoc;
    }

    /**
     * Gestione dei default. Risulta la migliore pratica in quanto è indipendente dal db utilizzato
     * e sfrutta diretta JPA quindi calabile sotto ogni contesto in termini di ORM
     *
     * ref. https://stackoverflow.com/a/13432234
     */
    @PrePersist
    void preInsert() {
	if (this.flUpdModifUnitaDoc == null) {
	    this.flUpdModifUnitaDoc = "0";
	}
	if (this.flUpdAnnulUnitaDoc == null) {
	    this.flUpdAnnulUnitaDoc = "0";
	}
    }
}
