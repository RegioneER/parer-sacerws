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

package it.eng.parer.ws.versamento.ejb.prs;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import static it.eng.parer.util.DateUtilsConverter.convert;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationException;
import javax.xml.validation.Schema;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.OrgStrut;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.ControlliTpi;
import it.eng.parer.ws.ejb.ControlliWS;
import it.eng.parer.ws.ejb.XmlVersCache;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.LogSessioneUtils;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.utils.VerificaVersione;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.utils.XmlValidationEventHandler;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.RispostaWSAggAll;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VersamentoExtAggAll;
import it.eng.parer.ws.versamento.ejb.ControlliPerFirme;
import it.eng.parer.ws.versamento.ejb.RapportoVersBuilder;
import it.eng.parer.ws.versamento.utils.Conteggi;
import it.eng.parer.ws.versamentoTpi.utils.FileServUtils;
import it.eng.parer.ws.xml.versReq.IntestazioneAggAllType;
import it.eng.parer.ws.xml.versReq.TipoConservazioneType;
import it.eng.parer.ws.xml.versReq.UnitaDocAggAllegati;
import it.eng.parer.ws.xml.versResp.ECConfigurazioneType;
import it.eng.parer.ws.xml.versResp.ECDocumentoType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarType;
import it.eng.parer.ws.xml.versResp.ECUnitaDocAggAllType;
import it.eng.parer.ws.xml.versResp.EsitoVersAggAllegati;
import it.eng.parer.ws.xml.versResp.SCVersatoreType;
import java.util.List;
import java.util.Map;

@Stateless(mappedName = "VersamentoExtAggAllPrsr")
@LocalBean
public class VersamentoExtAggAllPrsr {

    private static final Logger log = LoggerFactory.getLogger(VersamentoExtAggAllPrsr.class);
    // stateless ejb per i controlli sul db
    @EJB
    private ControlliSemantici controlliSemantici;
    // un altro stateless ejb per altri controlli sul db
    @EJB
    private ControlliPerFirme controlliPerFirme;
    // stateless ejb per verifica autorizzazione ws
    @EJB
    private ControlliWS controlliEjb;
    // singleton ejb di gestione cache dei parser Castor
    @EJB
    private XmlVersCache xmlVersCache;
    // stateless ejb per le operazioni relative al versamento su filesystem
    @EJB
    private ControlliTpi controlliTpi;
    // stateless ejb crea il rapporto di versamento e canonicalizza (C14N) il SIP
    @EJB
    private RapportoVersBuilder rapportoVersBuilder;
    // stateless ejb per recupero configurazioni
    @EJB
    private ConfigurationHelper configurationHelper;
    // stateless ejb di verifica e parsing dei documenti (tutti i tipi)
    @EJB
    DocumentoVersPrsr myDocumentoVersPrsr;

