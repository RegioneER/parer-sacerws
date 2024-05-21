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
 * The persistent class for the ARO_CONTR_MARCA_COMP database table.
 */
@Entity
@Table(name = "ARO_CONTR_MARCA_COMP")
public class AroContrMarcaComp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idContrMarcaComp;

    private String dsMsgEsitoContrMarca;

    private String tiContr;

    private String tiEsitoContrMarca;

    private AroMarcaComp aroMarcaComp;

    private FirCrl firCrl;

    private FirOcsp firOcsp;

    private List<AroUsoCertifCaContrMarca> aroUsoCertifCaContrMarcas = new ArrayList<>();

    public AroContrMarcaComp() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CONTR_MARCA_COMP")
    @GenericGenerator(name = "SARO_CONTR_MARCA_COMP_ID_CONTR_MARCA_COMP_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_CONTR_MARCA_COMP"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_CONTR_MARCA_COMP_ID_CONTR_MARCA_COMP_GENERATOR")
    public Long getIdContrMarcaComp() {
        return this.idContrMarcaComp;
    }

    public void setIdContrMarcaComp(Long idContrMarcaComp) {
        this.idContrMarcaComp = idContrMarcaComp;
    }

    @Column(name = "DS_MSG_ESITO_CONTR_MARCA")
    public String getDsMsgEsitoContrMarca() {
        return this.dsMsgEsitoContrMarca;
    }

    public void setDsMsgEsitoContrMarca(String dsMsgEsitoContrMarca) {
        this.dsMsgEsitoContrMarca = dsMsgEsitoContrMarca;
    }

    @Column(name = "TI_CONTR")
    public String getTiContr() {
        return this.tiContr;
    }

    public void setTiContr(String tiContr) {
        this.tiContr = tiContr;
    }

    @Column(name = "TI_ESITO_CONTR_MARCA")
    public String getTiEsitoContrMarca() {
        return this.tiEsitoContrMarca;
    }

    public void setTiEsitoContrMarca(String tiEsitoContrMarca) {
        this.tiEsitoContrMarca = tiEsitoContrMarca;
    }

    // bi-directional many-to-one association to AroMarcaComp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MARCA_COMP", nullable = false)
    public AroMarcaComp getAroMarcaComp() {
        return this.aroMarcaComp;
    }

    public void setAroMarcaComp(AroMarcaComp aroMarcaComp) {
        this.aroMarcaComp = aroMarcaComp;
    }

    // uni-directional many-to-one association to FirCrl
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CRL_USATA")
    public FirCrl getFirCrl() {
        return this.firCrl;
    }

    public void setFirCrl(FirCrl firCrl) {
        this.firCrl = firCrl;
    }

    // uni-directional many-to-one association to FirCrl
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_OCSP_USATA")
    public FirOcsp getFirOcsp() {
        return this.firOcsp;
    }

    public void setFirOcsp(FirOcsp firOcsp) {
        this.firOcsp = firOcsp;
    }

    // bi-directional many-to-one association to AroUsoCertifCaContrMarca
    @OneToMany(mappedBy = "aroContrMarcaComp", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REFRESH })
    public List<AroUsoCertifCaContrMarca> getAroUsoCertifCaContrMarcas() {
        return this.aroUsoCertifCaContrMarcas;
    }

    public void setAroUsoCertifCaContrMarcas(List<AroUsoCertifCaContrMarca> aroUsoCertifCaContrMarcas) {
        this.aroUsoCertifCaContrMarcas = aroUsoCertifCaContrMarcas;
    }

}
