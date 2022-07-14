package it.eng.parer.ws.utils;

import it.eng.parer.entity.AplParamApplic;

//
public class ParametroApplDB {

    public static final String NM_APPLIC = "NM_APPLIC";

    public static final String TIPO_STRUT_DOC = "TIPO_STRUT_DOC";
    public static final String TIPO_COMP_DOC = "TIPO_COMP_DOC";
    public static final String TIPO_SUPPORTO_COMP = "TIPO_SUPPORTO";
    public static final String USO_DATA_FIRMA = "USO_DATA_FIRMA";
    public static final String VERIFICA_PARTIZIONI = "VERIFICA_PARTIZIONI";
    public static final String FMT_UNKNOWN = "FMT_UNKNOWN";
    public static final String SERVER_NAME_SYSTEM_PROPERTY = "SERVER_NAME_SYSTEM_PROPERTY";
    //
    public static final String PATH_MM_IN_ = "PATH_MM_IN_";
    public static final String PATH_MM_OUT_ = "PATH_MM_OUT_";
    //
    public static final String TPI_ROOT_SACER = "root_SACER";
    public static final String TPI_ROOT_TPI = "root_TPI";
    public static final String TPI_ROOT_VERS = "root_vers";
    public static final String TPI_ROOT_ARK_VERS = "root_ark_vers";
    public static final String TPI_ROOT_DA_MIGRARE = "root_da_migrare";
    public static final String TPI_ROOT_MIGRAZ = "root_migraz";
    public static final String TPI_ROOT_ARK_MIGRAZ = "root_ark_migraz";
    public static final String TPI_ROOT_RECUP = "root_recup";
    public static final String TPI_DATA_INIZIO_USO_BLOB = "dataInizioUsoBlob";
    public static final String TPI_DATA_FINE_USO_BLOB = "dataFineUsoBlob";
    public static final String TPI_NM_USER_TPI = "nmUserTPI";
    public static final String TPI_CD_PSW_TPI = "cdPswTPI";
    public static final String TPI_TPI_HOST_URL = "TPI_HostURL";
    public static final String TPI_URL_STATOARKCARTELLE = "URL_StatoArkCartelle";
    public static final String TPI_URL_ELIMINACARTELLAARK = "URL_EliminaCartellaArk";
    public static final String TPI_URL_RETRIEVEFILEUNITADOC = "URL_RetrieveFileUnitaDoc";
    public static final String TPI_URL_REGISTRACARTELLARIARK = "URL_RegistraCartellaRiArk";
    public static final String TPI_URL_SCHEDULAZIONIJOB = "URL_SchedulazioniJobTPI";
    public static final String TPI_NI_GG_MAX_MIGRAZ = "niGgMaxMigraz";
    public static final String TPI_TIMEOUT = "timeoutTPI";
    public static final String TPI_TIMEOUT_RETRIEVE = "timeoutTPIRetrieve";
    //
    // questi verranno rimossi quando la migrazione sarà completata
    public static final String TPI_NM_USER_MIG_BLB = "nmUserMigBlb";
    public static final String TPI_CD_PSW_MIG_BLB = "cdPswMigBlb";
    public static final String TPI_MIG_BLB_WS_URL = "MigBlb_WsURL";
    //
    public static final String IMAGE_ROOT_IMAGE_TRASFORM = "root_image_trasform";

    // Costanti per indice AIP
    public static final String AGENT_PRESERVER_FORMALNAME = "AGENT_PRESERVER_FORMALNAME";
    public static final String AGENT_PRESERVER_TAXCODE = "AGENT_PRESERVER_TAXCODE";
    public static final String AGENT_PRESERVATION_MNGR_TAXCODE = "AGENT_PRESERVATION_MNGR_TAXCODE";
    public static final String AGENT_PRESERVATION_MNGR_LASTNAME = "AGENT_PRESERVATION_MNGR_LASTNAME";
    public static final String AGENT_PRESERVATION_MNGR_FIRSTNAME = "AGENT_PRESERVATION_MNGR_FIRSTNAME";
    public static final String AGENT_PRESERVATION_MNGR_USERNAME = "AGENT_PRESERVATION_MNGR_USERNAME";

    // Costanti per dati applicazione
    public static final String REG_ANNO_VALID_MINIMO = "REG_ANNO_VALID_MINIMO";
    // Costanti per scarto scadenza password utente
    public static final String NUM_GIORNI_ESPONI_SCAD_PSW = "NUM_GIORNI_ESPONI_SCAD_PSW";

