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
package it.eng.parer.ws.versamentoUpd.ejb.prs;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.utils.VerificaVersione;
import it.eng.parer.ws.utils.WsXmlValidationEventHandler;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdComponenteVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdDocumentoVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.UpdCostanti.AggiornamentoEffettuato;
import it.eng.parer.ws.xml.versUpdReq.IndiceSIPAggiornamentoUnitaDocumentaria;
import it.eng.parer.ws.xml.versUpdReq.IntestazioneType;
import it.eng.parer.ws.xml.versUpdResp.ChiaveType;
import it.eng.parer.ws.xml.versUpdResp.SIPType;
import it.eng.parer.ws.xml.versUpdResp.VersatoreType;
import it.eng.parer.ws.utils.LogSessioneUtils;
import java.io.StringReader;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "UpdVersamentoPrsr")
@LocalBean
public class UpdVersamentoPrsr extends UpdVersamentoBasePrsr {

    private static final Logger log = LoggerFactory.getLogger(UpdVersamentoPrsr.class);

    public void parseXML(SyncFakeSessn sessione, UpdVersamentoExt versamento, RispostaWSUpdVers rispostaWs) {
        // istanzia la classe di verifica retrocompatibilitï¿½
        VerificaVersione tmpVerificaVersione = new VerificaVersione();
        VerificaVersione.EsitiVerfica tmpEsitiVerfica;

        CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();
        StringReader tmpReader;
        // l'istanza dell'indice SIP di aggiornamento
        IndiceSIPAggiornamentoUnitaDocumentaria parsedIndiceUpd = null;
        //
        CSVersatore tagCSVersatore = new CSVersatore();
        CSChiave tagCSChiave = new CSChiave();
        String descChiaveUD = "";

        // a priori, un problema in questo punto provoca il fallimento completo del versamento
        rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);

        /*
         * produco la versione canonicalizzata del SIP. Gestisco l'eventuale errore relativo all'encoding indicato in
         * maniera errata (es. "ISO8859/1" oppure "utf8"), non rilevato dalla verifica precedente.
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            versamento
                    .addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_XMLVSXSD));

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
                JAXBContext tmpcontesto = xmlUpdVersCache.getVersReqCtxforAggiornamentoUD();
                Schema schema = xmlUpdVersCache.getSchemaOfAggVersReq();

                Unmarshaller unmarshaller = tmpcontesto.createUnmarshaller();
                unmarshaller.setSchema(schema);
                unmarshaller.setEventHandler(tmpVEventHandler);
                parsedIndiceUpd = (IndiceSIPAggiornamentoUnitaDocumentaria) unmarshaller.unmarshal(tmpReader);

                versamento.setVersamento(parsedIndiceUpd);
                log.debug("validazione XML OK");
                // memorizzo la tupla che descrive il versatore non ancora verificato.
                // in caso di versanento errato, ho giï¿½ pronti i dati da salvare nella
                // sessione errata.
                tagCSVersatore.setAmbiente(
                        parsedIndiceUpd.getUnitaDocumentaria().getIntestazione().getVersatore().getAmbiente());
                tagCSVersatore
                        .setEnte(parsedIndiceUpd.getUnitaDocumentaria().getIntestazione().getVersatore().getEnte());
                tagCSVersatore.setStruttura(
                        parsedIndiceUpd.getUnitaDocumentaria().getIntestazione().getVersatore().getStruttura());
                // new URN
                String sistemaConservazione = configurationHelper
                        .getValoreParamApplicByApplic(ParametroApplDB.NM_SISTEMACONSERVAZIONE);
                tagCSVersatore.setSistemaConservazione(sistemaConservazione);

                versamento.getStrutturaUpdVers().setVersatoreNonverificato(tagCSVersatore);

                // memorizzo la versione xml, come dichiarata
                versamento.getStrutturaUpdVers().setVersioneIndiceSipNonVerificata(
                        parsedIndiceUpd.getUnitaDocumentaria().getIntestazione().getVersione());

                versamento.getStrutturaUpdVers().setDescTipologiaUnitaDocumentariaNonVerificata(
                        parsedIndiceUpd.getUnitaDocumentaria().getIntestazione().getTipologiaUnitaDocumentaria());

                // memorizzo la chiave in una variabile di appoggio per usarla in diverse parti dell'elaborazione
                tagCSChiave.setAnno(
                        new BigDecimal(parsedIndiceUpd.getUnitaDocumentaria().getIntestazione().getChiave().getAnno())
                                .longValue());
                tagCSChiave.setNumero(parsedIndiceUpd.getUnitaDocumentaria().getIntestazione().getChiave().getNumero());
                tagCSChiave.setTipoRegistro(
                        parsedIndiceUpd.getUnitaDocumentaria().getIntestazione().getChiave().getTipoRegistro());

                descChiaveUD = MessaggiWSFormat.formattaUrnPartUnitaDoc(tagCSChiave);

                versamento.getStrutturaUpdVers().setChiaveNonVerificata(tagCSChiave);
                versamento.getStrutturaUpdVers().setUrnPartChiaveUd(descChiaveUD);

                /**
                 * NEW URN
                 */
                versamento.getStrutturaUpdVers()
                        .setUrnPartVersatore(MessaggiWSFormat.formattaUrnPartVersatore(tagCSVersatore));

