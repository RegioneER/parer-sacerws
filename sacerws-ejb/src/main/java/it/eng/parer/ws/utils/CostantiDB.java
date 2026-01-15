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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 *
 * @author Fioravanti_F
 */
public class CostantiDB {

    //
    public class DatiCablati {
        private DatiCablati() {
            throw new IllegalStateException("DatiCablati Utility class");
        }

        public static final String TPI_PATH_LISTA_FILE = "ListaFile";
        public static final String VERS_TIPO_CONSERVAZIONE_DEFAULT = "VERSAMENTO_ANTICIPATO";
    }

    //
    public class TipoDocumento {
        private TipoDocumento() {
            throw new IllegalStateException("TipoDocumento Utility class");
        }

        public static final String PRINCIPALE = "PRINCIPALE";
        public static final String ALLEGATO = "ALLEGATO";
        public static final String ANNESSO = "ANNESSO";
        public static final String ANNOTAZIONE = "ANNOTAZIONE";
    }

    //
    public class TipiXmlDati {

        private TipiXmlDati() {
            throw new IllegalStateException("TipiXmlDati Utility class");
        }

        public static final String INDICE_FILE = "INDICE_FILE";
        public static final String RICHIESTA = "RICHIESTA";
        public static final String RISPOSTA = "RISPOSTA";
        public static final String RAPP_VERS = "RAPP_VERS";
    }

    //
    public class TipoUsoComponente {

        private TipoUsoComponente() {
            throw new IllegalStateException("TipoUsoComponente Utility class");
        }

        public static final String CONTENUTO = "CONTENUTO";
        public static final String CONVERTITORE = "CONVERTITORE";
        public static final String FIRMA = "FIRMA";
        public static final String MARCA = "MARCA";
        public static final String RAPPRESENTAZIONE = "RAPPRESENTAZIONE";
        public static final String SEGNATURA = "SEGNATURA";
        public static final String FIRMA_ELETTRONICA = "FIRMA_ELETTRONICA";
    }

    public class SubStruttura {

        private SubStruttura() {
            throw new IllegalStateException("SubStruttura Utility class");
        }

        public static final String DEFAULT_NAME = "DEFAULT";
        public static final String DEFAULT_DESC = "Sub struttura di default per la struttura ";
        public static final String DEFAULT_TEMPLATE_DESC = "Sub struttura di default";
    }

    //
    public enum TipiUsoDatiSpec {

        MIGRAZ, VERS
    }

    public enum TiUsoModelloXsd {
        MIGRAZ, VERS
    }

    public enum TiModelloXsd {
        METADATI_PROFILO, METADATI_SEGNATURA_ARCHIVISTICA, METADATI_SPECIFICI
    }

    public enum TipiStatoElementoVersato {

        IN_ATTESA_MEMORIZZAZIONE, IN_ATTESA_SCHED,
    }

    //
    public enum TipiEntitaSacer {

        UNI_DOC("Unit\u00E0 Documentaria"), DOC("Documento"), COMP("Componente"),
        SUB_COMP("Sottocomponente");

        private String valore;

        private TipiEntitaSacer(String val) {
            this.valore = val;
        }

        public String descrivi() {
            return valore;
        }
    }

    public enum TipiEntitaRecupero {

        UNI_DOC("Unit\u00E0 Documentaria"), DOC("Documento"), COMP("Componente"),
        SUB_COMP("Sottocomponente"),
        //
        UNI_DOC_UNISYNCRO("Unit\u00E0 Documentaria UniSyncro"),
        //
        UNI_DOC_DIP("Unit\u00E0 Documentaria DIP"), DOC_DIP("Documento DIP"),
        COMP_DIP("Componente DIP"),
        //
        UNI_DOC_DIP_ESIBIZIONE("Unit\u00E0 Documentaria DIP per esibizione"),
        DOC_DIP_ESIBIZIONE("Documento DIP per esibizione"),
        COMP_DIP_ESIBIZIONE("Componente DIP per esibizione"),
        // EVO#20972
        PROVE_CONSERV_AIPV2("Prove di conservazione"),
        //
        UNI_DOC_UNISYNCRO_V2("Unit\u00E0 Documentaria UniSyncro v2.0");
        // end EVO#20972

