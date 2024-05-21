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

package it.eng.parer.entity.constraint;

/**
 * ElvStatoElencoVers constraint
 *
 * @author DiLorenzo_F
 */
public final class ElvStatoElencoVer {

    private ElvStatoElencoVer() {
    }

    public enum TiStatoElenco {
        APERTO, CHIUSO, COMPLETATO, DA_CHIUDERE, ELENCO_INDICI_AIP_CREATO, ELENCO_INDICI_AIP_ERR_MARCA,
        ELENCO_INDICI_AIP_FIRMATO, ELENCO_INDICI_AIP_FIRMA_IN_CORSO, FIRMA_IN_CORSO, FIRME_VERIFICATE_DT_VERS,
        INDICI_AIP_GENERATI, IN_CODA_INDICE_AIP, IN_CODA_JMS_INDICE_AIP_DA_ELAB, IN_CODA_JMS_VERIFICA_FIRME_DT_VERS,
        VALIDATO

    }
}
