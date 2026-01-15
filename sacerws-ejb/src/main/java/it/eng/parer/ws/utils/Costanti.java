/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.utils;

/**
 *
 * @author Fioravanti_F
 */
public class Costanti {

    //
    /**
     * Si reperiscono da DB (parametri applicativi)
     */
    public static final String WS_VERSAMENTO_VRSN = "1.5";
    public static final String WS_AGGIUNTA_VRSN = "1.4";
    public static final String WS_VERS_FASCICOLO_VRSN = "1.0";
    public static final String WS_AGGIORNAMENTO_VERS_VRSN = "1.5";
    /**
     * Si reperiscono da DB (parametri applicativi)
     */
    public static final String WS_VERSAMENTO_NOME = "VersamentoSync";
    public static final String WS_AGGIUNTA_DOC_NOME = "AggiuntaAllegatiSync";

    public static final String WS_VERS_FASCICOLO_NOME = "VersamentoFascicoloSync";

    public static final String WS_AGGIORNAMENTO_VERS_NOME = "AggiornamentoUnitaDocumentariaSync";

    //
    // NOTA: i servizi Multimedia hanno nome diverso, ma versioni identiche agli
    // omologhi servizi sincroni
    public static final String WS_VERSAMENTO_MM_NOME = "VersamentoMultiMedia";
    public static final String WS_REC_UNITA_DOC_MM_NOME = "RecUniDocMultiMedia";
    public static final String WS_REC_PROVE_CON_MM_NOME = "RecPCUniDocMultiMedia";
    //
    public static final String XML_RAPPORTO_VERS_VRSN = "1.0";

    public class UrnFormatter {

        private UrnFormatter() {
            throw new IllegalStateException("UrnFormatter Utility class");
        }

        public static final char URN_STD_SEPARATOR = ':';

        public static final String VERS_FMT_STRING = "{0}:{1}:{2}";
        public static final String UD_FMT_STRING = "{0}-{1}-{2}";
        public static final String DOC_FMT_STRING = "{0}-{1}";
        //
        public static final String CHIAVE_DOC_FMT_STRING = "{0}-{1}";
        public static final String CHIAVE_COMP_FMT_STRING = "{0}:{1}:{2}";
        //
        public static final String SPATH_DATA_FMT_STRING = "yyyy_MM_dd";
        public static final String SPATH_VERS_FMT_STRING = "{0}-{1}-{2}";
        public static final String SPATH_UD_FMT_STRING = "{0}-{1}-{2}";
        public static final String SPATH_COMP_FMT_STRING = "{0}-{1}-{2}-{3}";
        public static final String SPATH_FILE_FMT_STRING = "{0}/{1}/{2}/{3}/{4}/{5}";
        public static final char SPATH_FILE_STD_SEPARATOR = '-';
        public static final char SPATH_FILE_STD_SEPARATOR_V2 = '_';

        public static final String FNAME_LOG_TSM_RETRIEVE = "OutputTSM_RETRIEVE_{0}-{1}-{2}_{3}-{4}-{5}.txt";

        //
        public static final String URN_UD_FMT_STRING = "{0}:{1}"; // VERS_FMT_STRING + UD_FMT_STRING
        public static final String URN_DOC_FMT_STRING = "{0}:{1}-{2}"; // VERS_FMT_STRING +
        // UD_FMT_STRING +
        // DOC_FMT_STRING

        //
        public static final String URN_DOC_UNI_DOC_FMT_STRING = "urn:{0}"; // URN_UD_FMT_STRING
        // oppure
        // URN_DOC_FMT_STRING
        //
        public static final String URN_INDICE_SIP_FMT_STRING = "urn:IndiceSIP:{0}"; // URN_UD_FMT_STRING
        // oppure
        // URN_DOC_FMT_STRING
        public static final String URN_PI_SIP_FMT_STRING = "urn:PISIP:{0}"; // URN_UD_FMT_STRING
        // oppure
        // URN_DOC_FMT_STRING
        public static final String URN_ESITO_VERS_FMT_STRING = "urn:EsitoVersamento:{0}"; // URN_UD_FMT_STRING
        // oppure
        // URN_DOC_FMT_STRING

        public static final String URN_RAPP_VERS_FMT_STRING = "urn:RapportoVersamento:{0}"; // URN_UD_FMT_STRING
        // oppure
        // URN_DOC_FMT_STRING
        //
        public static final String URN_INDICE_AIP_FMT_STRING = "urn:IndiceAIP-{0}:{1}"; // versione
        // AIP +
        // URN_UD_FMT_STRING