        private String valore;

        private TipiEntitaRecupero(String val) {
            this.valore = val;
        }

        public String descrivi() {
            return valore;
        }
    }

    public enum TipoSalvataggioFile {

        BLOB, FILE
    }

    public enum TipoCampo {

        DATO_PROFILO("Dato di profilo"), DATO_SPEC_DOC_PRINC("Dato specifico documento principale"),
        DATO_SPEC_UNI_DOC("Dato specifico unit\u00E0 documentaria"), SUB_STRUT("Sotto struttura");

        private final String descrizione;

        private TipoCampo(String descrizione) {
            this.descrizione = descrizione;
        }

        public String getDescrizione() {
            return this.descrizione;
        }

        public static TipoCampo[] getEnums(TipoCampo... vals) {
            return vals;
        }

        public static TipoCampo[] getCampiOutSelUd() {
            return getEnums(DATO_PROFILO, DATO_SPEC_DOC_PRINC, DATO_SPEC_UNI_DOC);
        }
    }

    public enum NomeCampo {

        REGISTRO("Registro", 1), ANNO("Anno", 2), NUMERO("Numero", 3),
        KEY_ORD_UD("Ordinamento ud", 4), TIPO_UNITA_DOC("Tipologia unit\u00E0 documentaria", 5),
        DATA_REG("Data unit\u00E0 documentaria", 6), OGGETTO("Oggetto", 7),
        TIPO_DOC_PRINC("Tipo documento principale", 8), DATA_VERS("Data di versamento", 9),
        ANNO_SERIE("Anno della serie", 10), CODICE_SERIE("Codice della serie", 11),
        SUB_STRUTTURA("SUB_STRUTTURA", 12);

        private final String descrizione;
        private final int numeroOrdine;

        private NomeCampo(String descrizione, int numeroOrdine) {
            this.descrizione = descrizione;
            this.numeroOrdine = numeroOrdine;
        }

        public String getDescrizione() {
            return this.descrizione;
        }

        public int getNumeroOrdine() {
            return numeroOrdine;
        }

        public static NomeCampo[] getEnums(NomeCampo... vals) {
            return vals;
        }

        public static NomeCampo[] getComboDatoProfilo() {
            return getEnums(TIPO_UNITA_DOC, REGISTRO, TIPO_DOC_PRINC);
        }

        public static NomeCampo[] getComboSubStruttura() {
            return getEnums(SUB_STRUTTURA);
        }

        public static NomeCampo[] getListaDatoProfilo() {
            return getEnums(REGISTRO, ANNO, NUMERO, KEY_ORD_UD, TIPO_UNITA_DOC, DATA_REG, OGGETTO,
                    TIPO_DOC_PRINC, DATA_VERS, ANNO_SERIE, CODICE_SERIE);
        }

        public static NomeCampo[] getListaDatoProfiloIndividuazione() {
            return getEnums(REGISTRO, ANNO, NUMERO, TIPO_UNITA_DOC, DATA_REG, OGGETTO,
                    TIPO_DOC_PRINC, DATA_VERS);
        }

        public static NomeCampo fromString(String text) {
            NomeCampo result = null;
            for (NomeCampo b : NomeCampo.values()) {
                if (b.descrizione.equals(text)) {
                    result = b;
                    break;
                }
            }
            return result;
        }

        public static NomeCampo byName(String name) {
            NomeCampo result = null;
            for (NomeCampo b : NomeCampo.values()) {
                if (b.name().equals(name)) {
                    result = b;
                    break;
                }
            }
            return result;
        }

        public static NomeCampo[] getListaCampiCsvRichiestaAnnul() {
            return getEnums(REGISTRO, ANNO, NUMERO);
        }

    }

    public enum TipoPartizione {

        UNI_DOC, UNI_DOC_SUB_STRUT, BLOB, FILE, SES, FILE_SES, AIP_UD
    }

