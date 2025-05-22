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
import java.util.Date;
import java.util.List;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the VRS_PATH_DT_VERS database table.
 */
@Entity
@Table(name = "VRS_PATH_DT_VERS")
public class VrsPathDtVers implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idPathDtVers;

    private String dlPath;

    private Date dtLastArkPath;

    private Date dtLastArkPathSecondario;

    private String flCancFileMigraz;

    private String flPathArk;

    private String flPathArkSecondario;

    private String flPathFileNoArk;

    private String flPathFileNoArkSecondario;

    private BigDecimal niFilePath;

    private BigDecimal niFilePathArk;

    private BigDecimal niFilePathArkSecondario;

    private VrsDtVers vrsDtVers;

    public VrsPathDtVers() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_PATH_DT_VERS")
    @GenericGenerator(name = "SVRS_PATH_DT_VERS_ID_PATH_DT_VERS_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
	    @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_PATH_DT_VERS"),
	    @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_PATH_DT_VERS_ID_PATH_DT_VERS_GENERATOR")
    public Long getIdPathDtVers() {
	return this.idPathDtVers;
    }

    public void setIdPathDtVers(Long idPathDtVers) {
	this.idPathDtVers = idPathDtVers;
    }

    @Column(name = "DL_PATH")
    public String getDlPath() {
	return this.dlPath;
    }

    public void setDlPath(String dlPath) {
	this.dlPath = dlPath;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_LAST_ARK_PATH")
    public Date getDtLastArkPath() {
	return this.dtLastArkPath;
    }

    public void setDtLastArkPath(Date dtLastArkPath) {
	this.dtLastArkPath = dtLastArkPath;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_LAST_ARK_PATH_SECONDARIO")
    public Date getDtLastArkPathSecondario() {
	return this.dtLastArkPathSecondario;
    }

    public void setDtLastArkPathSecondario(Date dtLastArkPathSecondario) {
	this.dtLastArkPathSecondario = dtLastArkPathSecondario;
    }

    @Column(name = "FL_CANC_FILE_MIGRAZ", columnDefinition = "char(1)")
    public String getFlCancFileMigraz() {
	return this.flCancFileMigraz;
    }

    public void setFlCancFileMigraz(String flCancFileMigraz) {
	this.flCancFileMigraz = flCancFileMigraz;
    }

    @Column(name = "FL_PATH_ARK", columnDefinition = "char(1)")
    public String getFlPathArk() {
	return this.flPathArk;
    }

    public void setFlPathArk(String flPathArk) {
	this.flPathArk = flPathArk;
    }

    @Column(name = "FL_PATH_ARK_SECONDARIO", columnDefinition = "char(1)")
    public String getFlPathArkSecondario() {
	return this.flPathArkSecondario;
    }

    public void setFlPathArkSecondario(String flPathArkSecondario) {
	this.flPathArkSecondario = flPathArkSecondario;
    }

    @Column(name = "FL_PATH_FILE_NO_ARK", columnDefinition = "char(1)")
    public String getFlPathFileNoArk() {
	return this.flPathFileNoArk;
    }

    public void setFlPathFileNoArk(String flPathFileNoArk) {
	this.flPathFileNoArk = flPathFileNoArk;
    }

    @Column(name = "FL_PATH_FILE_NO_ARK_SECONDARIO", columnDefinition = "char(1)")
    public String getFlPathFileNoArkSecondario() {
	return this.flPathFileNoArkSecondario;
    }

    public void setFlPathFileNoArkSecondario(String flPathFileNoArkSecondario) {
	this.flPathFileNoArkSecondario = flPathFileNoArkSecondario;
    }

    @Column(name = "NI_FILE_PATH")
    public BigDecimal getNiFilePath() {
	return this.niFilePath;
    }

    public void setNiFilePath(BigDecimal niFilePath) {
	this.niFilePath = niFilePath;
    }

    @Column(name = "NI_FILE_PATH_ARK")
    public BigDecimal getNiFilePathArk() {
	return this.niFilePathArk;
    }

    public void setNiFilePathArk(BigDecimal niFilePathArk) {
	this.niFilePathArk = niFilePathArk;
    }

    @Column(name = "NI_FILE_PATH_ARK_SECONDARIO")
    public BigDecimal getNiFilePathArkSecondario() {
	return this.niFilePathArkSecondario;
    }

    public void setNiFilePathArkSecondario(BigDecimal niFilePathArkSecondario) {
	this.niFilePathArkSecondario = niFilePathArkSecondario;
    }

    // bi-directional many-to-one association to VrsDtVer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DT_VERS")
    public VrsDtVers getVrsDtVers() {
	return this.vrsDtVers;
    }

    public void setVrsDtVers(VrsDtVers vrsDtVers) {
	this.vrsDtVers = vrsDtVers;
    }
}