        public static final String URN_INDICE_VOLUME_FMT_STRING = "urn:IndiceVolumeSerie-{0}:{1}";
        public static final String URN_VERS_SERIE_FMT_STRING = "{0}:{1}";
        public static final String URN_INDICE_AIP_SERIE_UD_FMT_STRING = "urn:IndiceAIPSerieUD-{0}:{1}:{2}"; // versione
        // AIP +
        // URN_SERIE_FMT_STRING

        // urn:ElencoVersamento:Ambiente:Ente:Struttura:ID elenco
        public static final String URN_ELENCO_VERSAMENTO_FMT_STRING = "urn:ElencoVersamento:{0}:{1}:{2}:{3}";
        //
        public static final String URN_ELENCO_AIP_FIRMATI_FMT_STRING = "urn:ElencoAIPFirmati:{0}:{1}:{2}:{3}";
        public static final String URN_ELENCO_INDICI_AIP_FMT_STRING = "urn:ElencoIndiciAIP:{0}:{1}:{2}:{3}";
        public static final String URN_FIRMA_ELV_INDICI_AIP_FMT_STRING = "urn:FirmaElencoIndiciAIP:{0}:{1}:{2}:{3}";
        public static final String URN_MARCA_ELV_INDICI_AIP_FMT_STRING = "urn:MarcaElencoIndiciAIP:{0}:{1}:{2}:{3}";

        //
        // FASCICOLI
        //
        public static final String FASC_FMT_STRING = "{0}-{1}";

        public static final String CHIAVE_FASC_FMT_STRING = "{0}:{1}:{2}:{3}-{4}";

        public static final String URN_INDICE_SIP_FASC_FMT_STRING = "urn:IndiceSIP:{0}"; // CHIAVE_FASC_FMT_STRING
        public static final String URN_RAPP_VERS_FASC_FMT_STRING = "urn:RapportoVersamento:{0}"; // CHIAVE_FASC_FMT_STRING
        public static final String URN_RAPP_NEG_FASC_FMT_STRING = "urn:RapportoNegativoVersamento:{0}"; // CHIAVE_FASC_FMT_STRING

        //
        // AGGIORNAMENTO UD
        //
        public static final String UPD_FMT_STRING = "{0}:{1}:{2}"; //
        public static final String UPD_FMT_STRING_V2 = "{0}:{1}:UPD{2}"; //
        public static final String UPD_FMT_STRING_V3 = "{0}:{1}:AGG_MD{2}"; //

        // REPORT VERIFICA COMPONENTE
        public static final String URN_REPORT_FMT_STRING = "{0}:ReportVerificaFirma";
        public static final String URN_REPORT_SIMPLE_FMT_STRING = "{0}:ReportVerificaFirmaEidasSintesi-{1}";
        public static final String URN_REPORT_DETAILED_FMT_STRING = "{0}:ReportVerificaFirmaEidasDettaglio-{1}";
        public static final String URN_REPORT_DIAG_DATA_FMT_STRING = "{0}:ReportVerificaFirmaEidas-{1}";
        public static final String URN_REPORT_CRYPTO_FMT_STRING = "{0}:ReportVerificaFirmaCrypto";
        public static final String URN_REPORT_ORIG_FMT_STRING = "{0}:ReportVerificaFirma";

        // NEW URN FMT
        public static final String URN_DOC_PREFIX = "DOC";

        public static final String URN_INDICE_SIP_V2 = "urn:{0}:IndiceSIP"; //
        public static final String URN_RAPP_VERS_V2 = "urn:{0}:RdV"; //
        public static final String URN_PI_SIP_V2 = "urn:{0}:PISIP"; //
        public static final String URN_ESITO_VERS_V2 = "urn:{0}:EdV"; //
        //
        // MEV#23176
        public static final String URN_SIP_UD = "urn:{0}:SIP-UD"; //
        public static final String URN_SIP_DOC = "urn:{0}:SIP-AGGIUNTA_DOC"; //
        public static final String URN_SIP_UPD = "urn:{0}:SIP-AGGIORNAMENTO_UPD"; //
        public static final String URN_SIP_UPD_V2 = "urn:{0}:SIP-AGG_MD"; //
        // end MEV#23176

        // MEV#25288
        public static final String URN_SIP_FASC = "urn:{0}:SIP-FASCICOLO"; //
        // end MEV#25288
        //
        public static final String DOC_FMT_STRING_V2 = "{0}{1}";
        public static final String URN_DOC_FMT_STRING_V2 = "{0}:{1}:{2}";

