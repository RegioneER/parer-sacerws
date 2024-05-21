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
 * (written by Francesco Fioravanti)
 */
// Last update: 2018-02-08 17:52:30.148111
package it.eng.parer.ws.utils;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;
import it.eng.spagoCore.util.UUIDMdcLogUtil;

public class MessaggiWSBundle {

    private static Logger log = LoggerFactory.getLogger(MessaggiWSBundle.class);

    public static final String DEFAULT_LOCALE = "it";

    private MessaggiWSBundle() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * Metodi statici, implementazione causata dalla necessità di mantenere invariata l'interfaccia della classe
     * originale: un normalissimo Bundle con un file di properties
     */
    public static String getString(String key) {
        switch (key) {
        case MessaggiWSBundle.ERR_666:
            return getDefaultErrorMessage(key);
        case MessaggiWSBundle.ERR_666P:
            return getDefaultErrorMessage(key);
        default:
            // l'operazione di StringEscapeUtils.unescapeJava viene svolta nel singleton
            return lookupCacheRef().getString(key);
        }
    }

    public static String getString(String key, Object... params) {
        switch (key) {
        case MessaggiWSBundle.ERR_666:
            return getDefaultErrorMessage(key, params);
        case MessaggiWSBundle.ERR_666P:
            return getDefaultErrorMessage(key, params);
        default:
            // l'operazione di StringEscapeUtils.unescapeJava viene svolta nel singleton
            return lookupCacheRef().getString(key, params);
        }
    }

