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

import static it.eng.parer.util.DateUtilsConverter.convert;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationException;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.OrgStrut;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.IRispostaWS;
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
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.KeyOrdUtility;
import it.eng.parer.ws.utils.KeySizeUtility;
import it.eng.parer.ws.utils.LogSessioneUtils;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.utils.VerificaVersione;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.utils.XmlValidationEventHandler;
import it.eng.parer.ws.versamento.dto.ConfigRegAnno;
import it.eng.parer.ws.versamento.dto.DatiRegistroFiscale;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.RispostaControlliAttSpec;
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.UnitaDocColl;
import it.eng.parer.ws.versamento.dto.VersamentoExt;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.ejb.ControlliPerFirme;
import it.eng.parer.ws.versamento.ejb.ControlliProfiliUd;
import it.eng.parer.ws.versamento.ejb.GestioneDatiSpec;
import it.eng.parer.ws.versamento.ejb.RapportoVersBuilder;
import it.eng.parer.ws.versamento.utils.Conteggi;
import it.eng.parer.ws.versamentoTpi.utils.FileServUtils;
import it.eng.parer.ws.xml.versReq.DocumentoCollegatoType;
import it.eng.parer.ws.xml.versReq.IntestazioneType;
import it.eng.parer.ws.xml.versReq.TipoConservazioneType;
import it.eng.parer.ws.xml.versReq.UnitaDocumentaria;
import it.eng.parer.ws.xml.versResp.ECConfigurazioneType;
import it.eng.parer.ws.xml.versResp.ECDocumentoType;
import it.eng.parer.ws.xml.versResp.ECEsitoExtType;
import it.eng.parer.ws.xml.versResp.ECEsitoGeneraleType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarDisType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarType;
import it.eng.parer.ws.xml.versResp.ECUnitaDocType;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;
import it.eng.parer.ws.xml.versResp.SCVersatoreType;

@Stateless(mappedName = "VersamentoExtPrsr")
@LocalBean
public class VersamentoExtPrsr {

