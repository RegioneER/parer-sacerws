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
 * The persistent class for the FAS_SOG_FASCICOLO database table.
 */
@Entity
@Table(name = "FAS_SOG_FASCICOLO")
public class FasSogFascicolo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idSogFascicolo;

    private String cdSog;

    private String dsDenomSog;

    private String nmCognSog;

    private String nmNomeSog;

    private String tiCdSog;

    private String tiRapp;

    private FasFascicolo fasFascicolo;

    public FasSogFascicolo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_SOG_FASCICOLO")
    @GenericGenerator(name = "SFAS_SOG_FASCICOLO_ID_SOG_FASCICOLO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFAS_SOG_FASCICOLO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFAS_SOG_FASCICOLO_ID_SOG_FASCICOLO_GENERATOR")
    public Long getIdSogFascicolo() {
        return this.idSogFascicolo;
    }

    public void setIdSogFascicolo(Long idSogFascicolo) {
        this.idSogFascicolo = idSogFascicolo;
    }

    @Column(name = "CD_SOG")
    public String getCdSog() {
        return this.cdSog;
    }

    public void setCdSog(String cdSog) {
        this.cdSog = cdSog;
    }

    @Column(name = "DS_DENOM_SOG")
    public String getDsDenomSog() {
        return this.dsDenomSog;
    }

    public void setDsDenomSog(String dsDenomSog) {
        this.dsDenomSog = dsDenomSog;
    }

    @Column(name = "NM_COGN_SOG")
    public String getNmCognSog() {
        return this.nmCognSog;
    }

    public void setNmCognSog(String nmCognSog) {
        this.nmCognSog = nmCognSog;
    }

    @Column(name = "NM_NOME_SOG")
    public String getNmNomeSog() {
        return this.nmNomeSog;
    }

    public void setNmNomeSog(String nmNomeSog) {
        this.nmNomeSog = nmNomeSog;
    }

    @Column(name = "TI_CD_SOG")
    public String getTiCdSog() {
        return this.tiCdSog;
    }

    public void setTiCdSog(String tiCdSog) {
        this.tiCdSog = tiCdSog;
    }

    @Column(name = "TI_RAPP")
    public String getTiRapp() {
        return this.tiRapp;
    }

    public void setTiRapp(String tiRapp) {
        this.tiRapp = tiRapp;
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
