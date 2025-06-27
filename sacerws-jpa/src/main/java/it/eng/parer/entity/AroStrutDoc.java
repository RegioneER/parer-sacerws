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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the ARO_STRUT_DOC database table.
 */
@Entity
@Table(name = "ARO_STRUT_DOC")
public class AroStrutDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idStrutDoc;

    private String flStrutOrig;

    private BigDecimal idStrut;

    private BigDecimal niOrdStrutDoc;

    private List<AroCompDoc> aroCompDocs = new ArrayList<>();

    private AroDoc aroDoc;

    private DecTipoStrutDoc decTipoStrutDoc;

    public AroStrutDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_STRUT_DOC")
    @GenericGenerator(name = "SARO_STRUT_DOC_ID_STRUT_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_STRUT_DOC"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_STRUT_DOC_ID_STRUT_DOC_GENERATOR")
    public Long getIdStrutDoc() {
	return this.idStrutDoc;
    }

    public void setIdStrutDoc(Long idStrutDoc) {
	this.idStrutDoc = idStrutDoc;
    }

    @Column(name = "FL_STRUT_ORIG", columnDefinition = "char(1)")
    public String getFlStrutOrig() {
	return this.flStrutOrig;
    }

    public void setFlStrutOrig(String flStrutOrig) {
	this.flStrutOrig = flStrutOrig;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
	return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
	this.idStrut = idStrut;
    }

    @Column(name = "NI_ORD_STRUT_DOC")
    public BigDecimal getNiOrdStrutDoc() {
	return this.niOrdStrutDoc;
    }

    public void setNiOrdStrutDoc(BigDecimal niOrdStrutDoc) {
	this.niOrdStrutDoc = niOrdStrutDoc;
    }

    // bi-directional many-to-one association to AroCompDoc
    @OneToMany(mappedBy = "aroStrutDoc", cascade = {
	    CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH })
    public List<AroCompDoc> getAroCompDocs() {
	return this.aroCompDocs;
    }

    public void setAroCompDocs(List<AroCompDoc> aroCompDocs) {
	this.aroCompDocs = aroCompDocs;
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

    // bi-directional many-to-one association to DecTipoStrutDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_STRUT_DOC")
    public DecTipoStrutDoc getDecTipoStrutDoc() {
	return this.decTipoStrutDoc;
    }

    public void setDecTipoStrutDoc(DecTipoStrutDoc decTipoStrutDoc) {
	this.decTipoStrutDoc = decTipoStrutDoc;
    }

}
