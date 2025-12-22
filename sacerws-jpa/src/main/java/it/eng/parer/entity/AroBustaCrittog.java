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
 * The persistent class for the ARO_BUSTA_CRITTOG database table.
 */
@Entity
@Table(name = "ARO_BUSTA_CRITTOG")
public class AroBustaCrittog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idBustaCrittog;

    private BigDecimal idStrut;

    private BigDecimal pgBustaCrittog;

    private AroCompDoc aroCompDoc;

    private DecFormatoFileStandard decFormatoFileStandard;

    private List<AroFirmaComp> aroFirmaComps = new ArrayList<>();

    private List<AroMarcaComp> aroMarcaComps = new ArrayList<>();

    public AroBustaCrittog() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_BUSTA_CRITTOG")
    @GenericGenerator(name = "SARO_BUSTA_CRITTOG_ID_BUSTA_CRITTOG_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_BUSTA_CRITTOG"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_BUSTA_CRITTOG_ID_BUSTA_CRITTOG_GENERATOR")
    public Long getIdBustaCrittog() {
        return this.idBustaCrittog;
    }

    public void setIdBustaCrittog(Long idBustaCrittog) {
        this.idBustaCrittog = idBustaCrittog;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "PG_BUSTA_CRITTOG")
    public BigDecimal getPgBustaCrittog() {
        return this.pgBustaCrittog;
    }

    public void setPgBustaCrittog(BigDecimal pgBustaCrittog) {
        this.pgBustaCrittog = pgBustaCrittog;
    }

    // bi-directional many-to-one association to AroCompDoc
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID_COMP_DOC", nullable = false)
    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public void setAroCompDoc(AroCompDoc aroCompDoc) {
        this.aroCompDoc = aroCompDoc;
    }

    // bi-directional many-to-one association to DecFormatoFileStandard
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FORMATO_FILE_CALC")
    public DecFormatoFileStandard getDecFormatoFileStandard() {
        return this.decFormatoFileStandard;
    }

    public void setDecFormatoFileStandard(DecFormatoFileStandard decFormatoFileStandard) {
        this.decFormatoFileStandard = decFormatoFileStandard;
    }

    // bi-directional many-to-one association to AroFirmaComp
    @OneToMany(mappedBy = "aroBustaCrittog")
    public List<AroFirmaComp> getAroFirmaComps() {
        return this.aroFirmaComps;
    }

    public void setAroFirmaComps(List<AroFirmaComp> aroFirmaComps) {
        this.aroFirmaComps = aroFirmaComps;
    }

    // bi-directional many-to-one association to AroMarcaComp
    @OneToMany(mappedBy = "aroBustaCrittog")
    public List<AroMarcaComp> getAroMarcaComps() {
        return this.aroMarcaComps;
    }

    public void setAroMarcaComps(List<AroMarcaComp> aroMarcaComps) {
        this.aroMarcaComps = aroMarcaComps;
    }

}
