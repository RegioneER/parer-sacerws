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
package it.eng.parer.entity.constraint;

/**
 *
 * @author gilioli_p
 */
public final class MonContaSesUpdUd {

    private MonContaSesUpdUd() {
    }

    /**
     * Stato Update
     * 
     * ti_stato_udp_ud_ IN ('IN_ATTESA_SCHED', 'NON_SELEZ_SCHED', 'TOTALE')
     */
    public enum TiStatoUdpUdMonContaSesUpdUd {
        IN_ATTESA_SCHED, NON_SELEZ_SCHED, TOTALE
    }

}
