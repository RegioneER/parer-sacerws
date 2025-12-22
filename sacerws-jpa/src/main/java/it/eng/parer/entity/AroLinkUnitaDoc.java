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
 * The persistent class for the ARO_LINK_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_LINK_UNITA_DOC")
public class AroLinkUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idLinkUnitaDoc;

    private BigDecimal aaKeyUnitaDocLink;

    private String cdKeyUnitaDocLink;

    private String cdRegistroKeyUnitaDocLink;

    private String dsLinkUnitaDoc;

    private BigDecimal idStrut;

    private AroUnitaDoc aroUnitaDocLink;

    private AroUnitaDoc aroUnitaDoc;

    public AroLinkUnitaDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_LINK_UNITA_DOC")
    @GenericGenerator(name = "SARO_LINK_UNITA_DOC_ID_LINK_UNITA_DOC_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_LINK_UNITA_DOC"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_LINK_UNITA_DOC_ID_LINK_UNITA_DOC_GENERATOR")
    public Long getIdLinkUnitaDoc() {
        return this.idLinkUnitaDoc;
    }

    public void setIdLinkUnitaDoc(Long idLinkUnitaDoc) {
        this.idLinkUnitaDoc = idLinkUnitaDoc;
    }

    @Column(name = "AA_KEY_UNITA_DOC_LINK")
    public BigDecimal getAaKeyUnitaDocLink() {
        return this.aaKeyUnitaDocLink;
    }

    public void setAaKeyUnitaDocLink(BigDecimal aaKeyUnitaDocLink) {
        this.aaKeyUnitaDocLink = aaKeyUnitaDocLink;
    }

    @Column(name = "CD_KEY_UNITA_DOC_LINK")
    public String getCdKeyUnitaDocLink() {
        return this.cdKeyUnitaDocLink;
    }

    public void setCdKeyUnitaDocLink(String cdKeyUnitaDocLink) {
        this.cdKeyUnitaDocLink = cdKeyUnitaDocLink;
    }

    @Column(name = "CD_REGISTRO_KEY_UNITA_DOC_LINK")
    public String getCdRegistroKeyUnitaDocLink() {
        return this.cdRegistroKeyUnitaDocLink;
    }

    public void setCdRegistroKeyUnitaDocLink(String cdRegistroKeyUnitaDocLink) {
        this.cdRegistroKeyUnitaDocLink = cdRegistroKeyUnitaDocLink;
    }

    @Column(name = "DS_LINK_UNITA_DOC")
    public String getDsLinkUnitaDoc() {
        return this.dsLinkUnitaDoc;
    }

    public void setDsLinkUnitaDoc(String dsLinkUnitaDoc) {
        this.dsLinkUnitaDoc = dsLinkUnitaDoc;
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
    @JoinColumn(name = "ID_UNITA_DOC_LINK")
    public AroUnitaDoc getAroUnitaDocLink() {
        return this.aroUnitaDocLink;
    }

    public void setAroUnitaDocLink(AroUnitaDoc aroUnitaDocLink) {
        this.aroUnitaDocLink = aroUnitaDocLink;
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
