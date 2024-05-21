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
 * FasFascicolo's constraint
 *
 * @author Moretti_Lu
 */
public final class FasFascicolo {

    private FasFascicolo() {
    }

    /**
     * Tipo di conservazione
     */
    public enum TiConservazione {
        IN_ARCHIVIO, MIGRAZIONE, VERSAMENTO_ANTICIPATO
    }

    /**
     * Stato di conservazione
     */
    public enum TiStatoConservazione {
        PRESA_IN_CARICO, AIP_IN_AGGIORNAMENTO, AIP_GENERATO, ANNULLATO, IN_ARCHIVIO, VERSAMENTO_IN_ARCHIVIO
    }

    /**
     * Stato di conservazione con stato &lt;&gt; ANNULLATO
     */
    public enum TiStatoConservazioneNonAnnullato {
        PRESA_IN_CARICO, AIP_IN_AGGIORNAMENTO, AIP_GENERATO, IN_ARCHIVIO, VERSAMENTO_IN_ARCHIVIO
    }

    // /**
    // * Stato dei fascicoli in conservazione
    // */
    // public enum TiStatoFascElencoVers {
    // IN_ATTESA_SCHED,
    // IN_ELENCO_APERTO,
    // IN_ELENCO_CHIUSO,
    // IN_ELENCO_CON_INDICI_AIP_GENERATI,
    // IN_ELENCO_DA_CHIUDERE,
    // IN_ELENCO_FIRMATO,
    // IN_ELENCO_IN_CODA_INDICE_AIP,
    // NON_SELEZ_SCHED
    // }

    /**
     * Stato dei fascicoli in conservazione
     */
    public enum TiStatoFascElencoVers {
        IN_ATTESA_SCHED, IN_ELENCO_APERTO, IN_ELENCO_CHIUSO, IN_ELENCO_COMPLETATO, IN_ELENCO_CON_AIP_CREATO,
        IN_ELENCO_CON_ELENCO_INDICI_AIP_CREATO, IN_ELENCO_DA_CHIUDERE, IN_ELENCO_FIRMATO,
        IN_ELENCO_IN_CODA_CREAZIONE_AIP, NON_SELEZ_SCHED
    }

}
