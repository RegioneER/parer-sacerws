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
 * The persistent class for the ARO_INDICE_AIP_UD database table.
 */
@Entity
@Table(name = "ARO_INDICE_AIP_UD")
public class AroIndiceAipUd implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idIndiceAip;
    private BigDecimal idVerIndiceAipLast = BigDecimal.ZERO;
    private String tiFormatoIndiceAip;

    private AroUnitaDoc aroUnitaDoc;

    private List<AroVerIndiceAipUd> aroVerIndiceAipUds = new ArrayList<>();

    public AroIndiceAipUd() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_INDICE_AIP")
    @GenericGenerator(name = "SARO_INDICE_AIP_UD_ID_INDICE_AIP_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_INDICE_AIP_UD"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_INDICE_AIP_UD_ID_INDICE_AIP_GENERATOR")
    public Long getIdIndiceAip() {
        return this.idIndiceAip;
    }

    public void setIdIndiceAip(Long idIndiceAip) {
        this.idIndiceAip = idIndiceAip;
    }

    @Column(name = "ID_VER_INDICE_AIP_LAST")
    public BigDecimal getIdVerIndiceAipLast() {
        return this.idVerIndiceAipLast;
    }

    public void setIdVerIndiceAipLast(BigDecimal idVerIndiceAipLast) {
        this.idVerIndiceAipLast = idVerIndiceAipLast;
    }

    @Column(name = "TI_FORMATO_INDICE_AIP")
    public String getTiFormatoIndiceAip() {
        return this.tiFormatoIndiceAip;
    }

    public void setTiFormatoIndiceAip(String tiFormatoIndiceAip) {
        this.tiFormatoIndiceAip = tiFormatoIndiceAip;
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

    // bi-directional many-to-one association to AroVerIndiceAipUd
    @OneToMany(mappedBy = "aroIndiceAipUd", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    public List<AroVerIndiceAipUd> getAroVerIndiceAipUds() {
        return this.aroVerIndiceAipUds;
    }

    public void setAroVerIndiceAipUds(List<AroVerIndiceAipUd> aroVerIndiceAipUds) {
        this.aroVerIndiceAipUds = aroVerIndiceAipUds;
    }
}
