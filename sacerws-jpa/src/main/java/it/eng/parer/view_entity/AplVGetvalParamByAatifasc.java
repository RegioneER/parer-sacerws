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

package it.eng.parer.view_entity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The persistent class for the APL_V_GETVAL_PARAM_BY_AATIFASC database table.
 *
 */
@Entity
@Table(name = "APL_V_GETVAL_PARAM_BY_AATIFASC")
public class AplVGetvalParamByAatifasc implements Serializable {

    private static final long serialVersionUID = 1L;
    private BigDecimal aaIniTipoFascicolo;
    private String dsValoreParamApplic;
    private BigDecimal idAaTipoFascicolo;
    private BigDecimal idEnte;
    private BigDecimal idParamApplic;
    private BigDecimal idStrut;
    private BigDecimal idTipoFascicolo;
    private BigDecimal idValoreParamApplic;
    private String nmEnte;
    private String nmParamApplic;
    private String nmStrut;
    private String nmTipoFascicolo;
    private String tiAppart;

    public AplVGetvalParamByAatifasc() {/* Hibernate */
    }

    @Column(name = "AA_INI_TIPO_FASCICOLO")
    public BigDecimal getAaIniTipoFascicolo() {
	return this.aaIniTipoFascicolo;
    }

    public void setAaIniTipoFascicolo(BigDecimal aaIniTipoFascicolo) {
	this.aaIniTipoFascicolo = aaIniTipoFascicolo;
    }

    @Column(name = "DS_VALORE_PARAM_APPLIC")
    public String getDsValoreParamApplic() {
	return this.dsValoreParamApplic;
    }

    public void setDsValoreParamApplic(String dsValoreParamApplic) {
	this.dsValoreParamApplic = dsValoreParamApplic;
    }

    @Column(name = "ID_AA_TIPO_FASCICOLO")
    public BigDecimal getIdAaTipoFascicolo() {
	return this.idAaTipoFascicolo;
    }

    public void setIdAaTipoFascicolo(BigDecimal idAaTipoFascicolo) {
	this.idAaTipoFascicolo = idAaTipoFascicolo;
    }

    @Column(name = "ID_ENTE")
    public BigDecimal getIdEnte() {
	return this.idEnte;
    }

    public void setIdEnte(BigDecimal idEnte) {
	this.idEnte = idEnte;
    }

    @Column(name = "ID_PARAM_APPLIC")
    public BigDecimal getIdParamApplic() {
	return this.idParamApplic;
    }

    public void setIdParamApplic(BigDecimal idParamApplic) {
	this.idParamApplic = idParamApplic;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
	return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
	this.idStrut = idStrut;
    }

    @Column(name = "ID_TIPO_FASCICOLO")
    public BigDecimal getIdTipoFascicolo() {
	return this.idTipoFascicolo;
    }

    public void setIdTipoFascicolo(BigDecimal idTipoFascicolo) {
	this.idTipoFascicolo = idTipoFascicolo;
    }

    @Id
    @Column(name = "ID_VALORE_PARAM_APPLIC")
    public BigDecimal getIdValoreParamApplic() {
	return this.idValoreParamApplic;
    }

    public void setIdValoreParamApplic(BigDecimal idValoreParamApplic) {
	this.idValoreParamApplic = idValoreParamApplic;
    }

    @Column(name = "NM_ENTE")
    public String getNmEnte() {
	return this.nmEnte;
    }

    public void setNmEnte(String nmEnte) {
	this.nmEnte = nmEnte;
    }

    @Column(name = "NM_PARAM_APPLIC")
    public String getNmParamApplic() {
	return this.nmParamApplic;
    }

    public void setNmParamApplic(String nmParamApplic) {
	this.nmParamApplic = nmParamApplic;
    }

    @Column(name = "NM_STRUT")
    public String getNmStrut() {
	return this.nmStrut;
    }

    public void setNmStrut(String nmStrut) {
	this.nmStrut = nmStrut;
    }

    @Column(name = "NM_TIPO_FASCICOLO")
    public String getNmTipoFascicolo() {
	return this.nmTipoFascicolo;
    }

    public void setNmTipoFascicolo(String nmTipoFascicolo) {
	this.nmTipoFascicolo = nmTipoFascicolo;
    }

    @Column(name = "TI_APPART")
    public String getTiAppart() {
	return this.tiAppart;
    }

    public void setTiAppart(String tiAppart) {
	this.tiAppart = tiAppart;
    }

}
