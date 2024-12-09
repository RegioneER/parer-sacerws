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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.ejb;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.ControlliWS;
import it.eng.parer.ws.ejb.XmlFascCache;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.ws.utils.LogSessioneUtils;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.utils.VerificaVersione;
import it.eng.parer.ws.utils.WsXmlValidationEventHandler;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versFascicoli.dto.CompRapportoVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.ConfigNumFasc;
import it.eng.parer.ws.versFascicoli.dto.FascicoloLink;
import it.eng.parer.ws.versFascicoli.dto.FlControlliFasc;
import it.eng.parer.ws.versFascicoli.dto.RispostaWSFascicolo;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versFascicoli.utils.CostantiFasc;
import it.eng.parer.ws.versFascicoli.utils.KeyOrdFascUtility;
import it.eng.parer.ws.versFascicoli.utils.KeySizeFascUtility;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.ejb.RapportoVersBuilder;
import it.eng.parer.ws.xml.versfascicolo.IndiceSIPFascicolo;
import it.eng.parer.ws.xml.versfascicolo.IntestazioneType;
import it.eng.parer.ws.xml.versfascicolo.TipoConservazioneType;
import it.eng.parer.ws.xml.versfascicoloresp.ECConfigurazioneSIPType;
import it.eng.parer.ws.xml.versfascicoloresp.ECConfigurazioneType;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoPosNegType;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoPosNegWarType;
import it.eng.parer.ws.xml.versfascicoloresp.ECFascicoloType;
import it.eng.parer.ws.xml.versfascicoloresp.ECFascicoloType.EsitoControlliFascicolo;
import it.eng.parer.ws.xml.versfascicoloresp.SCChiaveType;
import it.eng.parer.ws.xml.versfascicoloresp.SCVersatoreType;
import it.eng.parer.ws.xml.versfascicoloresp.SIPType;

/**
 *
 * @author fioravanti_f
 */
@Stateless(mappedName = "VersFascicoloExtPrsr")
@LocalBean
public class VersFascicoloExtPrsr {

    private static final Logger log = LoggerFactory.getLogger(VersFascicoloExtPrsr.class);
    //
    // stateless ejb per i controlli sul db
    @EJB
    private ControlliSemantici controlliSemantici;
    // stateless ejb per i controlli specifici del fascicolo
    @EJB
    private ControlliFascicoli controlliFascicoli;
    // stateless ejb per i controlli su profilo archivistico e profilo generale fascicolo
    @EJB
    private ControlliProfiliFascicolo controlliProfiliFascicolo;
    // stateless ejb dei controlli collegamenti dei fascicoli
    @EJB
    private ControlliCollFascicolo controlliCollFascicolo;
    // stateless ejb per verifica autorizzazione ws
    @EJB
    private ControlliWS controlliEjb;
    // singleton ejb di gestione cache dei parser jaxb dei fascicoli
    @EJB
    private XmlFascCache xmlFascCache;
    // stateless ejb: crea il rapporto di versamento e canonicalizza (C14N) il SIP
    @EJB
    private RapportoVersBuilder rapportoVersBuilder;
    // singleton ejb di gestione configurazioni applicative
    @EJB
    private ConfigurationHelper configurationHelper;

    public void parseXML(SyncFakeSessn sessione, RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento) {
        // istanzia la classe di verifica retrocompatibilità
        VerificaVersione tmpVerificaVersione = new VerificaVersione();
        VerificaVersione.EsitiVerfica tmpEsitiVerfica;

        CSVersatore tagCSVersatore = new CSVersatore();
        CSChiaveFasc tagCSChiave = new CSChiaveFasc();
        String descChiaveFasc = "";
        String sistema = "";

        CompRapportoVersFascicolo myEsito = rispostaWs.getCompRapportoVersFascicolo();
        // AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();
        StringReader tmpReader;
        IndiceSIPFascicolo parsedIndiceFasc = null;
        // a priori, un problema in questo punto provoca il fallimento completo del versamento
        rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);

