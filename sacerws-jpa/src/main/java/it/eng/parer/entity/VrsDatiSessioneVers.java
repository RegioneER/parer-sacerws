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
import java.util.List;

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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the VRS_DATI_SESSIONE_VERS database table.
 */
@Entity
@Table(name = "VRS_DATI_SESSIONE_VERS")
public class VrsDatiSessioneVers implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idDatiSessioneVers;

    private String cdKeyAlleg;

    private BigDecimal idStrut;

    private BigDecimal niFile;

    private BigDecimal pgDatiSessioneVers;

    private String tiDatiSessioneVers;

    private VrsSessioneVers vrsSessioneVers;

    private List<VrsErrSessioneVers> vrsErrSessioneVers = new ArrayList<>();

    private List<VrsXmlDatiSessioneVers> vrsXmlDatiSessioneVers = new ArrayList<>();

    public VrsDatiSessioneVers() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_DATI_SESSIONE_VERS")
    @GenericGenerator(name = "SVRS_DATI_SESSIONE_VERS_ID_DATI_SESSIONE_VERS_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_DATI_SESSIONE_VERS"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_DATI_SESSIONE_VERS_ID_DATI_SESSIONE_VERS_GENERATOR")
    public Long getIdDatiSessioneVers() {
	return this.idDatiSessioneVers;
    }

    public void setIdDatiSessioneVers(Long idDatiSessioneVers) {
	this.idDatiSessioneVers = idDatiSessioneVers;
    }

    @Column(name = "CD_KEY_ALLEG")
    public String getCdKeyAlleg() {
	return this.cdKeyAlleg;
    }

    public void setCdKeyAlleg(String cdKeyAlleg) {
	this.cdKeyAlleg = cdKeyAlleg;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
	return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
	this.idStrut = idStrut;
    }

    @Column(name = "NI_FILE")
    public BigDecimal getNiFile() {
	return this.niFile;
    }

    public void setNiFile(BigDecimal niFile) {
	this.niFile = niFile;
    }

    @Column(name = "PG_DATI_SESSIONE_VERS")
    public BigDecimal getPgDatiSessioneVers() {
	return this.pgDatiSessioneVers;
    }

    public void setPgDatiSessioneVers(BigDecimal pgDatiSessioneVers) {
	this.pgDatiSessioneVers = pgDatiSessioneVers;
    }

    @Column(name = "TI_DATI_SESSIONE_VERS")
    public String getTiDatiSessioneVers() {
	return this.tiDatiSessioneVers;
    }

    public void setTiDatiSessioneVers(String tiDatiSessioneVers) {
	this.tiDatiSessioneVers = tiDatiSessioneVers;
    }

    // bi-directional many-to-one association to VrsSessioneVers
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SESSIONE_VERS")
    public VrsSessioneVers getVrsSessioneVers() {
	return this.vrsSessioneVers;
    }

    public void setVrsSessioneVers(VrsSessioneVers vrsSessioneVers) {
	this.vrsSessioneVers = vrsSessioneVers;
    }

    // bi-directional many-to-one association to VrsErrSessioneVers
    @OneToMany(mappedBy = "vrsDatiSessioneVers")
    public List<VrsErrSessioneVers> getVrsErrSessioneVers() {
	return this.vrsErrSessioneVers;
    }

    public void setVrsErrSessioneVers(List<VrsErrSessioneVers> vrsErrSessioneVers) {
	this.vrsErrSessioneVers = vrsErrSessioneVers;
    }

    // bi-directional many-to-one association to VrsXmlDatiSessioneVers
    @OneToMany(mappedBy = "vrsDatiSessioneVers")
    public List<VrsXmlDatiSessioneVers> getVrsXmlDatiSessioneVers() {
	return this.vrsXmlDatiSessioneVers;
    }

    public void setVrsXmlDatiSessioneVers(List<VrsXmlDatiSessioneVers> vrsXmlDatiSessioneVers) {
	this.vrsXmlDatiSessioneVers = vrsXmlDatiSessioneVers;
    }

}
