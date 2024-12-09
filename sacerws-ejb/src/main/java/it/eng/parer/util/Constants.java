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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.util;

/**
 *
 * @author Quaranta_M (40M o "FortyEm")
 */
public class Constants {

    public static final String SACERWS = "SACERWS";
    public static final String JAVAX_PERSISTENCE_LOCK_TIMEOUT = "javax.persistence.lock.timeout";
    public static final String FLAG_TRUE = "1";
    public static final String FLAG_FALSE = "0";

    // Log stato conservazione UD

    public static final String AGGIORNAMENTO_UD = "Aggiornamento unità documentaria";
    public static final String AGGIUNTA_DOCUMENTO = "Aggiunta documento";
    public static final String VERSAMENTO_UD = "Versamento unità documentaria";

    public static final String WS_AGGIORNAMENTO_UD = "WS aggiornamento UD";
    public static final String WS_AGGIUNTA_DOC = "WS aggiunta documenti UD";
    public static final String WS_VERSAMENTO_UD = "WS versamento UD";

    // Enum per tipo sessione
    public enum TipoSessione {

        VERSAMENTO, AGGIUNGI_DOCUMENTO
    }

    // Enum per tipo entità sacer
    public enum TipoEntitaSacer {

        UNI_DOC, DOC, COMP
    }
    // Enum per tipo sistema migrazione

    public enum TipoSisMigr {

        ASC
    }

    // Enum per esito su calcoli nel monitoraggio
    public enum EsitoCalcolo {

        OK, OKNOUPDATE
    }

    // Enum per tipo dato
    public enum TipoDato {

        REGISTRO, TIPO_UNITA_DOC, TIPO_DOC, TIPO_DOC_PRINC, SUB_STRUTTURA, TIPO_FASCICOLO, TIPO_OBJECT
    }

    public enum TiOperReplic {

        INS, MOD, CANC
    }

    public enum TiStatoReplic {

        DA_REPLICARE, REPLICA_OK, REPLICA_NON_POSSIBILE, REPLICA_IN_ERRORE, REPLICA_IN_TIMEOUT
    }

    /*
     * NOTA PAOLO: Probabilmente da "fondere" in un unicop enum con gli altri presenti altrove ma non legati alla parte
     * web
     */
    public enum TiDoc {

        PRINCIPALE, ALLEGATO, ANNESSO, ANNOTAZIONE
    }

}