        /*
         * produco la versione canonicalizzata del SIP. Gestisco l'eventuale errore relativo all'encoding indicato in
         * maniera errata (es. "ISO8859/1" oppure "utf8"), non rilevato dalla verifica precedente.
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            RispostaControlli rispostaControlli = rapportoVersBuilder.canonicalizzaDaSalvareIndiceSip(sessione);
            if (!rispostaControlli.isrBoolean()) {

                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setErrorCode(rispostaControlli.getCodErr());
                rispostaWs.setErrorMessage(rispostaControlli.getDsErr());

                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            } else {
                //
                sessione.setDatiDaSalvareIndiceSip(sessione.getDatiC14NIndiceSip());
            }
        }

        /*
         * Si utilizza il SIP canonicalized onde evitare problemi di encoding
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpReader = new StringReader(sessione.getDatiC14NIndiceSip());
            WsXmlValidationEventHandler tmpVEventHandler = new WsXmlValidationEventHandler();
            try {
                JAXBContext tmpcontesto = xmlFascCache.getVersReqFascicoloCtx();
                Schema schema = xmlFascCache.getVersReqFascicoloSchema();
                Unmarshaller unmarshaller = tmpcontesto.createUnmarshaller();
                unmarshaller.setSchema(schema);
                unmarshaller.setEventHandler(tmpVEventHandler);
                parsedIndiceFasc = (IndiceSIPFascicolo) unmarshaller.unmarshal(tmpReader);
                versamento.setVersamento(parsedIndiceFasc);
                log.debug("validazione XML OK");
                // memorizzo la tupla che descrive il versatore non ancora verificato.
                // in caso di versanento errato, ho già pronti i dati da salvare nella
                // sessione errata.
                tagCSVersatore.setAmbiente(parsedIndiceFasc.getIntestazione().getVersatore().getAmbiente());
                tagCSVersatore.setEnte(parsedIndiceFasc.getIntestazione().getVersatore().getEnte());
                tagCSVersatore.setStruttura(parsedIndiceFasc.getIntestazione().getVersatore().getStruttura());
                versamento.getStrutturaComponenti().setVersatoreNonverificato(tagCSVersatore);
                // memorizzo il tipo fascicolo non verificato
                versamento.getStrutturaComponenti()
                        .setTipoFascicoloNonverificato(parsedIndiceFasc.getIntestazione().getTipoFascicolo());
                // memorizzo la versione xml, come dichiarata
                versamento.getStrutturaComponenti().setVersioneIndiceSipNonVerificata(
                        parsedIndiceFasc.getParametri().getVersioneIndiceSIPFascicolo());
                // memorizzo la chiave in una variabile di appoggio per usarla in diverse parti dell'elaborazione
                tagCSChiave.setAnno(parsedIndiceFasc.getIntestazione().getChiave().getAnno());
                tagCSChiave.setNumero(parsedIndiceFasc.getIntestazione().getChiave().getNumero());
                descChiaveFasc = MessaggiWSFormat.formattaChiaveFascicolo(tagCSVersatore, tagCSChiave);
                versamento.getStrutturaComponenti().setChiaveNonVerificata(tagCSChiave);
                //
                // sistema = configurationHelper.getParamApplicValue(ParametroApplDB.NM_SISTEMACONSERVAZIONE);
                sistema = configurationHelper.getValoreParamApplicByApplic(ParametroApplDB.NM_SISTEMACONSERVAZIONE);
                versamento.getStrutturaComponenti().setUrnPartChiaveFascicolo(
                        MessaggiWSFormat.formattaChiaveFascicolo(tagCSVersatore, tagCSChiave, sistema));
                // normalized key
                versamento.getStrutturaComponenti().setUrnPartChiaveFascicoloNormalized(
                        MessaggiWSFormat.formattaChiaveFascicolo(tagCSVersatore, tagCSChiave, sistema, true));
            } catch (JAXBException ex) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                            "Impossibile convalidare il documento XML SIP: " + ex.getMessage());
                } else {
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FAS_XSD_001_001, tmpVEventHandler.getMessaggio());
                }
                // se non riesco a convalidare l'XML magari posso provare a leggerlo
                // in modo meno preciso e tentare di riscostruire la struttura, la chiave ed il tipo fascicolo
                // e trasformare la sessione errata in una sessione fallita
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
                log.debug("validazione XML fallita!");
                log.debug("Eccezione: " + ex.getMessage());
                log.debug("Messaggio del validatore: " + tmpVEventHandler.getMessaggio());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
            }
        }

        ECConfigurazioneSIPType myConfigurazioneSIPType = new ECConfigurazioneSIPType();
        ECFascicoloType myFascicoloType = new ECFascicoloType();
        ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo = new ECFascicoloType.EsitoControlliFascicolo();
        // se l'unmarshalling è andato bene
        // imposta il flag globale di simulazione scrittura
        // preparo la risposta relativa alla configurazione SIP
        // preparo la risposta relativa al fascicolo
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (parsedIndiceFasc.getParametri() != null) {

                // imposta il flag globale di simulazione scrittura
                Boolean b = parsedIndiceFasc.getParametri().isSimulaSalvataggioDatiInDB();
                if (b != null && b) {
                    versamento.setSimulaScrittura(true);
                    IntestazioneType intestazione = parsedIndiceFasc.getIntestazione();
                    LogSessioneUtils.logSimulazioni(intestazione.getVersatore().getAmbiente(),
                            intestazione.getVersatore().getEnte(), intestazione.getVersatore().getStruttura(),
                            "fascicolo", intestazione.getChiave().getAnno(), intestazione.getChiave().getNumero(),
                            intestazione.getVersatore().getUserID(), log);
                }
                //
                myEsito.setIdentificativoRapportoVersamento(MessaggiWSFormat.formattaUrnRappVersFasc(
                        versamento.getStrutturaComponenti().getUrnPartChiaveFascicolo(),
                        Costanti.UrnFormatter.URN_RAPP_VERS_V2));
                //
                SIPType mySip = new SIPType();
                myEsito.setSIP(mySip);
                mySip.setDataVersamento(XmlDateUtility.dateToXMLGregorianCalendar(sessione.getTmApertura()));
                mySip.setURNIndiceSIP(MessaggiWSFormat.formattaUrnIndiceSipFasc(
                        versamento.getStrutturaComponenti().getUrnPartChiaveFascicolo(),
                        Costanti.UrnFormatter.URN_INDICE_SIP_V2));
                //
                myEsito.setParametriVersamento(myConfigurazioneSIPType);
                myConfigurazioneSIPType
                        .setTipoConservazione(parsedIndiceFasc.getParametri().getTipoConservazione().name());
                myConfigurazioneSIPType
                        .setForzaClassificazione(parsedIndiceFasc.getParametri().isForzaClassificazione());
                myConfigurazioneSIPType.setForzaNumero(parsedIndiceFasc.getParametri().isForzaNumero());
                myConfigurazioneSIPType.setForzaCollegamento(parsedIndiceFasc.getParametri().isForzaCollegamento());
            }

            myEsito.setFascicolo(myFascicoloType);
            myFascicoloType.setEsitoControlliFascicolo(myControlliFascicolo);
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
            // preparo il tag Versatore nella response, obbligatorio
            SCVersatoreType tmpVersatoreType = new SCVersatoreType();
            myFascicoloType.setVersatore(tmpVersatoreType);
            tmpVersatoreType.setAmbiente(tagCSVersatore.getAmbiente());
            tmpVersatoreType.setEnte(tagCSVersatore.getEnte());
            tmpVersatoreType.setStruttura(tagCSVersatore.getStruttura());
            tmpVersatoreType.setUserID(parsedIndiceFasc.getIntestazione().getVersatore().getUserID());
            // preparo anche il tag relativo alla chiave
            SCChiaveType tmpChiaveType = new SCChiaveType();
            myFascicoloType.setChiave(tmpChiaveType);
            tmpChiaveType.setAnno(tagCSChiave.getAnno());
            tmpChiaveType.setNumero(tagCSChiave.getNumero());
            // e quello del tipo fascicolo
            myFascicoloType.setTipoFascicolo(versamento.getStrutturaComponenti().getTipoFascicoloNonverificato());
        }

        // MEV#25288
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            // prepara risposta con urn sip
            this.buildUrnSipOnEsito(myEsito, versamento);
        }
        // end MEV#25288

        // ////////////////////////////////////////////////////////////
        // in questo punto verranno messi i controlli
        // sula struttura del versamento basati sulla versione
        // dichiarata. Gli errori verranno censiti some "errori XSD".
        // ////////////////////////////////////////////////////////////
        // verifica se la struttura della chiamata al ws è coerente con la versione dichiarata
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpVerificaVersione.verifica(rispostaWs, versamento);
            tmpEsitiVerfica = tmpVerificaVersione.getEsitiVerfica();
            if (tmpEsitiVerfica.isFlgErrore()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(tmpEsitiVerfica.getCodErrore(), tmpEsitiVerfica.getMessaggio());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
            }
            if (tmpEsitiVerfica.isFlgWarning()) {
                // rispostaWs.setSeverity(SeverityEnum.WARNING);
                // rispostaWs.setEsitoWsError(tmpEsitiVerfica.getCodErrore(), tmpEsitiVerfica.getMessaggio());
                // myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegWarType.WARNING);
            }
        }

        // in seguito vengono i controlli strutturali non basati sulla versione:
        // verifica nel complesso se ci sono problemi strutturali, non verificati tramite XSD
        // tipo id duplicati o conteggi che non coincidono
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
        }

        // come prima cosa verifico che il versatore e la versione dichiarati nel WS coincidano
        // con quelli nell'xml
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            this.controllaVersatore(myControlliFascicolo, versamento, rispostaWs, parsedIndiceFasc);
        }

        // se ho passato la verifica strutturale...
        // verifica la struttura versante
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            this.controllaStruttura(myControlliFascicolo, versamento, rispostaWs, tagCSVersatore, tagCSChiave,
                    descChiaveFasc);
        }

        // se il versatore alla fine è ok e la struttura è utilizzabile, lo scrivo
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            myControlliFascicolo.setIdentificazioneVersatore(ECEsitoPosNegType.POSITIVO);
            versamento.getStrutturaComponenti().setVersatoreVerificato(true);
        }

        // verifico il tipo fascicolo - in caso di errore la sessione è DUBBIA
        //
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            this.controllaTipoFasc(myControlliFascicolo, versamento, rispostaWs, descChiaveFasc);
        }

        // verifico il tipo fascicolo - abilitato su utente
        //
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            this.controllaTipoFascUserOrg(myControlliFascicolo, versamento, rispostaWs, descChiaveFasc);
        }

        // verifico il tipo fascicolo - abilitato su anno
        //
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            this.controllaTipoFascAnno(myControlliFascicolo, versamento, rispostaWs, descChiaveFasc);
        }

        // nota: comunque vada, da qui in poi la sessione non può essere più DUBBIA
        // (può essere solo OK, FALLITA oppure ERRATA) perché ho tutti gli elementi per memorizzare
        // una sessione fallita: id struttura, id tipo fascicolo e chiave, che è certamente
        // valorizzata in quanto ho superato la verifica XSD.
        //
        // Predispongo lo stato della sessione a FALLITA, in via precauzionale.
        // in caso di verifica positiva dei metadati, porterò questo valore a OK
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FALLITA);
        }

        // verifico la chiave
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            this.controllaChiave(myControlliFascicolo, myEsito, versamento, rispostaWs, tagCSChiave, descChiaveFasc);
        }

        // verifico tipo conservazione
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            this.controllaTipoConservazione(myControlliFascicolo, parsedIndiceFasc, rispostaWs, descChiaveFasc);
        }

        // questi controlli li faccio sempre tutti, dal momento che ognuno di essi
        // può essere eseguito anche se il precedente è fallito
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {

            // blocco controlli profilo archivistico

            boolean prosegui = this.controllaProfiloArchivistico(myEsito, versamento, descChiaveFasc);

            if (prosegui) {
                prosegui = this.controllaProfiloSpecifico(myEsito, versamento, parsedIndiceFasc, descChiaveFasc);// TODO:
                                                                                                                 // al
                                                                                                                 // momento
                                                                                                                 // non
                                                                                                                 // supportato
                                                                                                                 // verificare
                                                                                                                 // se
                                                                                                                 // va
                // bene in questo punto
            }
            // viene sempre eseguito
            // blocco controlli profilo generale
            prosegui = this.controllaProfiloGenerale(myEsito, myControlliFascicolo, versamento);

            if (prosegui) {
                prosegui = this.controllaSoggettoProd(myEsito, versamento, parsedIndiceFasc, descChiaveFasc);
            }
            // viene sempre eseguito (dati già controllati in precedenza)
            if (prosegui) {
                prosegui = this.controllaFlagStruttura(myEsito, rispostaWs, versamento, descChiaveFasc);
            }

            if (prosegui) {
                prosegui = this.controllaClassificazione(myEsito, versamento, parsedIndiceFasc, descChiaveFasc);
            }

            if (prosegui) {
                prosegui = this.controllaFormatoNumero(myEsito, versamento, parsedIndiceFasc, descChiaveFasc,
                        tagCSChiave, sistema);
            }

            if (prosegui) {
                this.controllaCollegamenti(myEsito, versamento, parsedIndiceFasc);
            }

            // calcolo l'esito del ws in funzione di warning ed errori (sia degli errori
            // legati al profilo archivistico che generale)
            VoceDiErrore tmpVdE = versamento.calcolaErrorePrincipale();
            if (tmpVdE != null) {
                rispostaWs.setSeverity(tmpVdE.getSeverity());
                if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                    rispostaWs.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                    myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                } else if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                    rispostaWs.setEsitoWsWarning(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                    myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                }
            } else {
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
            }

        }

        // se sono arrivato vivo fino a qui,
        // imposto la sessione di versamento a OK, in modo da
        // poter salvare il fascicolo con la relativa sessione OK
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.OK);
        }
    }

    private void controllaTipoConservazione(EsitoControlliFascicolo myControlliFascicolo,
            IndiceSIPFascicolo parsedIndiceFasc, RispostaWSFascicolo rispostaWs, String descChiaveFasc) {
        // tipo conservazione VERSAMENTO_ANTICIPATO al momento non supportata
        //
        if (parsedIndiceFasc.getParametri().getTipoConservazione()
                .equals(TipoConservazioneType.VERSAMENTO_ANTICIPATO)) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);

            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(MessaggiWSBundle.FAS_PF_GEN_003_010,
                    MessaggiWSBundle.getString(MessaggiWSBundle.FAS_PF_GEN_003_010, descChiaveFasc));
        } else {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
        }
    }

    private boolean controllaFlagStruttura(CompRapportoVersFascicolo myEsito, RispostaWSFascicolo rispostaWs,
            VersFascicoloExt versamento, String descChiaveFasc) {
        ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo = myEsito.getFascicolo()
                .getEsitoControlliFascicolo();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();

        boolean prosegui = true;

        // verifico se abilitato controllo di classificazione alla struttura
        RispostaControlli rispostaControlli = controlliFascicoli
                .checkFlDecAaTipoFascicoloOrgStrutt(svf.getIdAATipoFasc());

        if (rispostaControlli.isrBoolean()) {
            FlControlliFasc flControlliFasc = (FlControlliFasc) rispostaControlli.getrObject();
            versamento.getStrutturaComponenti().setFlControlliFasc(flControlliFasc);// set oggetto ottenuto

            ECConfigurazioneType ecconfigurazioneType = new ECConfigurazioneType();
            ecconfigurazioneType.setAbilitaControlloClassificazione(flControlliFasc.isFlAbilitaContrClassif());
            ecconfigurazioneType
                    .setAccettaControlloClassificazioneNegativo(flControlliFasc.isFlAccettaContrClassifNeg());
            ecconfigurazioneType.setForzaClassificazione(flControlliFasc.isFlForzaContrFlassif());

            ecconfigurazioneType.setAbilitaControlloFormatoNumero(flControlliFasc.isFlAbilitaContrNumero());
            ecconfigurazioneType.setAccettaControlloFormatoNumeroNegativo(flControlliFasc.isFlAccettaContrNumeroNeg());
            ecconfigurazioneType.setForzaNumero(flControlliFasc.isFlForzaContrNumero());// set config di struttura

            ecconfigurazioneType.setAbilitaControlloCollegamenti(flControlliFasc.isFlAbilitaContrColleg());
            ecconfigurazioneType.setAccettaControlloCollegamentiNegativo(flControlliFasc.isFlAccettaContrCollegNeg());
            ecconfigurazioneType.setForzaCollegamento(flControlliFasc.isFlForzaContrColleg());// set config di struttura

            // set risposta ...
            rispostaWs.getCompRapportoVersFascicolo().setConfigurazioneStruttura(ecconfigurazioneType);

        } else {
            // myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            // aggiungo l'errore alla lista
            versamento.addError(descChiaveFasc, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            prosegui = false;// TODO: non è stato impostato un codice di errore (non esiste da specifica perché non
                             // dovrebbe essere un caso possibile)
        }

        return prosegui;// i controlli successivi, senza il flag, non possono continuare! (non dovrebbe mai accadere!)
    }

    private boolean controllaClassificazione(CompRapportoVersFascicolo myEsito, VersFascicoloExt versamento,
            IndiceSIPFascicolo parsedIndiceFasc, String descChiaveFasc) {

        ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo = myEsito.getFascicolo()
                .getEsitoControlliFascicolo();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        boolean prosegui = true, flForzaClass = false;

        // se esiste il profilo generale e archivistico
        if (svf.getDatiXmlProfiloGenerale() != null && svf.getDatiXmlProfiloArchivistico() != null) {

            // recupero config flag per struttura
            FlControlliFasc flContrFasc = svf.getFlControlliFasc();

            // se nullo ma esiste su SIP di versamento lo setto ... altrimenti se presente, setto quello da
            // configurazione
            if (parsedIndiceFasc.getParametri().isForzaClassificazione() != null) {
                flForzaClass = parsedIndiceFasc.getParametri().isForzaClassificazione().booleanValue();// utilizza
                                                                                                       // questo
                // update flag (utilizzato per la scrittura su FAS_FASCICOLO)
                flContrFasc.setFlForzaContrFlassif(flForzaClass);
            } else {
                flForzaClass = flContrFasc.isFlForzaContrFlassif();// altrimenti, usa quello di struttura
            }

            /*
             * 7) Se il parametro fl_abilita_contr_classif vale false l’esito del controllo è “Controllo non eseguito”.
             * <ControlloClassificazione> assume valore “NON_ATTIVATO”. (Non viene generato warning) ed esce dal
             * controllo
             */
            if (!flContrFasc.isFlAbilitaContrClassif()) {
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NON_ATTIVATO);
                myControlliFascicolo.setControlloClassificazione(ECEsitoPosNegWarType.NON_ATTIVATO);
            } else {
                // verifica sul titolario
                RispostaControlli rispostaControlli = controlliFascicoli.verificaSIPTitolario(svf);

                if (!rispostaControlli.isrBoolean()) {
                    boolean isWarn = false;
                    // si eseguono una serie di verificare prima di restituire l'esito
                    myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    /*
                     * 9) Se almeno un controllo di classificazione è NEGATIVO: a) Se “Accetta controllo classificazione
                     * negativo” è false: <ControlloClassificazione> assume valore NEGATIVO. Il sistema esce dal
                     * presente controllo sulla classificazione.
                     */
                    if (!flContrFasc.isFlAccettaContrClassifNeg()) {
                        myControlliFascicolo.setControlloClassificazione(ECEsitoPosNegWarType.NEGATIVO);
                    } else {
                        /*
                         * se <ForzaClassificazione> vale true: <ControlloClassificazione> assume valore WARNING. Il
                         * sistema esce dal presente controllo sulla classificazione. se <ForzaClassificazione> vale
                         * false: <ControlloClassificazione> assume valore NEGATIVO. Il sistema esce dal presente
                         * controllo sulla classificazione.
                         */
                        if (flForzaClass) {
                            isWarn = true;
                            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                            myControlliFascicolo.setControlloClassificazione(ECEsitoPosNegWarType.WARNING);
                            // aggiungo l'errore restituito alla lista errori
                            versamento.addWarning(descChiaveFasc, rispostaControlli.getCodErr(),
                                    rispostaControlli.getDsErr());
                        } else {
                            myControlliFascicolo.setControlloClassificazione(ECEsitoPosNegWarType.NEGATIVO);
                        }
                    }

                    if (!isWarn) {
                        // aggiungo l'errore restituito alla lista errori
                        versamento.addError(descChiaveFasc, rispostaControlli.getCodErr(),
                                rispostaControlli.getDsErr());
                    }
                } else {

                    /*
                     * 10) Se tutti i controlli di classificazione sono positivi (quindi esiste una voce di titolario
                     * attiva alla data di apertura del fascicolo coincidente con l’indice di classificazione indicato
                     * nel SIP) l’esito controllo della classificazione sul titolario è POSITIVO. Il sistema setta il
                     * tag <ControlloClassificazione> con il valore POSITIVO.
                     */
                    // set risultato
                    versamento.getStrutturaComponenti().setIdVoceTitol(rispostaControlli.getrLong());
                    // se il risultato è OK lo si restituisce indipendentemente dalla condizioni a contorno
                    myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    myControlliFascicolo.setControlloClassificazione(ECEsitoPosNegWarType.POSITIVO);
                }

            }
        }

        return prosegui;
    }

    private boolean controllaFormatoNumero(CompRapportoVersFascicolo myEsito, VersFascicoloExt versamento,
            IndiceSIPFascicolo parsedIndiceFasc, String descChiaveFasc, CSChiaveFasc tagCSChiave, String sistema) {
        ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo = myEsito.getFascicolo()
                .getEsitoControlliFascicolo();

        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        boolean prosegui = true, flForzaContrNum = false;

        // recupero config flag per controlli
        FlControlliFasc flContrFasc = svf.getFlControlliFasc();

        // se nullo ma esiste su SIP di versamento lo setto ... altrimenti se presente, setto quello da configurazione
        if (parsedIndiceFasc.getParametri().isForzaNumero() != null) {
            flForzaContrNum = parsedIndiceFasc.getParametri().isForzaNumero().booleanValue();// utilizza questo
            // update flag (utilizzato per la scrittura su FAS_FASCICOLO)
            flContrFasc.setFlForzaContrNumero(flForzaContrNum);
        } else {
            flForzaContrNum = flContrFasc.isFlForzaContrNumero();// altrimenti, usa quello di struttura
        }

        // verifica formato numero e generazione chiave ordinata
        RispostaControlli rispostaControlli = controlliFascicoli.getPartiAANumero(descChiaveFasc,
                svf.getIdAATipoFasc());

        // ritorna eventuali errori e prosegue con successivi controlli
        if (!rispostaControlli.isrBoolean()) {
            // aggiungo l'errore restituito alla lista errori
            versamento.addError(descChiaveFasc, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            return prosegui;
        }

        // ha trovato le parti

        ConfigNumFasc tmpConfigNumFasc = (ConfigNumFasc) rispostaControlli.getrObject();
        // inizializzo il calcolo della lunghezza massima del campo numero
        KeySizeFascUtility tmpKeySizeUtility = new KeySizeFascUtility(svf.getVersatoreNonverificato(), tagCSChiave,
                sistema);
        // verifico se la chiave va bene in funzione del formato atteso per il numero
        KeyOrdFascUtility tmpKeyOrdFascUtility;
        tmpKeyOrdFascUtility = new KeyOrdFascUtility(tmpConfigNumFasc, tmpKeySizeUtility.getMaxLenNumero());
        // verifico se la chiave va bene e produco la chiave ordinata
        // Note : se il profilo generale non esiste si passa un "null" al fine di far fallire il check gestendo comunque
        // la chiave di ordinamento
        // vale lo stesso per il profilo archivistico
        rispostaControlli = tmpKeyOrdFascUtility.verificaChiave(tagCSChiave, svf.getIdStruttura(),
                svf.getDatiXmlProfiloGenerale() != null ? svf.getDatiXmlProfiloGenerale().getDataApertura() : null,
                svf.getDatiXmlProfiloArchivistico() != null
                        ? svf.getDatiXmlProfiloArchivistico().getIndiceClassificazione() : null);

        if (rispostaControlli.isrBoolean()) {
            // calcolo della chiave ordinata
            KeyOrdFascUtility.KeyOrdResult keyOrdResult = (KeyOrdFascUtility.KeyOrdResult) rispostaControlli
                    .getrObject();
            svf.setKeyOrdCalcolata(keyOrdResult.getKeyOrdCalcolata());
            svf.setProgressivoCalcolato(keyOrdResult.getProgressivoCalcolato());
        } else {
            // calcolo la chiave di ordinamento, come se il numero fosse di tipo GENERICO
            RispostaControlli rc = tmpKeyOrdFascUtility.calcolaKeyOrdGenerica(tagCSChiave);
            KeyOrdFascUtility.KeyOrdResult keyOrdResult = (KeyOrdFascUtility.KeyOrdResult) rc.getrObject();
            svf.setKeyOrdCalcolata(keyOrdResult.getKeyOrdCalcolata());
            svf.setProgressivoCalcolato(keyOrdResult.getProgressivoCalcolato());
        }

        // set della configurazione
        svf.setConfigNumFasc(tmpConfigNumFasc);

        /*
         * 7) Se il parametro fl_abilita_contr_classif vale false l’esito del controllo è “Controllo non eseguito”.
         * <ControlloClassificazione> assume valore “NON_ATTIVATO”. (Non viene generato warning) ed esce dal controllo
         */
        if (!flContrFasc.isFlAbilitaContrNumero()) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NON_ATTIVATO);
            myControlliFascicolo.setControlloFormatoNumero(ECEsitoPosNegWarType.NON_ATTIVATO);
        } else {
            if (!rispostaControlli.isrBoolean()) {
                boolean isWarn = false;
                // si eseguono una serie di verificare prima di restituire l'esito
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                /*
                 * c) Se “Accetta controllo formato numero negativo” è false: <ControlloFormatoNumero> assume valore
                 * NEGATIVO. Il sistema esce dal presente controllo sul formato numero.
                 */
                if (!flContrFasc.isFlAccettaContrNumeroNeg()) {
                    myControlliFascicolo.setControlloFormatoNumero(ECEsitoPosNegWarType.NEGATIVO);
                } else {
                    /*
                     * se <ForzaClassificazione> vale true: <ControlloClassificazione> assume valore WARNING. Il sistema
                     * esce dal presente controllo sulla classificazione. se <ForzaClassificazione> vale false:
                     * <ControlloClassificazione> assume valore NEGATIVO. Il sistema esce dal presente controllo sulla
                     * classificazione.
                     */
                    if (flForzaContrNum) {
                        isWarn = true;
                        myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                        myControlliFascicolo.setControlloFormatoNumero(ECEsitoPosNegWarType.WARNING);
                        // aggiungo l'errore restituito alla lista errori
                        versamento.addWarning(descChiaveFasc, rispostaControlli.getCodErr(),
                                rispostaControlli.getDsErr());
                        svf.setWarningFormatoNumero(true);// per salvataggio su decwarnaa
                    } else {
                        myControlliFascicolo.setControlloFormatoNumero(ECEsitoPosNegWarType.NEGATIVO);
                    }
                }

                if (!isWarn) {
                    // aggiungo l'errore restituito alla lista errori
                    versamento.addError(descChiaveFasc, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                }
            } else {

                /*
                 * 10) Se tutti i controlli di classificazione sono positivi (quindi esiste una voce di titolario attiva
                 * alla data di apertura del fascicolo coincidente con l’indice di classificazione indicato nel SIP)
                 * l’esito controllo della classificazione sul titolario è POSITIVO. Il sistema setta il tag
                 * <ControlloClassificazione> con il valore POSITIVO.
                 */
                // se il risultato è OK lo si restituisce indipendentemente dalla condizioni a contorno
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                myControlliFascicolo.setControlloFormatoNumero(ECEsitoPosNegWarType.POSITIVO);
            }
        }

        return prosegui;
    }

    private boolean controllaCollegamenti(CompRapportoVersFascicolo myEsito, VersFascicoloExt versamento,
            IndiceSIPFascicolo parsedIndiceFasc) {
        ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo = myEsito.getFascicolo()
                .getEsitoControlliFascicolo();
        // recupero flag per risposta
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();

        boolean prosegui = true, flForzaContrColl = false, linkHasBuilt = true;// verifica non bloccante per i controlli
                                                                               // successivi (qualunque esito)

        // se esistono fascicoli collegati
        if (svf.getDatiXmlProfiloArchivistico() != null
                && !svf.getDatiXmlProfiloArchivistico().getFascCollegati().isEmpty()) {

            FlControlliFasc flContrFasc = svf.getFlControlliFasc();

            // se nullo ma esiste su SIP di versamento lo setto ... altrimenti se presente,
            // setto quello da configurazione
            if (parsedIndiceFasc.getParametri().isForzaCollegamento() != null) {
                flForzaContrColl = parsedIndiceFasc.getParametri().isForzaCollegamento().booleanValue();// utilizza
                                                                                                        // questo
                // update flag (utilizzato per la scrittura su FAS_FASCICOLO)
                flContrFasc.setFlForzaContrColleg(flForzaContrColl);
            } else {
                flForzaContrColl = flContrFasc.isFlForzaContrColleg();// altrimenti, usa quello di struttura
            }

            RispostaControlli rispostaControlli = controlliCollFascicolo.buildCollegamentiFascicolo(versamento,
                    myControlliFascicolo);
            // terminata elaborazone finale della lista dei collegamenti....
            // TODO: non dovrebbe mai verificarsi (vedi check sopra), trattasi di un "overcheck"
            if (!rispostaControlli.isrBoolean()) {
                linkHasBuilt = false;// gestione esito
            }
            // eventuali (non controllati) fascicoli da inserire come link
            List<FascicoloLink> fascicoliToBeLinked = (List<FascicoloLink>) rispostaControlli.getrObject();
            svf.setFascicoliLinked(fascicoliToBeLinked);

            // NON_ATTIVATO
            // isFlAbilitaContrColleg = deve essere false
            // - scenario 1 : nessun collegamento trovato
            // - 2 : trovati collegamenti ma privi di errore
            // - 3 : / il codice di errore non è FASC_006_002 (collegamenti con stessa chiave) quindi sarà possibile
            // persistere su DB (limite max colonna desc)
            boolean isFlAbilitaContrColleg = flContrFasc.isFlAbilitaContrColleg();
            if (!isFlAbilitaContrColleg && (!linkHasBuilt || rispostaControlli.getCodErr() == null || !rispostaControlli
                    .getCodErr().equals(MessaggiWSBundle.FASC_006_002)/* eseguo lo stesso il controllo */)) {
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NON_ATTIVATO);
                myControlliFascicolo.setControlloCollegamenti(ECEsitoPosNegWarType.NON_ATTIVATO);
            } else {
                // verifica delle chiavi
                List<CSChiaveFasc> notFoundOnDb = new ArrayList<CSChiaveFasc>(0);
                // verifica decrizioni
                List<CSChiaveFasc> descError = new ArrayList<CSChiaveFasc>(0);
                if (linkHasBuilt) {
                    for (Iterator<FascicoloLink> it = svf.getFascicoliLinked().iterator(); it.hasNext();) {
                        FascicoloLink toBeLinked = it.next();
                        CSChiaveFasc csChiaveFascColl = toBeLinked.getCsChiaveFasc();
                        // verifico presenza fascicolo su quella struttura
                        rispostaControlli = controlliFascicoli.checkChiave(csChiaveFascColl, "dummy",
                                svf.getIdStruttura(), ControlliFascicoli.TipiGestioneFascAnnullati.CARICA);// indipendentemente
                                                                                                           // lo stato,
                                                                                                           // interessa
                                                                                                           // capire SE
                                                                                                           // esiste su
                                                                                                           // db

                        // non esiste su DB!
                        if (rispostaControlli.isrBoolean()) {
                            notFoundOnDb.add(csChiaveFascColl);// not found on db
                        } else {
                            toBeLinked.setIdLinkFasc(rispostaControlli.getrLong());// fk fascicolo trovato
                        }

                        // verifica descrizione
                        if (toBeLinked.getDescCollegamento().length() > CostantiFasc.COLLEGAMENTO_DESC_MAX_SIZE) {
                            descError.add(csChiaveFascColl);
                            flForzaContrColl = false; // in questo caso anche se l'errore sarebbe normalmente gestito
                                                      // come WARNING "forzo" ad ERRORE (non sarà mai possibile
                                                      // persistere questo dato)
                        }

                    } // fascicoliToBeLinked
                }

                /*
                 * . Se tutti i fascicoli della struttura versante identificate con la chiave definita dai tag “Numero”,
                 * “Anno” dei tag “ChiaveCollegamento” del XML di versamento, esistono nel DB, il tag
                 * <ControlloCollegamenti> è valorizzato con “POSITIVO”.
                 *
                 * 2) Il sistema controlla che la descrizione del collegamento non superi il limite massimo previsto da
                 * DB (attualmente 256 caratteri) Se la descrizione non supera il limite consentito il tag
                 * <ControlloCollegamenti> è valorizzato con “POSITIVO”. In caso contrario il sistema genera errore
                 */
                if (linkHasBuilt && (notFoundOnDb.size() == 0 && descError.size() == 0)) {
                    myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    myControlliFascicolo.setControlloCollegamenti(ECEsitoPosNegWarType.POSITIVO);
                } else {
                    boolean isWarn = false;
                    // si eseguono una serie di verificare prima di restituire l'esito
                    myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);

                    if (!flContrFasc.isFlAccettaContrCollegNeg()) {
                        myControlliFascicolo.setControlloCollegamenti(ECEsitoPosNegWarType.NEGATIVO);
                    } else {
                        if (flForzaContrColl) {
                            isWarn = true;
                            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.WARNING);
                            myControlliFascicolo.setControlloCollegamenti(ECEsitoPosNegWarType.WARNING);
                            // aggiungo l'errore restituito alla lista errori
                            for (Iterator<CSChiaveFasc> it = notFoundOnDb.iterator(); it.hasNext();) {
                                CSChiaveFasc csChiaveFascColl = it.next();
                                // aggiungo l'errore restituito alla lista errori
                                versamento.addWarning(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FASC_006_001,
                                        MessaggiWSBundle.getString(MessaggiWSBundle.FASC_006_001,
                                                MessaggiWSFormat.formattaUrnPartFasc(csChiaveFascColl)));
                            }

                            for (Iterator<CSChiaveFasc> it = descError.iterator(); it.hasNext();) {
                                CSChiaveFasc csChiaveFascColl = it.next();
                                // aggiungo l'errore restituito alla lista errori
                                versamento.addWarning(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FASC_006_002,
                                        MessaggiWSBundle.getString(MessaggiWSBundle.FASC_006_002,
                                                MessaggiWSFormat.formattaUrnPartFasc(csChiaveFascColl)));
                            }
                        } else {
                            myControlliFascicolo.setControlloCollegamenti(ECEsitoPosNegWarType.NEGATIVO);
                        }
                    }
                    // tutti i collegamenti, con errore, non trovati su db
                    if (!isWarn) {
                        // aggiungo l'errore restituito alla lista errori
                        for (Iterator<CSChiaveFasc> it = notFoundOnDb.iterator(); it.hasNext();) {
                            CSChiaveFasc csChiaveFascColl = it.next();
                            // aggiungo l'errore restituito alla lista errori
                            versamento.addError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FASC_006_001,
                                    MessaggiWSBundle.getString(MessaggiWSBundle.FASC_006_001,
                                            MessaggiWSFormat.formattaUrnPartFasc(csChiaveFascColl)));
                        }

                        for (Iterator<CSChiaveFasc> it = descError.iterator(); it.hasNext();) {
                            CSChiaveFasc csChiaveFascColl = it.next();
                            // aggiungo l'errore restituito alla lista errori
                            versamento.addError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FASC_006_002,
                                    MessaggiWSBundle.getString(MessaggiWSBundle.FASC_006_002,
                                            MessaggiWSFormat.formattaUrnPartFasc(csChiaveFascColl)));
                        }
                    }
                }
            }

        }

        return prosegui;
    }

    private void controllaVersatore(ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo,
            VersFascicoloExt versamento, RispostaWSFascicolo rispostaWs, IndiceSIPFascicolo parsedIndiceFasc) {
        // come prima cosa verifico che il versatore e la versione dichiarati nel WS coincidano
        // con quelli nell'xml
        if (!parsedIndiceFasc.getIntestazione().getVersatore().getUserID().equals(versamento.getLoginName())) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setIdentificazioneVersatore(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FAS_CONFIG_002_001,
                    parsedIndiceFasc.getIntestazione().getVersatore().getUserID());
            // se non coincidono questi elementi posso sempre
            // tentare di riscostruire la struttura, la chiave ed il tipo fascicolo
            // e trasformare la sessione errata in una sessione fallita
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
        }

        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            if (!parsedIndiceFasc.getParametri().getVersioneIndiceSIPFascicolo()
                    .equals(versamento.getVersioneWsChiamata())) {
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myControlliFascicolo.setIdentificazioneVersatore(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FAS_CONFIG_003_003,
                        parsedIndiceFasc.getParametri().getVersioneIndiceSIPFascicolo());
                // se non coincidono questi elementi posso sempre
                // tentare di riscostruire la struttura, la chiave ed il tipo fascicolo
                // e trasformare la sessione errata in una sessione fallita
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
            }
        }
    }

    private void controllaStruttura(ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo,
            VersFascicoloExt versamento, RispostaWSFascicolo rispostaWs, CSVersatore tagCSVersatore,
            CSChiaveFasc tagCSChiave, String descChiaveFasc) {
        // verifica la struttura versante
        RispostaControlli rispostaControlli = controlliSemantici.checkIdStrut(tagCSVersatore,
                TipiWSPerControlli.VERSAMENTO_FASCICOLO, versamento.getStrutturaComponenti().getDataVersamento());
        if (rispostaControlli.getrLong() < 1) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setIdentificazioneVersatore(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            if (rispostaControlli.getrLongExtended() < 1) {
                // se anche questo controllo fallisce la sessione
                // è sempre errata, visto quanto dichiarato non esiste
                // e non ho alcun modo di determinare la struttura
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
            } else {
                // se rLongExtended è valorizzato, la struttura esiste (ma è template):
                // posso sempre tentare di riscostruire la chiave ed il tipo fascicolo
                // e trasformare la sessione errata in una sessione fallita
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
                // salvo idstruttura template
                versamento.getStrutturaComponenti().setIdStruttura(rispostaControlli.getrLongExtended());
                // salvo idUtente
                versamento.getStrutturaComponenti().setIdUser(versamento.getUtente().getIdUtente());
            }
        } else {
            // salvo idstruttura individuata
            versamento.getStrutturaComponenti().setIdStruttura(rispostaControlli.getrLong());
            // salvo idUtente
            versamento.getStrutturaComponenti().setIdUser(versamento.getUtente().getIdUtente());
        }

        // verifica se l'utente è autorizzato ad usare il WS sulla struttura
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            versamento.getUtente()
                    .setIdOrganizzazioneFoglia(new BigDecimal(versamento.getStrutturaComponenti().getIdStruttura()));
            rispostaControlli = controlliEjb.checkAuthWS(versamento.getUtente(), versamento.getDescrizione(),
                    TipiWSPerControlli.VERSAMENTO_FASCICOLO);
            if (!rispostaControlli.isrBoolean()) {
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myControlliFascicolo.setIdentificazioneVersatore(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                // se non è autorizzato posso sempre
                // tentare di riscostruire la chiave ed il tipo fascicolo
                // e trasformare la sessione errata in una sessione fallita
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
            }
        }

        // verifica il partizionamento corretto della struttura
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli = controlliFascicoli.verificaPartizioniStruttAnnoFascicoli(descChiaveFasc,
                    versamento.getStrutturaComponenti().getIdStruttura(), tagCSChiave.getAnno());
            if (!rispostaControlli.isrBoolean()) {
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myControlliFascicolo.setIdentificazioneVersatore(ECEsitoPosNegType.NEGATIVO);
                // se il partizionamento è scorretto, la sessione di versamento è errata senza
                // possibilità di recupero (anche le sessioni fallite sono partizionate sul db)
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            }
        }
    }

    private void controllaTipoFasc(ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo,
            VersFascicoloExt versamento, RispostaWSFascicolo rispostaWs, String descChiaveFasc) {
        // verifico il tipo fascicolo - in caso di errore la sessione è DUBBIA
        //
        RispostaControlli rispostaControlli = controlliFascicoli.checkTipoFascicolo(
                versamento.getStrutturaComponenti().getTipoFascicoloNonverificato(), descChiaveFasc,
                versamento.getStrutturaComponenti().getIdStruttura());
        if (!rispostaControlli.isrBoolean()) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setVerificaTipoFascicolo(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            // se non è verificato posso sempre
            // tentare di riscostruire la chiave ed il tipo fascicolo
            // e trasformare la sessione errata in una sessione fallita
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
        } else {
            // salvo idtipofascicolo individuato
            versamento.getStrutturaComponenti().setIdTipoFascicolo(rispostaControlli.getrLong());
            // OK - popolo la risposta versamento
            myControlliFascicolo.setVerificaTipoFascicolo(ECEsitoPosNegType.POSITIVO);
        }
    }

    private void controllaTipoFascUserOrg(EsitoControlliFascicolo myControlliFascicolo, VersFascicoloExt versamento,
            RispostaWSFascicolo rispostaWs, String descChiaveFasc) {
        // verifico il tipo fascicolo abilitato per utente/org - in caso di errore la sessione è DUBBIA
        //
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        RispostaControlli rispostaControlli = controlliFascicoli.checkTipoFascicoloIamUserOrganizzazione(descChiaveFasc,
                svf.getTipoFascicoloNonverificato(), svf.getIdStruttura(), svf.getIdUser(), svf.getIdTipoFascicolo());
        if (!rispostaControlli.isrBoolean()) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setVerificaTipoFascicolo(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            // se non è verificato posso sempre
            // tentare di riscostruire la chiave ed il tipo fascicolo
            // e trasformare la sessione errata in una sessione fallita
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);// TODO: corretto????
        } else {
            versamento.getStrutturaComponenti().setIdIamAbilTipoDato(rispostaControlli.getrLong());
            // OK - popolo la risposta versamento
            myControlliFascicolo.setVerificaTipoFascicolo(ECEsitoPosNegType.POSITIVO);
        }
    }

    private void controllaTipoFascAnno(EsitoControlliFascicolo myControlliFascicolo, VersFascicoloExt versamento,
            RispostaWSFascicolo rispostaWs, String descChiaveFasc) {
        // verifico il tipo fascicolo abilito su anno - in caso di errore la sessione è DUBBIA
        //
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        RispostaControlli rispostaControlli = controlliFascicoli.checkTipoFascicoloAnno(descChiaveFasc,
                svf.getTipoFascicoloNonverificato(), svf.getChiaveNonVerificata().getAnno(), svf.getIdTipoFascicolo());
        if (!rispostaControlli.isrBoolean()) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setVerificaTipoFascicolo(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            // se non è verificato posso sempre
            // tentare di riscostruire la chiave ed il tipo fascicolo
            // e trasformare la sessione errata in una sessione fallita
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);// TODO: corretto????
        } else {
            // salvo idaddtipofasc individuato
            versamento.getStrutturaComponenti().setIdAATipoFasc(rispostaControlli.getrLong());
            // OK - popolo la risposta versamento
            myControlliFascicolo.setVerificaTipoFascicolo(ECEsitoPosNegType.POSITIVO);
        }

    }

    private void controllaChiave(ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo,
            CompRapportoVersFascicolo myEsito, VersFascicoloExt versamento, RispostaWSFascicolo rispostaWs,
            CSChiaveFasc tagCSChiave, String descChiaveFasc) {
        RispostaControlli rispostaControlli = controlliFascicoli.checkChiave(tagCSChiave, descChiaveFasc,
                versamento.getStrutturaComponenti().getIdStruttura(),
                ControlliFascicoli.TipiGestioneFascAnnullati.CONSIDERA_ASSENTE);
        if (!rispostaControlli.isrBoolean()) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setUnivocitaChiave(ECEsitoPosNegType.NEGATIVO);
            if (rispostaControlli.getrLong() > 0) {
                // se ho trovato un elemento doppio, è un errore specifico
                myEsito.setRapportoVersamento(rispostaControlli.getrString());
                rispostaWs.setErroreElementoDoppio(true);
                rispostaWs.setIdElementoDoppio(rispostaControlli.getrLong());
            } else {
                // altrimenti è un errore generico legato al db
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
            }
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
        } else {
            // OK - popolo la risposta versamento
            myControlliFascicolo.setUnivocitaChiave(ECEsitoPosNegType.POSITIVO);
        }
    }

    private boolean controllaProfiloArchivistico(CompRapportoVersFascicolo myEsito, VersFascicoloExt versamento,
            String descChiaveFasc) {

        boolean prosegui = true;
        ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo = myEsito.getFascicolo()
                .getEsitoControlliFascicolo();
        // Controllo profilo Archivistico
        RispostaControlli rispostaControlli = controlliProfiliFascicolo.verificaProfiloArchivistico(versamento);
        if (!rispostaControlli.isrBoolean()) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setControlloProfiloArchivistico(ECEsitoPosNegType.NEGATIVO);
            // aggiungo l'errore restituito alla lista errori
            versamento.addError(descChiaveFasc, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            prosegui = false;
        } else {
            myControlliFascicolo.setControlloProfiloArchivistico(ECEsitoPosNegType.POSITIVO);
        }

        // se il controllo consistenza è fallito, il ws _deve_ interrompere il controllo
        return prosegui;
    }

    private boolean controllaProfiloGenerale(CompRapportoVersFascicolo myEsito,
            ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo, VersFascicoloExt versamento) {
        // Controllo Consistenza; è bloccante per quanto riguarda il controllo successivo
        boolean prosegui = true;
        RispostaControlli rispostaControlli = controlliCollFascicolo.verificaUdFascicolo(versamento,
                myEsito.getFascicolo());
        if (!rispostaControlli.isrBoolean()) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setControlloConsistenza(ECEsitoPosNegType.NEGATIVO);
            // la lista errori è già stata popolata dal metodo, non faccio nulla
            prosegui = false;
        } else {
            myControlliFascicolo.setControlloConsistenza(ECEsitoPosNegType.POSITIVO);
        }

        if (prosegui) {
            // Controllo profilo Generale
            rispostaControlli = controlliProfiliFascicolo.verificaProfiloGenerale(versamento, myEsito.getFascicolo());
            if (!rispostaControlli.isrBoolean()) {
                myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                myControlliFascicolo.setControlloProfiloGenerale(ECEsitoPosNegType.NEGATIVO);
                // la lista errori è già stata popolata dal metodo, non faccio nulla
                // prosegui = false;//bloccante per i successivi
            } else {
                myControlliFascicolo.setControlloProfiloGenerale(ECEsitoPosNegType.POSITIVO);
            }
        }
        return prosegui;
    }

    private boolean controllaProfiloSpecifico(CompRapportoVersFascicolo myEsito, VersFascicoloExt versamento,
            IndiceSIPFascicolo parsedIndiceFasc, String descChiaveFasc) {
        ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo = myEsito.getFascicolo()
                .getEsitoControlliFascicolo();
        boolean prosegui = true;

        if (parsedIndiceFasc.getParametri().getVersioneProfiloSpecificoFascicolo() != null
                || parsedIndiceFasc.getProfiloSpecifico() != null) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            myControlliFascicolo.setControlloProfiloSpecifico(ECEsitoPosNegType.NEGATIVO);
            // aggiungo l'errore alla lista
            versamento.listErrAddError(descChiaveFasc, MessaggiWSBundle.FAS_CONFIG_005_001);
        } else {
            myControlliFascicolo.setControlloProfiloSpecifico(ECEsitoPosNegType.NON_ATTIVATO);
        }

        return prosegui;
    }

    private boolean controllaSoggettoProd(CompRapportoVersFascicolo myEsito, VersFascicoloExt versamento,
            IndiceSIPFascicolo parsedIndiceFasc, String descChiaveFasc) {
        ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo = myEsito.getFascicolo()
                .getEsitoControlliFascicolo();
        boolean prosegui = true;

        // al momento non supportato
        if (parsedIndiceFasc.getIntestazione().getSoggettoProduttore() != null) {
            myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NON_ATTIVATO);
            myControlliFascicolo.setIdentificazioneSoggettoProduttore(ECEsitoPosNegWarType.NON_ATTIVATO);
            // aggiungo l'errore alla lista
            versamento.listErrAddError(descChiaveFasc, MessaggiWSBundle.FAS_CONFIG_004_003);
        }
        // TODO : per il momento commentato (codice testato e funzionante da attivare nella prossima versione del WS)
        /*
         * if (parsedIndiceFasc.getIntestazione() .getSoggettoProduttore() != null) {
         *
         * String code = parsedIndiceFasc. getIntestazione(). getSoggettoProduttore(). getCodice(); String den =
         * parsedIndiceFasc. getIntestazione(). getSoggettoProduttore(). getDenominazione();
         *
         * Nota: vengono sempre testati entrambi i campi!
         *
         * Casi 1 : codice 2 : denominazione
         *
         * //verifica per codice if(StringUtils.isNotBlank(code)) {
         *
         * rispostaControlli = controlliFascicoli.verificaCodSoggettoProduttore(versamento, code);
         *
         * if (!rispostaControlli.isrBoolean()) { myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
         * myControlliFascicolo.setIdentificazioneSoggettoProduttore(ECEsitoPosNegWarType.NEGATIVO); } else {
         * versamento.getStrutturaComponenti().setIdOrgEnteConv(rispostaControlli.getrLong());
         * myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
         * myControlliFascicolo.setIdentificazioneSoggettoProduttore(ECEsitoPosNegWarType.POSITIVO); } }//codice
         * //verifica per denominazione if(StringUtils.isNotBlank(den)) {
         *
         * rispostaControlli = controlliFascicoli.verificaDenSoggettoProduttore(versamento, den);
         *
         * if (!rispostaControlli.isrBoolean()) { myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
         * myControlliFascicolo.setIdentificazioneSoggettoProduttore(ECEsitoPosNegWarType.NEGATIVO); } else {
         * versamento.getStrutturaComponenti().setIdOrgEnteConv(rispostaControlli.getrLong());
         * myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
         * myControlliFascicolo.setIdentificazioneSoggettoProduttore(ECEsitoPosNegWarType.POSITIVO);
         *
         * } }//denominazione //Nota: non indicati codice/denominazione -> si restituiscono entrambi gli errori?
         * if(StringUtils.isBlank(code) && StringUtils.isBlank(den)) {
         *
         * myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
         * myControlliFascicolo.setIdentificazioneSoggettoProduttore(ECEsitoPosNegWarType.NEGATIVO); // aggiungo
         * l'errore restituito alla lista errori versamento.addError(descChiaveFasc,
         * MessaggiWSBundle.FAS_CONFIG_004_001, MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_004_001, "N/A"));
         * // aggiungo l'errore restituito alla lista errori versamento.addError(descChiaveFasc,
         * MessaggiWSBundle.FAS_CONFIG_004_002, MessaggiWSBundle.getString(MessaggiWSBundle.FAS_CONFIG_004_002, "N/A"));
         * }
         *
         * } else { myControlliFascicolo.setCodiceEsito(ECEsitoPosNegWarType.NON_ATTIVATO);
         * myControlliFascicolo.setIdentificazioneSoggettoProduttore(ECEsitoPosNegWarType.NON_ATTIVATO); }
         */

        return prosegui;
    }

    // MEV#25288
    /**
     * @param myEsito
     */
    private void buildUrnSipOnEsito(CompRapportoVersFascicolo myEsito, VersFascicoloExt versamento) {

        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_URN_SIP_FASC_1_1)) {

            // calcolo l'URN del Fascicolo
            String tmpUrn = versamento.getStrutturaComponenti().getUrnPartChiaveFascicolo();

            // calcolo URN del SIP del Fascicolo
            String urnSip = MessaggiWSFormat.formattaUrnSip(tmpUrn, Costanti.UrnFormatter.URN_SIP_FASC);

            //
            myEsito.setURNSIP(urnSip);
        }
    }
    // end MEV#25288
}
