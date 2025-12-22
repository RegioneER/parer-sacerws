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
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.AroVersIniDatiSpec.TiEntitaSacerAroVersIniDatiSpec;
import it.eng.parer.entity.constraint.AroVersIniDatiSpec.TiUsoXsdAroVersIniDatiSpec;

/**
 * The persistent class for the ARO_VERS_INI_DATI_SPEC database table.
 */
@Entity
@Table(name = "ARO_VERS_INI_DATI_SPEC")
public class AroVersIniDatiSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idVersIniDatiSpec;

    private String blXmlDatiSpec;

    private LocalDate dtReg;

    private BigDecimal idStrut;

    private AroVersIniComp aroVersIniComp;

    private AroVersIniDoc aroVersIniDoc;

    private AroVersIniUnitaDoc aroVersIniUnitaDoc;

    private DecXsdDatiSpec decXsdDatiSpec;

    private TiEntitaSacerAroVersIniDatiSpec tiEntitaSacer;

    private TiUsoXsdAroVersIniDatiSpec tiUsoXsd;

    private Integer aaDtReg;

    public AroVersIniDatiSpec() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_VERS_INI_DATI_SPEC")
    @GenericGenerator(name = "SARO_VERS_INI_DATI_SPEC_ID_VERS_INI_DATI_SPEC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_VERS_INI_DATI_SPEC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_VERS_INI_DATI_SPEC_ID_VERS_INI_DATI_SPEC_GENERATOR")
    public Long getIdVersIniDatiSpec() {
        return this.idVersIniDatiSpec;
    }

    public void setIdVersIniDatiSpec(Long idVersIniDatiSpec) {
        this.idVersIniDatiSpec = idVersIniDatiSpec;
    }

    @Lob
    @Column(name = "BL_XML_DATI_SPEC")
    public String getBlXmlDatiSpec() {
        return this.blXmlDatiSpec;
    }

    public void setBlXmlDatiSpec(String blXmlDatiSpec) {
        this.blXmlDatiSpec = blXmlDatiSpec;
    }

    @Column(name = "DT_REG")
    public LocalDate getDtReg() {
        return this.dtReg;
    }

    public void setDtReg(LocalDate dtReg) {
        this.dtReg = dtReg;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VERS_INI_COMP")
    public AroVersIniComp getAroVersIniComp() {
        return this.aroVersIniComp;
    }

    public void setAroVersIniComp(AroVersIniComp aroVersIniComp) {
        this.aroVersIniComp = aroVersIniComp;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VERS_INI_DOC")
    public AroVersIniDoc getAroVersIniDoc() {
        return this.aroVersIniDoc;
    }

    public void setAroVersIniDoc(AroVersIniDoc aroVersIniDoc) {
        this.aroVersIniDoc = aroVersIniDoc;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VERS_INI_UNITA_DOC")
    public AroVersIniUnitaDoc getAroVersIniUnitaDoc() {
        return this.aroVersIniUnitaDoc;
    }

    public void setAroVersIniUnitaDoc(AroVersIniUnitaDoc aroVersIniUnitaDoc) {
        this.aroVersIniUnitaDoc = aroVersIniUnitaDoc;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_XSD_DATI_SPEC")
    public DecXsdDatiSpec getDecXsdDatiSpec() {
        return this.decXsdDatiSpec;
    }

    public void setDecXsdDatiSpec(DecXsdDatiSpec decXsdDatiSpec) {
        this.decXsdDatiSpec = decXsdDatiSpec;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_ENTITA_SACER")
    public TiEntitaSacerAroVersIniDatiSpec getTiEntitaSacer() {
        return this.tiEntitaSacer;
    }

    public void setTiEntitaSacer(TiEntitaSacerAroVersIniDatiSpec tiEntitaSacer) {
        this.tiEntitaSacer = tiEntitaSacer;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_USO_XSD")
    public TiUsoXsdAroVersIniDatiSpec getTiUsoXsd() {
        return this.tiUsoXsd;
    }

    public void setTiUsoXsd(TiUsoXsdAroVersIniDatiSpec tiUsoXsd) {
        this.tiUsoXsd = tiUsoXsd;
    }

    @Column(name = "AA_DT_REG")
    public Integer getAaDtReg() {
        return aaDtReg;
    }

    public void setAaDtReg(Integer aaDtReg) {
        this.aaDtReg = aaDtReg;
    }
}
