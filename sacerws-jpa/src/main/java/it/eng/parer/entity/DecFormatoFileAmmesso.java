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
import javax.xml.bind.annotation.XmlID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_FORMATO_FILE_AMMESSO database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_FORMATO_FILE_AMMESSO")
public class DecFormatoFileAmmesso implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFormatoFileAmmesso;

    private DecFormatoFileDoc decFormatoFileDoc;

    private DecTipoCompDoc decTipoCompDoc;

    public DecFormatoFileAmmesso() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_FORMATO_FILE_AMMESSO")
    @XmlID
    @GenericGenerator(name = "SDEC_FORMATO_FILE_AMMESSO_ID_FORMATO_FILE_AMMESSO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_FORMATO_FILE_AMMESSO"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_FORMATO_FILE_AMMESSO_ID_FORMATO_FILE_AMMESSO_GENERATOR")
    public Long getIdFormatoFileAmmesso() {
	return this.idFormatoFileAmmesso;
    }

    public void setIdFormatoFileAmmesso(Long idFormatoFileAmmesso) {
	this.idFormatoFileAmmesso = idFormatoFileAmmesso;
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

    // bi-directional many-to-one association to DecTipoCompDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_COMP_DOC")
    public DecTipoCompDoc getDecTipoCompDoc() {
	return this.decTipoCompDoc;
    }

    public void setDecTipoCompDoc(DecTipoCompDoc decTipoCompDoc) {
	this.decTipoCompDoc = decTipoCompDoc;
    }

}