                if (parsedIndiceUpd.getUnitaDocumentaria().getDocumentoPrincipale() != null) {
                    versamento.getStrutturaUpdVers().setDescTipoDocPrincipaleNonVerificato(
                            parsedIndiceUpd.getUnitaDocumentaria().getDocumentoPrincipale().getTipoDocumento());
                }

            } catch (Exception ex) {

                String msg = null;
                if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                    msg = "Impossibile convalidare il documento XML SIP: " + ex.getMessage();
                    log.debug("Eccezione: {}", ex.getMessage());
                } else {
                    msg = tmpVEventHandler.getMessaggio();
                    log.debug("Messaggio del validatore: {}", tmpVEventHandler.getMessaggio());
                }

                rispostaWs.setSeverity(SeverityEnum.ERROR);

                // bundle
                rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_XMLVSXSD),
                        MessaggiWSBundle.XSD_001_002, msg);

                // add on generic error
                versamento.addEsitoControlloOnGeneraliBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_XMLVSXSD), SeverityEnum.ERROR,
                        TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.XSD_001_002, msg);

                if (ex instanceof JAXBException) {
                    // se non riesco a convalidare l'XML magari posso provare a leggerlo
                    // in modo meno preciso e tentare di riscostruire la struttura, la chiave a il tipo documento
                    // e trasformare la sessione errata in una sessione fallita
                    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
                } else {
                    // errore grave, sessione ERRATA
                    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
                }
                log.debug("validazione XML fallita!");
            }
        }

        // ////////////////////////////////////////////////////////////
        // in questo punto verranno messi i controlli
        // sula struttura del versamento basati sulla versione
        // dichiarata. Gli errori verranno censiti some "errori XSD".
        // ////////////////////////////////////////////////////////////
        // verifica se la struttura della chiamata al ws ï¿½ coerente con la versione dichiarata
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            tmpVerificaVersione.verifica(rispostaWs, versamento);
            tmpEsitiVerfica = tmpVerificaVersione.getEsitiVerfica();
            if (tmpEsitiVerfica.isFlgErrore()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_XMLVSXSD),
                        tmpEsitiVerfica.getCodErrore(), tmpEsitiVerfica.getMessaggio());

                // add on generic error
                versamento.addEsitoControlloOnGeneraliBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERALI_XMLVSXSD), SeverityEnum.ERROR,
                        TipiEsitoErrore.NEGATIVO, tmpEsitiVerfica.getCodErrore(), tmpEsitiVerfica.getMessaggio());

            }
        }

        // se l'unmarshalling ï¿½ andato bene
        // imposta il flag globale di simulazione scrittura
        // preparo la risposta relativa alla configurazione SIP
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
            // verifica cosa dovrï¿½ essere aggiornato e quindi controllato all'interno dell'XML
            //
            this.initElementiDaAggiornare(versamento);

            //
            if (versamento.getVersamento().getParametriAggiornamento() != null) {
                // tipo aggiornamento
                strutturaUpdVers.setTipoAggiornamento(
                        versamento.getVersamento().getParametriAggiornamento().getTipoAggiornamento());

                // note aggiornamento
                myEsito.getParametriAggiornamento().setTipoAggiornamento(
                        versamento.getVersamento().getParametriAggiornamento().getTipoAggiornamento());// valore
                strutturaUpdVers.setNoteAggiornamento(
                        versamento.getVersamento().getParametriAggiornamento().getNoteAggiornamento());

                if (versamento.getVersamento().getParametriAggiornamento().isForzaAggiornamento() != null) {
                    // risposta
                    myEsito.getParametriAggiornamento().setForzaAggiornamento(
                            versamento.getVersamento().getParametriAggiornamento().isForzaAggiornamento());// valore
                                                                                                           // comunicato

                    // struttura
                    strutturaUpdVers.setFlForzaAggiornamento(
                            versamento.getVersamento().getParametriAggiornamento().isForzaAggiornamento());
                }

            }

            //
            // calcolo l'URN dell'UPD
            String tmpUrn = MessaggiWSFormat.formattaBaseUrnUpdUnitaDoc(
                    versamento.getStrutturaUpdVers().getUrnPartVersatore(),
                    versamento.getStrutturaUpdVers().getUrnPartChiaveUd(), Costanti.UrnFormatter.UPD_FMT_STRING_V3);
            //
            myEsito.setIdentificativoRapportoVersamento(
                    MessaggiWSFormat.formattaUrnPartRappVersUpd(tmpUrn, Costanti.UrnFormatter.URN_RAPP_VERS_V2));
            // SIP
            SIPType mySip = new SIPType();
            myEsito.setSIP(mySip);
            mySip.setDataVersamento(XmlDateUtility.dateToXMLGregorianCalendar(sessione.getTmApertura()));
            mySip.setURNIndiceSIP(
                    MessaggiWSFormat.formattaUrnIndiceSipUpd(tmpUrn, Costanti.UrnFormatter.URN_INDICE_SIP_V2));
            //
            // comunicato
            if (versamento.getVersamento().getUnitaDocumentaria().getConfigurazione() != null) {
                // flag
                if (versamento.getVersamento().getUnitaDocumentaria().getConfigurazione()
                        .getTipoConservazione() != null) {

                    strutturaUpdVers.setTipoConservazione(versamento.getVersamento().getUnitaDocumentaria()
                            .getConfigurazione().getTipoConservazione().name());
                }

                // imposta il flag globale di simulazione scrittura
                Boolean simulaScrittura = versamento.getVersamento().getUnitaDocumentaria().getConfigurazione()
                        .isSimulaSalvataggioDatiInDB();
                if (simulaScrittura != null && simulaScrittura) {
                    versamento.setSimulaScrittura(true);
                    IntestazioneType intestazione = parsedIndiceUpd.getUnitaDocumentaria().getIntestazione();
                    LogSessioneUtils.logSimulazioni(intestazione.getVersatore().getAmbiente(),
                            intestazione.getVersatore().getEnte(), intestazione.getVersatore().getStruttura(),
                            intestazione.getChiave().getTipoRegistro(), intestazione.getChiave().getAnno(),
                            intestazione.getChiave().getNumero(), intestazione.getVersatore().getUserID(), log);
                }
            }

            // preparo il tag Versatore nella response, obbligatorio
            VersatoreType tmpVersatoreType = new VersatoreType();
            myEsito.setUnitaDocumentariaVersatore(tmpVersatoreType);
            tmpVersatoreType.setAmbiente(tagCSVersatore.getAmbiente());
            tmpVersatoreType.setEnte(tagCSVersatore.getEnte());
            tmpVersatoreType.setStruttura(tagCSVersatore.getStruttura());
            tmpVersatoreType.setUserID(
                    versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getVersatore().getUserID());
            // preparo anche il tag relativo alla chiave
            ChiaveType tmpChiaveType = new ChiaveType();
            myEsito.setUnitaDocumentariaChiave(tmpChiaveType);
            tmpChiaveType.setAnno(String.valueOf(tagCSChiave.getAnno()));
            tmpChiaveType.setNumero(tagCSChiave.getNumero());
            tmpChiaveType.setRegistro(tagCSChiave.getTipoRegistro());

            myEsito.setTipologiaUnitaDocumentaria(versamento.getVersamento().getUnitaDocumentaria().getIntestazione()
                    .getTipologiaUnitaDocumentaria());
        }

        // MEV#23176
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // prepara risposta con urn sip
            this.buildUrnSipOnEsito(myEsito, versamento);
        }
        // end MEV#23176

        // ////////////////////////////////////////////////////////////
        // se in precedenza non si sono registrati errori su XSD
        // si eseguono i controlli che determinano se aggiornato positivo/fallito/errato
        // ////////////////////////////////////////////////////////////

        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {

            boolean prosegui = true;

            // ////////////////////////////////////////////////////////////
            // in questo punto verranno messi i controlli
            // sula struttura del versamento basati sulla versione
            // dichiarata. Gli errori verranno censiti some "errori XSD".
            // ////////////////////////////////////////////////////////////
            prosegui = this.controllaUnivocitaDocIDs(versamento, rispostaWs);

            // tipo aggiornamento
            if (prosegui) {
                prosegui = this.controllaTipoAggiornamento(versamento, rispostaWs);
            }

            // versione XSD
            if (prosegui) {
                prosegui = this.controllaVersioneXSD(versamento, rispostaWs);
            }

            // se ho passato la verifica strutturale...
            // verifica la struttura versante e utente
            if (prosegui) {
                prosegui = this.controllaStrutturaVersatore(versamento, rispostaWs, tagCSVersatore, descChiaveUD);
            }

            // nota: comunque vada, da qui in poi la sessione non puï¿½ essere piï¿½ DUBBIA
            // (puï¿½ essere solo OK, FALLITA oppure ERRATA) perchï¿½ ho tutti gli elementi per memorizzare
            // una sessione fallita: id struttura, id tipo fascicolo e chiave, che ï¿½ certamente
            // valorizzata in quanto ho superato la verifica XSD.
            //
            // Predispongo lo stato della sessione a FALLITA, in via precauzionale.
            // in caso di verifica positiva dei metadati, porterï¿½ questo valore a OK

            // se il versatore alla fine ï¿½ ok e la struttura ï¿½ utilizzabile, lo scrivo
            if (prosegui) {
                versamento.getStrutturaUpdVers().setVersatoreVerificato(true);
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FALLITA);
            }

            // verifico la chiave (se esito struttura positivo - vedi ultimo controllo eseguito)
            if (prosegui) {
                super.controlloChiaveECdKeyNorm(versamento, rispostaWs, tagCSChiave, descChiaveUD);
            }

            // controlliXSD = POSITVO && controlliIntestazione = POSITVO (controlli generali) -> prosegui
            prosegui = this.verificaControlliGenerali(versamento);

            // verifico l'hash del SIP che sto versando
            if (prosegui) {
                prosegui = super.controllaHashSIP(sessione, versamento, rispostaWs, descChiaveUD);
            }

            // verifico tipo UD + recupero flag
            if (prosegui) {
                prosegui = super.checkTipoUDAndAbilUpdMeta(myEsito, versamento, rispostaWs, descChiaveUD);
            }

            // verifico abilitazione utente su REG+tipo UD
            if (prosegui) {
                prosegui = super.controllaAbilUserRegTipoUD(versamento, rispostaWs);
            }

            // verifico tipo conservazione
            if (prosegui) {
                prosegui = super.controllaStatoConservazione(versamento, rispostaWs, descChiaveUD);
            }

            // prossimi controlli sulla base degli elementi presenti su SIP

            // se trovati documenti collegati
            if (prosegui && versamento.hasDocumentiCollegatiToUpd()) {
                prosegui = super.controllaCollegamenti(myEsito, versamento, rispostaWs);
            }

            // se trovato profilo UD
            if (prosegui && versamento.hasProfiloUnitaDocumentariaToUpd()) {
                prosegui = super.controllaProfiloUnitaDocumentaria(versamento, rispostaWs, descChiaveUD);
            }

            // se trovato profilo UD
            if ((prosegui && versamento.hasProfiloNormativoToUpd())
                    && versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_PROFILI_UPD_1_6)) {
                prosegui = super.controllaProfiloNormativo(versamento, rispostaWs);
            }

            // se trovati dati specifici
            if (prosegui && versamento.hasDatiSpecificiToBeUpdated()) {
                prosegui = super.controllaDatiSpecifici(versamento, rispostaWs, descChiaveUD);
            }

            // se trovati dati specifici di migrazione
            if (prosegui && versamento.hasDatiSpecificiMigrazioneToUpd()) {
                prosegui = super.controllaDatiSpeificiMig(versamento, rispostaWs, descChiaveUD);
            }

            // se fascicolo principale non da aggiornare -> verifica dei secondari
            if (prosegui && versamento.hasPAFascicoliSecondariToUp() && !versamento.hasPAFascicoloPrincipaleToUpd()) {
                prosegui = super.controllaFascicoliSecondari(versamento, rispostaWs);
            }

            // se trovati documenti
            if (prosegui && versamento.hasDocumentiToUpd()) {
                super.controllaDocumenti(myEsito, versamento, rispostaWs);

            }

        }

        // se sono arrivato vivo fino a qui,
        // imposto la sessione di versamento a OK, in modo da
        // poter elaborare l'aggiornamento
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.OK);
            //
            this.initAggiornamentiDaEffettuare(versamento);
        }
    }

    /**
     * Nota: in caso di match con almeno un risultato NEGATIVO non si eseguono verifiche successive
     *
     * @return true/false con risultato dei controlli generali
     */
    private boolean verificaControlliGenerali(UpdVersamentoExt versamento) {
        boolean hasOneError = false;
        hasOneError = versamento.anyMatchEsitoControlli(
                versamento.getControlliDiSistema().stream().collect(Collectors.toList()),
                VoceDiErrore.TipiEsitoErrore.NEGATIVO)
                || versamento.anyMatchEsitoControlli(
                        versamento.getControlliGenerali().stream().collect(Collectors.toList()),
                        VoceDiErrore.TipiEsitoErrore.NEGATIVO);
        return !hasOneError;
    }

    private void initElementiDaAggiornare(UpdVersamentoExt versamento) {
        // ProfiloArchivistico
        versamento.setHasProfiloArchivisticoToUpd(
                versamento.getVersamento().getUnitaDocumentaria().getProfiloArchivistico() != null);
        // ProfiloArchivistico -> FascicoloPrincipale
        versamento.setHasPAFascicoloPrincipaleToUpd(versamento.hasProfiloArchivisticoToUpd() && versamento
                .getVersamento().getUnitaDocumentaria().getProfiloArchivistico().getFascicoloPrincipale() != null);
        // ProfiloArchivistico -> FascicoliSecondari
        versamento.setHasPAFascicoliSecondariToUp(versamento.hasProfiloArchivisticoToUpd() && versamento.getVersamento()
                .getUnitaDocumentaria().getProfiloArchivistico().getFascicoliSecondari() != null);
        // ProfiloArchivistico NOT FascicoloPrincipale && NOT FascicoliSecondari ->
        // UPDATE TO NULL!
        versamento.setHasProfiloArchivisticoToUpdNull(versamento.hasProfiloArchivisticoToUpd()
                && !versamento.hasPAFascicoloPrincipaleToUpd() && !versamento.hasPAFascicoliSecondariToUp());
        // ProfiloUnitaDocumentaria
        versamento.setHasProfiloUnitaDocumentariaToUpd(
                versamento.getVersamento().getUnitaDocumentaria().getProfiloUnitaDocumentaria() != null);
        // ProfiloNormativo
        versamento.setHasProfiloNormativoToUpd(
                versamento.getVersamento().getUnitaDocumentaria().getProfiloNormativo() != null);
        // DatiSpecifici
        versamento.setHasDatiSpecificiToBeUpdated(
                versamento.getVersamento().getUnitaDocumentaria().getDatiSpecifici() != null);
        // DatiSpecificiMigrazione
        versamento.setHasDatiSpecificiMigrazioneToUpd(
                versamento.getVersamento().getUnitaDocumentaria().getDatiSpecificiMigrazione() != null);
        // DocumentiCollegati
        versamento.setHasDocumentiCollegatiToUpd(
                versamento.getVersamento().getUnitaDocumentaria().getDocumentiCollegati() != null);

        // DocumentoPrincipale
        versamento.setHasDocumentoPrincipaleToUpd(
                versamento.getVersamento().getUnitaDocumentaria().getDocumentoPrincipale() != null);

        versamento.setHasDocPStrutturaOriginaleToUpd(versamento.hasDocumentoPrincipaleToUpd() && versamento
                .getVersamento().getUnitaDocumentaria().getDocumentoPrincipale().getStrutturaOriginale() != null);

        // Allegati
        versamento.setHasAllegatiToUpd(versamento.getVersamento().getUnitaDocumentaria().getAllegati() != null);
        // Annessi
        versamento.setHasAnnessiToUpd(versamento.getVersamento().getUnitaDocumentaria().getAnnessi() != null);
        // Annotazioni
        versamento.setHasAnnotazioniToUpd(versamento.getVersamento().getUnitaDocumentaria().getAnnotazioni() != null);

    }

    private void initAggiornamentiDaEffettuare(UpdVersamentoExt versamento) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        //
        this.aggiornamentiUnitaDoc(strutturaUpdVers, versamento);
        //
        this.aggiornamentiDocumenti(strutturaUpdVers, CategoriaDocumento.Principale, versamento);
        //
        this.aggiornamentiDocumenti(strutturaUpdVers, CategoriaDocumento.Allegato, versamento);
        //
        this.aggiornamentiDocumenti(strutturaUpdVers, CategoriaDocumento.Annesso, versamento);
        //
        this.aggiornamentiDocumenti(strutturaUpdVers, CategoriaDocumento.Annotazione, versamento);

    }

    private void aggiornamentiUnitaDoc(StrutturaUpdVers strutturaUpdVers, UpdVersamentoExt versamento) {
        if (versamento.hasProfiloArchivisticoToUpd()) {
            if (versamento.hasPAFascicoloPrincipaleToUpd()) {
                versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASPAFASCICOLOPRINCIPALETOUPD);
            }
            if (versamento.hasPAFascicoliSecondariToUp()) {
                versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASPAFASCICOLISECONDARITOUP);
            }
        }
        //
        if (versamento.hasProfiloUnitaDocumentariaToUpd()) {
            versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASPROFILOUNITADOCUMENTARIATOUPD);
        }
        //
        if (versamento.hasProfiloNormativoToUpd()) {
            versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASPROFILONORMATIVOTOUPD);
        }
        //
        if (versamento.hasDocumentiCollegatiToUpd()) {
            versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASDOCUMENTICOLLEGATITOUPD);
        }
        //
        if (versamento.hasDatiSpecificiToBeUpdated()) {
            versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASDATISPECIFICITOBEUPDATED);
        }
        if (versamento.hasDatiSpecificiMigrazioneToUpd()) {
            versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASDATISPECIFICIMIGRAZIONETOUPD);
        }
        //
        if (!strutturaUpdVers.getDocumentiAttesi().isEmpty()) {
            versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASDOCUMENTITOUPD);
        }
        //
        if (!strutturaUpdVers.getComponentiAttesi().isEmpty()) {
            versamento.addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato.HASCOMPONENTITOUPD);
        }
    }

    private void aggiornamentiDocumenti(StrutturaUpdVers strutturaUpdVers, CategoriaDocumento catDoc,
            UpdVersamentoExt versamento) {
        for (UpdDocumentoVers documento : strutturaUpdVers.getDocumentiAttesi().stream()
                .filter(d -> d.getCategoriaDoc().equals(catDoc)).collect(Collectors.toList())) {
            //
            if (documento.getRifUpdDocumento().getProfiloDocumento() != null) {
                versamento.addAggiornamentoDocumenti(AggiornamentoEffettuato.HASDOCCPROFILO, catDoc,
                        documento.getRifUpdDocumento().getIDDocumento());
            }
            //
            if (documento.getRifUpdDocumento().getDatiSpecifici() != null) {
                versamento.addAggiornamentoDocumenti(AggiornamentoEffettuato.HASDOCDATISPEC, catDoc,
                        documento.getRifUpdDocumento().getIDDocumento());
            }
            if (documento.getRifUpdDocumento().getDatiSpecificiMigrazione() != null) {
                versamento.addAggiornamentoDocumenti(AggiornamentoEffettuato.HASDOCDATISPECMIGRAZ, catDoc,
                        documento.getRifUpdDocumento().getIDDocumento());
            }
            //
            if (!documento.getUpdComponentiAttesi().isEmpty()) {
                versamento.addAggiornamentoDocumenti(AggiornamentoEffettuato.HASDOCCOMPONENTI, catDoc,
                        documento.getRifUpdDocumento().getIDDocumento());
            }
            //
            for (UpdComponenteVers componente : documento.getUpdComponentiAttesi()) {
                //
                versamento.addAggiornamentoCompDocumenti(AggiornamentoEffettuato.HASCOMPONENTE, catDoc,
                        componente.getKeyCtrl());
                //
                if (componente.getDatiSpecifici() != null) {
                    versamento.addAggiornamentoCompDocumenti(AggiornamentoEffettuato.HASCOMPONENTEDATISPEC, catDoc,
                            componente.getKeyCtrl());
                }
                //
                if (componente.getDatiSpecificiMigrazione() != null) {
                    versamento.addAggiornamentoCompDocumenti(AggiornamentoEffettuato.HASCOMPONENTEDATISPECMIGRAZ,
                            catDoc, componente.getKeyCtrl());
                }
            }
        }
    }

    // MEV#23176
    /**
     * @param myEsito
     */
    private void buildUrnSipOnEsito(CompRapportoUpdVers myEsito, UpdVersamentoExt versamento) {

        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_URN_SIP_1_5)) {
            //
            // calcolo l'URN dell'UPD
            String tmpUrn = MessaggiWSFormat.formattaBaseUrnUpdUnitaDoc(
                    versamento.getStrutturaUpdVers().getUrnPartVersatore(),
                    versamento.getStrutturaUpdVers().getUrnPartChiaveUd(), Costanti.UrnFormatter.UPD_FMT_STRING_V3);

            // calcolo URN del SIP dell'UPD
            String urnSip = MessaggiWSFormat.formattaUrnSip(tmpUrn, Costanti.UrnFormatter.URN_SIP_UPD_V2);

            //
            myEsito.setURNSIP(urnSip);
        }
    }
    // end MEV#23176

}