    // Costanti per il log dei login ws e la disattivazione automatica utenti
    public static final String IDP_MAX_TENTATIVI_FALLITI = "MAX_TENTATIVI_FALLITI";
    public static final String IDP_MAX_GIORNI = "MAX_GIORNI";
    public static final String IDP_QRY_DISABLE_USER = "QRY_DISABLE_USER";
    public static final String IDP_QRY_VERIFICA_DISATTIVAZIONE_UTENTE = "QRY_VERIFICA_DISATTIVAZIONE_UTENTE";
    public static final String IDP_QRY_REGISTRA_EVENTO_UTENTE = "QRY_REGISTRA_EVENTO_UTENTE";

    // Costati per URN (SISTEMA)
    public static final String NM_SISTEMACONSERVAZIONE = "SISTEMA_CONSERVAZIONE";
    public static final String DATA_INIZIO_CALC_NUOVI_URN = "DATA_INIZIO_CALC_NUOVI_URN";
    public static final String VERSIONI_WS_PREFIX = "VERSIONI_";
    //
    public static final String EIDAS_VERIFICA_FIRMA_ENDPOINT = "EIDAS_VERIFICA_FIRMA_ENDPOINT";

    public static final String CRYPTO_VERIFICA_FIRMA_ENDPOINT = "CRYPTO_VERIFICA_FIRMA_ENDPOINT";

    public static final String VERIFICA_FIRMA_TIMEOUT = "VERIFICA_FIRMA_TIMEOUT";

    /* CRYPTO */

    public static final String CRYPTO_VERIFICA_FIRMA_RETRY_TIMEOUT = "CRYPTO_VERIFICA_FIRMA_RETRY_TIMEOUT";

    public static final String CRYPTO_VERIFICA_FIRMA_MAX_TENTATIVI = "CRYPTO_VERIFICA_FIRMA_MAX_TENTATIVI";

    public static final String CRYPTO_VERIFICA_FIRMA_CIRCUIT_BREAKER_OPEN_TIMEOUT = "CRYPTO_VERIFICA_FIRMA_CIRCUIT_BREAKER_OPEN_TIMEOUT";

    public static final String CRYPTO_VERIFICA_FIRMA_CIRCUIT_BREAKER_RESET_TIMEOUT = "CRYPTO_VERIFICA_FIRMA_CIRCUIT_BREAKER_RESET_TIMEOUT";

    public static final String CRYPTO_VERIFICA_FIRMA_PERIODO_BACKOFF = "CRYPTO_VERIFICA_FIRMA_PERIODO_BACKOFF";

    /* EIDAS */

    public static final String EIDAS_VERIFICA_FIRMA_RETRY_TIMEOUT = "EIDAS_VERIFICA_FIRMA_RETRY_TIMEOUT";

    public static final String EIDAS_VERIFICA_FIRMA_MAX_TENTATIVI = "EIDAS_VERIFICA_FIRMA_MAX_TENTATIVI";

    public static final String EIDAS_VERIFICA_FIRMA_CIRCUIT_BREAKER_OPEN_TIMEOUT = "EIDAS_VERIFICA_FIRMA_CIRCUIT_BREAKER_OPEN_TIMEOUT";

    public static final String EIDAS_VERIFICA_FIRMA_CIRCUIT_BREAKER_RESET_TIMEOUT = "EIDAS_VERIFICA_FIRMA_CIRCUIT_BREAKER_RESET_TIMEOUT";

    public static final String EIDAS_VERIFICA_FIRMA_PERIODO_BACKOFF = "EIDAS_VERIFICA_FIRMA_PERIODO_BACKOFF";

    /* AWS */
    public static final String OBJECT_STORAGE_ADDR = "OBJECT_STORAGE_ADDR";
    public static final String TENANT_OBJECT_STORAGE = "TENANT_OBJECT_STORAGE";

    // Report verifica firma su Object storage
    public static final String REPORTVF_S3_ACCESS_KEY_ID = "REPORTVF_S3_ACCESS_KEY_ID";
    public static final String REPORTVF_S3_SECRET_KEY = "REPORTVF_S3_SECRET_KEY";
    public static final String BUCKET_REPORT_VERIFICAFIRMA = "BUCKET_REPORT_VERIFICAFIRMA";

    /**
     * Flags (specializzazione) {@link AplParamApplic}
     *
     */
    public class ParametroApplFl {

