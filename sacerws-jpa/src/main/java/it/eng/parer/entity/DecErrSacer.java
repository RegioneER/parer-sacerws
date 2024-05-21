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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * @author fioravanti_f
 */
@Entity
@Table(name = "DEC_ERR_SACER")
public class DecErrSacer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idErrSacer;

    private String cdErr;

    private String dsErr;

    private String dsErrFiltro;

    private String tiErrSacer;

    public DecErrSacer() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_ERR_SACER")
    @GenericGenerator(name = "SDEC_ERR_SACER_ID_ERR_SACER_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_ERR_SACER"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_ERR_SACER_ID_ERR_SACER_GENERATOR")
    public Long getIdErrSacer() {
        return idErrSacer;
    }

    public void setIdErrSacer(Long idErrSacer) {
        this.idErrSacer = idErrSacer;
    }

    @Column(name = "CD_ERR")
    public String getCdErr() {
        return cdErr;
    }

    public void setCdErr(String cdErr) {
        this.cdErr = cdErr;
    }

    @Column(name = "DS_ERR")
    public String getDsErr() {
        return dsErr;
    }

    public void setDsErr(String dsErr) {
        this.dsErr = dsErr;
    }

    @Column(name = "DS_ERR_FILTRO")
    public String getDsErrFiltro() {
        return dsErrFiltro;
    }

    public void setDsErrFiltro(String dsErrFiltro) {
        this.dsErrFiltro = dsErrFiltro;
    }

    @Column(name = "TI_ERR_SACER")
    public String getTiErrSacer() {
        return tiErrSacer;
    }

    public void setTiErrSacer(String tiErrSacer) {
        this.tiErrSacer = tiErrSacer;
    }

}