    public enum TipiOutputRappr {

        AIP("AIP"), DIP("DIP");

        private final String valore;

        private TipiOutputRappr(String val) {
            this.valore = val;
        }

        @Override
        public String toString() {
            return valore;
        }

        public static TipiOutputRappr[] getEnums(TipiOutputRappr... vals) {
            return vals;
        }

        public static TipiOutputRappr[] getComboTipoOutputRappr() {
            return getEnums(DIP);
        }
    }

    public enum TipoAlgoritmoRappr {

        XSLT("XSLT"),
        //
        ALTRO("Altro");

        private final String valore;

        private TipoAlgoritmoRappr(String val) {
            this.valore = val;
        }

        public static TipoAlgoritmoRappr[] getEnums(TipoAlgoritmoRappr... vals) {
            return vals;
        }

        public static TipoAlgoritmoRappr[] getComboTipoAlgoritmoRappr() {
            return getEnums(ALTRO, XSLT);
        }

        @Override
        public String toString() {
            return valore;
        }

        public static TipoAlgoritmoRappr fromString(String text) {
            if (text != null) {
                for (TipoAlgoritmoRappr b : TipoAlgoritmoRappr.values()) {
                    if (text.equalsIgnoreCase(b.valore)) {
                        return b;
                    }
                }
            }
            return ALTRO;
        }
    }

    public enum StatoFileTrasform {

        ERRATO, INSERITO, MODIFICATO, VERIFICATO
    }

    public enum TipoCreazioneDoc {

        VERSAMENTO_UNITA_DOC, AGGIUNTA_DOCUMENTO
    }

    public enum StatoTitolario {

        VALIDATO, DA_VALIDARE
    }

    public enum TipoFormatoLivelloTitolario {

        NUMERICO, ROMANO, ALFABETICO, ALFANUMERICO;
    }

    public enum TipoOrdinamentoTipiSerie {

        KEY_UD_SERIE("Chiave unit\u00E0 documentaria"), DT_UD_SERIE("Data unit\u00E0 documentaria"),
        DT_KEY_UD_SERIE("Chiave e Data unit\u00E0 documentaria"),
        //
        ALTRO("Altro");

        private final String valore;

        private TipoOrdinamentoTipiSerie(String val) {
            this.valore = val;
        }

        public static TipoOrdinamentoTipiSerie[] getEnums(TipoOrdinamentoTipiSerie... vals) {
            return vals;
        }

        public static TipoOrdinamentoTipiSerie[] getComboTipoOrdinamentoTipiSerie() {
            return getEnums(KEY_UD_SERIE, DT_UD_SERIE, DT_KEY_UD_SERIE);
        }

        @Override
        public String toString() {
            return valore;
        }

