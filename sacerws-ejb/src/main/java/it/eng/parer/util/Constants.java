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

    public final static int PASSWORD_EXPIRATION_DAYS = 90;
    public final static String SACER = "SACER";
    public final static String SACERWS = "SACERWS";
    // Constants for Transformer
    public final static String ENTITY_PACKAGE_NAME = "it.eng.parer.entity";
    public final static String GRANTED_ENTITY_PACKAGE_NAME = "it.eng.parer.grantedEntity";
    public final static String VIEWENTITY_PACKAGE_NAME = "it.eng.parer.viewEntity";
    public final static String GRANTED_VIEWENTITY_PACKAGE_NAME = "it.eng.parer.grantedViewEntity";
    public final static String ROWBEAN_PACKAGE_NAME = "it.eng.parer.slite.gen.tablebean";
    public final static String VIEWROWBEAN_PACKAGE_NAME = "it.eng.parer.slite.gen.viewbean";
    // Costanti per lista "Totale Definito Da" nella gestione dei dati specifici
    public final static String TI_UNI_DOC = "Tipo unità doc.";
    public final static String TI_DOC = "Tipo doc.";
    public final static String TI_SIS_MIGR_UD = "Migraz. unità doc.";
    public final static String TI_SIS_MIGR_DOC = "Migraz. doc.";
    // Costanti per dati specifici
    public final static String TI_USO_XSD_VERS = "VERS";
    public final static String TI_USO_XSD_MIGR = "MIGRAZ";

    // Formato data/ora
    public static final String DATE_FORMAT_TIMESTAMP_TYPE = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_DATE_TYPE = "dd/MM/yyyy";
    public static final String DATE_FORMAT_HOUR_MINUTE_TYPE = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT_DAY_MONTH_TYPE = "dd/MM";
    public static final String DATE_FORMAT_DATE_COMPACT_TYPE = "dd/MM/yy";

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