    private static MessaggiWSCache lookupCacheRef() {
        try {
            return (MessaggiWSCache) new InitialContext().lookup("java:app/sacerws-ejb/MessaggiWSCache");
        } catch (NamingException ex) {
            throw new SacerWsRuntimeException(
                    "Errore lookup singleton dei messaggi " + ExceptionUtils.getRootCauseMessage(ex),
                    SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    private static String getDefaultErrorMessage(String key, Object... params) {
        // get or generate uuid
        final String uuid = UUIDMdcLogUtil.getUuid();
        // log original message
        log.error("Risposta originale : {}", lookupCacheRef().getString(key, params));
        return lookupCacheRef().getString(MessaggiWSBundle.WS_GENERIC_ERROR_UUID, uuid);
    }

    // ERRORI IMPREVISTI TEMPLATE (ossia da restituire all'utente a fronte degli
    // errori imprevisti)
    public static final String WS_GENERIC_ERROR_UUID = "WS-GENERIC-ERROR-UUID";
    // Le righe che seguono verranno mostrate raggruppate in Netbeans
    //
    // Nota: queste righe sono state create con uno script Python, che per ora
    // non è pubblico. Non appena sarà gradevole da vedere verrà integrato
    // con il resto dei sorgenti
    //
    // <editor-fold defaultstate="collapsed" desc="COSTANTI DI ERRORE">
    /**
     * Errore imprevisto: {0}
     */
    public static final String ERR_666 = "666";

    /**
     * Errore imprevisto nella persistenza: {0}
     */
    public static final String ERR_666P = "666P";

    /**
     * Errore imprevisto su servizio (Network problems): {0}
     */
    public static final String ERR_666N = "666N";

    /**
     * Errore nella struttura della chiamata al Web service: {0}
     */
    public static final String WS_CHECK = "WS-CHECK";

    /**
     * Errore: XML malformato nel blocco di dati generali. Eccezione:{0}
     */
    public static final String FAS_XSD_001_001 = "FAS_XSD-001-001";

    /**
     * Errore di validazione del blocco di dati generali. Eccezione:{0}
     */
    public static final String FAS_XSD_001_002 = "FAS_XSD-001-002";

    /**
     * Il campo {0} di tipo data breve, non è valorizzato. Un campo di tipo data breve deve essere espresso nella forma
     * aaaa-mm-gg
     */
    public static final String FAS_XSD_001_003 = "FAS_XSD-001-003";

    /**
     * Per questo servizio il tag {0} è obbligatorio
     */
    public static final String FAS_XSD_002_001 = "FAS_XSD-002-001";

    /**
     * Per questo servizio il tag {0} non è supportato
     */
    public static final String FAS_XSD_002_002 = "FAS_XSD-002-002";

    /**
     * L''ambiente non è valorizzato o il valore indicato {0} non è definito nel sistema
     */
    public static final String FAS_CONFIG_001_001 = "FAS_CONFIG-001-001";

    /**
     * L''ente {0} non è valorizzato o è valorizzato con un valore non definito
     */
    public static final String FAS_CONFIG_001_002 = "FAS_CONFIG-001-002";

    /**
     * La struttura {0} non è valorizzata o è valorizzata con un valore non definito
     */
    public static final String FAS_CONFIG_001_003 = "FAS_CONFIG-001-003";

    /**
     * La struttura {0} è un template e non è abilitata al versamento
     */
    public static final String FAS_CONFIG_001_004 = "FAS_CONFIG-001-004";

    /**
     * La struttura {0} non \u00e8 valida alla data corrente e non accetta versamenti
     */
    public static final String FAS_CONFIG_001_005 = "FAS_CONFIG-001-005";

    /**
     * Il valore {0} indicato nel tag &lt;UserID&gt; non coincide con l''utente indicato nella chiamata al WS
     */
    public static final String FAS_CONFIG_002_001 = "FAS_CONFIG-002-001";

    /**
     * La password dell''utente {0} è scaduta
     */
    public static final String FAS_CONFIG_002_002 = "FAS_CONFIG-002-002";

    /**
     * Errore di autenticazione: {0}
     */
    public static final String FAS_CONFIG_002_003 = "FAS_CONFIG-002-003";

    /**
     * L''utente {0} non è attivo. Autenticazione fallita
     */
    public static final String FAS_CONFIG_002_004 = "FAS_CONFIG-002-004";

    /**
     * L''utente {0} non è autorizzato alla funzione {1}
     */
    public static final String FAS_CONFIG_002_005 = "FAS_CONFIG-002-005";

    /**
     * L''utente {0} non è abilitato entro la struttura versante
     */
    public static final String FAS_CONFIG_002_006 = "FAS_CONFIG-002-006";

    /**
     * Nella chiamata al WS, il login name deve essere dichiarato
     */
    public static final String FAS_CONFIG_002_007 = "FAS_CONFIG-002-007";

    /**
     * Nella chiamata al WS è necessario indicare la versione di riferimento
     */
    public static final String FAS_CONFIG_003_001 = "FAS_CONFIG-003-001";

    /**
     * La versione {0} non è supportata.
     */
    public static final String FAS_CONFIG_003_002 = "FAS_CONFIG-003-002";

    /**
     * Il valore {0} indicato nel tag &lt;Versione&gt; non coincide con la versione indicata nella chiamata al WS
     */
    public static final String FAS_CONFIG_003_003 = "FAS_CONFIG-003-003";

    /**
     * Il soggetto produttore con codice {0} è valorizzato con un valore non definito
     */
    public static final String FAS_CONFIG_004_001 = "FAS_CONFIG-004-001";

    /**
     * Il soggetto produttore con denominazione {0} è valorizzato con un valore non definito
     */
    public static final String FAS_CONFIG_004_002 = "FAS_CONFIG-004-002";

    /**
     * Il soggetto produttore non è supportato in questa release
     */
    public static final String FAS_CONFIG_004_003 = "FAS_CONFIG-004-003";

    /**
     * I dati specifici non sono supportati in questa release
     */
    public static final String FAS_CONFIG_005_001 = "FAS_CONFIG-005-001";

    /**
     * Il parametro {0} non \u00e8 stato configurato
     */
    public static final String FAS_CONFIG_006_001 = "FAS_CONFIG-006-001";

    /**
     * Fascicolo {0}:la chiave indicata corrisponde ad un fascicolo già presente nel sistema
     */
    public static final String FASC_001_001 = "FASC-001-001";

    /**
     * Fascicolo {0}: il tipo {1} non è presente entro la struttura versante o è disattivo
     */
    public static final String FASC_002_001 = "FASC-002-001";

    /**
     * Fascicolo {0}: l'utente versatore non è abilitato al tipo fascicolo {1}
     */
    public static final String FASC_002_002 = "FASC-002-002";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non è valido nell'anno {2}
     */
    public static final String FASC_002_003 = "FASC-002-003";

    /**
     * Fascicolo {0}:il numero di unità documentarie indicato nel contenuto sintetico {1} non coincide con il numero di
     * unità documentarie elencate nel contenuto analitico
     */
    public static final String FASC_003_001 = "FASC-003-001";

    /**
     * Fascicolo {0}:è necessario valorizzare il tag &lt;ContenutoAnaliticoUnitaDocumentarie&gt;
     */
    public static final String FASC_003_002 = "FASC-003-002";

    /**
     * Fascicolo {0}: l''unità documentaria {1} indicata nel contenuto analitico non è presente in Sacer o è stata
     * annullata.
     */
    public static final String FASC_003_003 = "FASC-003-003";

    /**
     * Sulla struttura versante non è definito un titolario di classificazione attivo alla data di apertura del
     * fascicolo
     */
    public static final String FASC_004_001 = "FASC-004-001";

    /**
     * Nel SIP di Versamento del Fascicolo non è stato indicato l''indice di classificazione o l''indice non è coerente
     * con le voci di classificazione descritte
     */
    public static final String FASC_004_002 = "FASC-004-002";

    /**
     * Non esiste una voce di titolario attiva alla data di apertura del fascicolo coincidente con l''indice di
     * classificazione {0}
     */
    public static final String FASC_004_003 = "FASC-004-003";

    /**
     * La descrizione della voce di classificazione + codice voce classificazione non coincide con la descrizione
     * presente sul titolario
     */
    public static final String FASC_004_004 = "FASC-004-004";

    /**
     * Fascicolo {0}: il codice indicato non rispetta i requisiti di formato: {1}
     */
    public static final String FASC_005_001 = "FASC-005-001";

    /**
     * Fascicolo {0}: sul periodo di validità non è definito il formato numero
     */
    public static final String FASC_005_002 = "FASC-005-002";

    /**
     * Il fascicolo {0} da collegare al fascicolo oggetto di versamento non è presente nel sistema
     */
    public static final String FASC_006_001 = "FASC-006-001";

    /**
     * Fascicolo {0}: la descrizione del fascicolo da collegare supera il limite previsto.
     */
    public static final String FASC_006_002 = "FASC-006-002";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non ha associato un xsd di profilo archivistico standard nel periodo di
     * validità vigente nell''anno del fascicolo.
     */
    public static final String FAS_PF_ARCH_001_001 = "FAS_PF_ARCH-001-001";

    /**
     * Fascicolo {0}: La versione {1} dell''xsd di profilo archivistico del fascicolo non è presente o è disattiva
     */
    public static final String FAS_PF_ARCH_001_002 = "FAS_PF_ARCH-001-002";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non ha associato un xsd di profilo archivistico nel periodo di validità
     * vigente nell''anno del fascicolo.
     */
    public static final String FAS_PF_ARCH_001_003 = "FAS_PF_ARCH-001-003";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} ha associato un xsd di profilo archivistico e non è stato dichiarato
     * nell''XML di Versamento
     */
    public static final String FAS_PF_ARCH_001_004 = "FAS_PF_ARCH-001-004";

    /**
     * Fascicolo {0}: Errore nella verifica dei dati di profilo archivistico del fascicolo. {1}
     */
    public static final String FAS_PF_ARCH_002_001 = "FAS_PF_ARCH-002-001";

    /**
     * Fascicolo {0}: Errore nella verifica dei dati di profilo archivistico del fascicolo. Il valore indicato nel tag
     * {0} supera la lunghezza massima prevista
     */
    public static final String FAS_PF_ARCH_002_002 = "FAS_PF_ARCH-002-002";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non ha associato un xsd di profilo fascicolo standard nel periodo di
     * validità vigente nell''anno del fascicolo.
     */
    public static final String FAS_PF_GEN_001_001 = "FAS_PF_GEN-001-001";

    /**
     * Fascicolo {0}: La versione {1} dell''xsd di profilo generale del fascicolo non è presente o è disattiva
     */
    public static final String FAS_PF_GEN_001_002 = "FAS_PF_GEN-001-002";

    /**
     * Fascicolo {0}: Errore nella verifica dei dati di profilo generale del fascicolo. {1}
     */
    public static final String FAS_PF_GEN_002_001 = "FAS_PF_GEN-002-001";

    /**
     * Fascicolo {0}: la data di apertura è più recente della data di chiusura del fascicolo
     */
    public static final String FAS_PF_GEN_003_001 = "FAS_PF_GEN-003-001";

    /**
     * Fascicolo {0}: il tipo conservazione è IN_ARCHIVIO e non è stata indicata la data di chiusura del fascicolo
     */
    public static final String FAS_PF_GEN_003_002 = "FAS_PF_GEN-003-002";

    /**
     * Fascicolo {0}: l''unità documentaria {1} indicata come PrimoDocumentoNelFascicolo non è presente nel contenuto
     * analitico del fascicolo
     */
    public static final String FAS_PF_GEN_003_003 = "FAS_PF_GEN-003-003";

    /**
     * Fascicolo {0}: l''unità documentaria {1} indicata come UltimoDocumentoNelFascicolo non è presente nel contenuto
     * analitico del fascicolo
     */
    public static final String FAS_PF_GEN_003_004 = "FAS_PF_GEN-003-004";

    /**
     * Fascicolo {0}: è necessario indicare il tempo di conservazione
     */
    public static final String FAS_PF_GEN_003_005 = "FAS_PF_GEN-003-005";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto mancanti, indicare Denominazione oppure Cognome e Nome del
     * soggetto
     */
    public static final String FAS_PF_GEN_003_006 = "FAS_PF_GEN-003-006";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto mancanti, indicare sia il Cognome che il Nome del soggetto
     */
    public static final String FAS_PF_GEN_003_007 = "FAS_PF_GEN-003-007";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto errate, non è possibile indicare per un soggetto sia il
     * Cognome/Nome sia la Denominazione
     */
    public static final String FAS_PF_GEN_003_008 = "FAS_PF_GEN-003-008";

    /**
     * Fascicolo {0}: non è possibile inserire per lo stesso soggetto {1} il tipo di rapporto indicato {2}
     */
    public static final String FAS_PF_GEN_003_009 = "FAS_PF_GEN-003-009";

    /**
     * Fascicolo {0}: il tipo conservazione VERSAMENTO_ANTICIPATO non è supportato dalla versione del WS
     */
    public static final String FAS_PF_GEN_003_010 = "FAS_PF_GEN-003-010";

    /**
     * Errore: XML malformato nel blocco di dati generali. Eccezione: {0}
     */
    public static final String XSD_001_001 = "XSD-001-001";

    /**
     * Errore di validazione del blocco di dati generali. Eccezione: {0}
     */
    public static final String XSD_001_002 = "XSD-001-002";

    /**
     * Controllare che i tag &lt;ID&gt; dei componenti e dei sottocomponenti siano stati valorizzati correttamente. I
     * valori devono essere univoci entro l''Unità Documentaria
     */
    public static final String XSD_002_001 = "XSD-002-001";

    /**
     * Controllare che i tag &lt;IDDocumento&gt; di ogni documento siano stati valorizzati correttamente. I valori
     * devono essere univoci entro l''Unità Documentaria
     */
    public static final String XSD_002_002 = "XSD-002-002";

    /**
     * Il campo {0} di tipo data breve, non è valorizzato. Un campo di tipo data breve deve essere espresso nella forma
     * aaaa-mm-gg
     */
    public static final String XSD_002_003 = "XSD-002-003";

    /**
     * Il numero di allegati dichiarato non corrisponde al numero di elementi &lt;Allegato&gt;
     */
    public static final String XSD_003_001 = "XSD-003-001";

    /**
     * Il numero di annessi dichiarato non corrisponde al numero di elementi &lt;Annesso&gt;
     */
    public static final String XSD_004_001 = "XSD-004-001";

    /**
     * Il numero di annotazioni dichiarate non corrisponde al numero di elementi &lt;Annotazione&gt;
     */
    public static final String XSD_005_001 = "XSD-005-001";

    /**
     * Il tipo di conservazione è MIGRAZIONE ma la versione del WS non lo supporta
     */
    public static final String XSD_006_001 = "XSD-006-001";

    /**
     * Il tipo di conservazione è MIGRAZIONE ma non è indicato il sistema di migrazione
     */
    public static final String XSD_006_002 = "XSD-006-002";

    /**
     * E'' stato indicato un sistema di migrazione ma il tipo di conservazione non è MIGRAZIONE
     */
    public static final String XSD_006_003 = "XSD-006-003";

    /**
     * Sono stati indicati dati specifici di migrazione, ma il tipo di conservazione non è MIGRAZIONE
     */
    public static final String XSD_006_004 = "XSD-006-004";

    /**
     * Sono stati indicati dati specifici estesi, ma la versione del WS non li supporta
     */
    public static final String XSD_006_005 = "XSD-006-005";

    /**
     * Il tipo di aggiornamento specificato non è fra i valori ammessi
     */
    public static final String XSD_006_006 = "XSD-006-006";

    /**
     * {0} {1}: {2} E'' stato indicato il profilo {3} per ma la versione del WS non lo supporta
     */
    public static final String XSD_006_007 = "XSD-006-007";

    /**
     * {0} {1}: {2} E'' stato indicato un campo {3} per ma la versione del WS non lo supporta
     */
    public static final String XSD_006_008 = "XSD-006-008";

    /**
     * Per questo servizio il tag {0} è obbligatorio
     */
    public static final String XSD_011_001 = "XSD-011-001";

    /**
     * Per questo servizio il tag {0} non è supportato
     */
    public static final String XSD_011_002 = "XSD-011-002";

    /**
     * E'' stato specificato il tag {0}, ma la versione indicata del WS non lo supporta
     */
    public static final String XSD_011_003 = "XSD-011-003";

    /**
     * La struttura {0} non ammette versamenti
     */
    public static final String PART_001_001 = "PART-001-001";

    /**
     * La struttura {0} non ammette versamenti per l''anno {1}
     */
    public static final String PART_001_002 = "PART-001-002";

    /**
     * La struttura {0} non ammette versamenti per i componenti di tipo {1}
     */
    public static final String PART_002_001 = "PART-002-001";

    /**
     * La struttura {0} non ammette versamenti per i componenti di tipo {1} per l''anno {2}
     */
    public static final String PART_002_002 = "PART-002-002";

    /**
     * Non sono definite partizioni DB per la sessione di versamento per la struttura {0}
     */
    public static final String PART_003_001 = "PART-003-001";

    /**
     * Non sono definite partizioni DB per la sessione di versamento per la struttura {0} per la data {1}
     */
    public static final String PART_003_002 = "PART-003-002";

    /**
     * Non sono definite partizioni DB per i componenti della sessione di versamento per la struttura {0}
     */
    public static final String PART_004_001 = "PART-004-001";

    /**
     * Non sono definite partizioni DB per i componenti della sessione di versamento per la struttura {0} per la data
     * {1}
     */
    public static final String PART_004_002 = "PART-004-002";

    /**
     * La substruttura {0} della struttura {1} non ammette versamenti
     */
    public static final String PART_005_001 = "PART-005-001";

    /**
     * La substruttura {0} della struttura {1} non ammette versamenti per l''anno {2}
     */
    public static final String PART_005_002 = "PART-005-002";

    /**
     * Per la struttura e l'anno {1} non sono definite le seguenti partizioni {2}
     */
    public static final String PART_006_001 = "PART-006-001";

    /**
     * L''Ambiente {0} non è presente nel sistema
     */
    public static final String UD_001_001 = "UD-001-001";

    /**
     * L''Ente {0} non è presente nel sistema
     */
    public static final String UD_001_002 = "UD-001-002";

    /**
     * La Struttura {0} non è presente nel sistema
     */
    public static final String UD_001_003 = "UD-001-003";

    /**
     * Nella chiamata al WS, il login name deve essere dichiarato
     */
    public static final String UD_001_004 = "UD-001-004";

    /**
     * Il valore [{0}] indicato nel tag &lt;UserID&gt; non coincide con l''utente indicato nella chiamata al WS
     */
    public static final String UD_001_005 = "UD-001-005";

    /**
     * La password dell''utente {0} è scaduta
     */
    public static final String UD_001_006 = "UD-001-006";

    /**
     * L''utente {0} è disattivato. Autenticazione fallita
     */
    public static final String UD_001_007 = "UD-001-007";

    /**
     * L''utente {0} non è autorizzato alla funzione {1}
     */
    public static final String UD_001_008 = "UD-001-008";

    /**
     * L''utente {0} non è abilitato entro la struttura versante
     */
    public static final String UD_001_009 = "UD-001-009";

    /**
     * Nella chiamata al WS, è necessario indicare la versione di riferimento
     */
    public static final String UD_001_010 = "UD-001-010";

    /**
     * La versione [{0}] indicata non è supportata
     */
    public static final String UD_001_011 = "UD-001-011";

    /**
     * Errore di autenticazione: {0}
     */
    public static final String UD_001_012 = "UD-001-012";

    /**
     * Il valore [{0}] indicato nel tag &lt;Versione&gt; non coincide con la versione indicata nella chiamata al WS
     */
    public static final String UD_001_013 = "UD-001-013";

    /**
     * Il nome del sistema di migrazione {0} non è presente nel sistema
     */
    public static final String UD_001_014 = "UD-001-014";

    /**
     * La Struttura {0} è un template e non è abilitata al versamento
     */
    public static final String UD_001_015 = "UD-001-015";

    /**
     * L'utente non è abilitato al tipo di unità documentaria {0} oppure al registro {1}
     */
    public static final String UD_001_016 = "UD-001-016";

    /**
     * L'utente non è abilitato al tipo documento {0}
     */
    public static final String UD_001_017 = "UD-001-017";

    /**
     * Per la struttura {0} deve essere definito il "Tipo di unita documentaria sconosciuta", il "Registro sconosciuto"
     * ed il Tipo documento principale sconosciuto"
     */
    public static final String UD_001_018 = "UD-001-018";

    /**
     * La struttura {0} non \u00e8 valida alla data corrente e non accetta versamenti
     */
    public static final String UD_001_019 = "UD-001-019";

    /**
     * Unità Documentaria {0}: la chiave indicata corrisponde ad una Unità Documentaria già presente nel sistema
     */
    public static final String UD_002_001 = "UD-002-001";

    /**
     * Unità Documentaria {0}: la tipologia {1} non è presente entro la struttura versante
     */
    public static final String UD_003_001 = "UD-003-001";

    /**
     * Unità Documentaria {0}: il tipo registro {1} non è presente entro la struttura versante
     */
    public static final String UD_003_002 = "UD-003-002";

    /**
     * Unità Documentaria {0}: il tipo registro {1} non è associato alla tipologia di unità documentaria {2}
     */
    public static final String UD_003_003 = "UD-003-003";

    /**
     * Unità Documentaria {0}: l''anno {2} non è valido per il tipo registro {1}
     */
    public static final String UD_003_004 = "UD-003-004";

    /**
     * Unità Documentaria {0}: esiste più di una tipologia di unità documentaria {1} attiva alla data corrente entro la
     * struttura versante. E’ necessario correggere la configurazione della struttura prima di ripetere il versamento.
     */
    public static final String UD_003_005 = "UD-003-005";

    /**
     * Unità Documentaria {0}: l''Unità Documentaria {1} da collegare alla UD oggetto di versamento non è presente nel
     * sistema
     */
    public static final String UD_004_001 = "UD-004-001";

    /**
     * Unità Documentaria {0}: il registro dell''unità documentaria {1} da collegare alla UD oggetto di versamento non è
     * presente nella struttura o non è attivo
     */
    public static final String UD_004_003 = "UD-004-003";

    /**
     * Unità Documentaria {0}: l''anno dell''unità documentaria {1} da collegare alla UD oggetto di versamento non è
     * compreso nei periodi di validità del registro{2}
     */
    public static final String UD_004_004 = "UD-004-004";

    /**
     * Unità Documentaria {0}: il numero dell''unità documentaria {1} da collegare alla UD oggetto di versamento non
     * rispetta le regole configurate sul registro nel periodo di validità: {2}
     */
    public static final String UD_004_005 = "UD-004-005";

    /**
     * Unità Documentaria {0}: la descrizione delle unità documentarie da collegare supera il limite previsto
     */
    public static final String UD_004_006 = "UD-004-006";

    /**
     * Unità Documentaria {0}: la chiave indicata non corrisponde a nessuna Unità Documentaria presente nel sistema
     */
    public static final String UD_005_001 = "UD-005-001";

    /**
     * Unità Documentaria {0}: L''unità documentaria non è contenuta in alcun volume di conservazione
     */
    public static final String UD_005_002 = "UD-005-002";

    /**
     * Unità Documentaria {0}: L''indice AIP dell''unità documentaria deve ancora essere prodotto
     */
    public static final String UD_005_003 = "UD-005-003";

    /**
     * Unità Documentaria {0}: Il Rapporto di Versamento dell''unità documentaria non è stato prodotto
     */
    public static final String UD_005_004 = "UD-005-004";

    /**
     * Unità Documentaria {0}: Nel sistema é già presente una unità documentaria con lo stesso numero normalizzato
     * prodotto
     */
    public static final String UD_005_005 = "UD-005-005";

    /**
     * Unità Documentaria {0}: la chiave indicata non rispetta i requisiti di formato; {1}
     */
    public static final String UD_007_001 = "UD-007-001";

    /**
     * Unità Documentaria {0}: la lunghezza di &lt;registro&gt;-&lt;anno&gt;-&lt;numero normalizzato&gt; supera i 100
     * caratteri e non consente la normalizzazione
     */
    public static final String UD_007_002 = "UD-007-002";

    /**
     * Unità Documentaria {0}: l''URN calcolato per i componenti di questa unità documentaria supera i 254 caratteri
     */
    public static final String UD_007_003 = "UD-007-003";

    /**
     * Unità Documentaria {0}: non sono stati trovati componenti firmati digitalmente
     */
    public static final String UD_008_001 = "UD-008-001";

    /**
     * Unità Documentaria {0}: impossibile memorizzare i dati relativi al certificato di firma. Il versamento deve
     * essere ripetuto.
     */
    public static final String UD_008_002 = "UD-008-002";

    /**
     * Unità Documentaria {0}: conservazione NON FISCALE nonostante il registro sia fiscalmente rilevante
     */
    public static final String UD_009_001 = "UD-009-001";

    /**
     * Unità Documentaria {0}: conservazione FISCALE nonostante il registro NON sia fiscalmente rilevante
     */
    public static final String UD_009_002 = "UD-009-002";

    /**
     * Unità Documentaria {0}: Errore nel calcolo della Substruttura: {1}
     */
    public static final String UD_011_001 = "UD-011-001";

    /**
     * Unità Documentaria {0}: Il dato specifico {1} dell''Unità Documentaria è nullo: impossibile determinare la
     * Substruttura
     */
    public static final String UD_011_002 = "UD-011-002";

    /**
     * Unità Documentaria {0}: Il dato specifico {1} del Documento Principale dell''Unità Documentaria è nullo:
     * impossibile determinare la Substruttura
     */
    public static final String UD_011_003 = "UD-011-003";

    /**
     * Unità Documentaria {0}: Il dato specifico {1} dell''Unità Documentaria non individua una Substruttura valida
     */
    public static final String UD_011_004 = "UD-011-004";

    /**
     * Unità Documentaria {0}: Il dato specifico {1} del Documento Principale dell''Unità Documentaria non individua una
     * Substruttura valida
     */
    public static final String UD_011_005 = "UD-011-005";

    /**
     * Unità Documentaria {0}: L''unità documentaria è sottoposta a sequestro
     */
    public static final String UD_012_001 = "UD-012-001";

    /**
     * Unità Documentaria {0}: L''unità documentaria è stata annullata
     */
    public static final String UD_012_002 = "UD-012-002";

    /**
     * Unità Documentaria {0}: L''unità documentaria è inserita in almeno una serie; per aggiungere il documento è
     * necessario richiedere l''aggiornamento della serie
     */
    public static final String UD_013_001 = "UD-013-001";

    /**
     * Per unità documentaria {0} non è abilitato l'aggiornamento dei metadati
     */
    public static final String UD_013_002 = "UD-013-002";

    /**
     * Unità Documentaria {0} ha stato di conservazione {1} e, quindi, non può essere modificata
     */
    public static final String UD_013_003 = "UD-013-003";

    /**
     * Unità Documentaria {0}: L''oggetto è un dato di profilo configurato come obbligatorio
     */
    public static final String UD_014_001 = "UD-014-001";

    /**
     * Unità Documentaria {0}: La data dell''unità documentaria è un dato di profilo configurato come obbligatorio
     */
    public static final String UD_014_002 = "UD-014-002";

    /**
     * L’unità Documentaria {0} ha tipo diverso da {1}
     */
    public static final String UD_015_001 = "UD-015-001";

    /**
     * Aggiornamento UD: Controllo HASH SIP
     */
    public static final String UD_015_002 = "UD-015-002";

    /**
     * Per almeno un documento da aggiornare si è rilevato almeno un errore
     */
    public static final String UD_016_001 = "UD-016-001";

    /**
     * Per almeno un componente da aggiornare si è rilevato almeno un errore
     */
    public static final String UD_016_002 = "UD-016-002";

    /**
     * Almeno un collegamento da aggiornare presenta un errore
     */
    public static final String UD_016_003 = "UD-016-003";

    /**
     * Un fascicolo secondario coincide con il fascicolo principale precedentemente versato
     */
    public static final String UD_017_001 = "UD-017-001";

    /**
     * Il parametro {0} non \u00e8 stato configurato
     */
    public static final String UD_018_001 = "UD-018-001";

    /**
     * Documento {0}: il tipo documento {1} non è presente entro la struttura versante
     */
    public static final String DOC_001_001 = "DOC-001-001";

    /**
     * Documento {0}: il tipo documento {1} non è ammesso per il documento principale
     */
    public static final String DOC_001_002 = "DOC-001-002";

    /**
     * Documento {0}: esiste più di un tipo di documento {1} attivo alla data corrente entro la struttura versante. E’
     * necessario correggere la configurazione della struttura prima di ripetere il versamento.
     */
    public static final String DOC_001_003 = "DOC-001-003";

    /**
     * Documento {0}: Il tipo struttura {1} non è presente entro la struttura versante
     */
    public static final String DOC_005_001 = "DOC-005-001";

    /**
     * Documento {0}: &lt;OrdinePresentazione&gt;{1}&lt;/OrdinePresentazione&gt; del componente &lt;ID&gt;{2}&lt;/ID&gt;
     * non univoco
     */
    public static final String DOC_007_001 = "DOC-007-001";

    /**
     * Documento {0}: &lt;OrdinePresentazione&gt; del componente &lt;ID&gt;{1}&lt;/ID&gt; deve essere maggiore di 0
     */
    public static final String DOC_007_002 = "DOC-007-002";

    /**
     * Documento {0}: &lt;OrdinePresentazione&gt;{1}&lt;/OrdinePresentazione&gt; del componente non univoco
     */
    public static final String DOC_007_003 = "DOC-007-003";

    /**
     * Un documento con il tag &lt;IDDocumento&gt; uguale a {0} è già presente nell''Unità Documentaria {1}
     */
    public static final String DOC_008_001 = "DOC-008-001";

    /**
     * Documento {0}: L''utilizzo del Tag &lt;DatiFiscali&gt; è DEPRECATO
     */
    public static final String DOC_009_001 = "DOC-009-001";

    /**
     * Documento {0}: identificativo non presente nell''Unità Documentaria specificata
     */
    public static final String DOC_010_001 = "DOC-010-001";

    /**
     * Il documento identificato da {0} non è di tipo {1}
     */
    public static final String DOC_010_002 = "DOC-010-002";

    /**
     * Il documento identificato da {0} ha tipo diverso da {1}
     */
    public static final String DOC_010_003 = "DOC-010-003";

    /**
     * Il documento identificato da {0} ha tipo struttura documento diverso da {1}
     */
    public static final String DOC_010_004 = "DOC-010-004";

    /**
     * Componente {0}: Il tipo componente {1} non è presente entro la struttura versante
     */
    public static final String COMP_001_001 = "COMP-001-001";

    /**
     * Componente {0}: Il tipo di uso del tipo componente deve essere pari a "CONTENUTO" o a "CONVERTITORE"
     */
    public static final String COMP_001_002 = "COMP-001-002";

    /**
     * Componente {0}: Il tag &lt;Riferimento&gt; non deve essere valorizzato se il tag &lt;TipoSupportoComponente&gt;
     * non è stato valorizzato con "RIFERIMENTO"
     */
    public static final String COMP_002_001 = "COMP-002-001";

    /**
     * Componente {0}: tag &lt;Riferimento&gt; obbligatorio e non valorizzato (il tag &lt;TipoSupportoComponente&gt;
     * "RIFERIMENTO")
     */
    public static final String COMP_002_002 = "COMP-002-002";

    /**
     * Componente {0}: Il tag &lt;SottoComponenti&gt; non deve essere indicato (tag &lt;TipoSupportoComponente&gt;
     * diverso da "FILE")
     */
    public static final String COMP_002_004 = "COMP-002-004";

    /**
     * Componente {0}: Il tipo Rappresentazione Componente {1} indicato nel tag &lt;TipoRappresentazioneComponente&gt;
     * non è presente entro la struttura versante
     */
    public static final String COMP_003_001 = "COMP-003-001";

    /**
     * Componente {0}: Il tipo di output del Tipo Rappresentazione Componente {1} deve essere configurato
     */
    public static final String COMP_003_002 = "COMP-003-002";

    /**
     * Componente {0}: Se è valorizzato il tag &lt;TipoRappresentazioneComponente&gt;, il tipo supporto deve essere
     * uguale a "FILE"
     */
    public static final String COMP_003_003 = "COMP-003-003";

    /**
     * Componente {0}: Se è valorizzato il tag &lt;TipoRappresentazioneComponente&gt;, l''unità documentaria deve essere
     * salvata su BLOB
     */
    public static final String COMP_003_004 = "COMP-003-004";

    /**
     * Componente {0}: Se è valorizzato il tag &lt;TipoRappresentazioneComponente&gt;, il tipo uso componente deve
     * essere uguale a "CONTENUTO"
     */
    public static final String COMP_003_005 = "COMP-003-005";

    /**
     * Componente {0}: Se non è valorizzato Tipo Rappresentazione Componente non devono essere presenti sottocomponenti
     * aventi tipo uso CONVERTITORE o RAPPRESENTAZIONE
     */
    public static final String COMP_004_001 = "COMP-004-001";

    /**
     * Componente {0}: Se è valorizzato Tipo Rappresentazione Componente deve essere presente uno ed uno solo
     * sottocomponente avente tipo uso CONVERTITORE o RAPPRESENTAZIONE
     */
    public static final String COMP_004_002 = "COMP-004-002";

    /**
     * Componente {0}: DEPRECATO Sottocomponente non correttamente impostato in base al componente
     */
    public static final String COMP_004_003 = "COMP-004-003";

    /**
     * Componente {0}: Il tag &lt;NomeComponente&gt; deve essere valorizzato (il componente ha un tipo di supporto pari
     * a "FILE")
     */
    public static final String COMP_005_001 = "COMP-005-001";

    /**
     * Componente {0}: il Formato {1} non è ammesso per la struttura versante, il tipo struttura o il tipo componente
     */
    public static final String COMP_006_001 = "COMP-006-001";

    /**
     * Componente {0}: il Formato {1} non è ammesso per il Tipo Rappresentazione Componente
     */
    public static final String COMP_006_002 = "COMP-006-002";

    /**
     * Componente {0}: il numero di componenti del documento principale della UD di Riferimento {1} non può essere
     * maggiore di 1
     */
    public static final String COMP_008_001 = "COMP-008-001";

    /**
     * Componente {0}: il componente del documento principale della UD di Riferimento {1} è di tipo diverso da
     * "CONVERTITORE"
     */
    public static final String COMP_008_002 = "COMP-008-002";

    /**
     * Componente {0}: l''UD identificata dal Riferimento {1} non è presente nel sistema
     */
    public static final String COMP_008_003 = "COMP-008-003";

    /**
     * Componente {0}: L''UD del convertitore indicato dal riferimento {1} deve essere salvata su BLOB
     */
    public static final String COMP_008_004 = "COMP-008-004";

    /**
     * Componente {0}: Nel documento principale dell''UD indicata dal riferimento {1}, non sono stati individuati
     * componenti con tipo supporto uguale a "FILE"
     */
    public static final String COMP_008_005 = "COMP-008-005";

    /**
     * Componente {0}: identificativo non presente nell''Unità Documentaria specificata
     */
    public static final String COMP_010_001 = "COMP-010-001";

    /**
     * Componente {0}: se si valorizza l''ordine di presentazione è obbligatorio fornire l''identificativo del documento
     */
    public static final String COMP_010_002 = "COMP-010-002";

    /**
     * Il componente {0} ha tipo componente diverso da {1}
     */
    public static final String COMP_010_003 = "COMP-010-003";

    /**
     * SottoComponente {0}: Il Tipo Componente {1} non è presente entro la struttura versante
     */
    public static final String SUBCOMP_001_001 = "SUBCOMP-001-001";

    /**
     * SottoComponente {0}: Il Tipo Componente non ha tipo di uso corretto
     */
    public static final String SUBCOMP_001_002 = "SUBCOMP-001-002";

    /**
     * SottoComponente {0}: Il tag &lt;Riferimento&gt; non deve essere valorizzato se il tag
     * &lt;TipoSupportoComponente&gt; non è stato valorizzato con "RIFERIMENTO"
     */
    public static final String SUBCOMP_002_001 = "SUBCOMP-002-001";

    /**
     * SottoComponente {0}: tag &lt;Riferimento&gt; obbligatorio e non valorizzato (il tag
     * &lt;TipoSupportoComponente&gt; "RIFERIMENTO")
     */
    public static final String SUBCOMP_002_002 = "SUBCOMP-002-002";

    /**
     * SottoComponente {0}: tag &lt;TipoSupportoComponente&gt; valorizzato con "RIFERIMENTO" e tipo uso diverso da
     * "RAPPRESENTAZIONE"
     */
    public static final String SUBCOMP_002_003 = "SUBCOMP-002-003";

    /**
     * SottoComponente {0}: tag &lt;TipoSupportoComponente&gt; diverso da "RIFERIMENTO" e tipo uso uguale a
     * "RAPPRESENTAZIONE"
     */
    public static final String SUBCOMP_002_004 = "SUBCOMP-002-004";

    /**
     * SottoComponente {0}: tag &lt;TipoSupportoComponente&gt; diverso da "FILE" e tipo uso uguale a "CONVERTITORE"
     */
    public static final String SUBCOMP_002_005 = "SUBCOMP-002-005";

    /**
     * SottoComponente {0}: Il tag &lt;NomeComponente&gt; deve essere valorizzato (il componente ha un tipo di supporto
     * pari a "FILE")
     */
    public static final String SUBCOMP_003_001 = "SUBCOMP-003-001";

    /**
     * SottoComponente {0}: il Formato {1} non è ammesso per la struttura versante, il tipo struttura e il tipo
     * componente
     */
    public static final String SUBCOMP_004_001 = "SUBCOMP-004-001";

    /**
     * SottoComponente {0}: il Formato {1} non è ammesso per il Tipo Rappresentazione Componente
     */
    public static final String SUBCOMP_004_002 = "SUBCOMP-004-002";

    /**
     * SottoComponente {0}: il numero di componenti del documento principale della UD di Riferimento {1} non può essere
     * maggiore di 1
     */
    public static final String SUBCOMP_006_001 = "SUBCOMP-006-001";

    /**
     * SottoComponente {0}: il componente del documento principale della UD di Riferimento {1} è di tipo diverso da
     * "CONVERTITORE"
     */
    public static final String SUBCOMP_006_002 = "SUBCOMP-006-002";

    /**
     * SottoComponente {0}: l''UD identificata dal Riferimento {1} non è presente nel sistema
     */
    public static final String SUBCOMP_006_003 = "SUBCOMP-006-003";

    /**
     * SottoComponente {0}: L''UD del convertitore indicato dal riferimento {1} deve essere salvata su BLOB
     */
    public static final String SUBCOMP_006_004 = "SUBCOMP-006-004";

    /**
     * SottoComponente {0}: Nel documento principale dell''UD indicata dal riferimento {1}, non sono stati individuati
     * componenti con tipo supporto uguale a "FILE"
     */
    public static final String SUBCOMP_006_005 = "SUBCOMP-006-005";

    /**
     * SottoComponente {0}: il formato del componente del documento principale della UD di Riferimento {1} non è ammesso
     * per il Tipo Rappresentazione Componente
     */
    public static final String SUBCOMP_006_006 = "SUBCOMP-006-006";

    /**
     * {0} {1}: La versione di dati specifici {2} indicata per il tipo {0} {3} non esiste.
     */
    public static final String DATISPEC_001_001 = "DATISPEC-001-001";

    /**
     * {0} {1}: il tipo {0} {2} ha associato dei dati specifici. Il file XML deve pertanto includere il tag
     * &lt;DatiSpecifici&gt;
     */
    public static final String DATISPEC_001_002 = "DATISPEC-001-002";

    /**
     * {0} {1}: DEPRECATO il tipo {0} {2} ha associato dei dati specifici. Nel file XML occorre valorizzare il tag
     * &lt;VersioneDatiSpecifici&gt;
     */
    public static final String DATISPEC_001_003 = "DATISPEC-001-003";

    /**
     * {0} {1}: La versione di dati specifici {2} indicata per il tipo {0} {3} è disattiva.
     */
    public static final String DATISPEC_001_004 = "DATISPEC-001-004";

    /**
     * {0} {1}: Errore nei dati specifici: il valore indicato nel tag {2} supera la lunghezza di {3} caratteri
     */
    public static final String DATISPEC_002_001 = "DATISPEC-002-001";

    /**
     * {0} {1}: Errore nei dati specifici: il metadato {2} risulta duplicato
     */
    public static final String DATISPEC_002_002 = "DATISPEC-002-002";

    /**
     * {0} {1}: Errore nella verifica dei dati specifici: {2}
     */
    public static final String DATISPEC_003_001 = "DATISPEC-003-001";

    /**
     * {0} {1}: I dati specifici attesi non coincidono con l''XSD
     */
    public static final String DATISPEC_003_002 = "DATISPEC-003-002";

    /**
     * {0} {1}: La versione di dati specifici {2} indicata per il tipo {0} {3} non esiste.
     */
    public static final String DATISPECM_001_001 = "DATISPECM-001-001";

    /**
     * {0} {1}: il tipo {0} {2} ha associato dei dati specifici di migrazione. Il file XML deve pertanto includere il
     * tag &lt;DatiSpecificiMigrazione&gt;
     */
    public static final String DATISPECM_001_002 = "DATISPECM-001-002";

    /**
     * {0} {1}: DEPRECATO il tipo {0} {2} ha associato dei dati specifici di migrazione. Nel file XML occorre
     * valorizzare il tag &lt;VersioneDatiSpecifici&gt;
     */
    public static final String DATISPECM_001_003 = "DATISPECM-001-003";

    /**
     * {0} {1}: La versione di dati specifici {2} indicata per il tipo {0} {3} è disattiva.
     */
    public static final String DATISPECM_001_004 = "DATISPECM-001-004";

    /**
     * {0} {1}: Errore nei dati specifici di migrazione: il valore indicato nel tag {2} supera la lunghezza di {3}
     * caratteri
     */
    public static final String DATISPECM_002_001 = "DATISPECM-002-001";

    /**
     * {0} {1}: Errore nei dati specifici di migrazione: il metadato {2} risulta duplicato
     */
    public static final String DATISPECM_002_002 = "DATISPECM-002-002";

    /**
     * {0} {1}: Errore nella verifica dei dati specifici di migrazione: {2}
     */
    public static final String DATISPECM_003_001 = "DATISPECM-003-001";

    /**
     * {0} {1}: Esiste profilo normativo archivisitico associato a {2} ma non dichiarato su XML versato
     */
    public static final String PROFNORM_001_001 = "PROFNORM-001-001";

    /**
     * {0} {1}: La versione {2} dell''xsd di profilo normativo di {3} non è presente o è disattiva
     */
    public static final String PROFNORM_001_002 = "PROFNORM-001-002";

    /**
     * {0} {1}: Errore nella verifica dei dati di profilo normativo di {2}. {3}
     */
    public static final String PROFNORM_001_003 = "PROFNORM-001-003";

    /**
     * {0} {1}: {2} non ha un xsd di profilo normativo associato nel periodo di validità
     */
    public static final String PROFNORM_001_004 = "PROFNORM-001-004";

    /**
     * {0} {1}: Esiste un xsd di profilo normativo associato a {2} attivo alla data di versamento. Eliminazione non
     * consentita per l''elemento ProfiloNormativo.
     */
    public static final String PROFNORM_001_005 = "PROFNORM-001-005";
    /**
     * {0} {1}: Profilo Normativo non presente nell''xml del SIP per l''Unità Documentaria. Eliminazione non consentita
     * per l''elemento ProfiloNormativo.
     */
    public static final String PROFNORM_001_006 = "PROFNORM-001-006";
    /**
     * Necessario valorizzare xml del Profilo Normativo per la versione specificata
     */
    public static final String PROFNORM_001_007 = "PROFNORM-001-007";
    /**
     * Componente {0}: per questo componente è obbligatorio indicare l''hash
     */
    public static final String HASH_001_001 = "HASH-001-001";

    /**
     * Componente {0}: l''encoding usato per indicare l''hash non è supportato (sono gestiti Hex e Base64)
     */
    public static final String HASH_002_001 = "HASH-002-001";

    /**
     * Componente {0}: l''algoritmo usato per indicare l''hash non è supportato (sono gestiti MD5, SHA-1 e SHA-256,
     * codificati in Hex e Base64)
     */
    public static final String HASH_003_001 = "HASH-003-001";

    /**
     * Componente {0}: l''hash calcolato non coincide con l''hash versato (l''encoding usato è {1}, l''algoritmo è {2})
     */
    public static final String HASH_004_001 = "HASH-004-001";

    /**
     * Componente {0}: Errore VerificaMarca.ControlloConformita
     */
    public static final String MARCA_001_001 = "MARCA-001-001";

    /**
     * Componente {0}: Errore VerificaMarca.ControlloCrittografico
     */
    public static final String MARCA_002_001 = "MARCA-002-001";

    /**
     * Componente {0}: Errore VerificaMarca.ControlloCatenaTrusted
     */
    public static final String MARCA_003_001 = "MARCA-003-001";

    /**
     * Componente {0}: Errore VerificaMarca.ControlloCertificato
     */
    public static final String MARCA_004_001 = "MARCA-004-001";

    /**
     * Componente {0}: Errore VerificaMarca.ControlloCRL
     */
    public static final String MARCA_005_001 = "MARCA-005-001";

    /**
     * Componente {0}: Errore VerificaMarca.ControlloOCSP
     */
    public static final String MARCA_006_001 = "MARCA-006-001";
    /**
     * Componente {0}: ControlloConformità pari a FORMATO_NON_CONOSCIUTO
     */
    public static final String FIRMA_001_001 = "FIRMA-001-001";

    /**
     * Componente {0}: ControlloConformità pari a FORMATO_NON_CONFORME
     */
    public static final String FIRMA_001_002 = "FIRMA-001-002";

    /**
     * Componente {0}: ControlloConformità pari a NON_AMMESSO_DELIB_45_CNIPA
     */
    public static final String FIRMA_001_003 = "FIRMA-001-003";

    /**
     * Componente {0}: Errore VerificaFirma.ControlloCrittografico
     */
    public static final String FIRMA_002_001 = "FIRMA-002-001";

    /**
     * Componente {0}: Errore VerificaFirma.ControlloCatenaTrusted
     */
    public static final String FIRMA_003_001 = "FIRMA-003-001";

    /**
     * Componente {0}: Errore VerificaFirma.ControlloCertificato
     */
    public static final String FIRMA_004_001 = "FIRMA-004-001";

    /**
     * Componente {0}: Errore VerificaFirma.ControlloCRL
     */
    public static final String FIRMA_005_001 = "FIRMA-005-001";

    /**
     * Componente {0}: Errore VerificaFirma.ControlloOCSP
     */
    public static final String FIRMA_007_001 = "FIRMA-007-001";

    /**
     * Servizio di verifica firma non disponibile
     */
    public static final String FIRMA_006_001 = "FIRMA-006-001";

    /**
     * Errore durante verifica documento firmato {0}
     */
    public static final String FIRMA_006_002 = "FIRMA-006-002";

    /**
     * Componente {0}: Errore Controllo Formato: {1}
     */
    public static final String FORMATO_001_001 = "FORMATO-001-001";

    /**
     * SottoComponente di tipo FIRMA {0}: Errore Controllo Formato: {1}
     */
    public static final String FORMATO_001_002 = "FORMATO-001-002";

    /**
     * SottoComponente di tipo MARCA {0}: Errore Controllo Formato: {1}
     */
    public static final String FORMATO_001_003 = "FORMATO-001-003";

    /**
     * SottoComponente {0}: Errore Controllo Formato: {1}
     */
    public static final String FORMATO_001_004 = "FORMATO-001-004";

    /**
     * Errore: XML malformato nel blocco di dati generali. Eccezione: {0}
     */
    public static final String MM_XSD_001_001 = "MM-XSD-001-001";

    /**
     * Errore di validazione del blocco di dati generali. Eccezione: {0}
     */
    public static final String MM_XSD_001_002 = "MM-XSD-001-002";

    /**
     * Controllare che i tag &lt;ID&gt; siano stati valorizzati correttamente. I valori devono essere univoci entro
     * l''indice Multimedia
     */
    public static final String MM_XSD_002_001 = "MM-XSD-002-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: Tag &lt;ForzaFormato&gt; presente ma tag &lt;VerificaFirmeFormati&gt; true
     */
    public static final String MM_COMP_001_001 = "MM-COMP-001-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: Tag &lt;ForzaHash&gt; presente ma tag &lt;CalcolaHash&gt; true
     */
    public static final String MM_COMP_002_001 = "MM-COMP-002-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: Il formato [{1}] non è presente nel DB
     */
    public static final String MM_COMP_003_001 = "MM-COMP-003-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: L''hash forzato non è valido (sono ammessi solo hash SHA-1 in formato
     * hexBinary)
     */
    public static final String MM_COMP_004_001 = "MM-COMP-004-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: DEPRECATO Il tag &lt;URNFile&gt; obbligatorio e non valorizzato
     */
    public static final String MM_COMP_005_001 = "MM-COMP-005-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: Non è stato trovato un componente di versamento corrispondente
     */
    public static final String MM_COMP_006_001 = "MM-COMP-006-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: Il componente di versamento deve essere di tipo FILE
     */
    public static final String MM_COMP_007_001 = "MM-COMP-007-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: La forzatura di firme o formati è possibile solo sui componenti
     */
    public static final String MM_COMP_008_001 = "MM-COMP-008-001";

    /**
     * Componente Multimedia &lt;ID&gt; {0}: La forzatura di firme o formati è possibile solo sui componenti privi di
     * sottocomponenti
     */
    public static final String MM_COMP_009_001 = "MM-COMP-009-001";

    /**
     * Componente di versamento &lt;ID&gt; {0}: Non è stato trovato un componente Multimedia corrispondente
     */
    public static final String MM_COMP_010_001 = "MM-COMP-010-001";

    /**
     * Il file ZIP {0} dichiarato nell''indiceMM non esiste o non è raggiungibile
     */
    public static final String MM_FILE_001_001 = "MM-FILE-001-001";

    /**
     * Il file {0} riferito nel componente Multimedia &lt;ID&gt; {1} non esiste o non è raggiungibile
     */
    public static final String MM_FILE_002_001 = "MM-FILE-002-001";

    /**
     * Il file {0} riferito nel componente Multimedia &lt;ID&gt; {1} non è presente nel file ZIP {2}
     */
    public static final String MM_FILE_003_001 = "MM-FILE-003-001";

    /**
     * La directory {0} dichiarata nell''indiceMM non esiste o non è raggiungibile
     */
    public static final String MM_FILE_004_001 = "MM-FILE-004-001";

    /**
     * l''Ambiente {0} è definito da caratteri non compresi nel set POSIX per i nomi di file: è impossbile archiviare il
     * documento sul filesystem
     */
    public static final String FILESYS_001_001 = "FILESYS-001-001";

    /**
     * l''Ente {0} è definito da caratteri non compresi nel set POSIX per i nomi di file: è impossibile archiviare il
     * documento sul filesystem
     */
    public static final String FILESYS_001_002 = "FILESYS-001-002";

    /**
     * la Struttura {0} è definita da caratteri non compresi nel set POSIX per i nomi di file: è impossbile archiviare
     * il documento sul filesystem
     */
    public static final String FILESYS_001_003 = "FILESYS-001-003";

    /**
     * il Registro {0} è definito da caratteri non compresi nel set POSIX per i nomi di file: è impossbile archiviare il
     * documento sul filesystem
     */
    public static final String FILESYS_001_004 = "FILESYS-001-004";

    /**
     * il Numero {0} è definito da caratteri non compresi nel set POSIX per i nomi di file: è impossbile archiviare il
     * documento sul filesystem
     */
    public static final String FILESYS_001_005 = "FILESYS-001-005";

    /**
     * il componente {0} non può essere archiviato su filesystem perchè il suo pathname supera i 254 caratteri
     */
    public static final String FILESYS_002_001 = "FILESYS-002-001";

    /**
     * il sottocomponente {0} non può essere archiviato su filesystem perchè il suo pathname supera i 254 caratteri
     */
    public static final String FILESYS_002_002 = "FILESYS-002-002";

    /**
     * impossibile procedere con l''archiviazione su filesystem perché lo spazio su disco residuo è prossimo
     * all''esaurimento
     */
    public static final String FILESYS_003_001 = "FILESYS-003-001";

    /**
     * La richiesta di recupero dell''unità documentaria {0} è stata accettata. Il recupero dei file è in corso
     */
    public static final String SESREC_001_001 = "SESREC-001-001";

    /**
     * La precedente richiesta di recupero dei file dell''unità documentaria {0} è in errore: [{1}]. SACER proverà
     * nuovamente a rendere disponibili i file dell''unità documentaria non appena possibile
     */
    public static final String SESREC_001_002 = "SESREC-001-002";

    /**
     * Il recupero dei file dell''unità documentaria {0} è in corso
     */
    public static final String SESREC_002_001 = "SESREC-002-001";

    /**
     * L''utente ha già in corso il recupero di file dell''unità documentaria {0}: si deve aspettare che tale recupero
     * sia terminato
     */
    public static final String SESREC_002_002 = "SESREC-002-002";

    /**
     * E'' già in corso il recupero di file dell''unità documentaria {0} da parte di un altro utente
     */
    public static final String SESREC_002_003 = "SESREC-002-003";

    /**
     * Il recupero dei file dell''unità documentaria {0} è in corso da parte di un operatore on-line; per il momento non
     * è possibile richiedere il recupero dell''unità documentaria mediante servizio web
     */
    public static final String SESREC_002_004 = "SESREC-002-004";

    /**
     * Il recupero dei file dell''unità documentaria {0} è in corso da parte del servizio di recupero; per il momento
     * non è possibile richiedere il recupero dell''unità documentaria mediante download on-line
     */
    public static final String SESREC_002_005 = "SESREC-002-005";

    /**
     * Non sono stati individuati componenti il cui formato di rappresentazione sia compatibile con il recupero DIP
     */
    public static final String RECDIP_001_001 = "RECDIP-001-001";

    /**
     * Il convertitore {0} del formato di rappresentazione {1}, relativo al componente {2}, risulta marcato come ERRATO,
     * impossibile procedere con la trasformazione
     */
    public static final String RECDIP_001_002 = "RECDIP-001-002";

    /**
     * Il formato di rappresentazione {0}, relativo al componente {1} con formato {2}, richiede un file {3}, impossibile
     * procedere con la trasformazione
     */
    public static final String RECDIP_001_003 = "RECDIP-001-003";

    /**
     * Il convertitore {0} del formato di rappresentazione {1}, relativo al componente {2}, ha reso un errore di
     * conversione ed è stato marcato come ERRATO: {3}
     */
    public static final String RECDIP_002_001 = "RECDIP-002-001";

    /**
     * Nessun modello di esibizione definito per la struttura di appartenenza dell''unità documentaria da esibire {0}
     */
    public static final String RECDIP_003_001 = "RECDIP-003-001";

    /**
     * L''utente che ha attivato il servizio non esiste oppure non è attivo oppure la sua password non è valida
     */
    public static final String RICH_ANN_VERS_001 = "RICH-ANN-VERS-001";

    /**
     * Lo xml ricevuto non è coerente con lo xsd previsto
     */
    public static final String RICH_ANN_VERS_002 = "RICH-ANN-VERS-002";

    /**
     * La versione dell''XML di richiesta non è fra quelle previste: {0}
     */
    public static final String RICH_ANN_VERS_003 = "RICH-ANN-VERS-003";

    /**
     * L''ambiente specificato non esiste
     */
    public static final String RICH_ANN_VERS_004 = "RICH-ANN-VERS-004";

    /**
     * L''ente specificato non esiste
     */
    public static final String RICH_ANN_VERS_005 = "RICH-ANN-VERS-005";

    /**
     * La struttura versante specificata non esiste
     */
    public static final String RICH_ANN_VERS_006 = "RICH-ANN-VERS-006";

    /**
     * L''utente specificato nell''xml non coincide con quello che ha attivato il servizio
     */
    public static final String RICH_ANN_VERS_007 = "RICH-ANN-VERS-007";

    /**
     * L''utente che ha attivato il servizio non è abilitato alla struttura versante oppure non è autorizzato ad
     * attivare il servizio
     */
    public static final String RICH_ANN_VERS_008 = "RICH-ANN-VERS-008";

    /**
     * Nella struttura versante corrente, è già presente una richiesta di annullamento versamenti con lo stesso codice
     * con stato diverso da INVIO_FALLITO
     */
    public static final String RICH_ANN_VERS_009 = "RICH-ANN-VERS-009";

    /**
     * La versione dell''xml di richiesta specificata nell''xml non coincide con quello ricevuto nell''attivazione del
     * web service
     */
    public static final String RICH_ANN_VERS_010 = "RICH-ANN-VERS-010";

    /**
     * Nessuna unità documentaria definita nella richiesta è annullabile
     */
    public static final String RICH_ANN_VERS_011 = "RICH-ANN-VERS-011";

    /**
     * Alcune unità documentarie definite nella richiesta non sono annullabili
     */
    public static final String RICH_ANN_VERS_012 = "RICH-ANN-VERS-012";

    /**
     * Per almeno una unità documentaria definita nella richiesta è necessario provvedere al suo annullamento preventivo
     * in PreIngest
     */
    public static final String RICH_ANN_VERS_013 = "RICH-ANN-VERS-013";

    /**
     * Nella struttura versante corrente è già presente una richiesta di annullamento versamenti con lo stesso codice e
     * con stato pari a CHIUSA
     */
    public static final String RICH_ANN_VERS_014 = "RICH-ANN-VERS-014";

    /**
     * Nella struttura versante corrente è già presente una richiesta di annullamento versamenti con lo stesso codice e
     * con stato pari a EVASA
     */
    public static final String RICH_ANN_VERS_015 = "RICH-ANN-VERS-015";

    /**
     * Forza annullamento o Richiesta da PreIngest definiti ma la versione del WS non li supporta
     */
    public static final String RICH_ANN_VERS_016 = "RICH-ANN-VERS-016";

    /**
     * Errore inaspettato al salvataggio: {0}
     */
    public static final String SERVIZI_USR_001 = "SERVIZI-USR-001";

    /**
     * Utente gia'' presente
     */
    public static final String SERVIZI_USR_002 = "SERVIZI-USR-002";

    /**
     * Esiste un utente con lo stesso userid
     */
    public static final String SERVIZI_USR_003 = "SERVIZI-USR-003";

    /**
     * Utente non e'' presente
     */
    public static final String SERVIZI_USR_004 = "SERVIZI-USR-004";

    /**
     * Utente che attiva il servizio non riconosciuto o non abilitato
     */
    public static final String SERVIZI_USR_005 = "SERVIZI-USR-005";

    // </editor-fold>
}