        public static TipoOrdinamentoTipiSerie fromString(String text) {
            if (text != null) {
                for (TipoOrdinamentoTipiSerie b : TipoOrdinamentoTipiSerie.values()) {
                    if (text.equalsIgnoreCase(b.valore)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }

    public enum TipoSelUdTipiSerie {

        ANNO_KEY("Anno della chiave delle unit\u00E0 documentarie"),
        DT_UD_SERIE("Range di date di selezione delle unit\u00E0 documentarie"),
        //
        ALTRO("Altro");

        private final String valore;

        private TipoSelUdTipiSerie(String val) {
            this.valore = val;
        }

        public static TipoSelUdTipiSerie[] getEnums(TipoSelUdTipiSerie... vals) {
            return vals;
        }

        public static TipoSelUdTipiSerie[] getComboTipoSelUdTipiSerie() {
            return getEnums(ANNO_KEY, DT_UD_SERIE);
        }

        @Override
        public String toString() {
            return valore;
        }

        public static TipoSelUdTipiSerie fromString(String text) {
            if (text != null) {
                for (TipoSelUdTipiSerie b : TipoSelUdTipiSerie.values()) {
                    if (text.equalsIgnoreCase(b.valore)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }

    public enum TipoFiltroSerieUd {

        TIPO_DOC_PRINC("Tipo di documento principale"),
        // RANGE_DT_UD("Intervallo di date delle unit\u00E0 documentarie"),
        // RANGE_DT_CREA("Intervallo di date di versamento delle unit\u00E0
        // documentarie"),
        //
        ALTRO("Altro");

        private final String valore;

        private TipoFiltroSerieUd(String val) {
            this.valore = val;
        }

        public static TipoFiltroSerieUd[] getEnums(TipoFiltroSerieUd... vals) {
            return vals;
        }

        public static TipoFiltroSerieUd[] getComboTipoDiFiltro() {
            return getEnums(TIPO_DOC_PRINC);
        }

        @Override
        public String toString() {
            return valore;
        }

        public static TipoFiltroSerieUd fromString(String text) {
            if (text != null) {
                for (TipoFiltroSerieUd b : TipoFiltroSerieUd.values()) {
                    if (text.equalsIgnoreCase(b.valore)) {
                        return b;
                    }
                }
            }
            return null;
        }

        public static TipoFiltroSerieUd byName(String text) {
            if (text != null) {
                for (TipoFiltroSerieUd b : TipoFiltroSerieUd.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            return null;
        }
    }

    public enum TipoCreazioneSerie {

        CALCOLO_AUTOMATICO, ACQUISIZIONE_FILE
    }

    public enum TipoChiamataAsync {

        CALCOLO_AUTOMATICO, ACQUISIZIONE_FILE, GENERAZIONE_EFFETTIVO, CONTROLLO_CONTENUTO,
        VALIDAZIONE_SERIE
    }

    public enum StatoVersioneSerie {

        APERTA, APERTURA_IN_CORSO, CONTROLLATA, DA_CONTROLLARE, DA_FIRMARE, DA_VALIDARE, FIRMATA,
        VALIDATA, IN_CUSTODIA, ANNULLATA, VALIDAZIONE_IN_CORSO, FIRMA_IN_CORSO, FIRMATA_NO_MARCA;

        public static StatoVersioneSerie[] getEnums(StatoVersioneSerie... vals) {
            return vals;
        }

        public static StatoVersioneSerie[] getStatiVerSerieAutom() {
            return getEnums(APERTA, DA_CONTROLLARE, CONTROLLATA, DA_VALIDARE);
        }
    }

    public enum StatoVersioneSerieDaElab {

        APERTA, CONTROLLATA, DA_CONTROLLARE, DA_FIRMARE, DA_VALIDARE, FIRMATA, FIRMATA_NO_MARCA,
        VALIDATA

    }

    public enum StatoConservazioneSerie {

        AIP_DA_AGGIORNARE, AIP_GENERATO, AIP_IN_AGGIORNAMENTO, ANNULLATA, IN_ARCHIVIO, IN_CUSTODIA,
        PRESA_IN_CARICO
    }

    public enum StatoConservazioneUnitaDoc {

        ANNULLATA, AIP_DA_GENERARE, AIP_GENERATO, AIP_IN_AGGIORNAMENTO, IN_ARCHIVIO, IN_CUSTODIA,
        IN_VOLUME_DI_CONSERVAZIONE, PRESA_IN_CARICO, VERSAMENTO_IN_ARCHIVIO, AIP_FIRMATO
    }

    public enum TipoAnnullamentoUnitaDoc {

        ANNULLAMENTO, SOSTITUZIONE
    }

    public enum TipoContenSerie {

        UNITA_DOC, UNITA_ARK
    }

    public enum TipoContenutoVerSerie {

        ACQUISITO, CALCOLATO, EFFETTIVO
    }

    public enum StatoContenutoVerSerie {

        CONTROLLATA_CONSIST, CONTROLLO_CONSIST_IN_CORSO, CREATO, CREAZIONE_IN_CORSO,
        DA_CONTROLLARE_CONSIST
    }

    public enum ScopoFileInputVerSerie {

        ACQUISIRE_CONTENUTO, CONTROLLO_CONTENUTO
    }

    public enum TipoDiRappresentazione {

        KEY_UD_SERIE("Chiave delle unit\u00E0 documentarie appartenenti alla serie (OBBLIGATORIO)",
                1),
        DT_UD_SERIE("Data delle unit\u00E0 documentarie appartenenti alla serie (OBBLIGATORIO)", 2),
        INFO_UD_SERIE(
                "Informazioni delle unit\u00E0 documentarie appartenenti alla serie (OBBLIGATORIO)",
                3),
        DS_KEY_ORD_UD_SERIE(
                "Chiave di ordinamento delle unit\u00E0 documentarie appartenenti alla serie (OBBLIGATORIO)",
                4),
        PG_UD_SERIE("Progressivo delle unit\u00E0 documentarie appartenenti alla serie", 5),
        //
        ALTRO("Altro", 10);

        private final String descrizione;
        private final int numeroOrdine;

        private TipoDiRappresentazione(String descrizione, int numeroOrdine) {
            this.descrizione = descrizione;
            this.numeroOrdine = numeroOrdine;
        }

        public static TipoDiRappresentazione[] getEnums(TipoDiRappresentazione... vals) {
            return vals;
        }

        public static TipoDiRappresentazione[] getComboTipoDiRappresentazione() {
            return getEnums(KEY_UD_SERIE, DT_UD_SERIE, INFO_UD_SERIE, DS_KEY_ORD_UD_SERIE,
                    PG_UD_SERIE);
        }

        @Override
        public String toString() {
            return this.descrizione;
        }

        public int getNumeroOrdine() {
            return this.numeroOrdine;
        }

        public static TipoDiRappresentazione fromString(String text) {
            if (text != null) {
                for (TipoDiRappresentazione b : TipoDiRappresentazione.values()) {
                    if (text.equalsIgnoreCase(b.descrizione)) {
                        return b;
                    }
                }
            }
            return null;
        }

        public static TipoDiRappresentazione byName(String text) {
            if (text != null) {
                for (TipoDiRappresentazione b : TipoDiRappresentazione.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            return null;
        }
    }

    public enum TipoTrasformatore {

        OUT_NO_SEP_CHAR("FROM_YYYYMMDD_TO_DATECHAR"),
        OUT_ANY_SEP_CHAR("FROM_YYYY-MM-DD_TO_DATECHAR"), OUT_PAD_CHAR("FROM_CHAR_TO_CHARPADSX"),
        OUT_NO_SEP_TO_YEAR("FROM_YYYYMMDD_TO_YYYY"), OUT_ANY_SEP_TO_YEAR("FROM_YYYY-MM-DD_TO_YYYY"),
        IN_NO_SEP_CHAR("FROM_DATECHAR_TO_YYYYMMDD");

        private final String transformString;

        private TipoTrasformatore(String transformString) {
            this.transformString = transformString;
        }

        public final String getTransformString() {
            return transformString;
        }

        public static TipoTrasformatore[] getEnums(TipoTrasformatore... vals) {
            return vals;
        }

        public static TipoTrasformatore[] getComboTipiOut() {
            return getEnums(OUT_NO_SEP_CHAR, OUT_ANY_SEP_CHAR, OUT_NO_SEP_TO_YEAR,
                    OUT_ANY_SEP_TO_YEAR, OUT_PAD_CHAR);
        }

        public static TipoTrasformatore[] getComboTipiIn() {
            return getEnums(IN_NO_SEP_CHAR);
        }

        public static TipoTrasformatore fromString(String text) {
            if (text != null) {
                for (TipoTrasformatore b : TipoTrasformatore.values()) {
                    if (text.equalsIgnoreCase(b.transformString)) {
                        return b;
                    }
                }
            }
            return null;
        }

        public static TipoTrasformatore byName(String text) {
            if (text != null) {
                for (TipoTrasformatore b : TipoTrasformatore.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            return null;
        }

    }

    // Enum per operatori dati specifici
    public enum TipoOperatoreDatiSpec {

        CONTIENE, INIZIA_PER, DIVERSO, MAGGIORE, MAGGIORE_UGUALE, MINORE, MINORE_UGUALE,
        NON_CONTIENE, NULLO, UGUALE, NON_NULLO, E_UNO_FRA
    }

    public enum TipoErroreFileInputSerie {

        NO_UD, SQL_ERRATA, TROPPE_UD, UD_DOPPIA, CAMPI_NON_VALORIZZATI
    }

    public enum TipoContenutoPerEffettivo {

        CALCOLATO, ACQUISITO, ENTRAMBI
    }

    public enum ModalitaDefPrimaUltimaUd {

        CODICE, PROGRESSIVO, CHIAVE_UD
    }

    public enum TipoGravitaErrore {

        ERRORE, WARNING
    }

    public enum TipoErroreContenuto {

        BUCO_NUMERAZIONE, CHIAVE_DOPPIA, CHIAVE_FINALE, CHIAVE_INIZIALE, CONSISTENZA_NON_DEFINITA,
        LACUNA_ERRATA, PROGRESSIVO_DOPPIO, PROGRESSIVO_NULLO, NUMERO_TOTALE, UD_NON_VERS,
        UD_NON_SELEZIONATE, UD_ANNULLATA, ANOMALIA_ORDINAMENTO, UD_VERSATA_DOPO_CALCOLO,
        CONTENUTO_EFFETTIVO_VUOTO
    }

    public enum TipoOrigineErroreContenuto {
        CONTROLLO, VALIDAZIONE
    }

    public enum TipoLacuna {

        NON_PRODOTTE, MANCANTI
    }

    public enum TipoModLacuna {

        DESCRIZIONE, RANGE_PROGRESSIVI
    }

    public static final BigDecimal NUM_SERIE_36 = BigDecimal.valueOf(0.3);
    public static final BigDecimal NUM_SERIE_24 = BigDecimal.valueOf(0.5);
    public static final BigDecimal NUM_SERIE_12 = new BigDecimal(1);
    public static final BigDecimal NUM_SERIE_6 = new BigDecimal(2);
    public static final BigDecimal NUM_SERIE_4 = new BigDecimal(3);
    public static final BigDecimal NUM_SERIE_3 = new BigDecimal(4);
    public static final BigDecimal NUM_SERIE_2 = new BigDecimal(6);

    public enum IntervalliMeseCreazioneSerie {

        DECADE(CostantiDB.NUM_SERIE_36), QUINDICINA(CostantiDB.NUM_SERIE_24),
        MESE(CostantiDB.NUM_SERIE_12), BIMESTRE(CostantiDB.NUM_SERIE_6),
        TRIMESTRE(CostantiDB.NUM_SERIE_4), QUADRIMESTRE(CostantiDB.NUM_SERIE_3),
        SEMESTRE(CostantiDB.NUM_SERIE_2);

        BigDecimal numSerie;

        private IntervalliMeseCreazioneSerie(BigDecimal numSerie) {
            this.numSerie = numSerie;
        }

        public BigDecimal getNumSerie() {
            return this.numSerie;
        }

    }

    public enum TipoConservazioneSerie {

        FISCALE, IN_ARCHIVIO
    }

    public enum TipoFileVerSerie {

        IX_AIP_UNISINCRO, IX_AIP_UNISINCRO_FIRMATO, MARCA_IX_AIP_UNISINCRO
    }

    public enum TipoCreazioneIndiceAip {

        ANTICIPATO, ARCHIVIO
    }

    public enum TipoNotaSerie {

        NOTE_CONSERVATORE, NOTE_PRODUTTORE
    }

    public enum TipoDefTemplateEnte {

        NO_TEMPLATE("Ente NON template"), TEMPLATE_DEF_AMBIENTE("Ente template"),
        TEMPLATE_DEF_ENTE("Ente con strutture template definite specificatamente per l'ente");

        private String descrizione;

        TipoDefTemplateEnte(String descrizione) {
            this.descrizione = descrizione;
        }

        public String descrizione() {
            return this.descrizione;
        }
    }

    public enum TiPartition {

        AIP_UD, BLOB, FILE, FILE_ELENCHI_VERS, FILE_SER, FILE_SES, FILE_VOL_SER, SES, UNI_DOC,
        XML_SES
    }

    public enum TipiEsitoVerificaHash {

        POSITIVO, NEGATIVO, DISABILITATO, // nel caso la verifica hash non fosse da fare o nel caso
        // di mancata
        // identificazione dell'algoritmo
        NON_EFFETTUATO // nel caso di hash forzato nel versamento MM
    }

    public enum TipiHash {

        SCONOSCIUTO("SCONOSCIUTO", -1), MD5("MD5", 16), SHA_1("SHA-1", 20), SHA_224("SHA-224", 28),
        SHA_256("SHA-256", 32), SHA_384("SHA-384", 48), SHA_512("SHA-512", 64);

        private String desc;
        private int lenght;

        private TipiHash(String ds, int ln) {
            desc = ds;
            lenght = ln;
        }

        public String descrivi() {
            return desc;
        }

        public int lunghezza() {
            return lenght;
        }

        public static TipiHash evaluateByLenght(int lenght) {
            for (TipiHash hash : values()) {
                if (hash.lunghezza() == lenght) {
                    return hash;
                }
            }
            return SCONOSCIUTO;
        }

        public static TipiHash evaluateByDesc(String desc) {
            for (TipiHash hash : values()) {
                if (hash.descrivi().equals(desc)) {
                    return hash;
                }
            }
            return SCONOSCIUTO;
        }
    }

    public enum TipiEncBinari {

        SCONOSCIUTO("SCONOSCIUTO"), HEX_BINARY("hexBinary"), BASE64("Base64");

        private String desc;

        private TipiEncBinari(String ds) {
            desc = ds;
        }

        public String descrivi() {
            return desc;
        }

        public static TipiEncBinari evaluateByDesc(String desc) {
            for (TipiEncBinari bin : values()) {
                /*
                 * equalsIgnoreCase = dato che non esiste una codifica "forte" il chiamante (e.g.
                 * ping che utilizza BASE64 e non Base64 come sui ws, ma non ha importanza la
                 * sintassi quanto la semantica ...)
                 */
                if (bin.descrivi().equalsIgnoreCase(desc)) {
                    return bin;
                }
            }
            return TipiEncBinari.SCONOSCIUTO;
        }
    }

    public enum TipoCreazioneRichAnnulVers {

        ON_LINE, UPLOAD_FILE, WEB_SERVICE
    }

    public enum StatoRichAnnulVers {

        APERTA, CHIUSA, COMUNICATA_A_SACER, EVASA, INVIO_FALLITO, RECUPERATA_DA_PING, RIFIUTATA
    }

    public enum TipoFileRichAnnulVers {

        FILE_UD_ANNUL, XML_RICH, XML_RISP
    }

    public enum StatoItemRichAnnulVers {

        ANNULLATO, DA_ANNULLARE_IN_PING, DA_ANNULLARE_IN_SACER, NON_ANNULLABILE
    }

    public enum TipoErrRichAnnulVers {

        ITEM_GIA_PRESENTE, ITEM_IN_CORSO_DI_ANNUL, ITEM_NON_ESISTE, ITEM_RIFERITO,
        ITEM_VERSATA_IN_DATA_RICH, STATO_CONSERV_NON_AMMESSO;

        public static String[] getStatiControlloItem() {
            // <<<<<<< Ritorna tutti gli stati tranne ITEM_NON_ESISTE e ITEM_GIA_PRESENTE -
            // DA MODIFICARE IN CASO DI
            // AGGIUNTE

            return new String[] {
                    TipoErrRichAnnulVers.ITEM_IN_CORSO_DI_ANNUL.name(),
                    TipoErrRichAnnulVers.ITEM_RIFERITO.name(),
                    TipoErrRichAnnulVers.ITEM_VERSATA_IN_DATA_RICH.name(),
                    TipoErrRichAnnulVers.STATO_CONSERV_NON_AMMESSO.name() };
        }
    }

    public enum TipoRegolaModelloTipoSerie {

        DEFINITO_NEL_MODELLO, TUTTI, EREDITA_DA_REG, EREDITA_DA_TIPO_UD_REG;

        public static TipoRegolaModelloTipoSerie[] getEnums(TipoRegolaModelloTipoSerie... vals) {
            return vals;
        }

        public static TipoRegolaModelloTipoSerie[] getTiRglAnniConserv() {
            return getEnums(DEFINITO_NEL_MODELLO, EREDITA_DA_REG);
        }

        public static TipoRegolaModelloTipoSerie[] getTiRglCdSerie() {
            return getEnums(DEFINITO_NEL_MODELLO, EREDITA_DA_TIPO_UD_REG);
        }

        public static TipoRegolaModelloTipoSerie[] getTiRglConservazioneSerie() {
            return getEnums(DEFINITO_NEL_MODELLO);
        }

        public static TipoRegolaModelloTipoSerie[] getTiRglDsSerie() {
            return getEnums(DEFINITO_NEL_MODELLO, EREDITA_DA_TIPO_UD_REG);
        }

        public static TipoRegolaModelloTipoSerie[] getTiRglDsTipoSerie() {
            return getEnums(DEFINITO_NEL_MODELLO, EREDITA_DA_TIPO_UD_REG);
        }

        public static TipoRegolaModelloTipoSerie[] getTiRglFiltroTiDoc() {
            return getEnums(DEFINITO_NEL_MODELLO, TUTTI);
        }

        public static TipoRegolaModelloTipoSerie[] getTiRglNmTipoSerie() {
            return getEnums(DEFINITO_NEL_MODELLO, EREDITA_DA_TIPO_UD_REG);
        }

        public static TipoRegolaModelloTipoSerie[] getTiRglRangeAnniCreaAutom() {
            return getEnums(DEFINITO_NEL_MODELLO, EREDITA_DA_REG);
        }
    }

    public enum TipoSerieCreaStandard {

        BASATA_SU_REGISTRO, BASATA_SU_TIPO_UNITA_DOC
    }

    public enum TiXmlRichAnnulVers {

        RICHIESTA, RISPOSTA
    }

    public enum TiClasseTipoServizio {

        ALTRO, ATTIVAZIONE_SISTEMA_VERSANTE, CONSERVAZIONE
    }

    public enum TiParte {
        ANNO, CLASSIF, PROGR_FASC, PROGR_SUB_FASC
    }

    public enum TiStatoSesioneVers {
        CHIUSA_OK, CHIUSA_ERR
    }

    //
    public class Flag {

        private Flag() {
            throw new IllegalStateException("Utility class");
        }

        public static final String TRUE = "1";
        public static final String FALSE = "0";
    }

    /*
     * Tutte le versioni relative ai report zip gestiti su servizi di verifica firma
     *
     * V_10 -> all, none versions (il report versione 1.0 è il primo supportato da tutte le versioni
     * di tutti i servizi delle librerie sui micro di verifica firma in essere).
     *
     * Una eventuale versione 1.1 o 2.0 specifica per certi servizi / versioni dovrà essere censita
     * nella logica sottostante del tipo:
     *
     * V_11("EIDAS|CRYPTO","6.0|1.13.0") oppure V_20("EIDAS","7.0")
     *
     * differenziare poi la gestione sia lato generazione del report che in fase di parsing.
     *
     */
    public enum ReportvfZipVersion {

        V_10("", "all", "none");

        private final String[] services;
        private final String[] versions;

        private ReportvfZipVersion(String delimiter, String services, String versions) {
            this.services = services.split(delimiter);
            this.versions = versions.split(delimiter);
        }

        public static ReportvfZipVersion getByServiceAndVersion(String service, String version) {
            return Stream.of(values()).filter(v -> Arrays.asList(v.services).contains(service)
                    && Arrays.asList(v.versions).contains(version)).findAny().orElse(V_10);
        }

    }

}
