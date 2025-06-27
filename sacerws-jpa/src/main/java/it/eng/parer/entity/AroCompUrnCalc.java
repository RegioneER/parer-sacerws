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

import it.eng.parer.entity.constraint.AroCompUrnCalc.TiUrn;

/**
 * The persistent class for the ARO_COMP_URN_CALC database table.
 */
@Entity
@Table(name = "ARO_COMP_URN_CALC")
public class AroCompUrnCalc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCompUrnCalc;

    private String dsUrn;

    private TiUrn tiUrn;

    private AroCompDoc aroCompDoc;

    public AroCompUrnCalc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_COMP_URN_CALC")
    @GenericGenerator(name = "SARO_COMP_URN_CALC_ID_COMP_URN_CALC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_COMP_URN_CALC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_COMP_URN_CALC_ID_COMP_URN_CALC_GENERATOR")
    public Long getIdCompUrnCalc() {
	return this.idCompUrnCalc;
    }

    public void setIdCompUrnCalc(Long idCompUrnCalc) {
	this.idCompUrnCalc = idCompUrnCalc;
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
    public TiUrn getTiUrn() {
	return this.tiUrn;
    }

    public void setTiUrn(TiUrn tiUrn) {
	this.tiUrn = tiUrn;
    }

    // bi-directional many-to-one association to AroCompDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    public AroCompDoc getAroCompDoc() {
	return this.aroCompDoc;
    }

    public void setAroCompDoc(AroCompDoc aroCompDoc) {
	this.aroCompDoc = aroCompDoc;
    }
}
