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
 *
 * @author Gilioli_P
 */
public class SIOrgEnteSiam {

    private SIOrgEnteSiam() {
    }

    public static String[] tiCdEnteConvenz = { "Altro", "IPA Ente/AOO", "IPA Ente", "ISTAT" };

    /**
     * Tipo ente siam ti_ente_siam IN ('CONVENZIONATO', 'NON_CONVENZIONATO')
     */
    public enum TiEnteSiam {
        CONVENZIONATO, NON_CONVENZIONATO
    }

    /**
     * Tipo ente convenzionato ti_ente_convenz IN ('AMMINISTRATORE', 'CONSERVATORE', 'GESTORE', 'PRODUTTORE')
     */
    public enum TiEnteConvenz {
        AMMINISTRATORE, CONSERVATORE, GESTORE, PRODUTTORE
    }

    /**
     * Tipo ente non convenzionato ti_ente_non_convenz IN ('FORNITORE_ESTERNO', 'ORGANO_VIGILANZA', 'VERSATORE_ESTERNO')
     */
    public enum TiEnteNonConvenz {
        FORNITORE_ESTERNO, ORGANO_VIGILANZA, VERSATORE_ESTERNO
    }
}
