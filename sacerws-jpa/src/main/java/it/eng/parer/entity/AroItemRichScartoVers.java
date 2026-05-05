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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the ARO_ITEM_RICH_SCARTO_VERS database table.
 *
 */
@Entity
@Table(name = "ARO_ITEM_RICH_SCARTO_VERS")
@NamedQuery(name = "AroItemRichScartoVers.findAll", query = "SELECT a FROM AroItemRichScartoVers a")
public class AroItemRichScartoVers implements Serializable {
    private static final long serialVersionUID = 1L;
    private long idItemRichScartoVers;
    private BigDecimal aaKeyUnitaDoc;
    private String cdKeyUnitaDoc;
    private String cdRegistroKeyUnitaDoc;
    private String dmSoftDelete;
    private BigDecimal idStrut;
    private BigDecimal pgItemRichScartoVers;
    private String tiStatoItemScarto;
    private Timestamp tsSoftDelete;
    private AroRichScartoVers aroRichScartoVers;
    private AroUnitaDoc aroUnitaDoc;
    private List<AroErrRichScartoVers> aroErrRichScartoVers = new ArrayList<>();

    public AroItemRichScartoVers() {
        /* Hibernate */
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ITEM_RICH_SCARTO_VERS")
    public long getIdItemRichScartoVers() {
        return this.idItemRichScartoVers;
    }

    public void setIdItemRichScartoVers(long idItemRichScartoVers) {
        this.idItemRichScartoVers = idItemRichScartoVers;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
        return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Column(name = "CD_KEY_UNITA_DOC")
    public String getCdKeyUnitaDoc() {
        return this.cdKeyUnitaDoc;
    }

    public void setCdKeyUnitaDoc(String cdKeyUnitaDoc) {
        this.cdKeyUnitaDoc = cdKeyUnitaDoc;
    }

    @Column(name = "CD_REGISTRO_KEY_UNITA_DOC")
    public String getCdRegistroKeyUnitaDoc() {
        return this.cdRegistroKeyUnitaDoc;
    }

    public void setCdRegistroKeyUnitaDoc(String cdRegistroKeyUnitaDoc) {
        this.cdRegistroKeyUnitaDoc = cdRegistroKeyUnitaDoc;
    }

    @Column(name = "DM_SOFT_DELETE")
    public String getDmSoftDelete() {
        return this.dmSoftDelete;
    }

    public void setDmSoftDelete(String dmSoftDelete) {
        this.dmSoftDelete = dmSoftDelete;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "PG_ITEM_RICH_SCARTO_VERS")
    public BigDecimal getPgItemRichScartoVers() {
        return this.pgItemRichScartoVers;
    }

    public void setPgItemRichScartoVers(BigDecimal pgItemRichScartoVers) {
        this.pgItemRichScartoVers = pgItemRichScartoVers;
    }

    @Column(name = "TI_STATO_ITEM_SCARTO")
    public String getTiStatoItemScarto() {
        return this.tiStatoItemScarto;
    }

    public void setTiStatoItemScarto(String tiStatoItemScarto) {
        this.tiStatoItemScarto = tiStatoItemScarto;
    }

    @Column(name = "TS_SOFT_DELETE")
    public Timestamp getTsSoftDelete() {
        return this.tsSoftDelete;
    }

    public void setTsSoftDelete(Timestamp tsSoftDelete) {
        this.tsSoftDelete = tsSoftDelete;
    }

    // bi-directional many-to-one association to AroRichScartoVer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICH_SCARTO_VERS")
    public AroRichScartoVers getAroRichScartoVers() {
        return this.aroRichScartoVers;
    }

    public void setAroRichScartoVers(AroRichScartoVers aroRichScartoVers) {
        this.aroRichScartoVers = aroRichScartoVers;
    }

    @OneToMany(mappedBy = "aroItemRichScartoVers", cascade = CascadeType.PERSIST)
    public List<AroErrRichScartoVers> getAroErrRichScartoVers() {
        return this.aroErrRichScartoVers;
    }

    public void setAroErrRichScartoVers(List<AroErrRichScartoVers> aroErrRichScartoVers) {
        this.aroErrRichScartoVers = aroErrRichScartoVers;
    }

    public AroErrRichScartoVers addAroErrRichScartoVers(AroErrRichScartoVers aroErrRichScartoVers) {
        getAroErrRichScartoVers().add(aroErrRichScartoVers);
        aroErrRichScartoVers.setAroItemRichScartoVers(this);
        return aroErrRichScartoVers;
    }

    public AroErrRichScartoVers removeAroErrRichScartoVers(
            AroErrRichScartoVers aroErrRichScartoVers) {
        getAroErrRichScartoVers().remove(aroErrRichScartoVers);
        aroErrRichScartoVers.setAroItemRichScartoVers(null);
        return aroErrRichScartoVers;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

    public void setAroUnitaDoc(AroUnitaDoc aroUnitaDoc) {
        this.aroUnitaDoc = aroUnitaDoc;
    }

}
