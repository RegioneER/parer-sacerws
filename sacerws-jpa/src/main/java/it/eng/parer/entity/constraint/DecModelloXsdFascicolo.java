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
 * DecModelloXsdFascicolo's constraint
 * 
 */
public final class DecModelloXsdFascicolo {

    private DecModelloXsdFascicolo() {
    }

    /**
     * Tipo modello Xsd fascicolo
     */
    public enum TiModelloXsd {
        FASCICOLO, PROFILO_GENERALE_FASCICOLO, PROFILO_ARCHIVISTICO_FASCICOLO, PROFILO_SPECIFICO_FASCICOLO,
        AIP_SELF_DESCRIPTION_MORE_INFO, AIP_UNISYNCRO, PROFILO_NORMATIVO_FASCICOLO
    }

    /**
     * Tipo uso modello Xsd fascicolo
     */
    public enum TiUsoModelloXsd {
        MIGRAZ, VERS
    }

}