    private static final Logger log = LoggerFactory.getLogger(VersamentoExtPrsr.class);
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
    // stateless ejb: crea il rapporto di versamento e canonicalizza (C14N) il SIP
    @EJB
    private RapportoVersBuilder rapportoVersBuilder;
    // stateless ejb per recupero configurazioni
    @EJB
    private ConfigurationHelper configurationHelper;
    // stateless ejb per i controlli sui profili ud
    @EJB
    private ControlliProfiliUd controlliProfiliUd;
    // stateless ejb di verifica e parsing dei documenti (tutti i tipi)
    @EJB
    private DocumentoVersPrsr myDocumentoVersPrsr;
    //
    @EJB
    private GestioneDatiSpec gestioneDatiSpec;

    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void parseXML(SyncFakeSessn sessione, RispostaWS rispostaWs, VersamentoExt versamento) {
        EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();
        StringReader tmpReader;
        UnitaDocumentaria parsedUnitaDoc = null;

        // istanzia la classe di verifica strutturale
        Conteggi tmpConteggi = new Conteggi();

        // istanzia la classe di verifica retrocompatibilità
        VerificaVersione tmpVerificaVersione = new VerificaVersione();
        VerificaVersione.EsitiVerfica tmpEsitiVerfica;

        RispostaControlli rispostaControlli = new RispostaControlli();

        XmlValidationEventHandler validationHandler = new XmlValidationEventHandler();
        Unmarshaller unmarshaller = null;
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            try {
                unmarshaller = xmlVersCache.getVersReqCtxforUD().createUnmarshaller();
                unmarshaller.setSchema(xmlVersCache.getSchemaOfVersReq());
                unmarshaller.setEventHandler(validationHandler);
            } catch (JAXBException ex) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore nella creazione dell'unmarshaller " + ex.getMessage());
                log.error("Errore nella creazione dell'unmarshaller dell'UnitaDocumentaria per ",
                        ex);
            }
        }

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
            myAvanzamentoWs.setFase("Unmarshall XML").logAvanzamento();

            try {
                parsedUnitaDoc = (UnitaDocumentaria) unmarshaller.unmarshal(tmpReader);
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
                myEsito.getEsitoXSD().setControlloStrutturaXML(MessaggiWSBundle
                        .getString(MessaggiWSBundle.XSD_001_001, event.getMessage()));
            } catch (ValidationException e) {
                ValidationEvent event = validationHandler.getFirstErrorValidationEvent();
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: XML malformato nel blocco di dati generali. Eccezione: "
                                + event.getMessage());
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_001_002, event.getMessage());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setControlloStrutturaXML(MessaggiWSBundle
                        .getString(MessaggiWSBundle.XSD_001_002, event.getMessage()));
            } catch (Exception e) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Impossibile convalidare il documento; controllare il campo <UnitaDocumentaria>/<ProfiloUnitaDocumentaria>/<Data>");
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
            myEsito.setUnitaDocumentaria(new it.eng.parer.ws.xml.versResp.ECUnitaDocType());
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
            //
            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_VERSATORE_1_5)) {
                myEsito.getUnitaDocumentaria().getVersatore()
                        .setIndirizzoIp(sessione.getIpChiamante());
                myEsito.getUnitaDocumentaria().getVersatore()
                        .setSistemaVersante(versamento.getUtente().getSistemaVersante());
            }
        }

        // imposta il flag globale di simulazione scrittura
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (parsedUnitaDoc.getConfigurazione() != null) {
                Boolean b = parsedUnitaDoc.getConfigurazione().isSimulaSalvataggioDatiInDB();
                if (b != null && b) {
                    versamento.setSimulaScrittura(true);
                    IntestazioneType intestazione = parsedUnitaDoc.getIntestazione();
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

        myAvanzamentoWs.setFase("verifica semantica unità documentaria - inizio").logAvanzamento();

        /*
         * come prima cosa verifico che il versatore e la versione dichiarati nel WS coincidano con
         * quelli nell'xml
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (!parsedUnitaDoc.getIntestazione().getVersione()
                    .equals(versamento.getVersioneWsChiamata())) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_001_013,
                        parsedUnitaDoc.getIntestazione().getVersione());
                myEsito.getEsitoChiamataWS().setVersioneWSCorretta(ECEsitoPosNegType.NEGATIVO);
            }
        }

        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (!parsedUnitaDoc.getIntestazione().getVersatore().getUserID()
                    .equals(versamento.getLoginName())) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_001_005,
                        parsedUnitaDoc.getIntestazione().getVersatore().getUserID());
                myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.NEGATIVO);
            }
        }

        HashMap<String, String> xmlDefaults = null;

        // leggo i valori di default per il WS. Verrano applicati al DOM dell'unità
        // documentaria
        // durante la fase di verifica ID duplicati e la verifica delle versioni
        // compatibili
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli.reset();
            rispostaControlli = controlliSemantici.caricaDefaultDaDB(new String[] {
                    ParametroApplDB.TipoParametroAppl.VERSAMENTO_DEFAULT,
                    ParametroApplDB.TipoParametroAppl.VERSIONI_WS });
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

            if (tmpConteggi.getStrutturaVersamentoAtteso().getTrovataDataNullaIn() != null) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: trovati uno o più campi di tipo data breve vuoti.");
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_002_003,
                        tmpConteggi.getStrutturaVersamentoAtteso().getTrovataDataNullaIn());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setControlloStrutturaXML("Un campo di tipo data breve "
                        + "deve essere espresso nella forma aaaa-mm-gg");
            }

            // questo test, sui documenti, ha senso perché l'ID Documento può essere nullo
            if (rispostaWs.getSeverity() == SeverityEnum.OK
                    && tmpConteggi.getStrutturaVersamentoAtteso().isTrovatiIdDocDuplicati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: ci sono degli ID documento duplicati oppure un ID è nullo.");
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_002_002);
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setUnivocitaIDDocumenti(
                        "Errore: ci sono degli ID documento duplicati oppure un ID è nullo.");
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

            // rimappa i componenti dichiarati in una nuova struttura dati
            if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                versamento.setStrutturaComponenti(tmpConteggi.getStrutturaVersamentoAtteso());

                ZonedDateTime d = (myEsito.getDataVersamento() == null) ? null
                        : XmlDateUtility
                                .xmlGregorianCalendarToZonedDateTime(myEsito.getDataVersamento());
                versamento.getStrutturaComponenti().setDataVersamento(d);
            }
        }

        // verifica se il numero di allegati coincide con quanto dichiarato
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (!versamento.getStrutturaComponenti().isCorrAllegatiDichiarati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: num. allegati non coincide con quanto dichiarato");
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_003_001);
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD()
                        .setCorrispondenzaAllegatiDichiarati(ECEsitoPosNegType.NEGATIVO);
            }
        }

        // verifica se il numero di annessi coincide con quanto dichiarato
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (!versamento.getStrutturaComponenti().isCorrAnnessiDichiarati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs
                        .setErrorMessage("Errore: num. annessi non coincide con quanto dichiarato");
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_004_001);
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD()
                        .setCorrispondenzaAnnessiDichiarati(ECEsitoPosNegType.NEGATIVO);
            }
        }

        // verifica se il numero di annotazioni coincide con quanto dichiarato
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (!versamento.getStrutturaComponenti().isCorrAnnotazioniDichiarati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode("-1");
                rispostaWs.setErrorMessage(
                        "Errore: num. annotazioni non coincide con quanto dichiarato");
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.XSD_005_001);
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD()
                        .setCorrispondenzaAnnotazioniDichiarate(ECEsitoPosNegType.NEGATIVO);
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
             * VersamentoExtAggAllPrsr, VerificaFirmeHash e VerificaFirmeHashAggAll cercando la
             * stringa MAC #23544.
             *
             * Aggiornamento: RIMOSSA la gestione di un default sui flag che sono presenti anche su
             * DB (ossia che possono essere presenti su SIP).
             */
            tmpConfigurazione.setForzaAccettazione(false);
            tmpConfigurazione
                    .setTipoConservazione(CostantiDB.DatiCablati.VERS_TIPO_CONSERVAZIONE_DEFAULT);

            if (parsedUnitaDoc.getConfigurazione() != null) {
                if (parsedUnitaDoc.getConfigurazione().isForzaAccettazione() != null) {
                    tmpConfigurazione.setForzaAccettazione(
                            parsedUnitaDoc.getConfigurazione().isForzaAccettazione());
                }
                if (parsedUnitaDoc.getConfigurazione().isForzaCollegamento() != null) {
                    tmpConfigurazione.setForzaCollegamento(
                            parsedUnitaDoc.getConfigurazione().isForzaCollegamento());
                }
                if (parsedUnitaDoc.getConfigurazione().isForzaConservazione() != null) {
                    tmpConfigurazione.setForzaConservazione(
                            parsedUnitaDoc.getConfigurazione().isForzaConservazione());
                }

                // recupera il sistema di migrazione, se presente
                tmpConfigurazione.setSistemaDiMigrazione(
                        parsedUnitaDoc.getConfigurazione().getSistemaDiMigrazione());
                versamento.getStrutturaComponenti().setSistemaDiMigrazione(
                        parsedUnitaDoc.getConfigurazione().getSistemaDiMigrazione());

                // recupera il tipo conservazione, se presente. Altrimenti usa quello di default
                if (parsedUnitaDoc.getConfigurazione().getTipoConservazione() != null) {
                    tmpConfigurazione.setTipoConservazione(
                            parsedUnitaDoc.getConfigurazione().getTipoConservazione().name());
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
                    .setEsitoUnitaDocumentaria(new ECUnitaDocType.EsitoUnitaDocumentaria());
        }

        CSVersatore tmpCSVersatore = new CSVersatore();
        // verifica il versatore
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpCSVersatore
                    .setAmbiente(parsedUnitaDoc.getIntestazione().getVersatore().getAmbiente());
            tmpCSVersatore.setEnte(parsedUnitaDoc.getIntestazione().getVersatore().getEnte());
            tmpCSVersatore
                    .setStruttura(parsedUnitaDoc.getIntestazione().getVersatore().getStruttura());
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

        // verifica se l'eventuale sistema di migrazione esiste
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (myEsito.getConfigurazione().getSistemaDiMigrazione() != null
                    && myEsito.getConfigurazione().getSistemaDiMigrazione().length() > 0) {
                rispostaControlli.reset();
                rispostaControlli = controlliSemantici
                        .checkSistemaMigraz(myEsito.getConfigurazione().getSistemaDiMigrazione());
                if (!rispostaControlli.isrBoolean()) {
                    setRispostaWsError(rispostaWs, rispostaControlli);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setIdentificazioneVersatore(rispostaControlli.getDsErr());
                    rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                }
            }
        }

        CSChiave tmpCSChiave = new CSChiave();
        String descChiaveUD = "";
        // memorizzo il versatore e la chiave in una variabile di appoggio per usarli in
        // diverse parti dell'xml di
        // risposta
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpCSChiave
                    .setAnno(Long.valueOf(parsedUnitaDoc.getIntestazione().getChiave().getAnno()));
            tmpCSChiave.setNumero(parsedUnitaDoc.getIntestazione().getChiave().getNumero());
            tmpCSChiave.setTipoRegistro(
                    parsedUnitaDoc.getIntestazione().getChiave().getTipoRegistro());

            // set chiave
            versamento.getStrutturaComponenti().setChiaveNonVerificata(tmpCSChiave);

            if (rispostaWs.getSeverity() == SeverityEnum.OK) {
                versamento.getStrutturaComponenti().setUrnPartVersatore(
                        MessaggiWSFormat.formattaUrnPartVersatore(tmpCSVersatore));
                descChiaveUD = MessaggiWSFormat.formattaUrnPartUnitaDoc(tmpCSChiave);
                versamento.getStrutturaComponenti().setUrnPartChiaveUd(descChiaveUD);
            }
        }

        // verifica la chiave
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli.reset();
            rispostaControlli = controlliSemantici.checkChiave(tmpCSChiave,
                    versamento.getStrutturaComponenti().getIdStruttura(),
                    ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
            if (!rispostaControlli.isrBoolean()) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setUnivocitaChiave(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
                //
                if (rispostaControlli.getCodErr().equals(MessaggiWSBundle.UD_002_001)) {
                    rispostaWs.setErroreElementoDoppio(true);
                    rispostaWs.setIdElementoDoppio(rispostaControlli.getrLong());
                }
            } else {
                // OK - popolo la risposta versamento
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setUnivocitaChiave(ECEsitoPosNegType.POSITIVO);
            }
        }

        // verifica la tipologia UD
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli.reset();
            rispostaControlli = controlliSemantici.checkTipologiaUD(
                    parsedUnitaDoc.getIntestazione().getTipologiaUnitaDocumentaria(), descChiaveUD,
                    versamento.getStrutturaComponenti().getIdStruttura());
            if (!rispostaControlli.isrBoolean()) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {
                // salvo l'id tipologia UD
                versamento.getStrutturaComponenti()
                        .setIdTipologiaUnitaDocumentaria(rispostaControlli.getrLong());
                versamento.getStrutturaComponenti()
                        .setDescTipologiaUnitaDocumentaria(rispostaControlli.getrStringExtended());

                // salvo il tipo di salvataggio (blob in tabella o filesystem)
                versamento.getStrutturaComponenti().setTipoSalvataggioFile(
                        CostantiDB.TipoSalvataggioFile.valueOf(rispostaControlli.getrString()));
            }
        }

        /*
         * se i documenti dell'UD dovranno essere memorizzati su nastro
         * (CostantiDB.TipoSalvataggioFile.FILE) verifica che gli elementi che comporranno il path
         * name dei file (versatore, chiave) siano coerenti con lo standard POSIX per i nomi dei
         * file.
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK && versamento.getStrutturaComponenti()
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

        // Questa classe definisce la configurazione dei path per il TPI nella verifica
        // della lunghezza del campo numero della chiave UD
        KeySizeUtility.TpiConfig tpiConfVerificaNumkeyUD = null;

        // leggo i parametri per la memorizzazione su file system, se necessario.
        if (rispostaWs.getSeverity() == SeverityEnum.OK && versamento.getStrutturaComponenti()
                .getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE) {
            Map<String, String> tpiDefaults = new HashMap<>();
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
                // imposta i valori della configurazione TPI per la verifica del numero della
                // chiave UD
                tpiConfVerificaNumkeyUD = new KeySizeUtility.TpiConfig(
                        versamento.getStrutturaComponenti().getTpiRootTpi(),
                        versamento.getStrutturaComponenti().getTpiRootArkVers());

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
            if (rispostaWs.getSeverity() == SeverityEnum.OK) {
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

        DatiRegistroFiscale datiRegistroFiscale = null;

        // verifico che la tipologia unità documentaria e il tipo registro siano tra
        // loro compatibili
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli.reset();
            rispostaControlli = controlliSemantici.checkTipoRegistroTipoUD(
                    parsedUnitaDoc.getIntestazione().getChiave().getTipoRegistro(), descChiaveUD,
                    parsedUnitaDoc.getIntestazione().getTipologiaUnitaDocumentaria(),
                    versamento.getStrutturaComponenti().getIdStruttura(),
                    versamento.getStrutturaComponenti().getIdTipologiaUnitaDocumentaria());
            if (rispostaControlli.isrBoolean()) {
                // salvo l'id del tipo registro
                versamento.getStrutturaComponenti()
                        .setIdRegistroUnitaDoc(rispostaControlli.getrLong());
                datiRegistroFiscale = (DatiRegistroFiscale) rispostaControlli.getrObject();
            } else {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
        }

        // verifico che il tipo registro sia valido nell'anno indicato
        // e recupero la configurazione per il formato del numero relativa
        // al registro/anno
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli.reset();
            rispostaControlli = controlliSemantici.caricaRegistroAnno(
                    parsedUnitaDoc.getIntestazione().getChiave().getTipoRegistro(), descChiaveUD,
                    Long.valueOf(parsedUnitaDoc.getIntestazione().getChiave().getAnno()),
                    versamento.getStrutturaComponenti().getIdRegistroUnitaDoc());
            if (rispostaControlli.isrBoolean()) {
                versamento.getStrutturaComponenti()
                        .setConfigRegAnno((ConfigRegAnno) rispostaControlli.getrObject());
            } else {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            }
        }

        /*
         * una volta verificata la chiave, la struttura, il registro si normalizza
         * versatore/chiave/cdkey
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            // normalizing cs_versatore
            versamento.getStrutturaComponenti().setUrnPartVersatoreNormalized(
                    MessaggiWSFormat.formattaUrnPartVersatore(tmpCSVersatore, true,
                            Costanti.UrnFormatter.VERS_FMT_STRING));

            // normalizing cs_chiave
            // calcola e verifica la chiave normalizzata
            String cdKeyNormalized = MessaggiWSFormat.normalizingKey(tmpCSChiave.getNumero()); // base
            rispostaControlli = controlliSemantici.calcAndcheckCdKeyNormalized(
                    versamento.getStrutturaComponenti().getIdRegistroUnitaDoc(), tmpCSChiave,
                    cdKeyNormalized);
            // 666 error
            if (rispostaControlli.getCodErr() != null) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
            } else {
                // normalized cd_key
                versamento.getStrutturaComponenti()
                        .setNumeroUdNormalized(rispostaControlli.getrString());

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

        // Determino se accettare o meno numeri di chiave con formato non ammesso.
        // Determino inoltre come gestire gli hash dei componenti versati e se
        // la data e l'oggetto del Profilo UD sono obbligatori
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            rispostaControlli = controlliPerFirme
                    .getOrgStrutt(versamento.getStrutturaComponenti().getIdStruttura());
            if (rispostaControlli.getrLong() != -1) {
                OrgStrut os = (OrgStrut) rispostaControlli.getrObject();
                controlliPerFirme.retrieveOrgEnteFor(os);
                // build flags
                this.elabFlagSipVDB(os, versamento, parsedUnitaDoc);
            } else {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore nel recupero della tabella di decodifica OrgStrut");
                log.error("Errore nel recupero della tabella di decodifica OrgStrut");
            }
        }

        // se non sono presenti il profilo UD oppure non è presente la data o l'oggetto
        // verifico se sono opzionali, in base ai flag letti al punto precedente
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (versamento.getStrutturaComponenti().isFlagProfiloUdObbOggetto()
                    && (parsedUnitaDoc.getProfiloUnitaDocumentaria() == null
                            || parsedUnitaDoc.getProfiloUnitaDocumentaria().getOggetto() == null
                            || parsedUnitaDoc.getProfiloUnitaDocumentaria().getOggetto()
                                    .isEmpty())) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_014_001, descChiaveUD);
            }
        }

        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (versamento.getStrutturaComponenti().isFlagProfiloUdObbData()
                    && (parsedUnitaDoc.getProfiloUnitaDocumentaria() == null
                            || parsedUnitaDoc.getProfiloUnitaDocumentaria().getData() == null)) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_014_002, descChiaveUD);
            }
        }

        // verifico se la chiave va bene in funzione del formato atteso per il numero
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            // inizializzo il calcolo della lunghezza massima del campo numero
            KeySizeUtility tmpKeySizeUtility = new KeySizeUtility(tmpCSVersatore, tmpCSChiave,
                    tpiConfVerificaNumkeyUD);
            // decodifico il tipo calcolo per l'ordinamento
            KeyOrdUtility tmpKeyOrdUtility;
            tmpKeyOrdUtility = new KeyOrdUtility(
                    versamento.getStrutturaComponenti().getConfigRegAnno(),
                    tmpKeySizeUtility.getMaxLenNumero());
            // verifico se la chiave va bene
            rispostaControlli = tmpKeyOrdUtility.verificaChiave(tmpCSChiave);
            if (rispostaControlli.isrBoolean()) {
                KeyOrdUtility.KeyOrdResult keyOrdResult = (KeyOrdUtility.KeyOrdResult) rispostaControlli
                        .getrObject();
                // salvo la chiave per l'ordinamento
                versamento.getStrutturaComponenti()
                        .setKeyOrdCalcolata(keyOrdResult.getKeyOrdCalcolata());
                versamento.getStrutturaComponenti()
                        .setProgressivoCalcolato(keyOrdResult.getProgressivoCalcolato());
            } else {
                // calcolo la chiave di ordinamento, come se il numero fosse di tipo GENERICO
                RispostaControlli rc = tmpKeyOrdUtility.calcolaKeyOrdGenerica(tmpCSChiave);
                KeyOrdUtility.KeyOrdResult keyOrdResult = (KeyOrdUtility.KeyOrdResult) rc
                        .getrObject();
                versamento.getStrutturaComponenti()
                        .setKeyOrdCalcolata(keyOrdResult.getKeyOrdCalcolata());
                versamento.getStrutturaComponenti().setProgressivoCalcolato(null);
                versamento.getStrutturaComponenti().setWarningFormatoNumero(true);
            }

            // gestione response
            if (versamento.getStrutturaComponenti().isFlagAbilitaContrFmtNumero()) {
                if (rispostaControlli.isrBoolean()) {
                    // salvo la chiave per l'ordinamento
                    versamento.getStrutturaComponenti().setWarningFormatoNumero(false); // inutile,
                    // aggiunto
                    // per
                    // chiarezza
                    // OK - popolo la risposta versamento
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setVerificaTipologiaUD(ECEsitoPosNegType.POSITIVO);
                } else if (versamento.getStrutturaComponenti().isConfigFlagForzaFormatoNumero()
                        || (versamento.getStrutturaComponenti().isFlagAccettaErroreFmtNumero()
                                && myEsito.getConfigurazione().isForzaAccettazione())) {

                    // scrivi warning nella lista warning...
                    versamento.addWarningUd(descChiaveUD, rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);

                    // se ho accettato con forzatura il numero, verifico che sia ancora possibile
                    // produrre un URN valido
                    // se non è possibile, il versamento viene comunque bloccato
                    if (tmpKeySizeUtility.getLenURN() > KeySizeUtility.MAX_LEN_URN) {
                        versamento.listErrAddErrorUd(descChiaveUD, MessaggiWSBundle.UD_007_003,
                                descChiaveUD);
                        myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                                .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                                .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                    }
                } else {
                    // scrivi errore nella lista...
                    versamento.addErrorUd(descChiaveUD, rispostaControlli.getCodErr(),
                            rispostaControlli.getDsErr());
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                            .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                }
                // calcolo l'esito del ws in funzione di warning ed errori
                VoceDiErrore tmpVdE = versamento.calcolaErrorePrincipale();
                if (tmpVdE != null) {
                    rispostaWs.setSeverity(tmpVdE.getSeverity());
                    if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                        rispostaWs.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                    } else if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                        rispostaWs.setEsitoWsWarning(tmpVdE.getErrorCode(),
                                tmpVdE.getErrorMessage());
                    }
                }
            }
        }

        // verifico se registro fiscale -> conservazione fiscale e viceversa
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            String tipoConservazione = myEsito.getConfigurazione().getTipoConservazione();
            // $MEV15309 (aggiunta MIGRAZIONE come tipo conservazione accetatta per reg
            // fiscale)
            if (datiRegistroFiscale.isFlRegistroFisc() && (!(TipoConservazioneType.FISCALE.name()
                    .equals(tipoConservazione))
                    && !(TipoConservazioneType.MIGRAZIONE.name().equals(tipoConservazione)))) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                // Unità Documentaria {0}: conservazione NON FISCALE nonostante il registro sia
                // fiscalmente rilevante
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_009_001, descChiaveUD);
            }
            if (!datiRegistroFiscale.isFlRegistroFisc()
                    && TipoConservazioneType.FISCALE.name().equals(tipoConservazione)) {
                setRispostaWsError(rispostaWs, rispostaControlli);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setVerificaTipologiaUD(ECEsitoPosNegType.NEGATIVO);
                // Unità Documentaria {0}: conservazione FISCALE nonostante il registro NON sia
                // fiscalmente rilevante
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_009_002, descChiaveUD);
            }
        }

        // **************************************************************************
        // NOTA Bene:
        // la numerazione fiscale NON viene più verificata
        // **************************************************************************
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR
                && versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_DATISPEC_EXT)) {
            // alloca la classe di verifica ed estrazione dei dati specifici e dei dati
            // specifici di migrazione
            // NB: questa allocazione può fallire. Al termine devo verificare lo stato di
            // RispostaWS e gestire il
            // problema
            if (rispostaWs.getSeverity() == SeverityEnum.ERROR) {
                rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
            }

            // verifico gli eventuali dati specifici e di migrazione
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                parseDatiSpecUnitadoc(rispostaWs, versamento, parsedUnitaDoc, gestioneDatiSpec);
            }
        }

        // verifica dei profili
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR
                && versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_PROFILI_1_5)) {
            verificaProfiliUnitaDoc(myEsito, versamento, rispostaControlli);
        }

        // verifico la presenza di UD collegate
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            verificaUdCollegate(tmpCSVersatore, tpiConfVerificaNumkeyUD, rispostaWs, versamento,
                    rispostaControlli, parsedUnitaDoc);
        }

        myAvanzamentoWs.setFase("verifica semantica unità documentaria - fine").logAvanzamento();

        // verifico i documenti che compongono l'UD
        // buona parte del lavoro viene svolta dalla classe
        // DocumentoVersPrsr che viene usata anche per il ws di aggiunta documenti.
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            this.verificaDocumentiUd(rispostaWs, versamento);
        }

        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // prepara risposta con flag
            buildFlagsOnEsito(myEsito, versamento);
        }

        // MEV#23176
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // prepara risposta con urn sip
            buildUrnSipOnEsito(myEsito, versamento);
        }
        // end MEV#23176
    }

    private void elabFlagSipVDB(OrgStrut os, VersamentoExt versamento,
            UnitaDocumentaria parsedUnitaDoc) {
        //
        boolean isConfigurazioneOnSip = !Objects.isNull(parsedUnitaDoc.getConfigurazione());
        //
        long idStrut = os.getIdStrut();
        long idAmbiente = os.getOrgEnte().getOrgAmbiente().getIdAmbiente();
        long idTipoUd = versamento.getStrutturaComponenti().getIdTipologiaUnitaDocumentaria();
        // forza accettazione
        boolean isForzaAccettazione = isConfigurazioneOnSip
                && parsedUnitaDoc.getConfigurazione().isForzaAccettazione() != null
                && parsedUnitaDoc.getConfigurazione().isForzaAccettazione().booleanValue();
        // flags
        versamento.getStrutturaComponenti()
                .setFlagAccettaErroreFmtNumero(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_FMT_NUM_NEG,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));
        //
        versamento.getStrutturaComponenti()
                .setConfigFlagForzaFormatoNumero(
                        configurationHelper
                                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_FORZA_FMT_NUM,
                                        idStrut, idAmbiente, idTipoUd)
                                .equals(CostantiDB.Flag.TRUE));
        //
        versamento.getStrutturaComponenti()
                .setFlagProfiloUdObbOggetto(
                        configurationHelper
                                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_OBBL_OGGETTO,
                                        idStrut, idAmbiente, idTipoUd)
                                .equals(CostantiDB.Flag.TRUE));

        //
        versamento.getStrutturaComponenti()
                .setFlagProfiloUdObbData(
                        configurationHelper
                                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_OBBL_DATA,
                                        idStrut, idAmbiente, idTipoUd)
                                .equals(CostantiDB.Flag.TRUE));
        /*
         * MAC #23544: La gestione del parametro FORZA_COLLEGAMENTO è opzionale sul SIP. Nel caso
         * non siano configurati sul SIP viene utilizzato il valore impostato sul DB. A causa
         * dell'errata impostazione del default su VersamentoExtPrsr e VersamentoExtAggAllPrsr qui
         * non posso utilizzare la configurazione esposta da myesito.getConfigurazione() ma
         * utilizzare il file xml originale, ovvero versamento.getVersamento().getConfigurazione()
         * I* Cerca lo stesso commento in VersamentoExtPrsr, VersamentoExtAggAllPrsr e
         * VerificaFirmeHashAggAll (tramite la stringa MAC #23544)
         */
        boolean forzaCollegamento = false;
        if (isConfigurazioneOnSip
                && parsedUnitaDoc.getConfigurazione().isForzaCollegamento() != null) {
            forzaCollegamento = parsedUnitaDoc.getConfigurazione().isForzaCollegamento()
                    .booleanValue();
        } else {
            forzaCollegamento = configurationHelper
                    .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_FORZA_COLLEG, idStrut,
                            idAmbiente, idTipoUd)
                    .equals(CostantiDB.Flag.TRUE);
        }
        versamento.getStrutturaComponenti().setConfigFlagForzaColleg(forzaCollegamento);

        //
        versamento.getStrutturaComponenti()
                .setFlagVerificaHash(configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_ABILITA_CONTR_HASH_VERS, idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        boolean flForzaHashVers = configurationHelper.getValoreParamApplicByTipoUdAsFl(
                ParametroApplFl.FL_FORZA_HASH_VERS, idStrut, idAmbiente, idTipoUd).equals("1");
        boolean flAccettaContrHashNeg = configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ACCETTA_CONTR_HASH_NEG,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE);
        versamento.getStrutturaComponenti().setFlagAccettaContrHashNeg(flAccettaContrHashNeg);

        if (flForzaHashVers || (flAccettaContrHashNeg && isForzaAccettazione)) {
            versamento.getStrutturaComponenti().setFlagAccettaHashErrato(true);
        }

        //
        versamento.getStrutturaComponenti().setConfigFlagAbilitaContrColleg(configurationHelper
                .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ABILITA_CONTR_COLLEG_UD,
                        idStrut, idAmbiente, idTipoUd)
                .equals(CostantiDB.Flag.TRUE));

        //
        versamento.getStrutturaComponenti()
                .setFlagAbilitaContrFmtNumero(configurationHelper
                        .getValoreParamApplicByTipoUdAsFl(ParametroApplFl.FL_ABILITA_CONTR_FMT_NUM,
                                idStrut, idAmbiente, idTipoUd)
                        .equals(CostantiDB.Flag.TRUE));

        //
        versamento.getStrutturaComponenti()
                .setFlagAccettaContrCollegNeg(configurationHelper.getValoreParamApplicByTipoUdAsFl(
                        ParametroApplFl.FL_ACCETTA_CONTR_COLLEG_NEG, idStrut, idAmbiente, idTipoUd)
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

            // sip vs db
            if (isConfigurazioneOnSip
                    && parsedUnitaDoc.getConfigurazione().isForzaFormatoNumero() != null) {
                versamento.getStrutturaComponenti().setConfigFlagForzaFormatoNumero(
                        parsedUnitaDoc.getConfigurazione().isForzaFormatoNumero().booleanValue());
            }

            flForzaHashVers = isConfigurazioneOnSip
                    && parsedUnitaDoc.getConfigurazione().isForzaHash() != null
                            ? parsedUnitaDoc.getConfigurazione().isForzaHash().booleanValue()
                            : flForzaHashVers;// se non da SIP
            // è immutato
            // (vedi valore
            // precedente da
            // DB)
            //
            versamento.getStrutturaComponenti().setConfigFlagForzaHashVers(flForzaHashVers);
            // override
            if (flForzaHashVers || (flAccettaContrHashNeg && isForzaAccettazione)) {
                versamento.getStrutturaComponenti().setFlagAccettaHashErrato(true);
            }

            boolean flForzaFmtNumero = isConfigurazioneOnSip
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
            versamento.getStrutturaComponenti().setConfigFlagForzaFormato(flForzaFmtNumero);

        }
    }

    private void verificaUdCollegate(CSVersatore csVersatore,
            KeySizeUtility.TpiConfig tpiConfVerificaNumkeyUD, RispostaWS rispostaWs,
            VersamentoExt versamento, RispostaControlli rispostaControlli,
            UnitaDocumentaria parsedUnitaDoc) {

        EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
        String descChiaveUD = versamento.getStrutturaComponenti().getUrnPartChiaveUd();

        boolean isConfigFlagAbilitaContrColleg = versamento.getStrutturaComponenti()
                .isConfigFlagAbilitaContrColleg();

        boolean collegamentoHasErr = false;
        boolean collegamentoHasWarn = false;
        rispostaControlli.reset();
        // se il tag <DocumentiCollegati> esiste
        if (parsedUnitaDoc.getDocumentiCollegati() != null) {
            CSChiave tmpCsChiaveColl = new CSChiave();
            // l'insieme di tutti i collegamenti (già accorpati nelle descrizioni)
            // che verrà memorizzato sul DB
            Map<List<String>, UnitaDocColl> tmpCollegamentiUd = new HashMap<>();

            // CICLO FOR SUI DOCUMENTI COLLEGATI
            for (DocumentoCollegatoType.DocumentoCollegato documCollMd : parsedUnitaDoc
                    .getDocumentiCollegati().getDocumentoCollegato()) {
                tmpCsChiaveColl
                        .setTipoRegistro(documCollMd.getChiaveCollegamento().getTipoRegistro());
                tmpCsChiaveColl
                        .setAnno(Long.valueOf(documCollMd.getChiaveCollegamento().getAnno()));
                tmpCsChiaveColl.setNumero(documCollMd.getChiaveCollegamento().getNumero());

                // cerco se si sta tentando di collegare la stessa UD più volte
                String[] tmpArr = new String[] {
                        "", "", "" };
                tmpArr[0] = tmpCsChiaveColl.getNumero();
                tmpArr[1] = tmpCsChiaveColl.getTipoRegistro();
                tmpArr[2] = String.valueOf(tmpCsChiaveColl.getAnno());
                List<String> tmpChiaveUdAsList = Arrays.asList(tmpArr);

                UnitaDocColl tmpUnitaDocColl = tmpCollegamentiUd.get(tmpChiaveUdAsList);
                if (tmpUnitaDocColl == null) {
                    // in questo caso non ho censito altri collegamenti alla stessa UD.
                    String descChiaveUdColl = MessaggiWSFormat
                            .formattaUrnPartUnitaDoc(tmpCsChiaveColl);
                    tmpUnitaDocColl = new UnitaDocColl();
                    // inserisco il collegamento nella collezione di collegamenti censiti
                    tmpCollegamentiUd.put(tmpChiaveUdAsList, tmpUnitaDocColl);
                    // aggiungo al versamento il riferimento all'unità documentaria collegata
                    versamento.getStrutturaComponenti().getUnitaDocCollegate().add(tmpUnitaDocColl);

                    tmpUnitaDocColl.setChiave(documCollMd.getChiaveCollegamento());
                    // inserisco la sua descrizione nella collezione di descrizioni per il
                    // collegamento da salvare (potrebbero esserici altri colegamenti alla stessa
                    // UD che non ho ancora processato).
                    tmpUnitaDocColl
                            .aggiungiDescrizioneUnivoca(documCollMd.getDescrizioneCollegamento());
                    //
                    // questo flag mi permette di capire se devo effettuare la verifica
                    // formale della chiave dell'UD riferita (Registro, Anno, Formato numero).
                    // esistono 3 casi:
                    // 1 - l'UD esiste: non faccio il controllo perché sarebbe inutile e potrebbe
                    // addirittura bloccare il collegamento ad UD inserite con forzature o con
                    // registri disattivati
                    // 2 - l'UD non esiste e non sto forzando il collegamento: non perdo tempo
                    // a fare verifiche su un'UD già certamente in errore
                    // 3 - l'UD non esiste e sto forzando il collegamento: faccio le verifiche per
                    // evitare di caricare un riferimento ad una UD che presumibilmente non
                    // potrà mai esistere.
                    boolean faiVerificaFormaleChiave = false;
                    //
                    // verifico il collegamento, se punta ad un'UD esistente
                    rispostaControlli = controlliSemantici.checkChiave(tmpCsChiaveColl,
                            versamento.getStrutturaComponenti().getIdStruttura(),
                            ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);

                    tmpUnitaDocColl.setIdUnitaDocLink(rispostaControlli.getrLong());

                    if (rispostaControlli.getrLong() == -1 && isConfigFlagAbilitaContrColleg) { // se
                        // non
                        // ha
                        // trovato
                        // la
                        // chiave...
                        if (rispostaControlli.isrBoolean()) { // se in ogni caso la query è andata a
                            // buon fine
                            if (versamento.getStrutturaComponenti().isConfigFlagForzaColleg()
                                    || versamento.getStrutturaComponenti()
                                            .isFlagAccettaContrCollegNeg()
                                            && myEsito.getConfigurazione().isForzaAccettazione()) {
                                // "Chiave dell'unità documentaria collegata non presente"
                                versamento.listErrAddWarningUd(descChiaveUD,
                                        MessaggiWSBundle.UD_004_001, descChiaveUD,
                                        descChiaveUdColl);
                                collegamentoHasWarn = true;
                                // in questo caso devo verificare se la chiave collegamento è
                                // formalmente corretta
                                faiVerificaFormaleChiave = true;
                            } else {
                                versamento.listErrAddErrorUd(descChiaveUD,
                                        MessaggiWSBundle.UD_004_001, descChiaveUD,
                                        descChiaveUdColl);
                                collegamentoHasErr = true; // non lo ha trovato -> va in errore e
                                // passa ad elaborare il collegamento successivo,
                                // inutile verificare eventuali altri errori su questo collegamento.
                                // Per altro anche verificare altri collegamenti ha poco senso,
                                // visto
                                // che questo versamento è già condannato.
                                continue;
                            }
                        } else {
                            // allora è un errore di database..
                            // non ha trovato la chiave ma non ha impostato a true
                            // il bool di rispostacontrolli
                            setRispostaWsError(rispostaWs, rispostaControlli);
                            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                                    rispostaControlli.getDsErr());
                            break;
                        }
                    }

                    if (faiVerificaFormaleChiave) {
                        long tmpIdTipoRegistroUdColl = 0;
                        // test per vedere se la chiave dell'UD riferita è corretta
                        // verifica il tipo registro
                        rispostaControlli = controlliSemantici.checkTipoRegistro(
                                tmpCsChiaveColl.getTipoRegistro(), descChiaveUD,
                                versamento.getStrutturaComponenti().getIdStruttura(),
                                descChiaveUdColl);

                        if (rispostaControlli.isrBoolean()) {
                            // salvo l'id del tipo registro
                            tmpIdTipoRegistroUdColl = rispostaControlli.getrLong();
                        } else {
                            if (rispostaControlli.getCodErr() != null && rispostaControlli
                                    .getCodErr().equals(MessaggiWSBundle.ERR_666)) {
                                // è un errore di database..
                                setRispostaWsError(rispostaWs, rispostaControlli);
                                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                                        rispostaControlli.getDsErr());
                                break;
                            } else {
                                // è un errore di registro... non faccio gli altri test ma proseguo
                                // con la verifica degli altri collegamenti. Anche in questo
                                // caso il versamento è condannato e la cosa probabilmente non
                                // ha molto senso.
                                versamento.addErrorUd(descChiaveUD, rispostaControlli.getCodErr(),
                                        rispostaControlli.getDsErr());
                                collegamentoHasErr = true;
                                continue;
                            }
                        }

                        ConfigRegAnno tmpConfigRegAnno = null;
                        // verifico che il tipo registro sia valido nell'anno indicato
                        // e recupero la configurazione per il formato del numero relativa
                        // al registro/anno
                        rispostaControlli = controlliSemantici.caricaRegistroAnno(
                                tmpCsChiaveColl.getTipoRegistro(), descChiaveUD,
                                tmpCsChiaveColl.getAnno(), tmpIdTipoRegistroUdColl,
                                descChiaveUdColl);
                        if (rispostaControlli.isrBoolean()) {
                            tmpConfigRegAnno = (ConfigRegAnno) rispostaControlli.getrObject();
                        } else {
                            if (rispostaControlli.getCodErr() != null && rispostaControlli
                                    .getCodErr().equals(MessaggiWSBundle.ERR_666)) {
                                // è un errore di database..
                                setRispostaWsError(rispostaWs, rispostaControlli);
                                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(),
                                        rispostaControlli.getDsErr());
                                break;
                            } else {
                                // è un errore di compatibilità tra anno e registro...
                                // non faccio gli altri test ma proseguo
                                // con la verifica degli altri collegamenti. Anche in questo
                                // caso il versamento è condannato e la cosa probabilmente non
                                // ha molto senso.
                                versamento.addErrorUd(descChiaveUD, rispostaControlli.getCodErr(),
                                        rispostaControlli.getDsErr());
                                collegamentoHasErr = true;
                                continue;
                            }
                        }

                        // inizializzo il calcolo della lunghezza massima del campo numero
                        KeySizeUtility tmpKeySizeUtility = new KeySizeUtility(csVersatore,
                                tmpCsChiaveColl, tpiConfVerificaNumkeyUD);
                        // verifico se la chiave va bene in funzione del formato atteso per il
                        // numero
                        KeyOrdUtility tmpKeyOrdUtility;
                        tmpKeyOrdUtility = new KeyOrdUtility(tmpConfigRegAnno,
                                tmpKeySizeUtility.getMaxLenNumero());
                        // verifico se la chiave va bene
                        rispostaControlli = tmpKeyOrdUtility.verificaChiave(tmpCsChiaveColl,
                                descChiaveUD);
                        if (!rispostaControlli.isrBoolean()) {
                            versamento.addErrorUd(descChiaveUD, rispostaControlli.getCodErr(),
                                    rispostaControlli.getDsErr());
                            collegamentoHasErr = true;
                            continue;
                        }
                    } // fine if - fai verifica formale chiave
                } else {
                    // nei metadati esiste già un riferimento alla stessa UD:
                    // non deve rendere errore ma accorpare le descrizioni, eliminando le
                    // descrizioni doppie
                    // NOTA l'accorpamento deve comunque limitarsi a 254 caratteri
                    // (max lunghezza per la colonna in tabella).
                    // In questo caso non ha senso perdere tempo a rivalutare il collegamento all'UD
                    tmpUnitaDocColl
                            .aggiungiDescrizioneUnivoca(documCollMd.getDescrizioneCollegamento());
                    if (tmpUnitaDocColl.generaDescrizione()
                            .length() > UnitaDocColl.MAX_LEN_DESCRIZIONE) {
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.UD_004_006, descChiaveUD);
                        versamento.aggiungErroreFatale(myEsito.getEsitoGenerale().getCodiceErrore(),
                                myEsito.getEsitoGenerale().getMessaggioErrore());
                        collegamentoHasErr = true;
                        break;
                    }
                }
            } // fine loop sui collegamenti

            // test per evitare di calcolare l'errore se
            // ho avuto un problema al DB ed ho già un errore 666
            // altrimenti se ConfigFlagAbilitaContrColleg = true imposto l'esito generale di
            // versamento
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR && isConfigFlagAbilitaContrColleg) {
                // calcolo l'esito del ws in funzione di warning ed errori
                VoceDiErrore tmpVdE = versamento.calcolaErrorePrincipale();
                if (tmpVdE != null) {
                    rispostaWs.setSeverity(tmpVdE.getSeverity());
                    if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                        rispostaWs.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                    } else if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                        rispostaWs.setEsitoWsWarning(tmpVdE.getErrorCode(),
                                tmpVdE.getErrorMessage());
                    }
                }
            }
        } // fine tag DocumentiCollegati != null

        // gestione esito
        // se abilitato
        if (isConfigFlagAbilitaContrColleg) {
            // se una chiave non ha superato il test
            if (collegamentoHasErr) {
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setPresenzaUDCollegate(ECEsitoPosNegWarDisType.NEGATIVO);
            } else if (collegamentoHasWarn) {
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setPresenzaUDCollegate(ECEsitoPosNegWarDisType.WARNING);
            } else {
                // salvo l'esito positivo
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setPresenzaUDCollegate(ECEsitoPosNegWarDisType.POSITIVO);
            }
        } else {
            // salvo l'esito disabilitato
            myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                    .setPresenzaUDCollegate(ECEsitoPosNegWarDisType.DISABILITATO);
        }

    }

    private void verificaDocumentiUd(RispostaWS rispostaWs, VersamentoExt versamento) {
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();
        EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
        String descChiaveUD = versamento.getStrutturaComponenti().getUrnPartChiaveUd();

        myAvanzamentoWs.setFase("verifica semantica documenti - inizio").logAvanzamento();
        // classe di verifica e parsing dei documenti (tutti i tipi)
        // inizializzazione parser del documento
        // NB: può fallire, quindi passo RispostaWS che può tornare in stato di errore

        // GESTIONE DOCUMENTO PRINCIPALE, ALLEGATI, ANNESSI, ANNOTAZIONI
        // loop sui documenti (Principale, Allegati, Annessi e Annotazioni)
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            for (DocumentoVers valueDocVers : versamento.getStrutturaComponenti()
                    .getDocumentiAttesi()) {

                // GESTIONE DOCUMENTO PRINCIPALE
                if (valueDocVers.getCategoriaDoc() == CategoriaDocumento.Principale) {

                    // creo la parte di risposta versamento relativa al documento principale
                    if (myEsito.getUnitaDocumentaria().getDocumentoPrincipale() == null) {
                        ECDocumentoType documentoPrincipale = new ECDocumentoType();
                        documentoPrincipale
                                .setChiaveDoc(MessaggiWSFormat.formattaChiaveDocumento(descChiaveUD,
                                        CategoriaDocumento.Documento, valueDocVers.getNiOrdDoc(),
                                        true, Costanti.UrnFormatter.DOC_FMT_STRING_V2,
                                        Costanti.UrnFormatter.PAD5DIGITS_FMT));
                        documentoPrincipale.setTipoDocumento(
                                valueDocVers.getRifDocumento().getTipoDocumento());
                        documentoPrincipale.setEsitoDocumento(new ECDocumentoType.EsitoDocumento());
                        documentoPrincipale.getEsitoDocumento()
                                .setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                        documentoPrincipale.getEsitoDocumento()
                                .setUnivocitaOrdinePresentazione(ECEsitoPosNegType.POSITIVO);
                        documentoPrincipale.getEsitoDocumento()
                                .setVerificaTipoDocumento(ECEsitoPosNegType.POSITIVO);

                        if (versamento.getModificatoriWSCalc()
                                .contains(ModificatoriWS.TAG_ESTESI_1_3_OUT)) {
                            documentoPrincipale.setIDDocumento(
                                    valueDocVers.getRifDocumento().getIDDocumento());
                        }
                        myEsito.getUnitaDocumentaria().setDocumentoPrincipale(documentoPrincipale);
                        valueDocVers.setRifDocumentoResp(documentoPrincipale);
                    }

                    // verifica completa del documento
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        myDocumentoVersPrsr.parseDocumentoGen(valueDocVers,
                                myEsito.getUnitaDocumentaria().getDocumentoPrincipale(), versamento,
                                rispostaWs);
                    }
                }

                //////////////////////
                // GESTIONE ALLEGATI//
                //////////////////////
                if (valueDocVers.getCategoriaDoc() == CategoriaDocumento.Allegato) {
                    // creo la parte di risposta versamento relativo agli allegati
                    if (myEsito.getUnitaDocumentaria().getAllegati() == null) {
                        myEsito.getUnitaDocumentaria().setAllegati(new ECUnitaDocType.Allegati());
                    }

                    // creo la parte di risposta vesamento relativo ad uno specifico allegato
                    ECDocumentoType allegato = new ECDocumentoType();
                    allegato.setChiaveDoc(MessaggiWSFormat.formattaChiaveDocumento(descChiaveUD,
                            CategoriaDocumento.Documento, valueDocVers.getNiOrdDoc(), true,
                            Costanti.UrnFormatter.DOC_FMT_STRING_V2,
                            Costanti.UrnFormatter.PAD5DIGITS_FMT));
                    allegato.setTipoDocumento(valueDocVers.getRifDocumento().getTipoDocumento());
                    ECDocumentoType.EsitoDocumento esitoDocumento = new ECDocumentoType.EsitoDocumento();
                    esitoDocumento.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    esitoDocumento.setUnivocitaOrdinePresentazione(ECEsitoPosNegType.POSITIVO);
                    esitoDocumento.setVerificaTipoDocumento(ECEsitoPosNegType.POSITIVO);
                    allegato.setEsitoDocumento(esitoDocumento);

                    if (versamento.getModificatoriWSCalc()
                            .contains(ModificatoriWS.TAG_ESTESI_1_3_OUT)) {
                        allegato.setIDDocumento(valueDocVers.getRifDocumento().getIDDocumento());
                    }

                    myEsito.getUnitaDocumentaria().getAllegati().getAllegato().add(allegato);
                    valueDocVers.setRifDocumentoResp(allegato);

                    // verifica completa del documento
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        myDocumentoVersPrsr.parseDocumentoGen(valueDocVers, allegato, versamento,
                                rispostaWs);
                    }
                }
                ///////////////////////////////////////////////
                // FINE PARTE RISPOSTA RELATIVA AGLI ALLEGATI//
                ///////////////////////////////////////////////

                //////////////////////
                // GESTIONE ANNESSI //
                //////////////////////
                if (valueDocVers.getCategoriaDoc() == CategoriaDocumento.Annesso) {
                    // creo la parte di risposta versamento relativo agli annessi
                    if (myEsito.getUnitaDocumentaria().getAnnessi() == null) {
                        myEsito.getUnitaDocumentaria().setAnnessi(new ECUnitaDocType.Annessi());
                    }

                    // creo la parte di risposta vesamento relativo ad uno specifico allegato
                    ECDocumentoType annesso = new ECDocumentoType();
                    annesso.setChiaveDoc(MessaggiWSFormat.formattaChiaveDocumento(descChiaveUD,
                            CategoriaDocumento.Documento, valueDocVers.getNiOrdDoc(), true,
                            Costanti.UrnFormatter.DOC_FMT_STRING_V2,
                            Costanti.UrnFormatter.PAD5DIGITS_FMT));
                    annesso.setTipoDocumento(valueDocVers.getRifDocumento().getTipoDocumento());
                    ECDocumentoType.EsitoDocumento esitoDocumento = new ECDocumentoType.EsitoDocumento();
                    esitoDocumento.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    esitoDocumento.setUnivocitaOrdinePresentazione(ECEsitoPosNegType.POSITIVO);
                    esitoDocumento.setVerificaTipoDocumento(ECEsitoPosNegType.POSITIVO);
                    annesso.setEsitoDocumento(esitoDocumento);

                    if (versamento.getModificatoriWSCalc()
                            .contains(ModificatoriWS.TAG_ESTESI_1_3_OUT)) {
                        annesso.setIDDocumento(valueDocVers.getRifDocumento().getIDDocumento());
                    }

                    myEsito.getUnitaDocumentaria().getAnnessi().getAnnesso().add(annesso);
                    valueDocVers.setRifDocumentoResp(annesso);

                    // verifica completa del documento
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        myDocumentoVersPrsr.parseDocumentoGen(valueDocVers, annesso, versamento,
                                rispostaWs);
                    }
                }
                ///////////////////////////////////////////////
                // FINE PARTE RISPOSTA RELATIVA AGLI ANNESSI //
                ///////////////////////////////////////////////

                //////////////////////////
                // GESTIONE ANNOTAZIONI //
                //////////////////////////
                if (valueDocVers.getCategoriaDoc() == CategoriaDocumento.Annotazione) {
                    // creo la parte di risposta versamento relativo alle annotazioni
                    if (myEsito.getUnitaDocumentaria().getAnnotazioni() == null) {
                        myEsito.getUnitaDocumentaria()
                                .setAnnotazioni(new ECUnitaDocType.Annotazioni());
                    }

                    // creo la parte di risposta vesamento relativo ad uno specifico allegato
                    ECDocumentoType annotazione = new ECDocumentoType();
                    annotazione.setChiaveDoc(MessaggiWSFormat.formattaChiaveDocumento(descChiaveUD,
                            CategoriaDocumento.Documento, valueDocVers.getNiOrdDoc(), true,
                            Costanti.UrnFormatter.DOC_FMT_STRING_V2,
                            Costanti.UrnFormatter.PAD5DIGITS_FMT));
                    annotazione.setTipoDocumento(valueDocVers.getRifDocumento().getTipoDocumento());
                    ECDocumentoType.EsitoDocumento esitoDocumento = new ECDocumentoType.EsitoDocumento();
                    esitoDocumento.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    esitoDocumento.setUnivocitaOrdinePresentazione(ECEsitoPosNegType.POSITIVO);
                    esitoDocumento.setVerificaTipoDocumento(ECEsitoPosNegType.POSITIVO);
                    annotazione.setEsitoDocumento(esitoDocumento);

                    if (versamento.getModificatoriWSCalc()
                            .contains(ModificatoriWS.TAG_ESTESI_1_3_OUT)) {
                        annotazione.setIDDocumento(valueDocVers.getRifDocumento().getIDDocumento());
                    }

                    myEsito.getUnitaDocumentaria().getAnnotazioni().getAnnotazione()
                            .add(annotazione);
                    valueDocVers.setRifDocumentoResp(annotazione);

                    // verifica completa del documento
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        myDocumentoVersPrsr.parseDocumentoGen(valueDocVers, annotazione, versamento,
                                rispostaWs);
                    }

                }
                ///////////////////////////////////////////////////
                // FINE PARTE RISPOSTA RELATIVA ALLE ANNOTAZIONI //
                ///////////////////////////////////////////////////
            }
            myAvanzamentoWs.resetFase().setFase("verifica semantica documenti - fine")
                    .logAvanzamento();
        }
    }

    private void parseDatiSpecUnitadoc(RispostaWS rispostaWs, VersamentoExt versamento,
            UnitaDocumentaria parsedUnitaDoc, GestioneDatiSpec gestioneDatiSpec) {
        RispostaControlliAttSpec tmpControlliAttSpec = null;
        EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();

        tmpControlliAttSpec = gestioneDatiSpec.parseDatiSpec(TipiEntitaSacer.UNI_DOC,
                versamento.getVersamento().getDatiSpecifici(),
                versamento.getStrutturaComponenti().getIdTipologiaUnitaDocumentaria(),
                versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
                parsedUnitaDoc.getIntestazione().getTipologiaUnitaDocumentaria(),
                versamento.getStrutturaComponenti().getSistemaDiMigrazione(),
                versamento.getStrutturaComponenti().getIdStruttura(), rispostaWs);
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            versamento.getStrutturaComponenti()
                    .setDatiSpecifici(tmpControlliAttSpec.getDatiSpecifici());
            versamento.getStrutturaComponenti()
                    .setIdRecXsdDatiSpec(tmpControlliAttSpec.getIdRecXsdDatiSpec());
            myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                    .setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.POSITIVO);
        } else {
            myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                    .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                    .setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
        }

        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_MIGRAZIONE)
                && rispostaWs.getSeverity() != SeverityEnum.ERROR
                && versamento.getStrutturaComponenti().getSistemaDiMigrazione() != null) {

            tmpControlliAttSpec = gestioneDatiSpec.parseDatiSpecMig(TipiEntitaSacer.UNI_DOC,
                    versamento.getVersamento().getDatiSpecificiMigrazione(),
                    versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
                    parsedUnitaDoc.getIntestazione().getTipologiaUnitaDocumentaria(),
                    versamento.getStrutturaComponenti().getSistemaDiMigrazione(),
                    versamento.getStrutturaComponenti().getIdStruttura(), rispostaWs);
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                versamento.getStrutturaComponenti()
                        .setDatiSpecificiMigrazione(tmpControlliAttSpec.getDatiSpecifici());
                versamento.getStrutturaComponenti()
                        .setIdRecXsdDatiSpecMigrazione(tmpControlliAttSpec.getIdRecXsdDatiSpec());
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.POSITIVO);
            } else {
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                        .setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
            }
        }
    }

    /**
     * @param myEsito esito versamento {@link EsitoVersamento}
     */
    private void buildFlagsOnEsito(EsitoVersamento myEsito, VersamentoExt versamento) {

        // MAC #23544
        // aggiorna il tag [Configurazione] dell'esito (vedi elabFlagSipVDB)
        myEsito.getConfigurazione().setForzaCollegamento(
                versamento.getStrutturaComponenti().isConfigFlagForzaColleg());

        // v1.5
        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_ABILITA_FORZA_1_5)) {
            //
            myEsito.getConfigurazione().setForzaFormatoFile(
                    versamento.getStrutturaComponenti().isConfigFlagForzaFormato());
            //
            myEsito.getConfigurazione()
                    .setForzaHash(versamento.getStrutturaComponenti().isConfigFlagForzaHashVers());
            //
            myEsito.getConfigurazione().setForzaFormatoNumero(
                    versamento.getStrutturaComponenti().isConfigFlagForzaFormatoNumero());
            //
            myEsito.getConfigurazione().setAbilitaControlloHash(
                    versamento.getStrutturaComponenti().isFlagVerificaHash());
            //
            myEsito.getConfigurazione().setAbilitaControlloFormatoNumero(
                    versamento.getStrutturaComponenti().isFlagAbilitaContrFmtNumero());
            //
            myEsito.getConfigurazione().setAccettaControlloFormatoNumeroNegativo(
                    versamento.getStrutturaComponenti().isFlagAccettaErroreFmtNumero());
            //
            //
            myEsito.getConfigurazione().setAccettaControlloHashNegativo(
                    versamento.getStrutturaComponenti().isFlagAccettaContrHashNeg());
            //
            myEsito.getConfigurazione().setOggettoObbligatorio(
                    versamento.getStrutturaComponenti().isFlagProfiloUdObbOggetto());
            //
            myEsito.getConfigurazione().setDataObbligatorio(
                    versamento.getStrutturaComponenti().isFlagProfiloUdObbData());
            //
            myEsito.getConfigurazione().setAbilitaControlloCollegamento(
                    versamento.getStrutturaComponenti().isConfigFlagAbilitaContrColleg());
            //
            myEsito.getConfigurazione().setAccettaControlloCollegamentiNegativo(
                    versamento.getStrutturaComponenti().isFlagAccettaContrCollegNeg());
        }
    }

    /*
     * Controllo profili
     */
    private void verificaProfiliUnitaDoc(EsitoVersamento myEsito, VersamentoExt versamento,
            RispostaControlli rispostaControlli) {
        UnitaDocumentaria parsedUd = versamento.getVersamento();

        rispostaControlli.reset();
        // profilo normativo
        rispostaControlli = controlliProfiliUd.checkProfiloNormativo(parsedUd.getProfiloNormativo(),
                TipiEntitaSacer.UNI_DOC,
                versamento.getStrutturaComponenti().getIdTipologiaUnitaDocumentaria(),
                versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
                parsedUd.getIntestazione().getTipologiaUnitaDocumentaria());
        if (!rispostaControlli.isrBoolean()) {
            myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                    .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                    .setCorrispondenzaProfiloNormativo(ECEsitoPosNegType.NEGATIVO);
            versamento.addErrorUd(versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
                    rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
        } else {
            // id uso
            if (rispostaControlli.getrLong() != -1) /* se individuato */ {
                versamento.getStrutturaComponenti()
                        .setIdRecUsoXsdProfiloNormativo(rispostaControlli.getrLong());
                // xml canonicalizzato
                versamento.getStrutturaComponenti()
                        .setDatiC14NProfNormXml(rispostaControlli.getrString());
            }
            myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria()
                    .setCorrispondenzaProfiloNormativo(ECEsitoPosNegType.POSITIVO);
        }
    }

    // MEV#23176
    private void buildUrnSipOnEsito(EsitoVersamento myEsito, VersamentoExt versamento) {

        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_URN_SIP_1_5)) {
            //
            // calcolo l'URN dell'UD
            String tmpUrn = MessaggiWSFormat.formattaBaseUrnUnitaDoc(
                    versamento.getStrutturaComponenti().getUrnPartVersatore(),
                    versamento.getStrutturaComponenti().getUrnPartChiaveUd());

            // calcolo URN del SIP dell'UD
            String urnSip = MessaggiWSFormat.formattaUrnSip(tmpUrn,
                    Costanti.UrnFormatter.URN_SIP_UD);

            //
            myEsito.setURNSIP(urnSip);
        }
    }
    // end MEV#23176

    private void setRispostaWsError(RispostaWS rispostaWs, RispostaControlli rispostaControlli) {
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        rispostaWs.setErrorCode(rispostaControlli.getCodErr());
        rispostaWs.setErrorMessage(rispostaControlli.getDsErr());
    }
}