        public static final String COMP_FMT_STRING_V2 = "{0}:{1}";
        public static final String URN_COMP_FMT_STRING = "urn:{0}:{1}:{2}";
        //
        public static final String CHIAVE_COMP_FMT_STRING_V2 = "{0}";
        //
        public static final String PAD5DIGITS_FMT = "%05d";
        public static final String PAD2DIGITS_FMT = "%02d";
        public static final String PADNODIGITS_FMT = "%00d";

        // FILENAME (componente)
        public static final String SPATH_COMP_FILENAME_REGEXP = "(" + URN_DOC_PREFIX
                + "([0-9])+:([0-9])+[:[0-9]+]*)"; // filename

    }

    // AWS
    public class S3UrnFormatter {

        private S3UrnFormatter() {
            throw new IllegalStateException("AwsFormatter Utility class");
        }

        // S3 KEYS formatter
        public static final String S3_KEY_UD_FMT = "{0}/{1}/{2}-{3}-{4}"; // ente/struttura/registro-anno-numero
        public static final String S3_KEY_COMP_FMT = "{0}/{1}";
        public static final String S3_KEY_DOC_FMT = "{0}{1}"; // DOCxxxxx
        public static final String S3_KEY_DOC_COMP_FMT = "{0}-{1}";
        public static final String S3_KEY_REPORTVF_FMT = "{0}_REPORTVF";
        public static final String S3_KEY_SIPUD_FMT = "{0}_SIPUD";
        public static final String S3_KEY_SIPDOC_FMT = "{0}_SIPDOC";

        public static final String S3_KEY_DOC_PREFIX = "DOC";
        public static final String S3_KEY_PAD5DIGITS_FMT = "%05d";

        // MAC#37222
        // ente_normalizzato/struttura_normalizzata, es.: Ente_test_di_carico/test_di_carico_00
        public static final String S3_KEY_VERSATORE_FMT = "{0}/{1}";
        // registro_normalizzato-anno-numero_normalizzato, es.:
        // SALVATAGGIO_OS_DB-2023-Test44_1675443130871_9446
        public static final String S3_KEY_CHIAVE_FMT = "{0}-{1}-{2}";
        // ente_normalizzato/struttura_normalizzata/registro_normalizzato-anno-numero_normalizzato_AGGMD00001
        public static final String S3_KEY_AGG_MD_FMT = "{0}/{1}_AGGMD{2}"; //
        // end MAC#37222
    }

    //
    public class S3Constants {

        private S3Constants() {
            throw new IllegalStateException("AwsS3Constants Utility class");
        }

        // custom tags
        public static final String TAG_KEY_VRSOBJ_TYPE = "vrs-object-type";
        public static final String TAG_VALUE_VRSOBJ_FILE_COMP = "file_componente_uddoc";
        public static final String TAG_VALUE_VRSOBJ_METADATI = "xml_metadati_uddoc";
        public static final String TAG_VALUE_VRSOBJ_TMP = "temporany";

        public static final String TAG_KEY_RVF_CDSERV = "rvf-cd-service";
        public static final String TAG_KEY_RVF_NMVERS = "rvf-nm-version";

        // custom metadata
        public static final String MEATADATA_INGEST_NODE = "ingest-node";
        public static final String MEATADATA_INGEST_TIME = "ingest-time";

        // object key prefix
        public static final String KEY_PREFIX_TMP = "tmp";
        public static final String KEY_PREFIX_SIPPUD = "sipud";
        public static final String KEY_PREFIX_COMPUD = "compud";

    }

    //
    public class JMSMsgProperties {

        private JMSMsgProperties() {
            throw new IllegalStateException("JMSMsgProperties Utility class");
        }

        // msg properties
        public static final String MSG_K_PAYLOADTYPE = "tipoPayload";
        public static final String MSG_K_STATUS = "statoElenco";
        public static final String MSG_K_APP = "fromApplication";

    }

    //
    public enum ModificatoriWS {

        TAG_VERIFICA_FORMATI_OLD, TAG_VERIFICA_FORMATI_1_25, TAG_MIGRAZIONE, TAG_DATISPEC_EXT,
        TAG_ESTESI_1_3_OUT, // ID
        // documento,
        // tag
        // Versatore

