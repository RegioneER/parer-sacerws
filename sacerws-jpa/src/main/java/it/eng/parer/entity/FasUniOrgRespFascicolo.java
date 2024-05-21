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
 * The persistent class for the FAS_UNI_ORG_RESP_FASCICOLO database table.
 */
@Entity
@Table(name = "FAS_UNI_ORG_RESP_FASCICOLO")
public class FasUniOrgRespFascicolo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUniOrgRespFascicolo;

    private String cdUniOrgResp;

    private FasFascicolo fasFascicolo;

    public FasUniOrgRespFascicolo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UNI_ORG_RESP_FASCICOLO")
    @GenericGenerator(name = "SFAS_UNI_ORG_RESP_FASCICOLO_ID_UNI_ORG_RESP_FASCICOLO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFAS_UNI_ORG_RESP_FASCICOLO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFAS_UNI_ORG_RESP_FASCICOLO_ID_UNI_ORG_RESP_FASCICOLO_GENERATOR")
    public Long getIdUniOrgRespFascicolo() {
        return this.idUniOrgRespFascicolo;
    }

    public void setIdUniOrgRespFascicolo(Long idUniOrgRespFascicolo) {
        this.idUniOrgRespFascicolo = idUniOrgRespFascicolo;
    }

    @Column(name = "CD_UNI_ORG_RESP")
    public String getCdUniOrgResp() {
        return this.cdUniOrgResp;
    }

    public void setCdUniOrgResp(String cdUniOrgResp) {
        this.cdUniOrgResp = cdUniOrgResp;
    }

    // bi-directional many-to-one association to FasFascicolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO")
    public FasFascicolo getFasFascicolo() {
        return this.fasFascicolo;
    }

    public void setFasFascicolo(FasFascicolo fasFascicolo) {
        this.fasFascicolo = fasFascicolo;
    }
}
