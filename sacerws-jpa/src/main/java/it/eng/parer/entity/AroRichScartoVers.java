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
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the ARO_RICH_SCARTO_VERS database table.
 *
 */
@Entity
@Table(name = "ARO_RICH_SCARTO_VERS")
@NamedQuery(name = "AroRichScartoVers.findAll", query = "SELECT a FROM AroRichScartoVers a")
public class AroRichScartoVers implements Serializable {
    private static final long serialVersionUID = 1L;
    private long idRichScartoVers;
    private String cdRichScartoVers;
    private String dmSoftDelete;
    private String dsRichScartoVers;
    private Date dtCreazioneRichScartoVers;
    private BigDecimal idStatoRichScartoVersCor;
    private String ntRichScartoVers;
    private String tiCreazioneRichScartoVers;
    private Timestamp tsSoftDelete;
    private List<AroItemRichScartoVers> aroItemRichScartoVers;
    private OrgStrut orgStrut;
    private List<AroFileRichScartoVers> aroFileRichScartoVers;
    private List<AroStatoRichScartoVers> aroStatoRichScartoVers;
    private List<AroXmlRichScartoVers> aroXmlRichScartoVers;

    public AroRichScartoVers() {
        /* Hibernate */
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RICH_SCARTO_VERS")
    public long getIdRichScartoVers() {
        return this.idRichScartoVers;
    }

    public void setIdRichScartoVers(long idRichScartoVers) {
        this.idRichScartoVers = idRichScartoVers;
    }

    @Column(name = "CD_RICH_SCARTO_VERS")
    public String getCdRichScartoVers() {
        return this.cdRichScartoVers;
    }

    public void setCdRichScartoVers(String cdRichScartoVers) {
        this.cdRichScartoVers = cdRichScartoVers;
    }

    @Column(name = "DM_SOFT_DELETE")
    public String getDmSoftDelete() {
        return this.dmSoftDelete;
    }

    public void setDmSoftDelete(String dmSoftDelete) {
        this.dmSoftDelete = dmSoftDelete;
    }

    @Column(name = "DS_RICH_SCARTO_VERS")
    public String getDsRichScartoVers() {
        return this.dsRichScartoVers;
    }

    public void setDsRichScartoVers(String dsRichScartoVers) {
        this.dsRichScartoVers = dsRichScartoVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_CREAZIONE_RICH_SCARTO_VERS")
    public Date getDtCreazioneRichScartoVers() {
        return this.dtCreazioneRichScartoVers;
    }

    public void setDtCreazioneRichScartoVers(Date dtCreazioneRichScartoVers) {
        this.dtCreazioneRichScartoVers = dtCreazioneRichScartoVers;
    }

    @Column(name = "ID_STATO_RICH_SCARTO_VERS_COR")
    public BigDecimal getIdStatoRichScartoVersCor() {
        return this.idStatoRichScartoVersCor;
    }

    public void setIdStatoRichScartoVersCor(BigDecimal idStatoRichScartoVersCor) {
        this.idStatoRichScartoVersCor = idStatoRichScartoVersCor;
    }

    @Column(name = "NT_RICH_SCARTO_VERS")
    public String getNtRichScartoVers() {
        return this.ntRichScartoVers;
    }

    public void setNtRichScartoVers(String ntRichScartoVers) {
        this.ntRichScartoVers = ntRichScartoVers;
    }

    @Column(name = "TI_CREAZIONE_RICH_SCARTO_VERS")
    public String getTiCreazioneRichScartoVers() {
        return this.tiCreazioneRichScartoVers;
    }

    public void setTiCreazioneRichScartoVers(String tiCreazioneRichScartoVers) {
        this.tiCreazioneRichScartoVers = tiCreazioneRichScartoVers;
    }

    @Column(name = "TS_SOFT_DELETE")
    public Timestamp getTsSoftDelete() {
        return this.tsSoftDelete;
    }

    public void setTsSoftDelete(Timestamp tsSoftDelete) {
        this.tsSoftDelete = tsSoftDelete;
    }

    // bi-directional many-to-one association to AroItemRichScartoVers
    @OneToMany(mappedBy = "aroRichScartoVers")
    public List<AroItemRichScartoVers> getAroItemRichScartoVers() {
        return this.aroItemRichScartoVers;
    }

    public void setAroItemRichScartoVers(List<AroItemRichScartoVers> aroItemRichScartoVers) {
        this.aroItemRichScartoVers = aroItemRichScartoVers;
    }

    public AroItemRichScartoVers addAroItemRichScartoVers(
            AroItemRichScartoVers aroItemRichScartoVers) {
        getAroItemRichScartoVers().add(aroItemRichScartoVers);
        aroItemRichScartoVers.setAroRichScartoVers(this);
        return aroItemRichScartoVers;
    }

    public AroItemRichScartoVers removeAroItemRichScartoVer(
            AroItemRichScartoVers aroItemRichScartoVers) {
        getAroItemRichScartoVers().remove(aroItemRichScartoVers);
        aroItemRichScartoVers.setAroRichScartoVers(null);
        return aroItemRichScartoVers;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to AroFileRichScartoVers
    @OneToMany(mappedBy = "aroRichScartoVers")
    public List<AroFileRichScartoVers> getAroFileRichScartoVers() {
        return this.aroFileRichScartoVers;
    }

    public void setAroFileRichScartoVers(List<AroFileRichScartoVers> aroFileRichScartoVers) {
        this.aroFileRichScartoVers = aroFileRichScartoVers;
    }

    public AroFileRichScartoVers addAroFileRichScartoVers(
            AroFileRichScartoVers aroFileRichScartoVers) {
        getAroFileRichScartoVers().add(aroFileRichScartoVers);
        aroFileRichScartoVers.setAroRichScartoVers(this);
        return aroFileRichScartoVers;
    }

    public AroFileRichScartoVers removeAroFileRichScartoVers(
            AroFileRichScartoVers aroFileRichScartoVers) {
        getAroFileRichScartoVers().remove(aroFileRichScartoVers);
        aroFileRichScartoVers.setAroRichScartoVers(null);
        return aroFileRichScartoVers;
    }

    // bi-directional many-to-one association to AroStatoRichScartoVers
    @OneToMany(mappedBy = "aroRichScartoVers")
    public List<AroStatoRichScartoVers> getAroStatoRichScartoVers() {
        return this.aroStatoRichScartoVers;
    }

    public void setAroStatoRichScartoVers(List<AroStatoRichScartoVers> aroStatoRichScartoVers) {
        this.aroStatoRichScartoVers = aroStatoRichScartoVers;
    }

    public AroStatoRichScartoVers addAroStatoRichScartoVers(
            AroStatoRichScartoVers aroStatoRichScartoVer) {
        getAroStatoRichScartoVers().add(aroStatoRichScartoVer);
        aroStatoRichScartoVer.setAroRichScartoVers(this);
        return aroStatoRichScartoVer;
    }

    public AroStatoRichScartoVers removeAroStatoRichScartoVers(
            AroStatoRichScartoVers aroStatoRichScartoVer) {
        getAroStatoRichScartoVers().remove(aroStatoRichScartoVer);
        aroStatoRichScartoVer.setAroRichScartoVers(null);
        return aroStatoRichScartoVer;
    }

    // bi-directional many-to-one association to AroXmlRichScartoVers
    @OneToMany(mappedBy = "aroRichScartoVers")
    public List<AroXmlRichScartoVers> getAroXmlRichScartoVers() {
        return this.aroXmlRichScartoVers;
    }

    public void setAroXmlRichScartoVers(List<AroXmlRichScartoVers> aroXmlRichScartoVers) {
        this.aroXmlRichScartoVers = aroXmlRichScartoVers;
    }
}
