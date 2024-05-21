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

import javax.persistence.CascadeType;
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
 * The persistent class for the ARO_CONTROFIRMA_FIRMA database table.
 */
@Entity
@Table(name = "ARO_CONTROFIRMA_FIRMA")
public class AroControfirmaFirma implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idControfirmaFirma;

    private AroFirmaComp aroFirmaFiglio;

    private AroFirmaComp aroFirmaPadre;

    public AroControfirmaFirma() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CONTROFIRMA_FIRMA")
    @GenericGenerator(name = "SARO_CONTROFIRMA_FIRMA_ID_CONTROFIRMA_FIRMA_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_CONTROFIRMA_FIRMA"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_CONTROFIRMA_FIRMA_ID_CONTROFIRMA_FIRMA_GENERATOR")
    public Long getIdControfirmaFirma() {
        return this.idControfirmaFirma;
    }

    public void setIdControfirmaFirma(Long idControfirmaFirma) {
        this.idControfirmaFirma = idControfirmaFirma;
    }

    // bi-directional many-to-one association to AroFirmaComp
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FIRMA_FIGLIO")
    public AroFirmaComp getAroFirmaFiglio() {
        return this.aroFirmaFiglio;
    }

    public void setAroFirmaFiglio(AroFirmaComp aroFirmaFiglio) {
        this.aroFirmaFiglio = aroFirmaFiglio;
    }

    // bi-directional many-to-one association to AroFirmaComp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FIRMA_PADRE")
    public AroFirmaComp getAroFirmaPadre() {
        return this.aroFirmaPadre;
    }

    public void setAroFirmaPadre(AroFirmaComp aroFirmaPadre) {
        this.aroFirmaPadre = aroFirmaPadre;
    }

}