    @SuppressWarnings("unchecked")
    public void parseXML(SyncFakeSessn sessione, VersamentoExtAggAll versamento,
            RispostaWSAggAll rispostaWs) {
        EsitoVersAggAllegati myEsito = rispostaWs.getIstanzaEsito();
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();
        StringReader tmpReader;
        // l'istanza dell'unità documentaria decodificata dall'XML di versamento
        UnitaDocAggAllegati parsedUnitaDoc = null;

        // istanzia la classe di verifica strutturale
        Conteggi tmpConteggi = new Conteggi();

        // istanzia la classe di verifica retrocompatibilità
        VerificaVersione tmpVerificaVersione = new VerificaVersione();
        VerificaVersione.EsitiVerfica tmpEsitiVerfica;
        //
        RispostaControlli rispostaControlli = new RispostaControlli();

        //
        versamento.setDatiXml(sessione.getDatiIndiceSipXml());

        /*
         * produco la versione canonicalizzata del SIP. Gestisco l'eventuale errore relativo
         * all'encoding indicato in maniera errata (es. "ISO8859/1" oppure "utf8"), non rilevato
         * dalla verifica precedente.
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli = rapportoVersBuilder.canonicalizzaDaSalvareIndiceSip(sessione);
            if (!rispostaControlli.isrBoolean()) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
                myEsito.getEsitoXSD().setControlloStrutturaXML(rispostaControlli.getDsErr());
            }
        }

        /*
         * Si utilizza il SIP canonicalized onde evitare problemi di encoding
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpReader = new StringReader(sessione.getDatiC14NIndiceSip());
            XmlValidationEventHandler validationHandler = new XmlValidationEventHandler();
            try {
                myAvanzamentoWs.setFase("Unmarshall XML").logAvanzamento();

                Unmarshaller tmpUnmarshaller = xmlVersCache.getVersReqCtxforUDAggAllegati()
                        .createUnmarshaller();
                Schema schema = xmlVersCache.getSchemaOfVersReq();
                tmpUnmarshaller.setSchema(schema);
                tmpUnmarshaller.setEventHandler(validationHandler);
                parsedUnitaDoc = (UnitaDocAggAllegati) tmpUnmarshaller.unmarshal(tmpReader);
                versamento.setVersamento(parsedUnitaDoc);

            } catch (UnmarshalException e) {
                ValidationEvent event = validationHandler.getFirstErrorValidationEvent();
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: XML malformato nel blocco di dati generali. Eccezione: "
                                + event.getMessage());
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_001_001, event.getMessage());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setControlloStrutturaXML(
                        "Errore: XML malformato nel blocco di dati generali. Eccezione: "
                                + event.getMessage());
            } catch (ValidationException e) {
                ValidationEvent event = validationHandler.getFirstErrorValidationEvent();
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: XML malformato nel blocco di dati generali. Eccezione: "
                                + event.getMessage());
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_001_002, event.getMessage());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setControlloStrutturaXML(
                        "Errore di validazione del blocco di dati generali. Eccezione: "
                                + event.getMessage());
            } catch (Exception e) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage("Impossibile convalidare il documento");
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_001_002,
                        rispostaWs.getErrorMessage());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setControlloStrutturaXML(MessaggiWSBundle
                        .getString(MessaggiWSBundle.XSD_001_002, rispostaWs.getErrorMessage()));
                log.error("Impossibile convalidare il documento", e);
            }
        }

        // se l'unmarshalling è andato bene
        // recupero la versione XML di versamento
        // preparo la risposta relativa all'UD
        // registro nel flight recorder dove sono arrivato
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            myEsito.setVersioneXMLChiamata(parsedUnitaDoc.getIntestazione().getVersione());
            myEsito.setUnitaDocumentaria(new it.eng.parer.ws.xml.versResp.ECUnitaDocAggAllType());
            myAvanzamentoWs.setFase("Unmarshall OK")
                    .setChAnno(
                            Long.toString(parsedUnitaDoc.getIntestazione().getChiave().getAnno()))
                    .setChNumero(parsedUnitaDoc.getIntestazione().getChiave().getNumero())
                    .setChRegistro(parsedUnitaDoc.getIntestazione().getChiave().getTipoRegistro())
                    .setVrsAmbiente(parsedUnitaDoc.getIntestazione().getVersatore().getAmbiente())
                    .setVrsEnte(parsedUnitaDoc.getIntestazione().getVersatore().getEnte())
                    .setVrsStruttura(parsedUnitaDoc.getIntestazione().getVersatore().getStruttura())
                    .setVrsUser(parsedUnitaDoc.getIntestazione().getVersatore().getUserID())
                    .logAvanzamento();
        }

        // POPOLO I CAMPI DI UNITADOCUMENTARIA: Chiave, DataVersamento, e Versatore
        // (opzionale, solo per le versioni
        // recenti del WS)
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            it.eng.parer.ws.xml.versResp.SCChiaveType chiaveType = new it.eng.parer.ws.xml.versResp.SCChiaveType();
            chiaveType
                    .setAnno(Long.toString(parsedUnitaDoc.getIntestazione().getChiave().getAnno()));
            chiaveType.setNumero(parsedUnitaDoc.getIntestazione().getChiave().getNumero());
            chiaveType.setTipoRegistro(
                    parsedUnitaDoc.getIntestazione().getChiave().getTipoRegistro());
            myEsito.getUnitaDocumentaria().setChiave(chiaveType);
            myEsito.getUnitaDocumentaria().setDataVersamento(myEsito.getDataVersamento());

            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_ESTESI_1_3_OUT)) {
                myEsito.getUnitaDocumentaria().setVersatore(new SCVersatoreType());
                myEsito.getUnitaDocumentaria().getVersatore()
                        .setAmbiente(parsedUnitaDoc.getIntestazione().getVersatore().getAmbiente());
                myEsito.getUnitaDocumentaria().getVersatore()
                        .setEnte(parsedUnitaDoc.getIntestazione().getVersatore().getEnte());
                myEsito.getUnitaDocumentaria().getVersatore().setStruttura(
                        parsedUnitaDoc.getIntestazione().getVersatore().getStruttura());
                myEsito.getUnitaDocumentaria().getVersatore()
                        .setUserID(parsedUnitaDoc.getIntestazione().getVersatore().getUserID());
                //
                if (versamento.getModificatoriWSCalc()
                        .contains(ModificatoriWS.TAG_ESTESI_1_5_OUT)) {
                    myEsito.getUnitaDocumentaria().getVersatore()
                            .setUtente(parsedUnitaDoc.getIntestazione().getVersatore().getUtente());
                }
            }
        }

        // imposta il flag globale di simulazione scrittura
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (parsedUnitaDoc.getConfigurazione() != null) {
                if (parsedUnitaDoc.getConfigurazione().isSimulaSalvataggioDatiInDB() != null) {
                    versamento.setSimulaScrittura(
                            parsedUnitaDoc.getConfigurazione().isSimulaSalvataggioDatiInDB());
                    if (versamento.isSimulaScrittura()) {
                        IntestazioneAggAllType intestazione = parsedUnitaDoc.getIntestazione();
                        LogSessioneUtils.logSimulazioni(intestazione.getVersatore().getAmbiente(),
                                intestazione.getVersatore().getEnte(),
                                intestazione.getVersatore().getStruttura(),
                                intestazione.getChiave().getTipoRegistro(),
                                intestazione.getChiave().getAnno(),
                                intestazione.getChiave().getNumero(),
                                intestazione.getVersatore().getUserID(), log);
                    }
                }
            }
        }

        myAvanzamentoWs.setFase("verifica semantica unità documentaria - inizio").logAvanzamento();

        /*
         * come prima cosa verifico che il versatore e la versione dichiarati nel WS coincidano con
         * quelli nell'xml
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK && !parsedUnitaDoc.getIntestazione()
                .getVersione().equals(versamento.getVersioneWsChiamata())) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_001_013,
                    parsedUnitaDoc.getIntestazione().getVersione());
            myEsito.getEsitoChiamataWS().setVersioneWSCorretta(ECEsitoPosNegType.NEGATIVO);
        }

        if (rispostaWs.getSeverity() == SeverityEnum.OK && !parsedUnitaDoc.getIntestazione()
                .getVersatore().getUserID().equals(versamento.getLoginName())) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_001_005,
                    parsedUnitaDoc.getIntestazione().getVersatore().getUserID());
            myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.NEGATIVO);
        }

        HashMap<String, String> xmlDefaults = new HashMap<>();

        // leggo i valori di default per il WS. Verrano applicati al DOM dell'unità
        // documentaria
        // durante la fase di verifica ID duplicati
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli.reset();
            rispostaControlli = controlliSemantici
                    .caricaDefaultDaDB(ParametroApplDB.TipoParametroAppl.VERSAMENTO_DEFAULT);
            if (!rispostaControlli.isrBoolean()) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {
                xmlDefaults = (HashMap<String, String>) rispostaControlli.getrObject();
            }
        }

        // verifica nel complesso se ci sono problemi strutturali, non verificati
        // tramite XSD
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpConteggi.verifica(parsedUnitaDoc, xmlDefaults);

            // questo test, sui documenti, ha senso perché l'ID Documento può essere nullo
            if (rispostaWs.getSeverity() == SeverityEnum.OK
                    && tmpConteggi.getStrutturaVersamentoAtteso().isTrovatiIdDocDuplicati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: ci sono degli ID documento duplicati oppure un ID è nullo.");
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_002_002);
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setUnivocitaIDComponenti(
                        "Errore: ci sono degli ID documento duplicati oppure un ID è nullo.");
            }

            // rimappa i componenti dichiarati in una nuova struttura dati
            if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                versamento.setStrutturaComponenti(tmpConteggi.getStrutturaVersamentoAtteso());
                versamento.getStrutturaComponenti().setDataVersamento(XmlDateUtility
                        .xmlGregorianCalendarToZonedDateTime(myEsito.getDataVersamento()));
            }

            if (rispostaWs.getSeverity() == SeverityEnum.OK
                    && tmpConteggi.getStrutturaVersamentoAtteso().isTrovatiIdCompDuplicati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: ci sono degli ID componenti duplicati oppure un ID è nullo.");
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_002_001);
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setUnivocitaIDComponenti(
                        "Errore: ci sono degli ID componenti duplicati oppure un ID è nullo.");
            }
        }

        // crea il tag [Configurazione] dell'esito partendo dai dati forniti dal
        // programma chiamante
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            ECConfigurazioneType tmpConfigurazione = new ECConfigurazioneType();
            /*
             * MAC #23544: questa configurazione predefinita è SBAGLIATA.I parametri
             * FORZA_COLLEGAMENTO (usato solo sul versamenti sincrono) e FORZA_CONSERVAZIONE sono
             * opzionali sul SIP. Se non vengono impostati valgono i valori definiti su DB. Mettendo
             * questo default diventa impossibile, a valle, capire se il valore "false" sia stato
             * impostato sul SIP oppure se non sia stata proprio creata la busta. Vedi anche
             * VersamentoExtPrsr, VerificaFirmeHash e VerificaFirmeHashAggAll cercando la stringa
             * MAC #23544.
             *
             * Aggiornamento: RIMOSSA la gestione di un default sui flag che sono presenti anche su
             * DB (ossia che possono essere presenti su SIP).
             */
            tmpConfigurazione.setForzaAccettazione(false);
            tmpConfigurazione.setTipoConservazione(TipoConservazioneType.SOSTITUTIVA.name());

            if (parsedUnitaDoc.getConfigurazione() != null) {
                if (parsedUnitaDoc.getConfigurazione().isForzaAccettazione() != null) {
                    tmpConfigurazione.setForzaAccettazione(
                            parsedUnitaDoc.getConfigurazione().isForzaAccettazione());
                }
                if (parsedUnitaDoc.getConfigurazione().isForzaConservazione() != null) {
                    tmpConfigurazione.setForzaConservazione(
                            parsedUnitaDoc.getConfigurazione().isForzaConservazione());
                }

                // recupera il sistema di migrazione, se presente
                if (parsedUnitaDoc.getConfigurazione().getSistemaDiMigrazione() != null) {
                    tmpConfigurazione.setSistemaDiMigrazione(
                            parsedUnitaDoc.getConfigurazione().getSistemaDiMigrazione());
                    versamento.getStrutturaComponenti().setSistemaDiMigrazione(
                            parsedUnitaDoc.getConfigurazione().getSistemaDiMigrazione());
                }

                if (parsedUnitaDoc.getConfigurazione().getTipoConservazione() != null
                        && parsedUnitaDoc.getConfigurazione()
                                .getTipoConservazione() == TipoConservazioneType.FISCALE) {
                    tmpConfigurazione.setTipoConservazione(TipoConservazioneType.FISCALE.name());
                }

                if (parsedUnitaDoc.getConfigurazione().getTipoConservazione() != null
                        && parsedUnitaDoc.getConfigurazione()
                                .getTipoConservazione() == TipoConservazioneType.MIGRAZIONE) {
                    tmpConfigurazione.setTipoConservazione(TipoConservazioneType.MIGRAZIONE.name());
                }

            }
            myEsito.setConfigurazione(tmpConfigurazione);
        }