        public static final String FL_GEST_FASCICOLI = "FL_GEST_FASCICOLI";
        public static final String FL_ACCETTA_CONTR_CRL_NOVAL = "FL_ACCETTA_CONTR_CRL_NOVAL";
        public static final String FL_ACCETTA_CONTR_CRL_SCAD = "FL_ACCETTA_CONTR_CRL_SCAD";
        public static final String FL_ACCETTA_FIRMA_NOCONOS = "FL_ACCETTA_FIRMA_NOCONOS";
        public static final String FL_ACCETTA_FIRMA_NOCONF = "FL_ACCETTA_FIRMA_NOCONF";
        public static final String FL_ACCETTA_CONTR_CRL_NEG = "FL_ACCETTA_CONTR_CRL_NEG";
        public static final String FL_ACCETTA_MARCA_NOCONOS = "FL_ACCETTA_MARCA_NOCONOS";
        public static final String FL_ACCETTA_CONTR_CERTIF_SCAD = "FL_ACCETTA_CONTR_CERTIF_SCAD";
        public static final String FL_ACCETTA_CONTR_CERTIF_NOVAL = "FL_ACCETTA_CONTR_CERTIF_NOVAL";
        public static final String FL_ACCETTA_CONTR_CERTIF_NOCERT = "FL_ACCETTA_CONTR_CERTIF_NOCERT";
        public static final String FL_ACCETTA_CONTR_CRITTOG_NEG = "FL_ACCETTA_CONTR_CRITTOG_NEG";
        public static final String FL_ACCETTA_CONTR_TRUST_NEG = "FL_ACCETTA_CONTR_TRUST_NEG";
        public static final String FL_ACCETTA_CONTR_CRL_NOSCAR = "FL_ACCETTA_CONTR_CRL_NOSCAR";
        public static final String FL_ACCETTA_FIRMA_GIUGNO_2011 = "FL_ACCETTA_FIRMA_GIUGNO_2011";
        public static final String FL_ACCETTA_CONTR_FMT_NEG = "FL_ACCETTA_CONTR_FMT_NEG";
        public static final String FL_FORZA_FMT = "FL_FORZA_FMT";
        public static final String FL_ABILITA_CONTR_FMT = "FL_ABILITA_CONTR_FMT";
        public static final String FL_ABILITA_CONTR_FMT_NUM = "FL_ABILITA_CONTR_FMT_NUM";
        public static final String FL_ACCETTA_FMT_NUM_NEG = "FL_ACCETTA_FMT_NUM_NEG";
        public static final String FL_FORZA_FMT_NUM = "FL_FORZA_FMT_NUM";
        public static final String FL_ABILITA_CONTR_HASH_VERS = "FL_ABILITA_CONTR_HASH_VERS";
        public static final String FL_ACCETTA_CONTR_HASH_NEG = "FL_ACCETTA_CONTR_HASH_NEG";
        public static final String FL_FORZA_HASH_VERS = "FL_FORZA_HASH_VERS";
        public static final String FL_ABILITA_UPD_META = "FL_ABILITA_UPD_META";
        public static final String FL_ACCETTA_UPD_META_INARK = "FL_ACCETTA_UPD_META_INARK";
        public static final String FL_FORZA_UPD_META_INARK = "FL_FORZA_UPD_META_INARK";
        public static final String FL_ABILITA_CONTR_REVOCA_VERS = "FL_ABILITA_CONTR_REVOCA_VERS";
        public static final String FL_ABILITA_CONTR_CERTIF_VERS = "FL_ABILITA_CONTR_CERTIF_VERS";
        public static final String FL_ABILITA_CONTR_CRITTOG_VERS = "FL_ABILITA_CONTR_CRITTOG_VERS";
        public static final String FL_ABILITA_CONTR_TRUST_VERS = "FL_ABILITA_CONTR_TRUST_VERS";
        public static final String FL_ABILITA_CONTR_CLASSIF = "FL_ABILITA_CONTR_CLASSIF";
        public static final String FL_FORZA_CONTR_CLASSIF = "FL_FORZA_CONTR_CLASSIF";
        public static final String FL_ACCETTA_CONTR_CLASSIF_NEG = "FL_ACCETTA_CONTR_CLASSIF_NEG";
        public static final String FL_FORZA_CONTR_COLLEG = "FL_FORZA_CONTR_COLLEG";
        public static final String FL_FORZA_COLLEG = "FL_FORZA_COLLEG";
        public static final String FL_ACCETTA_CONTR_COLLEG_NEG = "FL_ACCETTA_CONTR_COLLEG_NEG";
        public static final String FL_ABILITA_CONTR_COLLEG = "FL_ABILITA_CONTR_COLLEG";
        public static final String FL_ABILITA_CONTR_NUMERO = "FL_ABILITA_CONTR_NUMERO";
        public static final String FL_ACCETTA_CONTR_NUMERO_NEG = "FL_ACCETTA_CONTR_NUMERO_NEG";
        public static final String FL_FORZA_CONTR_NUMERO = "FL_FORZA_CONTR_NUMERO";
        public static final String FL_OBBL_OGGETTO = "FL_OBBL_OGGETTO";
        public static final String FL_OBBL_DATA = "FL_OBBL_DATA";
        //
        public static final String FL_ABILITA_FORZA_DOC_AGG = "FL_ABILITA_FORZA_DOC_AGG";
        public static final String FL_FORZA_DOC_AGG = "FL_FORZA_DOC_AGG";
        public static final String FL_ACCETTA_DOC_AGG_NEG = "FL_ACCETTA_DOC_AGG_NEG";
        //
        public static final String FL_ACCETTA_CONTR_COLLEG_NEG_FAS = "FL_ACCETTA_CONTR_COLLEG_NEG_FAS";
        //
        public static final String FL_ABILITA_CONTR_COLLEG_UD = "FL_ABILITA_CONTR_COLLEG_UD";
        //
        public static final String FL_ABILITA_VERIFICA_FIRMA = "FL_ABILITA_VERIFICA_FIRMA";
        public static final String FL_ABILITA_VERIFICA_FIRMA_SOLO_CRYPTO = "FL_ABILITA_VERIFICA_FIRMA_SOLO_CRYPTO";
        //
        public static final String GENERAZIONE_REPORT_VERIFICA_FIRMA = "GENERAZIONE_REPORT_VERIFICA_FIRMA";
        //
        public static final String FL_ABILITA_CONTR_NON_FIRMATI = "FL_ABILITA_CONTR_NON_FIRMATI";
        public static final String FL_ACCETTA_CONTR_NON_FIRMATI_NEG = "FL_ACCETTA_CONTR_NON_FIRMATI_NEG";
        public static final String FL_FORZA_CONTR_NON_FIRMATI_NEG = "FL_FORZA_CONTR_NON_FIRMATI_NEG";

