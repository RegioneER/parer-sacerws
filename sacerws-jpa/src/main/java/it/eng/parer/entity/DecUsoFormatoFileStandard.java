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

import javax.persistence.Cacheable;
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
 * The persistent class for the DEC_USO_FORMATO_FILE_STANDARD database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_USO_FORMATO_FILE_STANDARD")
public class DecUsoFormatoFileStandard implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUsoFormatoFileAmmesso;

    private BigDecimal niOrdUso;

    private DecFormatoFileDoc decFormatoFileDoc;

    private DecFormatoFileStandard decFormatoFileStandard;

    public DecUsoFormatoFileStandard() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_USO_FORMATO_FILE_AMMESSO")
    @GenericGenerator(name = "SDEC_USO_FORMATO_FILE_STANDARD_ID_USO_FORMATO_FILE_AMMESSO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_USO_FORMATO_FILE_STANDARD"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_USO_FORMATO_FILE_STANDARD_ID_USO_FORMATO_FILE_AMMESSO_GENERATOR")
    public Long getIdUsoFormatoFileAmmesso() {
	return this.idUsoFormatoFileAmmesso;
    }

    public void setIdUsoFormatoFileAmmesso(Long idUsoFormatoFileAmmesso) {
	this.idUsoFormatoFileAmmesso = idUsoFormatoFileAmmesso;
    }

    @Column(name = "NI_ORD_USO")
    public BigDecimal getNiOrdUso() {
	return this.niOrdUso;
    }

    public void setNiOrdUso(BigDecimal niOrdUso) {
	this.niOrdUso = niOrdUso;
    }

    // bi-directional many-to-one association to DecFormatoFileDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FORMATO_FILE_DOC")
    public DecFormatoFileDoc getDecFormatoFileDoc() {
	return this.decFormatoFileDoc;
    }

    public void setDecFormatoFileDoc(DecFormatoFileDoc decFormatoFileDoc) {
	this.decFormatoFileDoc = decFormatoFileDoc;
    }

    // bi-directional many-to-one association to DecFormatoFileStandard
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FORMATO_FILE_STANDARD")
    public DecFormatoFileStandard getDecFormatoFileStandard() {
	return this.decFormatoFileStandard;
    }

    public void setDecFormatoFileStandard(DecFormatoFileStandard decFormatoFileStandard) {
	this.decFormatoFileStandard = decFormatoFileStandard;
    }

}
