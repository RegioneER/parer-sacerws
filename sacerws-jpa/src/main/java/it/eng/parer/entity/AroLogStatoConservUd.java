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
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * The persistent class for the ARO_LOG_STATO_CONSERV_UD database table.
 *
 */
@Entity
@Table(name = "ARO_LOG_STATO_CONSERV_UD")
@NamedQuery(name = "AroLogStatoConservUd.findAll", query = "SELECT a FROM AroLogStatoConservUd a")
public class AroLogStatoConservUd implements Serializable {
    private static final long serialVersionUID = 1L;
    private long idLogStatoConservUd;
    private BigDecimal aaKeyUnitaDoc;
    private Date dtStato;
    private String nmAgente;
    private String tiEvento;
    private String tiMod;
    private String tiStatoConservazione;
    private AroUnitaDoc aroUnitaDoc;
    private OrgSubStrut orgSubStrut;

    public AroLogStatoConservUd() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LOG_STATO_CONSERV_UD")
    public long getIdLogStatoConservUd() {
        return this.idLogStatoConservUd;
    }

    public void setIdLogStatoConservUd(long idLogStatoConservUd) {
        this.idLogStatoConservUd = idLogStatoConservUd;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
        return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DT_STATO")
    public Date getDtStato() {
        return this.dtStato;
    }

    public void setDtStato(Date dtStato) {
        this.dtStato = dtStato;
    }

    // bi-directional many-to-one association to OrgSubStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SUB_STRUT")
    public OrgSubStrut getOrgSubStrut() {
        return this.orgSubStrut;
    }

    public void setOrgSubStrut(OrgSubStrut orgSubStrut) {
        this.orgSubStrut = orgSubStrut;
    }

    @Column(name = "NM_AGENTE")
    public String getNmAgente() {
        return this.nmAgente;
    }

    public void setNmAgente(String nmAgente) {
        this.nmAgente = nmAgente;
    }

    @Column(name = "TI_EVENTO")
    public String getTiEvento() {
        return this.tiEvento;
    }

    public void setTiEvento(String tiEvento) {
        this.tiEvento = tiEvento;
    }

    @Column(name = "TI_MOD")
    public String getTiMod() {
        return this.tiMod;
    }

    public void setTiMod(String tiMod) {
        this.tiMod = tiMod;
    }

    @Column(name = "TI_STATO_CONSERVAZIONE")
    public String getTiStatoConservazione() {
        return this.tiStatoConservazione;
    }

    public void setTiStatoConservazione(String tiStatoConservazione) {
        this.tiStatoConservazione = tiStatoConservazione;
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
