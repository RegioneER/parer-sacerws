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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.MonContaSesUpdUd.TiStatoUdpUdMonContaSesUpdUd;

/**
 * The persistent class for the MON_CONTA_SES_UPD_UD database table.
 */
@Entity
@Table(name = "MON_CONTA_SES_UPD_UD")
public class MonContaSesUpdUd implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idContaSesUpdUd;

    private Date dtRifConta;

    private BigDecimal niSesUpdUd;

    private MonKeyTotalUd monKeyTotalUd;

    private TiStatoUdpUdMonContaSesUpdUd tiStatoUdpUd;

    public MonContaSesUpdUd() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CONTA_SES_UPD_UD")
    @GenericGenerator(name = "SMON_CONTA_SES_UPD_UD_ID_CONTA_SES_UPD_UD_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SMON_CONTA_SES_UPD_UD"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SMON_CONTA_SES_UPD_UD_ID_CONTA_SES_UPD_UD_GENERATOR")
    public Long getIdContaSesUpdUd() {
        return this.idContaSesUpdUd;
    }

    public void setIdContaSesUpdUd(Long idContaSesUpdUd) {
        this.idContaSesUpdUd = idContaSesUpdUd;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DT_RIF_CONTA")
    public Date getDtRifConta() {
        return this.dtRifConta;
    }

    public void setDtRifConta(Date dtRifConta) {
        this.dtRifConta = dtRifConta;
    }

    @Column(name = "NI_SES_UPD_UD")
    public BigDecimal getNiSesUpdUd() {
        return this.niSesUpdUd;
    }

    public void setNiSesUpdUd(BigDecimal niSesUpdUd) {
        this.niSesUpdUd = niSesUpdUd;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_KEY_TOTAL_UD")
    public MonKeyTotalUd getMonKeyTotalUd() {
        return this.monKeyTotalUd;
    }

    public void setMonKeyTotalUd(MonKeyTotalUd monKeyTotalUd) {
        this.monKeyTotalUd = monKeyTotalUd;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_STATO_UDP_UD")
    public TiStatoUdpUdMonContaSesUpdUd getTiStatoUdpUd() {
        return this.tiStatoUdpUd;
    }

    public void setTiStatoUdpUd(TiStatoUdpUdMonContaSesUpdUd tiStatoUdpUd) {
        this.tiStatoUdpUd = tiStatoUdpUd;
    }
}
