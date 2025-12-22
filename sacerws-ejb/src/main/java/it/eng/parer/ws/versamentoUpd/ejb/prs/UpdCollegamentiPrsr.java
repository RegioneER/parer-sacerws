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
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ejb.prs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.KeyOrdUtility;
import it.eng.parer.ws.utils.KeySizeUtility;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.versamento.dto.ConfigRegAnno;
import it.eng.parer.ws.versamento.dto.UnitaDocColl;
import it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.ControlloEseguito;
import it.eng.parer.ws.versamentoUpd.dto.ControlloWSResp;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.UpdUnitaDocColl;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.utils.IRispostaUpdVersWS;
import it.eng.parer.ws.xml.versUpdReq.AggiornamentoDocumentoCollegatoType;

/**
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "UpdCollegamentiPrsr")
@LocalBean
public class UpdCollegamentiPrsr extends UpdBasePrsr {

    // stateless ejb per i controlli sul db
    @EJB
    private ControlliSemantici controlliSemantici;

    private void init(UpdVersamentoExt versamento) {

        this.initControlliCollegamenti(versamento);

    }

    private void initControlliCollegamenti(UpdVersamentoExt versamento) {
        // init NA
        for (AggiornamentoDocumentoCollegatoType.DocumentoCollegato documCollMd : versamento
                .getVersamento().getUnitaDocumentaria().getDocumentiCollegati()
                .getDocumentoCollegato()) {
            CSChiave tmpCsChiaveColl = new CSChiave();
            for (ControlloWSResp controllo : ControlliWSBundle.getControlliByCdFamiglia(
                    ControlloEseguito.FamigliaControllo.CONTROLLICOLLEGAMENTO.name())) {
                // CSChiave
                tmpCsChiaveColl
                        .setTipoRegistro(documCollMd.getChiaveCollegamento().getTipoRegistro());
                tmpCsChiaveColl
                        .setAnno(Long.valueOf(documCollMd.getChiaveCollegamento().getAnno()));
                tmpCsChiaveColl.setNumero(documCollMd.getChiaveCollegamento().getNumero());

                versamento.addControlloNAOnControlliCollegamento(controllo, tmpCsChiaveColl);
            }
        }
    }

    public boolean eseguiControlli(UpdVersamentoExt versamento, IRispostaUpdVersWS rispostaWs,
            CompRapportoUpdVers myEsito) {
        // init
        init(versamento);

        boolean collegamentoHasErr = false;

        CSChiave tmpCsChiaveColl = new CSChiave();
        // l'insieme di tutti i collegamenti (già accorpati nelle descrizioni)
        // che verrà memorizzato sul DB
        Map<List<String>, UpdUnitaDocColl> tmpCollegamentiUd = new HashMap<>();

        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();

        boolean tmpForzaColl = strutturaUpdVers.isFlForzaCollegamento();

        // CICLO FOR SUI DOCUMENTI COLLEGATI
        for (AggiornamentoDocumentoCollegatoType.DocumentoCollegato documCollMd : versamento
                .getVersamento().getUnitaDocumentaria().getDocumentiCollegati()
                .getDocumentoCollegato()) {
            tmpCsChiaveColl.setTipoRegistro(documCollMd.getChiaveCollegamento().getTipoRegistro());
            tmpCsChiaveColl.setAnno(Long.valueOf(documCollMd.getChiaveCollegamento().getAnno()));
            tmpCsChiaveColl.setNumero(documCollMd.getChiaveCollegamento().getNumero());

            // cerco se si sta tentando di collegare la stessa UD più volte
            String[] tmpArr = new String[] {
                    "", "", "" };
            tmpArr[0] = tmpCsChiaveColl.getNumero();
            tmpArr[1] = tmpCsChiaveColl.getTipoRegistro();
            tmpArr[2] = String.valueOf(tmpCsChiaveColl.getAnno());
            List<String> tmpChiaveUdAsList = Arrays.asList(tmpArr);

            UpdUnitaDocColl tmpUnitaDocColl = tmpCollegamentiUd.get(tmpChiaveUdAsList);
            if (tmpUnitaDocColl == null) {
                // in questo caso non ho censito altri collegamenti alla stessa UD.
                String descChiaveUdColl = MessaggiWSFormat.formattaUrnPartUnitaDoc(tmpCsChiaveColl);
                tmpUnitaDocColl = new UpdUnitaDocColl();
                // inserisco il collegamento nella collezione di collegamenti censiti
                tmpCollegamentiUd.put(tmpChiaveUdAsList, tmpUnitaDocColl);
                // aggiungo al versamento il riferimento all'unità documentaria collegata
                strutturaUpdVers.getUnitaDocCollegate().add(tmpUnitaDocColl);

                tmpUnitaDocColl.setAggChiave(documCollMd.getChiaveCollegamento());
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
                RispostaControlli rispostaControlli = controlliSemantici.checkChiave(
                        tmpCsChiaveColl, strutturaUpdVers.getIdStruttura(),
                        ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);

                tmpUnitaDocColl.setIdUnitaDocLink(rispostaControlli.getrLong());

                if (rispostaControlli.getrLong() == -1) { // se non ha trovato la chiave...
                    if (rispostaControlli.isrBoolean()) { // se in ogni caso la query è andata a
                        // buon fine

                        // aggiunta su controlli su collegamento
                        versamento.addEsitoControlloOnControlliCollegamento(
                                ControlliWSBundle.getControllo(
                                        ControlliWSBundle.CTRL_COLL_ESISTEUDOCCOLLEGATA),
                                rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, tmpCsChiaveColl,
                                rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

                        collegamentoHasErr = true; // non lo ha trovato -> va in errore e
                        // in questo caso devo verificare se la chiave collegamento è
                        // formalmente corretta
                        faiVerificaFormaleChiave = true;

                    } else {
                        // allora è un errore di database..

                        this.setEsitoControlloErr(ControlliWSBundle.CTRL_GENERIC_ERROR,
                                rispostaControlli.getCodErr(), rispostaControlli.getDsErr(),
                                rispostaWs);

                        // esco dal ciclo
                        break;
                    }
                } else {
                    // registro esito positivo su controllo
                    // aggiunta su controlli su collegamento
                    versamento.addControlloOkOnControlliCollegamento(
                            ControlliWSBundle
                                    .getControllo(ControlliWSBundle.CTRL_COLL_ESISTEUDOCCOLLEGATA),
                            tmpCsChiaveColl);
                }

                if (faiVerificaFormaleChiave) {
                    long tmpIdTipoRegistroUdColl = 0;
                    // test per vedere se la chiave dell'UD riferita è corretta
                    // verifica il tipo registro
                    rispostaControlli = controlliSemantici.checkTipoRegistro(
                            tmpCsChiaveColl.getTipoRegistro(), descChiaveUdColl,
                            strutturaUpdVers.getIdStruttura(), descChiaveUdColl);

                    if (rispostaControlli.isrBoolean()) {
                        // salvo l'id del tipo registro
                        tmpIdTipoRegistroUdColl = rispostaControlli.getrLong();
                        // registro esito positivo su controllo
                        // aggiunta su controlli su collegamento
                        versamento.addControlloOkOnControlliCollegamento(
                                ControlliWSBundle
                                        .getControllo(ControlliWSBundle.CTRL_COLL_REGUDCOLLEGATA),
                                tmpCsChiaveColl);
                    } else {
                        if (rispostaControlli.getCodErr() != null
                                && rispostaControlli.getCodErr().equals(MessaggiWSBundle.ERR_666)) {
                            // è un errore di database..
                            this.setEsitoControlloErr(ControlliWSBundle.CTRL_GENERIC_ERROR,
                                    rispostaControlli.getCodErr(), rispostaControlli.getDsErr(),
                                    rispostaWs);

                            // esco dal ciclo
                            break;
                        } else {
                            // è un errore di registro... non faccio gli altri test ma proseguo
                            // con la verifica degli altri collegamenti. Anche in questo
                            // caso il versamento è condannato e la cosa probabilmente non
                            // ha molto senso.
                            // aggiunta su controlli su collegamento
                            versamento.addEsitoControlloOnControlliCollegamento(
                                    ControlliWSBundle.getControllo(
                                            ControlliWSBundle.CTRL_COLL_REGUDCOLLEGATA),
                                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO,
                                    tmpCsChiaveColl, rispostaControlli.getCodErr(),
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
                            tmpCsChiaveColl.getTipoRegistro(),
                            strutturaUpdVers.getUrnPartChiaveUd(), tmpCsChiaveColl.getAnno(),
                            tmpIdTipoRegistroUdColl, descChiaveUdColl);
                    if (rispostaControlli.isrBoolean()) {
                        tmpConfigRegAnno = (ConfigRegAnno) rispostaControlli.getrObject();
                    } else {
                        if (rispostaControlli.getCodErr() != null
                                && rispostaControlli.getCodErr().equals(MessaggiWSBundle.ERR_666)) {
                            // è un errore di database..
                            this.setEsitoControlloErr(ControlliWSBundle.CTRL_GENERIC_ERROR,
                                    rispostaControlli.getCodErr(), rispostaControlli.getDsErr(),
                                    rispostaWs);
                            break;
                        } else {

                            // è un errore di compatibilità tra anno e registro...
                            // non faccio gli altri test ma proseguo
                            // con la verifica degli altri collegamenti. Anche in questo
                            // caso il versamento è condannato e la cosa probabilmente non
                            // ha molto senso.
                            // aggiunta su controlli su collegamento
                            versamento.addEsitoControlloOnControlliCollegamento(
                                    ControlliWSBundle.getControllo(
                                            ControlliWSBundle.CTRL_COLL_REGUDCOLLEGATA),
                                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO,
                                    tmpCsChiaveColl, rispostaControlli.getCodErr(),
                                    rispostaControlli.getDsErr());
                            collegamentoHasErr = true;
                            continue;
                        }
                    }

                    /*
                     * 2.4.3.3.1. controlla il numero dell’unità doc da collegare sia coerente con
                     * il formato previsto dal periodo di validità del registro determinato
                     * dall’anno dell’unità doc da collegare
                     *
                     * All'utility NON viene passata la parte relativa al TPI dato che si tratta
                     * esclusivamente di aggiornamento dei METADATI del singolo collegamento
                     */

                    // inizializzo il calcolo della lunghezza massima del campo numero
                    KeySizeUtility tmpKeySizeUtility = new KeySizeUtility(
                            strutturaUpdVers.getVersatoreNonverificato(), tmpCsChiaveColl,
                            null/* TPI: non utilizzato dato che si tratta solo di verificare */);
                    // verifico se la chiave va bene in funzione del formato atteso per il numero
                    KeyOrdUtility tmpKeyOrdUtility;
                    tmpKeyOrdUtility = new KeyOrdUtility(tmpConfigRegAnno,
                            tmpKeySizeUtility.getMaxLenNumero());
                    // verifico se la chiave va bene
                    rispostaControlli = tmpKeyOrdUtility.verificaChiave(tmpCsChiaveColl,
                            strutturaUpdVers.getUrnPartChiaveUd());
                    if (!rispostaControlli.isrBoolean()) {
                        // aggiunta su controlli su collegamento
                        versamento.addEsitoControlloOnControlliCollegamento(
                                ControlliWSBundle
                                        .getControllo(ControlliWSBundle.CTRL_COLL_REGUDCOLLEGATA),
                                rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, tmpCsChiaveColl,
                                rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                        collegamentoHasErr = true;
                    } else {
                        // registro esito positivo su controllo
                        // aggiunta su controlli su collegamento
                        versamento.addControlloOkOnControlliCollegamento(
                                ControlliWSBundle
                                        .getControllo(ControlliWSBundle.CTRL_COLL_REGUDCOLLEGATA),
                                tmpCsChiaveColl);
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

                    tmpForzaColl = false;/* fixed-> non deve generare MAI warning */
                    // aggiunta su controlli su collegamento
                    versamento.addEsitoControlloOnControlliCollegamentoBundle(
                            ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_COLL_DESCR),
                            rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, tmpCsChiaveColl,
                            MessaggiWSBundle.UD_004_006, strutturaUpdVers.getUrnPartChiaveUd());
                    collegamentoHasErr = true;
                } else {
                    // registro esito positivo su controllo
                    // aggiunta su controlli su collegamento
                    versamento.addControlloOkOnControlliCollegamento(
                            ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_COLL_DESCR),
                            tmpCsChiaveColl);
                }
            }
        } // fine loop sui collegamenti

        // valutazione dell'esito

        // se ho riscontrato un errore ...
        this.evaluateControlliOnCollegamenti(collegamentoHasErr, tmpForzaColl, versamento,
                rispostaWs);

        // build on esisto (collegamenti normalizzati)
        /*
         * aggiunta questa condizione qualora non fossero presenti collegamenti (casistica della
         * cancellazione)
         */
        if (!strutturaUpdVers.getUnitaDocCollegate().isEmpty()) {
            myEsito.setDocumentiCollegati(strutturaUpdVers.getUnitaDocCollegate());
        }

        return true;// prosegui con successivi controlli
    }

    private void evaluateControlliOnCollegamenti(boolean collegamentoHasErr, boolean tmpForzaColl,
            UpdVersamentoExt versamento, IRispostaUpdVersWS rispostaWs) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        if (collegamentoHasErr) {
            // ...ma esiste la forzatura sui controlli lo trasformo in WARNING
            if (tmpForzaColl) {
                // esito generale
                this.setEsitoControlloWarnBundle(ControlliWSBundle.CTRL_UD_COLLEGAMENTI,
                        MessaggiWSBundle.UD_016_003, rispostaWs);

                // aggiunta su warnings
                versamento.addControlloOnWarningsBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_COLLEGAMENTI),
                        MessaggiWSBundle.UD_016_003);

                // aggiunta su controlli documento (ultimo warning registrato)
                versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_COLLEGAMENTI),
                        rispostaWs.getSeverity(), TipiEsitoErrore.WARNING,
                        MessaggiWSBundle.UD_016_003);

            } else {
                // esito generale
                this.setEsitoControlloErrBundle(ControlliWSBundle.CTRL_UD_COLLEGAMENTI,
                        MessaggiWSBundle.UD_016_003, rispostaWs);
                // aggiunta su controlli documento (ultimo errore registrato)
                versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_COLLEGAMENTI),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO,
                        MessaggiWSBundle.UD_016_003);
            }
        } else if (!strutturaUpdVers.getUnitaDocCollegate()
                .isEmpty()) /*
                             * aggiunta questa condizione qualora non fossero presenti collegamenti
                             * (casistica della cancellazione)
                             */ {
            // salvo l'esito positivo
            // aggiunta su controlli documento
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_COLLEGAMENTI));

        }
    }

}
