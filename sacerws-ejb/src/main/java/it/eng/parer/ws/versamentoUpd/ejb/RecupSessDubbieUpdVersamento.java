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
package it.eng.parer.ws.versamentoUpd.ejb;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.util.Constants;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.ControlliWS;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.UpdCostanti;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "RecupSessDubbieUpdVersamento")
@LocalBean
public class RecupSessDubbieUpdVersamento {

    private static final Logger log = LoggerFactory.getLogger(RecupSessDubbieUpdVersamento.class);
    //
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    // stateless ejb per i controlli sul db
    @EJB
    private ControlliSemantici controlliSemantici;

    @EJB
    private ControlliUpdVersamento updVersamentoControlli;

    @EJB
    private ControlliWS controlliWS;

    /*
     * Nota: il documento richiede di effettuare una serie di verifiche estrapolando le info dalla sessione ERRATA per
     * essere trasformata in fallita Nell'implementazione sotto, all'atto pratico, ciò che viene fatto è quello di
     * popolare o meglio di TENTARE di popolare quelle informazioni che normalmente verrebbero estratte in ambito di
     * "parsing" del SIP inviato. Il recupero quindi è composto dalle operazioni "minime" che sancisco che la sessione
     * ERRATA può essere "trasformata" in FALLITA.
     * 
     * Nota2: la logica di recupero dell'unità documentaria, al momento, serve solo a verificare la presenza della
     * stessa su DB e non ha lo scopo di ricavare altre informazioni
     */
    //
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void recuperaSessioneErrata(RispostaWSUpdVers rispostaWs, UpdVersamentoExt versamento) {
        // questo codice ha senso solo per le sesioni DUBBIE, per capire se è
        // è possibile recuperare i dati essenziali per il salvataggio di una sessione
        // FALLITA pur non avendoli ricavati durante il normale processo del WS
        if (rispostaWs.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.DUBBIA) {
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);

            // verifico se le credenziali o la versione ws inserite sono errate
            // e devo quindi ricalcolare tutto
            // OR
            // verifico se sono fallito per un errore del parser
            if (versamento.getUtente() == null || versamento.getVersamento() == null) {
                // recupero dati
                this.recuperaDatiDaXml(versamento);
                // recupero struttura
                if (!this.recuperaStrutturaDaVersatore(versamento)) {
                    return;
                }
                // recupero tipo ud, registro e tipo doc princ
                this.recuperaTipologiaUDRegDocPrinc(versamento, rispostaWs);
                // verifica partizione
                if (!this.verificaPartizionamentoStruttura(versamento)) {
                    return;
                }
                // verifica esistenza unita doc
                if (this.verificaCtrlStruttPartz(versamento)) {
                    this.recuperaUnitaDoc(versamento, rispostaWs);
                }
            }

            // verifico se ho effettuato il marshall ma non ho un id struttura
            // avviene se versatore e/o versione xml non coincidono con la chiamata ws
            if (versamento.getVersamento() != null && versamento.getStrutturaUpdVers().getIdStruttura() < 1) {
                if (!this.recuperaStrutturaDaVersatore(versamento)) {
                    return;
                }
                // verifica partizione
                if (!this.verificaPartizionamentoStruttura(versamento)) {
                    return;
                }
                // recupero tipo ud, registro e tipo doc princ
                this.recuperaTipologiaUDRegDocPrinc(versamento, rispostaWs);
                // verifica esistenza unita doc
                if (this.verificaCtrlStruttPartz(versamento)) {
                    this.recuperaUnitaDoc(versamento, rispostaWs);
                }
            }

            // verifico se ho l'idstruttura ma il versatore non è valido
            // (avviene se la struttura è template o se il versatore non è autorizzato ad usarla)
            if (versamento.getVersamento() != null && versamento.getStrutturaUpdVers().getIdStruttura() > 0
                    && !versamento.getStrutturaUpdVers().isVersatoreVerificato()) {
                // recupero tipo ud, registro e tipo doc princ
                this.recuperaTipologiaUDRegDocPrinc(versamento, rispostaWs);
                // verifica partizione
                if (!this.verificaPartizionamentoStruttura(versamento)) {
                    return;
                }
                // verifica esistenza unita doc
                if (this.verificaCtrlStruttPartz(versamento)) {
                    this.recuperaUnitaDoc(versamento, rispostaWs);
                }
            }

            // sono sopravvissuto ai controlli, quindi posso dichiarare la sessione come FALLITA
            // dal momento che ho recuperato e verificato tutti gli elementi necessari alla sua
            // registrazione
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.FALLITA);
        }
    }

    private void recuperaDatiDaXml(UpdVersamentoExt versamento) {
        if (versamento.getDatiXml() != null) {
            CSVersatore tmpVersatore = new CSVersatore();
            // init
            tmpVersatore.setAmbiente("");
            tmpVersatore.setEnte("");
            tmpVersatore.setStruttura("");

            CSChiave tmpChiave = new CSChiave();
            // init
            tmpChiave.setAnno(BigDecimal.ZERO.longValue());
            tmpChiave.setNumero("");
            tmpChiave.setTipoRegistro("");

            String tipologiaUnitaDoc = "";
            String tipologiaDocPrinc = "";
            boolean cercaTipoUd = true;
            boolean cercaDocPrinc = true;
            try {
                Node tmpDati = this.convertStringToDocument(versamento.getDatiXml().trim());

                XPathFactory xpathfactory = XPathFactory.newInstance();
                XPath xpath = xpathfactory.newXPath();

                XPathExpression expr = xpath.compile("//Intestazione/Versatore/Ambiente/text()");
                NodeList nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    tmpVersatore.setAmbiente(nodes.item(i).getNodeValue());
                }
                //
                expr = xpath.compile("//Intestazione/Versatore/Ente/text()");
                nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    tmpVersatore.setEnte(nodes.item(i).getNodeValue());
                }
                //
                expr = xpath.compile("//Intestazione/Versatore/Struttura/text()");
                nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    tmpVersatore.setStruttura(nodes.item(i).getNodeValue());
                }
                // cerca la chiave (o almeno le parti da cui è definita)
                expr = xpath.compile("//Intestazione/Chiave/Anno/text()");
                nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    String tmpString = nodes.item(i).getNodeValue();
                    if (tmpString.matches("-?\\d+")) {
                        tmpChiave.setAnno(Long.parseLong(tmpString));
                    }
                }
                //
                expr = xpath.compile("//Intestazione/Chiave/Numero/text()");
                nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    tmpChiave.setNumero(nodes.item(i).getNodeValue());
                }
                // memorizzo il tipo registro, se definito
                expr = xpath.compile("//Intestazione/Chiave/TipoRegistro/text()");
                nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    tmpChiave.setTipoRegistro(nodes.item(i).getNodeValue());
                }
                // memorizzo il tipo unità documentaria, se esiste
                expr = xpath.compile("//Intestazione/TipologiaUnitaDocumentaria/text()");
                nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    tipologiaUnitaDoc = nodes.item(i).getNodeValue();
                }
                cercaTipoUd = cercaTipoUd && nodes.getLength() != 0;

                // memorizzo il tipo doc principale, se esiste
                expr = xpath.compile("//UnitaDocumentaria/DocumentoPrincipale/TipoDocumento/text()");
                nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    tipologiaDocPrinc = nodes.item(i).getNodeValue();
                }
                cercaDocPrinc = cercaDocPrinc && nodes.getLength() != 0;

            } catch (XPathExpressionException ex) {
                log.error("errore xpath!!", ex);
            } catch (Exception ex) {
                log.error("errore generico, " + "ma a questo punto ormai tutto è perduto, " + "inutile notificarlo: ",
                        ex);
            }

            // setto sempre una struttura "fittizzia" con ciò che è stato recuperato per i controlli successivi
            versamento.getStrutturaUpdVers().setVersatoreNonverificato(tmpVersatore);
            // setto sempre una chiave "fittizzia" con ciò che è stato recuperato per i controlli successivi
            versamento.getStrutturaUpdVers().setChiaveNonVerificata(tmpChiave);

            if (cercaTipoUd) {
                versamento.getStrutturaUpdVers().setDescTipologiaUnitaDocumentariaNonVerificata(tipologiaUnitaDoc);
            }
            if (cercaDocPrinc) {
                versamento.getStrutturaUpdVers().setDescTipoDocPrincipaleNonVerificato(tipologiaDocPrinc);
            }
        }
    }

    private boolean recuperaStrutturaDaVersatore(UpdVersamentoExt versamento) {
        boolean prosegui = false; // i dati successivi senza struttura non potranno essere recuperati
        long idStruttura;
        CSVersatore tmpVersatore = versamento.getStrutturaUpdVers().getVersatoreNonverificato();
        RispostaControlli rispostaControlli = controlliSemantici.checkIdStrut(tmpVersatore,
                Costanti.TipiWSPerControlli.AGGIORNAMENTO_VERSAMENTO,
                versamento.getStrutturaUpdVers().getDataVersamento());
        if (rispostaControlli.getrLongExtended() > 0) {
            idStruttura = rispostaControlli.getrLongExtended();
            versamento.getStrutturaUpdVers().setIdStruttura(idStruttura);
            prosegui = true;

            // esito positivo
            versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE));
            versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ENTE));
            versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_STRUTTURA));
            //
        } else {

            // tipologie di errore -> relativo controllo
            switch (rispostaControlli.getCodErr()) {
            case MessaggiWSBundle.UD_001_001:
                // ambiente
                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE), SeverityEnum.ERROR,
                        TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                break;
            case MessaggiWSBundle.UD_001_002:
                // ente
                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ENTE),
                        SeverityEnum.ERROR, TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());

                // check su ambiente superato
                // aggiunta su controlli generali
                versamento
                        .addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE));

                break;
            case MessaggiWSBundle.UD_001_003:
            case MessaggiWSBundle.UD_001_015:
                // struttura
                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_STRUTTURA), SeverityEnum.ERROR,
                        TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                // controllo struttura KO (non si effettuano ulteriori verifiche)
                // check su ambiente/ente superato
                // aggiunta su controlli generali
                versamento
                        .addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE));
                versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ENTE));

                break;
            default:
                // niente da fare negli altri casi
                break;
            }
        }

        return prosegui;
    }

    private boolean verificaPartizionamentoStruttura(UpdVersamentoExt versamento) {
        boolean trovato = false;
        CSChiave tagCSChiave = versamento.getStrutturaUpdVers().getChiaveNonVerificata();
        if (verificaCSChiave(tagCSChiave) && versamento.getStrutturaUpdVers().getIdStruttura() > 0) {
            trovato = true;
            /* MEV 30089 - non faccio più controlli sulle partizioni, ci sono partizionamenti automatici */
            versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_PARTIZIONI));
        }
        return trovato;
    }

    private boolean verificaCSChiave(CSChiave tagCSChiave) {
        return tagCSChiave.getAnno() > 0 && StringUtils.isNotBlank(tagCSChiave.getNumero())
                && StringUtils.isNotBlank(tagCSChiave.getTipoRegistro());
    }

    private void recuperaTipologiaUDRegDocPrinc(UpdVersamentoExt versamento, RispostaWSUpdVers rispostaWs) {
        boolean trovato = true;
        RispostaControlli tmpRispostaControlli = null;
        String descTipologiaUnitaDocumentariaNonVerificata = versamento.getStrutturaUpdVers()
                .getDescTipologiaUnitaDocumentariaNonVerificata();

        // se esiste un tipo ud recuperato in precedenza
        if (StringUtils.isNotBlank(descTipologiaUnitaDocumentariaNonVerificata)) {
            tmpRispostaControlli = controlliSemantici.checkTipologiaUD(descTipologiaUnitaDocumentariaNonVerificata,
                    "dummy", versamento.getStrutturaUpdVers().getIdStruttura());
            if (tmpRispostaControlli.isrBoolean()) {
                // salvo id individuato
                versamento.getStrutturaUpdVers().setIdTipologiaUnitaDocumentaria(tmpRispostaControlli.getrLong());
                versamento.getStrutturaUpdVers()
                        .setDescTipologiaUnitaDocumentaria(tmpRispostaControlli.getrStringExtended());
            }
        }

        // recupero id tipo ud "sconosciuto"
        tmpRispostaControlli = controlliSemantici.checkTipologiaUD(UpdCostanti.TIPOUD_SCONOSCIUTO, "dummy",
                versamento.getStrutturaUpdVers().getIdStruttura());

        // esiste il tipo sconosciuto
        if (tmpRispostaControlli.isrBoolean()) {
            // salvo idtipofascicolo individuato
            versamento.getStrutturaUpdVers().setIdTipoUDUnknown(tmpRispostaControlli.getrLong());
        } else {
            trovato = false;
        }
        //
        CSChiave tagCSChiave = versamento.getStrutturaUpdVers().getChiaveNonVerificata();
        // se esiste una chiave con registro
        if (StringUtils.isNotBlank(tagCSChiave.getTipoRegistro())) {
            // recupero id registro
            tmpRispostaControlli = controlliSemantici.checkTipoRegistro(tagCSChiave.getTipoRegistro(), "dummy",
                    versamento.getStrutturaUpdVers().getIdStruttura());
            //
            if (tmpRispostaControlli.isrBoolean()) {
                // salvo id individuato
                versamento.getStrutturaUpdVers().setIdRegistro(tmpRispostaControlli.getrLong());
            }
        }
        // tipologia reg "Sconosciuto"
        tmpRispostaControlli = controlliSemantici.checkTipoRegistro(UpdCostanti.TIPOREG_SCONOSCIUTO, "dummy",
                versamento.getStrutturaUpdVers().getIdStruttura());

        // esiste il tipo sconosciuto
        if (tmpRispostaControlli.isrBoolean()) {
            // salvo idtipofascicolo individuato
            versamento.getStrutturaUpdVers().setIdTipoREGUnknown(tmpRispostaControlli.getrLong());
        } else {
            trovato = false;
        }
        //
        String descTipoDocPrincipaleNonVerificato = versamento.getStrutturaUpdVers()
                .getDescTipoDocPrincipaleNonVerificato();

        // tag presente su XML di versamento
        if (StringUtils.isNotBlank(descTipoDocPrincipaleNonVerificato)) {
            tmpRispostaControlli = controlliSemantici.checkTipoDocumento(descTipoDocPrincipaleNonVerificato,
                    versamento.getStrutturaUpdVers().getIdStruttura(), true, "dummydoc");

            if (tmpRispostaControlli.isrBoolean()) {
                // salvo tipo doc princ individuato
                versamento.getStrutturaUpdVers().setIdTipoDocPrincipale(tmpRispostaControlli.getrLong());
            }
        }
        // tipo doc principale "Sconosciuto"
        tmpRispostaControlli = controlliSemantici.checkTipoDocumento(UpdCostanti.TIPDOC_PRINCIPALE_SCONOSCIUTO,
                versamento.getStrutturaUpdVers().getIdStruttura(), true, "dummydoc");

        // esiste il tipo sconosciuto
        if (tmpRispostaControlli.isrBoolean()) {
            // salvo id scoscosciuto
            versamento.getStrutturaUpdVers().setIdTipoDOCPRINCUnknown(tmpRispostaControlli.getrLong());
        } else {
            trovato = false;
        }

        // esisto errore struttura su tipo "sconosciuto"
        if (!trovato) {
            versamento.addEsitoControlloOnGeneraliBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_STRUTTURA), rispostaWs.getSeverity(),
                    TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_001_018,
                    versamento.getStrutturaUpdVers().getVersatoreNonverificato().getStruttura());
        }
    }

    /**
     * Nota: verifica esisteza UD, se esiste tutte le informazioni saranno recuperate dall'UD altrimenti si
     * utilizzeranno quelle determinate in precedenza Arrivato a questo controllo la sessione è già FALLITA
     * (=trasformazione OK)
     * 
     * @param versamento
     * @param rispostaWs
     */
    private void recuperaUnitaDoc(UpdVersamentoExt versamento, RispostaWSUpdVers rispostaWs) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();

        // controlla prensa chiave UD
        RispostaControlli rispostaControlli = updVersamentoControlli.checkChiaveAndTipoDocPrinc(
                strutturaUpdVers.getChiaveNonVerificata(), strutturaUpdVers.getIdStruttura(),
                ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
        // trovato
        if (!rispostaControlli.isrBoolean()) {

            // si assume il LOCK ESCLUSIVO su UNITA_DOC determinata
            Map<String, Object> properties = new HashMap<>();
            properties.put(Constants.JAVAX_PERSISTENCE_LOCK_TIMEOUT, 25000);
            entityManager.find(AroUnitaDoc.class, rispostaControlli.getrLong(), LockModeType.PESSIMISTIC_WRITE,
                    properties);

            // set esito positivo
            versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD));

            // recupero unita doc
            strutturaUpdVers.setIdUd(rispostaControlli.getrLong());

            // recupero tipo UD
            strutturaUpdVers.setDescTipologiaUnitaDocumentariaNonVerificata(rispostaControlli.getrStringExtended());

            // recupero ID tipo UD
            strutturaUpdVers.setIdTipoUDNonVerificata(rispostaControlli.getrLongExtended());

            // recupero tipo conservazione
            strutturaUpdVers.setTipoConservazione(
                    (String) rispostaControlli.getrMap().get(RispostaControlli.ValuesOnrMap.TI_CONSERVAZIONE.name()));

            // recupero ID tipo reg
            strutturaUpdVers.setIdRegistro(
                    (long) rispostaControlli.getrMap().get(RispostaControlli.ValuesOnrMap.ID_REGISTROUD.name()));

            // recupero Sistema Mig (
            strutturaUpdVers.setSistemaDiMigrazione(
                    (String) rispostaControlli.getrMap().get(RispostaControlli.ValuesOnrMap.NM_SISTEMAMIGRAZ.name()));

            // recupero id doc princ
            strutturaUpdVers.setIdTipoDocPrincipale(
                    (long) rispostaControlli.getrMap().get(RispostaControlli.ValuesOnrMap.ID_TIPO_DOC_PRINC.name()));

            // recupero Stato Conservazione
            strutturaUpdVers.setStatoConservazioneUnitaDoc(
                    (CostantiDB.StatoConservazioneUnitaDoc) rispostaControlli.getrObject());

            // aggiunta flag su riposta
            myEsito.getParametriAggiornamento().setForzaCollegamento(strutturaUpdVers.isFlForzaCollegamento());

        } else {
            // esito negativo
            versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD),
                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                    rispostaControlli.getDsErr());
        }

    }

    /**
     * Nota: in caso di match con almeno un risultato NEGATIVO non si eseguono verifiche successive
     * 
     * @return true/false con risultato di verifica della partizione struttura
     */
    private boolean verificaCtrlStruttPartz(UpdVersamentoExt versamento) {
        boolean hasOneError = false;
        hasOneError = versamento.anyMatchEsitoControlli(versamento.getControlliGenerali().stream()
                .filter(c -> c.getCdControllo().equals(ControlliWSBundle.CTRL_INTS_STRUTTURA)
                        && c.getCdControllo().equals(ControlliWSBundle.CTRL_INTS_PARTIZIONI))
                .collect(Collectors.toList()), VoceDiErrore.TipiEsitoErrore.NEGATIVO);
        return !hasOneError;
    }

    private Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlStr)));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            // in caso di eccezione non faccio nulla:
            // è perfettamente accettabile che questa stringa, che ha già fallito una
            // validazione, non sia well-formed e provochi uh'eccezione.
        }
        return null;
    }

}
