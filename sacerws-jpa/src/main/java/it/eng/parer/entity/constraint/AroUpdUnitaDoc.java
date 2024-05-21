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
 * ARO_UPD_UNITA_DOC's constraint
 *
 * @author sinatti_s
 */
public final class AroUpdUnitaDoc {

    private AroUpdUnitaDoc() {
    }

    /**
     * Stato UD in conservazione
     * 
     * " ti_stato_udp_elenco_vers IN ('IN_ATTESA_SCHED', 'IN_ELENCO_APERTO', 'IN_ELENCO_CHIUSO', 'IN_ELENCO_COMPLETATO',
     * 'IN_ELENCO_CON_INDICI_AIP_GENERATI', 'IN_ELENCO_DA_CHIUDERE', 'IN_ELENCO_VALIDATO',
     * 'IN_ELENCO_IN_CODA_INDICE_AIP', 'NON_SELEZ_SCHED', 'IN_ELENCO_CON_ELENCO_INDICI_AIP_CREATO',
     * 'IN_ELENCO_CON_ELENCO_INDICI_AIP_ERR_MARCA', 'IN_ELENCO_CON_ELENCO_INDICI_AIP_FIRMATO',
     * 'IN_ELENCO_CON_FIRME_VERIFICATE_DT_VERS', 'IN_CODA_JMS_VERIFICA_FIRME_DT_VERS',
     * 'IN_CODA_JMS_INDICE_AIP_DA_ELAB')"
     */
    public enum AroUpdUDTiStatoUpdElencoVers {
        IN_ATTESA_SCHED, IN_ELENCO_APERTO, IN_ELENCO_CHIUSO, IN_ELENCO_COMPLETATO, IN_ELENCO_CON_INDICI_AIP_GENERATI,
        IN_ELENCO_DA_CHIUDERE, IN_ELENCO_VALIDATO, IN_ELENCO_IN_CODA_INDICE_AIP, NON_SELEZ_SCHED,
        IN_ELENCO_CON_ELENCO_INDICI_AIP_CREATO, IN_ELENCO_CON_ELENCO_INDICI_AIP_ERR_MARCA,
        IN_ELENCO_CON_ELENCO_INDICI_AIP_FIRMATO, IN_ELENCO_CON_FIRME_VERIFICATE_DT_VERS,
        IN_CODA_JMS_VERIFICA_FIRME_DT_VERS, IN_CODA_JMS_INDICE_AIP_DA_ELAB
    }
}
