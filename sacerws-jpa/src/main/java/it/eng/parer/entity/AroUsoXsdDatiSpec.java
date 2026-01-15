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
 * The persistent class for the ARO_USO_XSD_DATI_SPEC database table.
 */
@Entity
@Table(name = "ARO_USO_XSD_DATI_SPEC")
public class AroUsoXsdDatiSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUsoXsdDatiSpec;

    private BigDecimal idStrut;

    private String tiEntitaSacer;

    private String tiUsoXsd;

    private AroCompDoc aroCompDoc;

    private AroDoc aroDoc;

    private AroUnitaDoc aroUnitaDoc;

    private DecXsdDatiSpec decXsdDatiSpec;

    private List<AroValoreAttribDatiSpec> aroValoreAttribDatiSpecs = new ArrayList<>();

    public AroUsoXsdDatiSpec() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_USO_XSD_DATI_SPEC")
    @GenericGenerator(name = "SARO_USO_XSD_DATI_SPEC_ID_USO_XSD_DATI_SPEC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_USO_XSD_DATI_SPEC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_USO_XSD_DATI_SPEC_ID_USO_XSD_DATI_SPEC_GENERATOR")
    public Long getIdUsoXsdDatiSpec() {
        return this.idUsoXsdDatiSpec;
    }

    public void setIdUsoXsdDatiSpec(Long idUsoXsdDatiSpec) {
        this.idUsoXsdDatiSpec = idUsoXsdDatiSpec;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "TI_ENTITA_SACER")
    public String getTiEntitaSacer() {
        return this.tiEntitaSacer;
    }

    public void setTiEntitaSacer(String tiEntitaSacer) {
        this.tiEntitaSacer = tiEntitaSacer;
    }

    @Column(name = "TI_USO_XSD")
    public String getTiUsoXsd() {
        return this.tiUsoXsd;
    }

    public void setTiUsoXsd(String tiUsoXsd) {
        this.tiUsoXsd = tiUsoXsd;
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

    // bi-directional many-to-one association to DecXsdDatiSpec
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_XSD_DATI_SPEC")
    public DecXsdDatiSpec getDecXsdDatiSpec() {
        return this.decXsdDatiSpec;
    }

    public void setDecXsdDatiSpec(DecXsdDatiSpec decXsdDatiSpec) {
        this.decXsdDatiSpec = decXsdDatiSpec;
    }

    // bi-directional many-to-one association to AroValoreAttribDatiSpec
    @OneToMany(mappedBy = "aroUsoXsdDatiSpec", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<AroValoreAttribDatiSpec> getAroValoreAttribDatiSpecs() {
        return this.aroValoreAttribDatiSpecs;
    }

    public void setAroValoreAttribDatiSpecs(
            List<AroValoreAttribDatiSpec> aroValoreAttribDatiSpecs) {
        this.aroValoreAttribDatiSpecs = aroValoreAttribDatiSpecs;
    }

}
