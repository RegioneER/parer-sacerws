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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the VRS_DT_VERS database table.
 */
@Entity
@Table(name = "VRS_DT_VERS")
public class VrsDtVers implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idDtVers;

    private String dlPathDtVers;

    private Date dtCreazioneDtVers;

    private Date dtLastArkDtVers;

    private Date dtLastArkDtVersSecondario;

    private Date dtStatoDtVers;

    // MAC#27629
    /*
     * NB. Data mantenuta intenzionalmente con LocalDate per mantenerla compatibile con il TPI NON TOCCARE
     */
    private LocalDate dtVers;
    // end MAC#27629

    private String flArk;

    private String flArkSecondario;

    private String flFileNoArk;

    private String flFileNoArkSecondario;

    private String flMigraz;

    private String flPresenzaSecondario;

    private String tiStatoDtVers;

    private List<VrsPathDtVers> vrsPathDtVers = new ArrayList<>();

    public VrsDtVers() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_DT_VERS")
    @GenericGenerator(name = "SVRS_DT_VERS_ID_DT_VERS_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SVRS_DT_VERS"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVRS_DT_VERS_ID_DT_VERS_GENERATOR")
    public Long getIdDtVers() {
        return this.idDtVers;
    }

    public void setIdDtVers(Long idDtVers) {
        this.idDtVers = idDtVers;
    }

    @Column(name = "DL_PATH_DT_VERS")
    public String getDlPathDtVers() {
        return this.dlPathDtVers;
    }

    public void setDlPathDtVers(String dlPathDtVers) {
        this.dlPathDtVers = dlPathDtVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_CREAZIONE_DT_VERS")
    public Date getDtCreazioneDtVers() {
        return this.dtCreazioneDtVers;
    }

    public void setDtCreazioneDtVers(Date dtCreazioneDtVers) {
        this.dtCreazioneDtVers = dtCreazioneDtVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_LAST_ARK_DT_VERS")
    public Date getDtLastArkDtVers() {
        return this.dtLastArkDtVers;
    }

    public void setDtLastArkDtVers(Date dtLastArkDtVers) {
        this.dtLastArkDtVers = dtLastArkDtVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_LAST_ARK_DT_VERS_SECONDARIO")
    public Date getDtLastArkDtVersSecondario() {
        return this.dtLastArkDtVersSecondario;
    }

    public void setDtLastArkDtVersSecondario(Date dtLastArkDtVersSecondario) {
        this.dtLastArkDtVersSecondario = dtLastArkDtVersSecondario;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_STATO_DT_VERS")
    public Date getDtStatoDtVers() {
        return this.dtStatoDtVers;
    }

    public void setDtStatoDtVers(Date dtStatoDtVers) {
        this.dtStatoDtVers = dtStatoDtVers;
    }

    // MAC#27629
    /*
     * NB. Data mantenuta intenzionalmente con LocalDate per mantenerla compatibile con il TPI NON TOCCARE
     */
    @Column(name = "DT_VERS")
    public LocalDate getDtVers() {
        return this.dtVers;
    }

    public void setDtVers(LocalDate dtVers) {
        this.dtVers = dtVers;
    }
    // end MAC#27629

    @Column(name = "FL_ARK", columnDefinition = "char(1)")
    public String getFlArk() {
        return this.flArk;
    }

    public void setFlArk(String flArk) {
        this.flArk = flArk;
    }

    @Column(name = "FL_ARK_SECONDARIO", columnDefinition = "char(1)")
    public String getFlArkSecondario() {
        return this.flArkSecondario;
    }

    public void setFlArkSecondario(String flArkSecondario) {
        this.flArkSecondario = flArkSecondario;
    }

    @Column(name = "FL_FILE_NO_ARK", columnDefinition = "char(1)")
    public String getFlFileNoArk() {
        return this.flFileNoArk;
    }

    public void setFlFileNoArk(String flFileNoArk) {
        this.flFileNoArk = flFileNoArk;
    }

    @Column(name = "FL_FILE_NO_ARK_SECONDARIO", columnDefinition = "char(1)")
    public String getFlFileNoArkSecondario() {
        return this.flFileNoArkSecondario;
    }

    public void setFlFileNoArkSecondario(String flFileNoArkSecondario) {
        this.flFileNoArkSecondario = flFileNoArkSecondario;
    }

    @Column(name = "FL_MIGRAZ", columnDefinition = "char(1)")
    public String getFlMigraz() {
        return this.flMigraz;
    }

    public void setFlMigraz(String flMigraz) {
        this.flMigraz = flMigraz;
    }

    @Column(name = "FL_PRESENZA_SECONDARIO", columnDefinition = "char(1)")
    public String getFlPresenzaSecondario() {
        return this.flPresenzaSecondario;
    }

    public void setFlPresenzaSecondario(String flPresenzaSecondario) {
        this.flPresenzaSecondario = flPresenzaSecondario;
    }

    @Column(name = "TI_STATO_DT_VERS")
    public String getTiStatoDtVers() {
        return this.tiStatoDtVers;
    }

    public void setTiStatoDtVers(String tiStatoDtVers) {
        this.tiStatoDtVers = tiStatoDtVers;
    }

    // bi-directional many-to-one association to VrsPathDtVer
    @OneToMany(mappedBy = "vrsDtVers")
    public List<VrsPathDtVers> getVrsPathDtVers() {
        return this.vrsPathDtVers;
    }

    public void setVrsPathDtVers(List<VrsPathDtVers> vrsPathDtVers) {
        this.vrsPathDtVers = vrsPathDtVers;
    }
}
