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
 * The persistent class for the ARO_CONTR_FIRMA_COMP database table.
 */
@Entity
@Table(name = "ARO_CONTR_FIRMA_COMP")
public class AroContrFirmaComp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idContrFirmaComp;

    private String dsMsgEsitoContrFirma;

    private String tiContr;

    private String tiEsitoContrFirma;

    private AroFirmaComp aroFirmaComp;

    private FirCrl firCrl;
    private FirOcsp firOcsp;
    private List<AroUsoCertifCaContrComp> aroUsoCertifCaContrComps = new ArrayList<>();

    public AroContrFirmaComp() {
        // hibernate
    }

    @Id
    @Column(name = "ID_CONTR_FIRMA_COMP")
    @GenericGenerator(name = "SARO_CONTR_FIRMA_COMP_ID_CONTR_FIRMA_COMP_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SARO_CONTR_FIRMA_COMP"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SARO_CONTR_FIRMA_COMP_ID_CONTR_FIRMA_COMP_GENERATOR")
    public Long getIdContrFirmaComp() {
        return this.idContrFirmaComp;
    }

    public void setIdContrFirmaComp(Long idContrFirmaComp) {
        this.idContrFirmaComp = idContrFirmaComp;
    }

    @Column(name = "DS_MSG_ESITO_CONTR_FIRMA")
    public String getDsMsgEsitoContrFirma() {
        return this.dsMsgEsitoContrFirma;
    }

    public void setDsMsgEsitoContrFirma(String dsMsgEsitoContrFirma) {
        this.dsMsgEsitoContrFirma = dsMsgEsitoContrFirma;
    }

    @Column(name = "TI_CONTR")
    public String getTiContr() {
        return this.tiContr;
    }

    public void setTiContr(String tiContr) {
        this.tiContr = tiContr;
    }

    @Column(name = "TI_ESITO_CONTR_FIRMA")
    public String getTiEsitoContrFirma() {
        return this.tiEsitoContrFirma;
    }

    public void setTiEsitoContrFirma(String tiEsitoContrFirma) {
        this.tiEsitoContrFirma = tiEsitoContrFirma;
    }

    // bi-directional many-to-one association to AroFirmaComp
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID_FIRMA_COMP", nullable = false)
    public AroFirmaComp getAroFirmaComp() {
        return this.aroFirmaComp;
    }

    public void setAroFirmaComp(AroFirmaComp aroFirmaComp) {
        this.aroFirmaComp = aroFirmaComp;
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

    // bi-directional many-to-one association to AroUsoCertifCaContrComp
    @OneToMany(mappedBy = "aroContrFirmaComp", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,
            CascadeType.REFRESH })
    public List<AroUsoCertifCaContrComp> getAroUsoCertifCaContrComps() {
        return this.aroUsoCertifCaContrComps;
    }

    public void setAroUsoCertifCaContrComps(List<AroUsoCertifCaContrComp> aroUsoCertifCaContrComps) {
        this.aroUsoCertifCaContrComps = aroUsoCertifCaContrComps;
    }

}
