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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_WARN_UPD_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_WARN_UPD_UNITA_DOC")
public class AroWarnUpdUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idWarnUpdUnitaDoc;

    private String dsErr;

    private DecControlloWs decControlloWs;

    private DecErrSacer decErrSacer;

    private AroUpdUnitaDoc aroUpdUnitaDoc;

    private BigDecimal pgWarn;

    public AroWarnUpdUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_WARN_UPD_UNITA_DOC")
    @GenericGenerator(name = "SARO_WARN_UPD_UNITA_DOC_ID_WARN_UPD_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_WARN_UPD_UNITA_DOC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_WARN_UPD_UNITA_DOC_ID_WARN_UPD_UNITA_DOC_GENERATOR")
    public Long getIdWarnUpdUnitaDoc() {
	return this.idWarnUpdUnitaDoc;
    }

    public void setIdWarnUpdUnitaDoc(Long idWarnUpdUnitaDoc) {
	this.idWarnUpdUnitaDoc = idWarnUpdUnitaDoc;
    }

    @Column(name = "DS_ERR")
    public String getDsErr() {
	return this.dsErr;
    }

    public void setDsErr(String dsErr) {
	this.dsErr = dsErr;
    }

    // bi-directional many-to-one association to DecControlloWs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONTROLLO_WS")
    public DecControlloWs getDecControlloWs() {
	return this.decControlloWs;
    }

    public void setDecControlloWs(DecControlloWs decControlloWs) {
	this.decControlloWs = decControlloWs;
    }

    // bi-directional many-to-one association to DecErrSacer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ERR_SACER")
    public DecErrSacer getDecErrSacer() {
	return this.decErrSacer;
    }

    public void setDecErrSacer(DecErrSacer decErrSacer) {
	this.decErrSacer = decErrSacer;
    }

    // bi-directional one-to-one association to AroUpdUnitaDoc
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_UNITA_DOC")
    public AroUpdUnitaDoc getAroUpdUnitaDoc() {
	return this.aroUpdUnitaDoc;
    }

    public void setAroUpdUnitaDoc(AroUpdUnitaDoc aroUpdUnitaDoc) {
	this.aroUpdUnitaDoc = aroUpdUnitaDoc;
    }

    @Column(name = "PG_WARN")
    public BigDecimal getPgWarn() {
	return this.pgWarn;
    }

    public void setPgWarn(BigDecimal pgWarn) {
	this.pgWarn = pgWarn;
    }

}
