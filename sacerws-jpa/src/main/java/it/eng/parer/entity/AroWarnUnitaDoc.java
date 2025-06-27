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

/**
 * The persistent class for the ARO_WARN_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_WARN_UNITA_DOC")
public class AroWarnUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idWarnUnitaDoc;

    private String cdWarn;

    private String dsWarn;

    private BigDecimal pgWarnUnitaDoc;

    private String tiEntitaSacer;

    private AroDoc aroDoc;

    private AroUnitaDoc aroUnitaDoc;

    public AroWarnUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_WARN_UNITA_DOC")
    @GenericGenerator(name = "SARO_WARN_UNITA_DOC_ID_WARN_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_WARN_UNITA_DOC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_WARN_UNITA_DOC_ID_WARN_UNITA_DOC_GENERATOR")
    public Long getIdWarnUnitaDoc() {
	return this.idWarnUnitaDoc;
    }

    public void setIdWarnUnitaDoc(Long idWarnUnitaDoc) {
	this.idWarnUnitaDoc = idWarnUnitaDoc;
    }

    @Column(name = "CD_WARN")
    public String getCdWarn() {
	return this.cdWarn;
    }

    public void setCdWarn(String cdWarn) {
	this.cdWarn = cdWarn;
    }

    @Column(name = "DS_WARN")
    public String getDsWarn() {
	return this.dsWarn;
    }

    public void setDsWarn(String dsWarn) {
	this.dsWarn = dsWarn;
    }

    @Column(name = "PG_WARN_UNITA_DOC")
    public BigDecimal getPgWarnUnitaDoc() {
	return this.pgWarnUnitaDoc;
    }

    public void setPgWarnUnitaDoc(BigDecimal pgWarnUnitaDoc) {
	this.pgWarnUnitaDoc = pgWarnUnitaDoc;
    }

    @Column(name = "TI_ENTITA_SACER")
    public String getTiEntitaSacer() {
	return this.tiEntitaSacer;
    }

    public void setTiEntitaSacer(String tiEntitaSacer) {
	this.tiEntitaSacer = tiEntitaSacer;
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

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    public AroUnitaDoc getAroUnitaDoc() {
	return this.aroUnitaDoc;
    }

    public void setAroUnitaDoc(AroUnitaDoc aroUnitaDoc) {
	this.aroUnitaDoc = aroUnitaDoc;
    }
}
