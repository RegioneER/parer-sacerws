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

import it.eng.parer.entity.constraint.MonContaSesUpdUdKo.TiStatoUdpUdKoMonContaSesUpdUdKo;

/**
 * The persistent class for the MON_CONTA_SES_UPD_UD_KO database table.
 */
@Entity
@Table(name = "MON_CONTA_SES_UPD_UD_KO")
public class MonContaSesUpdUdKo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idContaSesUpdUdKo;

    private Date dtRifConta;

    private BigDecimal niSesUpdUdKo;

    private TiStatoUdpUdKoMonContaSesUpdUdKo tiStatoUdpUdKo;

    private MonKeyTotalUdKo monKeyTotalUdKo;

    public MonContaSesUpdUdKo() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CONTA_SES_UPD_UD_KO")
    @GenericGenerator(name = "SMON_CONTA_SES_UPD_UD_KO_ID_CONTA_SES_UPD_UD_KO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SMON_CONTA_SES_UPD_UD_KO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SMON_CONTA_SES_UPD_UD_KO_ID_CONTA_SES_UPD_UD_KO_GENERATOR")
    public Long getIdContaSesUpdUdKo() {
        return this.idContaSesUpdUdKo;
    }

    public void setIdContaSesUpdUdKo(Long idContaSesUpdUdKo) {
        this.idContaSesUpdUdKo = idContaSesUpdUdKo;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DT_RIF_CONTA")
    public Date getDtRifConta() {
        return this.dtRifConta;
    }

    public void setDtRifConta(Date dtRifConta) {
        this.dtRifConta = dtRifConta;
    }

    @Column(name = "NI_SES_UPD_UD_KO")
    public BigDecimal getNiSesUpdUdKo() {
        return this.niSesUpdUdKo;
    }

    public void setNiSesUpdUdKo(BigDecimal niSesUpdUdKo) {
        this.niSesUpdUdKo = niSesUpdUdKo;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_STATO_UDP_UD_KO")
    public TiStatoUdpUdKoMonContaSesUpdUdKo getTiStatoUdpUdKo() {
        return this.tiStatoUdpUdKo;
    }

    public void setTiStatoUdpUdKo(TiStatoUdpUdKoMonContaSesUpdUdKo tiStatoUdpUdKo) {
        this.tiStatoUdpUdKo = tiStatoUdpUdKo;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_KEY_TOTAL_UD_KO")
    public MonKeyTotalUdKo getMonKeyTotalUdKo() {
        return this.monKeyTotalUdKo;
    }

    public void setMonKeyTotalUdKo(MonKeyTotalUdKo monKeyTotalUdKo) {
        this.monKeyTotalUdKo = monKeyTotalUdKo;
    }

}
