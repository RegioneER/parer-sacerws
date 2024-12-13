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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_USO_MODELLO_XSD_DOC database table.
 *
 */
@Entity
@Table(name = "DEC_USO_MODELLO_XSD_DOC")
public class DecUsoModelloXsdDoc implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idUsoModelloXsdDoc;
    private String flStandard;
    private Date dtIstituz;
    private Date dtSoppres;
    private DecModelloXsdUd decModelloXsdUd;
    private DecTipoDoc decTipoDoc;

    public DecUsoModelloXsdDoc() {
        // hibernate
    }

    @Id
    @GenericGenerator(name = "DEC_USO_MODELLO_XSD_DOC_IDUSOMODELLOXSDDOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_USO_MODELLO_XSD_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEC_USO_MODELLO_XSD_DOC_IDUSOMODELLOXSDDOC_GENERATOR")
    @Column(name = "ID_USO_MODELLO_XSD_DOC")
    public Long getIdUsoModelloXsdDoc() {
        return this.idUsoModelloXsdDoc;
    }

    public void setIdUsoModelloXsdDoc(Long idUsoModelloXsdDoc) {
        this.idUsoModelloXsdDoc = idUsoModelloXsdDoc;
    }

    @Column(name = "FL_STANDARD", columnDefinition = "CHAR")
    public String getFlStandard() {
        return this.flStandard;
    }

    public void setFlStandard(String flStandard) {
        this.flStandard = flStandard;
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

    // bi-directional many-to-one association to DecAaTipoFascicolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MODELLO_XSD_UD")
    public DecModelloXsdUd getDecModelloXsdUd() {
        return this.decModelloXsdUd;
    }

    public void setDecModelloXsdUd(DecModelloXsdUd decModelloXsdUd) {
        this.decModelloXsdUd = decModelloXsdUd;
    }

    // bi-directional many-to-one association to DecModelloXsdFascicolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_DOC")
    @XmlInverseReference(mappedBy = "decUsoModelloXsdDocs")
    public DecTipoDoc getDecTipoDoc() {
        return this.decTipoDoc;
    }

    public void setDecTipoDoc(DecTipoDoc decTipoDoc) {
        this.decTipoDoc = decTipoDoc;
    }

}