        // verifica se la struttura della chiamata al ws è coerente con la versione
        // dichiarata
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpVerificaVersione.verifica(rispostaWs, versamento);
            tmpEsitiVerfica = tmpVerificaVersione.getEsitiVerfica();
            if (tmpEsitiVerfica.isFlgErrore()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(tmpEsitiVerfica.getCodErrore(),
                        tmpEsitiVerfica.getMessaggio());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
            }
        }

        // se ho passato la verifica strutturale, creo il ramo dell'xml di risposta
        // relativo ai controlli semantici
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            myEsito.getUnitaDocumentaria()
                    .setEsitoUnitaDocumentaria(new ECUnitaDocAggAllType.EsitoUnitaDocumentaria());
        }

        CSVersatore tmpCSVersatore = new CSVersatore();
        // verifica il versatore
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpCSVersatore
                    .setAmbiente(parsedUnitaDoc.getIntestazione().getVersatore().getAmbiente());
            tmpCSVersatore.setEnte(parsedUnitaDoc.getIntestazione().getVersatore().getEnte());
            tmpCSVersatore
                    .setStruttura(parsedUnitaDoc.getIntestazione().getVersatore().getStruttura());
            //
            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_VERSATORE_1_5)) {
                myEsito.getUnitaDocumentaria().getVersatore()
                        .setIndirizzoIp(sessione.getIpChiamante());
                myEsito.getUnitaDocumentaria().getVersatore()
                        .setSistemaVersante(versamento.getUtente().getSistemaVersante());
            }
            // sistema (new URN)
            String sistemaConservazione = configurationHelper
                    .getValoreParamApplicByApplic(ParametroApplDB.NM_SISTEMACONSERVAZIONE);
            tmpCSVersatore.setSistemaConservazione(sistemaConservazione);

            // set versatore
            versamento.getStrutturaComponenti().setVersatoreNonverificato(tmpCSVersatore);

            rispostaControlli.reset();
            rispostaControlli = controlliSemantici.checkIdStrut(tmpCSVersatore,
                    TipiWSPerControlli.VERSAMENTO_RECUPERO,
                    convert(versamento.getStrutturaComponenti().getDataVersamento()));
            if (rispostaControlli.getrLong() < 1) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setIdentificazioneVersatore(rispostaControlli.getDsErr());
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {
                // salvo idstruttura
                versamento.getStrutturaComponenti().setIdStruttura(rispostaControlli.getrLong());

                // salvo idUtente
                versamento.getStrutturaComponenti().setIdUser(versamento.getUtente().getIdUtente());

                // OK - popolo la risposta versamento
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setIdentificazioneVersatore("POSITIVO");
            }
        }

        // verifica se l'utente è autorizzato ad usare il WS sulla struttura
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            versamento.getUtente().setIdOrganizzazioneFoglia(
                    new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
            rispostaControlli.reset();
            rispostaControlli = controlliEjb.checkAuthWS(versamento.getUtente(),
                    versamento.getDescrizione(), TipiWSPerControlli.VERSAMENTO_RECUPERO);
            if (!rispostaControlli.isrBoolean()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
                myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.NEGATIVO);
            }
        }

        CSChiave tmpCSChiave = new CSChiave();
        // verifica la chiave
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpCSChiave
                    .setAnno(Long.valueOf(parsedUnitaDoc.getIntestazione().getChiave().getAnno()));
            tmpCSChiave.setNumero(parsedUnitaDoc.getIntestazione().getChiave().getNumero());
            tmpCSChiave.setTipoRegistro(
                    parsedUnitaDoc.getIntestazione().getChiave().getTipoRegistro());

            // set chiave
            versamento.getStrutturaComponenti().setChiaveNonVerificata(tmpCSChiave);

            rispostaControlli.reset();
            rispostaControlli = controlliSemantici.checkChiave(tmpCSChiave,
                    versamento.getStrutturaComponenti().getIdStruttura(),
                    ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
            if (rispostaControlli.isrBoolean() || rispostaControlli.getrLong() == -1) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setIdentificazioneChiave(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {

                // OK - popolo la risposta versamento
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setIdentificazioneChiave(ECEsitoPosNegType.POSITIVO);

                // memorizzo scud
                versamento.getStrutturaComponenti().setStatoConservazioneUnitaDoc(
                        (CostantiDB.StatoConservazioneUnitaDoc) rispostaControlli.getrObject());
                // memorizzo l'ID della chiave Unità doc trovata
                versamento.getStrutturaComponenti().setIdUnitaDoc(rispostaControlli.getrLong());

                /*
                 * salvo il tipo di salvataggio (blob in tabella o filesystem) nel caso del
                 * versamento, questa informazione viene recuperata durante la fase dei controlli di
                 * versamento
                 */
                versamento.getStrutturaComponenti().setTipoSalvataggioFile(
                        CostantiDB.TipoSalvataggioFile.valueOf(rispostaControlli.getrString()));
                versamento.getStrutturaComponenti()
                        .setDescTipologiaUnitaDocumentaria(rispostaControlli.getrStringExtended());

                // memorizzo tipo ud
                versamento.getStrutturaComponenti()
                        .setIdTipologiaUnitaDocumentaria((long) rispostaControlli.getrMap()
                                .get(RispostaControlli.ValuesOnrMap.ID_TIPO_UD.name()));

                // normalized cd_key
                versamento.getStrutturaComponenti().setNumeroUdNormalized((String) rispostaControlli
                        .getrMap().get(RispostaControlli.ValuesOnrMap.CD_KEY_NORMALIZED.name()));
            }
        }

        /*
         * Verifica del pregresso
         *
         * 1. Recupero parameto da DB
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // recupero parametro da DB
            rispostaControlli = controlliSemantici.getDtCalcInizioNuoviUrn();

            if (rispostaControlli.getCodErr() != null) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {
                // dt inizio calcolo nuovi urn
                versamento.getStrutturaComponenti()
                        .setDtInizioCalcoloNewUrn(rispostaControlli.getrDate());
            }
        }

        /*
         * Verifica del pregresso
         *
         * 2. Verifica univocità chiave normalizzata
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            //
            rispostaControlli = controlliSemantici.checkUniqueCdKeyNormalized(tmpCSChiave,
                    versamento.getStrutturaComponenti().getIdRegistroUnitaDoc(),
                    versamento.getStrutturaComponenti().getIdUnitaDoc(),
                    versamento.getStrutturaComponenti().getNumeroUdNormalized(),
                    versamento.getStrutturaComponenti().getDtInizioCalcoloNewUrn());

            // 666 error
            if (rispostaControlli.getCodErr() != null) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {
                // cd key normalized (se calcolato)
                if (StringUtils
                        .isBlank(versamento.getStrutturaComponenti().getNumeroUdNormalized())) {
                    versamento.getStrutturaComponenti()
                            .setNumeroUdNormalized(rispostaControlli.getrString());
                }
                // dt vers max
                versamento.getStrutturaComponenti().setDtVersMax(rispostaControlli.getrDate());
            }
        }

        /*
         * Calcolo delle chiavi normalizzate (versatore + UD)
         *
         * Una volta individuata la UD (verifica precedente) se non esite si calcola la cd_key
         * normalizzata
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // normalizing cs_versatore
            versamento.getStrutturaComponenti().setUrnPartVersatoreNormalized(
                    MessaggiWSFormat.formattaUrnPartVersatore(tmpCSVersatore, true,
                            Costanti.UrnFormatter.VERS_FMT_STRING));

            boolean cdKeyUnitaDocNormalizExists = true; // default
            // calcola e verifica la chiave normalizzata (se non trovato in precedenza)
            if (StringUtils.isBlank(versamento.getStrutturaComponenti().getNumeroUdNormalized())) {
                String cdKeyNormalized = MessaggiWSFormat.normalizingKey(tmpCSChiave.getNumero()); // base
                rispostaControlli = controlliSemantici.calcAndcheckCdKeyNormalized(
                        versamento.getStrutturaComponenti().getIdRegistroUnitaDoc(), tmpCSChiave,
                        cdKeyNormalized);
                // 666 error
                if (rispostaControlli.getCodErr() != null) {
                    cdKeyUnitaDocNormalizExists = false;
                    setRispostaWsError(rispostaWs, rispostaControlli);
                    rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                } else {
                    // normalized cd_key
                    versamento.getStrutturaComponenti()
                            .setNumeroUdNormalized(rispostaControlli.getrString());
                }
            }
            if (cdKeyUnitaDocNormalizExists) {
                CSChiave tmpCSChiaveWithKeyNorm = SerializationUtils.clone(tmpCSChiave);
                // set cd_key norm calculated before
                tmpCSChiaveWithKeyNorm
                        .setNumero(versamento.getStrutturaComponenti().getNumeroUdNormalized());
                // normalized cs_chiave
                versamento.getStrutturaComponenti().setUrnPartChiaveUdNormalized(
                        MessaggiWSFormat.formattaUrnPartUnitaDoc(tmpCSChiaveWithKeyNorm, true,
                                Costanti.UrnFormatter.UD_FMT_STRING));
            }

        }

        // determino come gestire gli hash dei componenti versati e i forza controllo
        // doc
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            rispostaControlli = controlliPerFirme
                    .getOrgStrutt(versamento.getStrutturaComponenti().getIdStruttura());
            if (rispostaControlli.getrLong() != -1) {
                OrgStrut os = (OrgStrut) rispostaControlli.getrObject();
                // build flags
                this.elabFlagSipVDB(myEsito, os, versamento, parsedUnitaDoc);
            } else {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore nel recupero della tabella di decodifica OrgStrut");
                log.error("Errore nel recupero della tabella di decodifica OrgStrut");
            }
        }

        String descChiaveUD = "";
        // memorizzo il versatore e la chiave in una variabile di appoggio per usarli in
        // diverse parti dell'xml di
        // risposta
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            versamento.getStrutturaComponenti()
                    .setUrnPartVersatore(MessaggiWSFormat.formattaUrnPartVersatore(tmpCSVersatore));
            descChiaveUD = MessaggiWSFormat.formattaUrnPartUnitaDoc(tmpCSChiave);
            versamento.getStrutturaComponenti().setUrnPartChiaveUd(descChiaveUD);
        }

        // controlo stato conservazione UD
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR
                && myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .getIdentificazioneChiave().equals(ECEsitoPosNegType.POSITIVO)) {
            // recupero stato conservazione
            CostantiDB.StatoConservazioneUnitaDoc scud = versamento.getStrutturaComponenti()
                    .getStatoConservazioneUnitaDoc();

            // forza
            boolean flForzaAggiuntaDocumento = parsedUnitaDoc.getConfigurazione() != null
                    && parsedUnitaDoc.getConfigurazione().isForzaAggiuntaDocumento() != null
                            ? parsedUnitaDoc.getConfigurazione().isForzaAggiuntaDocumento()
                                    .booleanValue()
                            : versamento.getStrutturaComponenti().isFlagForzaDocAgg();
            // warning
            boolean isWarn = false;
            if (flForzaAggiuntaDocumento) {
                isWarn = true;
            } else {
                isWarn = versamento.getStrutturaComponenti().isFlagAccettaDocNeg()
                        && (parsedUnitaDoc.getConfigurazione() != null
                                && parsedUnitaDoc.getConfigurazione().isForzaAccettazione() != null
                                && parsedUnitaDoc.getConfigurazione().isForzaAccettazione()
                                        .booleanValue());
            }

            // verifica stato conservazione
            if (scud == CostantiDB.StatoConservazioneUnitaDoc.IN_ARCHIVIO
                    || scud == CostantiDB.StatoConservazioneUnitaDoc.IN_CUSTODIA) {
                // warning
                if (versamento.getStrutturaComponenti().isFlagAbilitaForzaDocAgg() && isWarn) {
                    // set warning
                    rispostaWs.setSeverity(SeverityEnum.WARNING);
                    //
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setVerificaStatoConservazione(ECEsitoPosNegWarType.WARNING);
                    //
                    rispostaWs.setEsitoWsWarnBundle(MessaggiWSBundle.UD_013_001,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(tmpCSChiave));

                    // add on list
                    versamento.listErrAddWarningUd(descChiaveUD, MessaggiWSBundle.UD_013_001,
                            descChiaveUD);
                } else {
                    setRispostaWsError(rispostaWs, rispostaControlli);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setVerificaStatoConservazione(ECEsitoPosNegWarType.NEGATIVO);
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_013_001,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(tmpCSChiave));

                    // add on list
                    versamento.listErrAddErrorUd(descChiaveUD, MessaggiWSBundle.UD_013_001,
                            descChiaveUD);

                }
            } else {
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setVerificaStatoConservazione(ECEsitoPosNegWarType.POSITIVO);
            }
        }

        /*
         * se i documenti dell'UD dovranno essere memorizzati su nastro
         * (CostantiDB.TipoSalvataggioFile.FILE) verifica che gli elementi che comporranno il path
         * name dei file (versatore, chiave) siano coerenti con lo standard POSIX per i nomi dei
         * file.
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR && versamento.getStrutturaComponenti()
                .getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE) {
            FileServUtils fileServUtils = new FileServUtils();
            if (!fileServUtils.controllaSubPath(tmpCSVersatore.getAmbiente())) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_001_001,
                        tmpCSVersatore.getAmbiente());
            } else if (!fileServUtils.controllaSubPath(tmpCSVersatore.getEnte())) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_001_002,
                        tmpCSVersatore.getEnte());
            } else if (!fileServUtils.controllaSubPath(tmpCSVersatore.getStruttura())) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_001_003,
                        tmpCSVersatore.getStruttura());
            } else if (!fileServUtils.controllaSubPath(tmpCSChiave.getTipoRegistro())) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_001_004,
                        tmpCSChiave.getTipoRegistro());
            } else if (!fileServUtils.controllaSubPath(tmpCSChiave.getNumero())) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_001_005,
                        tmpCSChiave.getNumero());
            }
        }

        // leggo i parametri per la memorizzazione su file system, se necessario.
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR && versamento.getStrutturaComponenti()
                .getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE) {
            HashMap<String, String> tpiDefaults = new HashMap<>();
            rispostaControlli.reset();
            rispostaControlli = controlliSemantici
                    .caricaDefaultDaDB(ParametroApplDB.TipoParametroAppl.TPI);
            if (!rispostaControlli.isrBoolean()) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {
                tpiDefaults = (HashMap<String, String>) rispostaControlli.getrObject();
            }
            if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                versamento.getStrutturaComponenti()
                        .setTpiRootTpi(tpiDefaults.get(ParametroApplDB.TPI_ROOT_TPI));
                versamento.getStrutturaComponenti()
                        .setTpiRootVers(tpiDefaults.get(ParametroApplDB.TPI_ROOT_VERS));
                versamento.getStrutturaComponenti()
                        .setTpiRootArkVers(tpiDefaults.get(ParametroApplDB.TPI_ROOT_ARK_VERS));
                rispostaControlli.reset();
                rispostaControlli = controlliTpi.caricaRootPath();
                if (!rispostaControlli.isrBoolean()) {
                    setRispostaWsError(rispostaWs, rispostaControlli);
                    rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                } else {
                    versamento.getStrutturaComponenti()
                            .setTpiRootTpiDaSacer(rispostaControlli.getrString());
                    //
                    versamento.getStrutturaComponenti()
                            .setSubPathDataVers(MessaggiWSFormat.formattaSubPathData(
                                    versamento.getStrutturaComponenti().getDataVersamento()));
                    versamento.getStrutturaComponenti().setSubPathVersatoreArk(
                            MessaggiWSFormat.formattaSubPathVersatoreArk(tmpCSVersatore));
                    versamento.getStrutturaComponenti().setSubPathUnitaDocArk(
                            MessaggiWSFormat.formattaSubPathUnitaDocArk(tmpCSChiave));
                }
            }

            /*
             * se il TPI non è stato installato, vuol dire che tutta la gestione asincrona del
             * versamento basata su TIVOLI è inutilizabile. In questo caso lo storage dei documenti
             * avviene su una tabella di blob dedicata chiamata ARO_FILE_COMP con struttura identica
             * a ARO_CONTENUTO_COMP
             */
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                rispostaControlli = controlliTpi.verificaAbilitazioneTpi();
                if (rispostaControlli.isrBoolean()) {
                    versamento.getStrutturaComponenti().setTpiAbilitato(true);
                } else if (rispostaControlli.getCodErr() != null) {
                    setRispostaWsError(rispostaWs, rispostaControlli);
                    rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                }
            }
        }

        myAvanzamentoWs.setFase("verifica semantica unità documentaria - fine").logAvanzamento();

        // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
        // inizializzazione parser del documento
        // NB: può fallire, quindi passo RispostaWS che può tornare in stato di errore
        // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            myAvanzamentoWs.setFase("verifica semantica documento - inizio").logAvanzamento();
        }

        // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
        // GESTIONE DOCUMENTO (ALLEGATO, ANNESSO, ANNOTAZIONE) da aggiungere all'UD
        // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // preparo la struttura dell'esito destinata a descrivere l'esito del documento
            // aggiunto
            DocumentoVers valueDocVers = versamento.getStrutturaComponenti().getDocumentiAttesi()
                    .get(0);

            rispostaControlli = controlliSemantici.trovaNuovoProgDocumento(
                    versamento.getStrutturaComponenti().getIdUnitaDoc(),
                    valueDocVers.getCategoriaDoc().getValoreDb());
            if (rispostaControlli.getrLong() != -1) {
                valueDocVers.setProgressivo((int) rispostaControlli.getrLong());
            } else {
                setRispostaWsError(rispostaWs, rispostaControlli);
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }

            // ordine globale dei documenti
            rispostaControlli = controlliSemantici
                    .trovaNuovoNiOrdDocumento(versamento.getStrutturaComponenti().getIdUnitaDoc());
            if (rispostaControlli.getrLong() != -1) {
                valueDocVers.setNiOrdDoc((int) rispostaControlli.getrLong());
            } else {
                setRispostaWsError(rispostaWs, rispostaControlli);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }

            /*
             * verifica che il documento che si intende inserire non sia già presente. (in pratica
             * verifica che all'interno della stessa UD non sia presente un documento con
             * IDDocumento uguale)
             */
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                rispostaControlli.reset();
                String tmpIdDocumento;
                tmpIdDocumento = versamento.getStrutturaComponenti().getDocumentiAttesi().get(0)
                        .getRifDocumento().getIDDocumento();
                String tmpDescUd = MessaggiWSFormat.formattaUrnPartUnitaDoc(tmpCSChiave);
                rispostaControlli = controlliSemantici.checkDocumentoInUd(
                        versamento.getStrutturaComponenti().getIdUnitaDoc(), tmpIdDocumento,
                        tmpDescUd);
                if (!rispostaControlli.isrBoolean()) {
                    setRispostaWsError(rispostaWs, rispostaControlli);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setDocumentoUnivocoInUD(ECEsitoPosNegType.NEGATIVO);
                    rispostaWs.setSeverity(SeverityEnum.ERROR);
                    rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                    //
                    if (rispostaControlli.getCodErr().equals(MessaggiWSBundle.DOC_008_001)) {
                        rispostaWs.setErroreElementoDoppio(true);
                        rispostaWs.setIdElementoDoppio(rispostaControlli.getrLong());
                        rispostaWs.setUrnPartDocumentoDoppio(rispostaControlli.getrString());
                    }
                } else {
                    // OK - popolo la risposta versamento
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setDocumentoUnivocoInUD(ECEsitoPosNegType.POSITIVO);
                }
            }

            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                ECDocumentoType tmpDocumento = new ECDocumentoType();

                String tmpChiaveDoc = MessaggiWSFormat.formattaChiaveDocumento(descChiaveUD,
                        CategoriaDocumento.Documento, valueDocVers.getNiOrdDoc(), true,
                        Costanti.UrnFormatter.DOC_FMT_STRING_V2,
                        Costanti.UrnFormatter.PAD5DIGITS_FMT);
                tmpDocumento.setChiaveDoc(tmpChiaveDoc);
                tmpDocumento.setTipoDocumento(valueDocVers.getRifDocumento().getTipoDocumento());
                tmpDocumento.setEsitoDocumento(new ECDocumentoType.EsitoDocumento());
                tmpDocumento.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                tmpDocumento.getEsitoDocumento()
                        .setUnivocitaOrdinePresentazione(ECEsitoPosNegType.POSITIVO);
                tmpDocumento.getEsitoDocumento()
                        .setVerificaTipoDocumento(ECEsitoPosNegType.POSITIVO);

                if (valueDocVers.getCategoriaDoc() == CategoriaDocumento.Allegato) {
                    myEsito.getUnitaDocumentaria().setAllegato(tmpDocumento);
                }
                if (valueDocVers.getCategoriaDoc() == CategoriaDocumento.Annesso) {
                    myEsito.getUnitaDocumentaria().setAnnesso(tmpDocumento);
                }
                if (valueDocVers.getCategoriaDoc() == CategoriaDocumento.Annotazione) {
                    myEsito.getUnitaDocumentaria().setAnnotazione(tmpDocumento);
                }

                if (versamento.getModificatoriWSCalc()
                        .contains(ModificatoriWS.TAG_ESTESI_1_3_OUT)) {
                    tmpDocumento.setIDDocumento(valueDocVers.getRifDocumento().getIDDocumento());
                }

                valueDocVers.setRifDocumentoResp(tmpDocumento);
                myDocumentoVersPrsr.parseDocumentoGen(valueDocVers, tmpDocumento, versamento,
                        rispostaWs);
            }
            myAvanzamentoWs.resetFase().setFase("verifica semantica documento - fine")
                    .logAvanzamento();
        }

        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // prepara risposta con flag
            this.buildFlagsOnEsito(myEsito, versamento);
        }

        // MEV#23176
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // prepara risposta con urn sip
            this.buildUrnSipOnEsito(myEsito, versamento);
        }
        // end MEV#23176
    }

    private void elabFlagSipVDB(EsitoVersAggAllegati myEsito, OrgStrut os,
            VersamentoExtAggAll versamento, UnitaDocAggAllegati parsedUnitaDoc) {
        long idStrut = os.getIdStrut();
        long idAmbiente = os.getOrgEnte().getOrgAmbiente().getIdAmbiente();
        long idTipoUd = versamento.getStrutturaComponenti().getIdTipologiaUnitaDocumentaria();
        // forza accettazione
        boolean isForzaAccettazione = parsedUnitaDoc.getConfigurazione() != null
                && parsedUnitaDoc.getConfigurazione().isForzaAccettazione() != null
                && parsedUnitaDoc.getConfigurazione().isForzaAccettazione().booleanValue();
        // flags
        versamento.getStrutturaComponenti()
                .setFlagVerificaHash(configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_ABILITA_CONTR_HASH_VERS, idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        boolean flForzaHashVers = configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_FORZA_HASH_VERS, idStrut,
                        idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE);
        boolean flAccettaContrHashNeg = configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_HASH_NEG,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE);
        versamento.getStrutturaComponenti().setFlagAccettaContrHashNeg(flAccettaContrHashNeg);

        if (flForzaHashVers || (flAccettaContrHashNeg && isForzaAccettazione)) {
            versamento.getStrutturaComponenti().setFlagAccettaHashErrato(true);
        }

        //
        versamento.getStrutturaComponenti()
                .setFlagAbilitaForzaDocAgg(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ABILITA_FORZA_DOC_AGG,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti()
                .setFlagForzaDocAgg(
                        configurationHelper
                                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_FORZA_DOC_AGG,
                                        idStrut, idAmbiente, idTipoUd)
                                .equals(CostantiDB.Flag.TRUE));

        versamento.getStrutturaComponenti()
                .setFlagAccettaDocNeg(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_DOC_AGG_NEG,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        // default pre 1.5
        versamento.getStrutturaComponenti()
                .setConfigFlagForzaFormato(
                        configurationHelper
                                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_FORZA_FMT,
                                        idStrut, idAmbiente, idTipoUd)
                                .equals(CostantiDB.Flag.TRUE));

        // v1.5
        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_ABILITA_FORZA_1_5)) {

            // SIP vs DB
            boolean forzaAggDoc = false;
            if (parsedUnitaDoc.getConfigurazione() != null) {
                if (parsedUnitaDoc.getConfigurazione().isForzaAggiuntaDocumento() != null) {
                    forzaAggDoc = parsedUnitaDoc.getConfigurazione().isForzaAggiuntaDocumento()
                            .booleanValue();
                } else {
                    forzaAggDoc = versamento.getStrutturaComponenti().isFlagAbilitaForzaDocAgg();
                }

                flForzaHashVers = parsedUnitaDoc.getConfigurazione().isForzaHash() != null
                        ? parsedUnitaDoc.getConfigurazione().isForzaHash().booleanValue()
                        : flForzaHashVers;// se non da
                // SIP
            }

            myEsito.getConfigurazione().setForzaAggiuntaDocumento(forzaAggDoc);
            //
            myEsito.getConfigurazione().setAbilitaForzaAggiuntaDocumento(
                    versamento.getStrutturaComponenti().isFlagAbilitaForzaDocAgg());
            //
            myEsito.getConfigurazione().setAccettaControlloDocumentoAggiuntoNegativo(
                    versamento.getStrutturaComponenti().isFlagAccettaDocNeg());
            // è immutato
            // (vedi valore
            // precedente da
            // DB)
            versamento.getStrutturaComponenti().setConfigFlagForzaHashVers(flForzaHashVers);

            // override
            // setFlagAccettaHashErrato aggiornato (se il forza proviene da SIP)
            if (flForzaHashVers || (flAccettaContrHashNeg && isForzaAccettazione)) {
                versamento.getStrutturaComponenti().setFlagAccettaHashErrato(true);
            }

            boolean flForzaFmt = parsedUnitaDoc.getConfigurazione() != null
                    && parsedUnitaDoc.getConfigurazione().isForzaFormatoFile() != null
                            ? parsedUnitaDoc.getConfigurazione().isForzaFormatoFile().booleanValue()
                            : versamento.getStrutturaComponenti().isConfigFlagForzaFormato();// se
            // non
            // da
            // SIP
            // è
            // immutato
            // (vedi
            // valore precedente da DB)
            // override
            versamento.getStrutturaComponenti().setConfigFlagForzaFormato(flForzaFmt);
        }
    }

    /**
     *
     * @param myEsito esito versamento {@link EsitoVersAggAllegati} con flag impostati
     */
    private void buildFlagsOnEsito(EsitoVersAggAllegati myEsito, VersamentoExtAggAll versamento) {
        // prepara risposta con flag (v1.5)
        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_ABILITA_FORZA_1_5)) {
            myEsito.getConfigurazione().setForzaFormatoFile(
                    versamento.getStrutturaComponenti().isConfigFlagForzaFormato());
            //
            myEsito.getConfigurazione()
                    .setForzaHash(versamento.getStrutturaComponenti().isConfigFlagForzaHashVers());
            //
            myEsito.getConfigurazione().setAbilitaControlloHash(
                    versamento.getStrutturaComponenti().isFlagVerificaHash());
            //
            myEsito.getConfigurazione().setAccettaControlloHashNegativo(
                    versamento.getStrutturaComponenti().isFlagAccettaContrHashNeg());
            //
        }
    }

    // MEV#23176
    /**
     * @param myEsito
     */
    private void buildUrnSipOnEsito(EsitoVersAggAllegati myEsito, VersamentoExtAggAll versamento) {

        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_URN_SIP_1_5)) {
            //
            // calcolo l'URN del documento
            String tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(
                    versamento.getStrutturaComponenti().getUrnPartVersatore(),
                    versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
                    versamento.getStrutturaComponenti().getDocumentiAttesi().get(0)
                            .getUrnPartDocumentoNiOrdDoc(),
                    Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);

            // calcolo URN del SIP del documento
            String urnSip = MessaggiWSFormat.formattaUrnSip(tmpUrn,
                    Costanti.UrnFormatter.URN_SIP_DOC);

            //
            myEsito.setURNSIP(urnSip);
        }
    }
    // end MEV#23176

    private void setRispostaWsError(RispostaWSAggAll rispostaWs,
            RispostaControlli rispostaControlli) {
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        rispostaWs.setErrorCode(rispostaControlli.getCodErr());
        rispostaWs.setErrorMessage(rispostaControlli.getDsErr());
    }
}