        TAG_RAPPORTO_VERS_OUT, // questi tag sono legati alla versione 1.4 del versamento
        TAG_LISTA_ERR_OUT, TAG_INFO_FIRME_EXT_OUT, TAG_CONSERV_ANTIC_ARCH_IN,
        //
        TAG_REC_USR_DOC_COMP, // tag di recupero, abilita i tag opzionali di Utente, IdDocumento e
        // NumOrdComponenete
        //
        TAG_ANNUL_FORZA_PING, // tag di annullamento, abilita i tag opzionali di ForzaAnnullamento e
        // RichiestaAPing
        //
        TAG_ABILITA_FORZA_1_5, // tag di verifica hash, aggiunta documento e controllo formati
        // (v1.5)
        //
        TAG_FIRMA_1_5, // tag di flag firma (v1.5)
        //
        TAG_ESTESI_1_5_OUT, // estensione dei tag per v1.5
        TAG_RAPPORTO_VERS_1_5, // tag di estensione del rapporto di versamento (v1.5)
        TAG_VERSATORE_1_5, // tag di estensione del versatore (v1.5)
        // MEV#23176
        TAG_URN_SIP_1_5, // tag per urn dei sip in Esito Versamento e Rapporto di Versamento
        // end MEV#23176
        TAG_PROFILI_1_5,
        // MEV#25288
        TAG_URN_SIP_FASC_1_1, // tag per urn dei sip in Esito Versamento e Rapporto di Versamento
        // dei
        // Fascicoli
        // end MEV#25288
        // MEV#26423
        TAG_PROFILI_UPD_1_6, // tag per ProfiloNormativo servizio aggiornamento versamento
        // MEV#28309
        TAG_DATISPEC_DEL_1_6 // tag per eliminazione DatiSpecifici servizio aggiornamento versamento

    }

    public enum CategoriaDocumento {

        Principale(CostantiDB.TipoDocumento.PRINCIPALE, 1),
        Allegato(CostantiDB.TipoDocumento.ALLEGATO, 2),
        Annotazione(CostantiDB.TipoDocumento.ANNOTAZIONE, 3),
        Annesso(CostantiDB.TipoDocumento.ANNESSO, 4),
        Documento(Costanti.UrnFormatter.URN_DOC_PREFIX);// "categoria"
        // speciale,
        // in
        // quanto
        // utilizzata
        // esclusivamente
        // per
        // il
        // supporto
        // legato
        // alla
        // composizione
        // dell'URN

        private String valore;
        private int ordine;

        private CategoriaDocumento(String val) {
            this.valore = val;
        }

        private CategoriaDocumento(String val, int ordine) {
            this.valore = val;
            this.ordine = ordine;
        }

        public String getValoreDb() {
            return valore;
        }

        public int getOrdine() {
            return ordine;
        }

        public static CategoriaDocumento getEnum(String value) {
            if (value == null) {
                return null;
            }
            for (CategoriaDocumento v : values()) {
                if (value.equalsIgnoreCase(v.getValoreDb())) {
                    return v;
                }
            }
            return null;
        }
    }

    public enum EsitoServizio {

        OK, KO, WARN
    }

    public enum TipiWSPerControlli {
        VERSAMENTO_RECUPERO, ANNULLAMENTO, VERSAMENTO_FASCICOLO, AGGIORNAMENTO_VERSAMENTO
    }

    /**
     * Tutte le versioni permesse trasversali ai servizi Deve essere aggiornata al pari del DB
     *
     */
    public enum VersioneWS {

        V_EMPTY(""), V1_0("1.0"), V1_1("1.1"), V1_2("1.2"), V1_25("1.25"), V1_3("1.3"), V1_4("1.4"),
        V1_5("1.5"), V1_6("1.6");

        private String version;

        private VersioneWS(String version) {
            this.version = version;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        public static VersioneWS evalute(String versione) {
            for (VersioneWS v : values()) {
                if (v.getVersion().equalsIgnoreCase(versione)) {
                    return v;
                }
            }
            return VersioneWS.V_EMPTY;// non esiste in quanto la versione viene sempre controllata
            // prima
        }
    }

    public static final String VERIFICA_FIRMA_METADATI_REPORT_NOVERSION = "NO_VERSION"; // VERS_FMT_STRING
    // +
    // UD_FMT_STRING

    public enum GenReportVerificaFirma {
        OFF, ON, ALL;
    }

    public static final String ERRORE_CLIENT_ERRATO = "QUESTO E' UN ERRORE PROVOCATO DA UN CLIENT SCRITTO MALE: SQLIntegrityConstraintViolationException ";

}