        //
        public static final String FL_FORZA_CONTR_REVOCA_VERS = "FL_FORZA_CONTR_REVOCA_VERS";
        public static final String FL_FORZA_CONTR_TRUST_VERS = "FL_FORZA_CONTR_TRUST_VERS";
        public static final String FL_FORZA_CONTR_CRITTOG_VERS = "FL_FORZA_CONTR_CRITTOG_VERS";
        public static final String FL_FORZA_CONTR_CERTIF_VERS = "FL_FORZA_CONTR_CERTIF_VERS";
        public static final String FL_FORZA_CONTR_NOCONF = "FL_FORZA_CONTR_NOCONF";

        //
        public static final String FL_ACCETTA_CONTR_OCSP_NEG = "FL_ACCETTA_CONTR_OCSP_NEG";
        public static final String FL_ACCETTA_CONTR_OCSP_NOSCAR = "FL_ACCETTA_CONTR_OCSP_NOSCAR";
        public static final String FL_ACCETTA_CONTR_OCSP_NOVAL = "FL_ACCETTA_CONTR_OCSP_NOVAL";

    }

    /**
     * {@link AplParamApplic}
     * 
     */
    public class TipoParametroAppl {

        public static final String VERSAMENTO_DEFAULT = "Default di versamento";
        public static final String MAX_RESULT = "Paginazione risultati";
        public static final String PATH = "Gestione servizi asincroni";
        public static final String TPI = "Salvataggio su nastro";
        public static final String IMAGE = "Trasformazione componenti";
        public static final String LOG_APPLIC = "Log accessi";
        public static final String IAM = "Gestione utenti";
        public static final String TSA = "Firma e Marca";
        public static final String VERSIONI_WS = "Versioni servizi";

    }

    // vista da cui recuperare i valori
    public enum TipoAplVGetValAppart {
        AATIPOFASCICOLO, TIPOUNITADOC, STRUT, AMBIENTE, APPLIC;

        public static TipoAplVGetValAppart next(TipoAplVGetValAppart last) {
            switch (last) {
            case AATIPOFASCICOLO:
                return STRUT;
            case TIPOUNITADOC:
                return STRUT;
            case STRUT:
                return AMBIENTE;
            case AMBIENTE:
                return APPLIC;
            default:
                return null;
            }
        }
    }

}
