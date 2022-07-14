/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.prsr;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.ControlliWS;
import it.eng.parer.ws.ejb.XmlUpdVersCache;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.RispostaControlliAttSpec;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore;
import it.eng.parer.ws.versamento.ejb.RapportoVersBuilder;
import it.eng.parer.ws.versamentoUpd.dto.CompRapportoUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.FlControlliUpd;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.ejb.ControlliUpdVersamento;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.versamentoUpd.prsr.strategy.UpdCollegamentiPrsr;
import it.eng.parer.ws.versamentoUpd.prsr.strategy.UpdDocumentiIdCountPrsr;
import it.eng.parer.ws.versamentoUpd.prsr.strategy.UpdDocumentiPrsr;
import it.eng.parer.ws.versamentoUpd.utils.UpdCostanti;
import it.eng.parer.ws.versamentoUpd.utils.UpdCostanti.TipoAggiornamento;
import it.eng.parer.ws.versamentoUpd.utils.UpdGestioneDatiSpec;
import it.eng.parer.ws.xml.versReq.TipoConservazioneType;
import it.eng.parer.ws.xml.versUpdReq.IndiceSIPAggiornamentoUnitaDocumentaria;

/**
 *
 * @author sinatti_s
 */
public abstract class UpdVersamentoBasePrsr {

    private static final Logger log = LoggerFactory.getLogger(UpdVersamentoBasePrsr.class);

    protected UpdVersamentoExt versamento;
    protected RispostaWSUpdVers rispostaWs;
    protected RispostaControlli rispostaControlli;
    // l'istanza dell'indice SIP di aggiornamento
    protected IndiceSIPAggiornamentoUnitaDocumentaria parsedIndiceUpd = null;
    //
    protected CSVersatore tagCSVersatore = new CSVersatore();
    protected CSChiave tagCSChiave = new CSChiave();
    protected String descChiaveUD = "";
    //
    // stateless ejb per i controlli sul db
    protected ControlliSemantici controlliSemantici = null;
    // stateless ejb per verifica autorizzazione ws
    protected ControlliWS controlliEjb = null;
    // singleton ejb di gestione cache dei parser jaxb dei fascicoli
    protected XmlUpdVersCache xmlUpdVersCache = null;
    // stateless ejb: crea il rapporto di versamento e canonicalizza (C14N) il SIP
    protected RapportoVersBuilder rapportoVersBuilder = null;
    // controlli su versamento
    protected ControlliUpdVersamento updVersamentoControlli = null;
    // classe di supporto per la verifica e l'estrazione dei dati specifici
    protected UpdGestioneDatiSpec gestioneDatiSpec = null; // allocata nel costruttore
    // singleton ejb di gestione configurazioni applicative
    protected ConfigurationHelper configurationHelper = null;

    public UpdVersamentoExt getVersamento() {
        return versamento;
    }

    public RispostaWSUpdVers getRispostaWs() {
        return rispostaWs;
    }

    protected boolean controllaUnivocitaDocIDs() {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        //
        UpdExecutePrsr controllaUnivocitaDocID = new UpdExecutePrsr();
        controllaUnivocitaDocID.eseguiControlli(new UpdDocumentiIdCountPrsr(versamento));

        // questo test, sui documenti, ha senso perché l'ID Documento può essere nullo

        // Univocità ID (solo in presenza di documenti da aggiornare altrimenti
        // NON_ATTIVATO)
        if (versamento.hasDocumentiToUpd()) {
            if (strutturaUpdVers.isTrovatiIdDocDuplicati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);

                // esito generale
                rispostaWs.setEsitoWsErrBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_UNIVOCITADOCUMENTI),
                        MessaggiWSBundle.XSD_002_002);

                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGeneraliBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_UNIVOCITADOCUMENTI),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.XSD_002_002);

            } else {
                // aggiunta su controlli generali
                versamento.addControlloOkOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_UNIVOCITADOCUMENTI));
            }
        }

        // ALLEGATI
        if (strutturaUpdVers.isCorrAllegatiDichiarati() != null) {
            if (!strutturaUpdVers.isCorrAllegatiDichiarati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);

                // esito generale
                rispostaWs.setEsitoWsErrBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAALLEGATI),
                        MessaggiWSBundle.XSD_003_001);

                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGeneraliBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAALLEGATI),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.XSD_003_001);
            } else {
                // aggiunta su controlli generali
                versamento.addControlloOkOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAALLEGATI));
            }
        }

        // ANNESSI
        if (strutturaUpdVers.isCorrAnnessiDichiarati() != null) {
            if (!strutturaUpdVers.isCorrAnnessiDichiarati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);

                // esito generale
                rispostaWs.setEsitoWsErrBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAANNESSI),
                        MessaggiWSBundle.XSD_004_001);

                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGeneraliBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAANNESSI),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.XSD_004_001);
            } else {
                // aggiunta su controlli generali
                versamento.addControlloOkOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAANNESSI));
            }
        }
        // ANNOTAZIONI
        if (strutturaUpdVers.isCorrAnnotazioniDichiarati() != null) {
            if (!strutturaUpdVers.isCorrAnnotazioniDichiarati()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);

                // esito generale
                rispostaWs.setEsitoWsErrBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAANNOTAZIONI),
                        MessaggiWSBundle.XSD_005_001);

                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGeneraliBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAANNOTAZIONI),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.XSD_005_001);
            } else {
                // aggiunta su controlli generali
                versamento.addControlloOkOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CORRISPONDENZAANNOTAZIONI));
            }
        }

        return true; // esegui controllo successivo
    }

    protected boolean controllaTipoAggiornamento() {
        // se esiste si esegue il controllo, altrimenti NON_ATTIVATO
        if (StringUtils.isNotBlank(versamento.getStrutturaUpdVers().getTipoAggiornamento())) {
            if (!versamento.getStrutturaUpdVers().getTipoAggiornamento().equals(TipoAggiornamento.METADATI.name())) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                // esito generale
                rispostaWs.setEsitoWsErrBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CONTROLLOTIPOAGGIORNAMENTO),
                        MessaggiWSBundle.XSD_006_006);

                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGeneraliBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CONTROLLOTIPOAGGIORNAMENTO),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.XSD_006_006);

                // se non coincidono questi elementi posso sempre
                // tentare di riscostruire la struttura, la chiave ed il tipo
                // e trasformare la sessione errata in una sessione fallita
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
            } else {
                // aggiunta su controlli generali
                versamento.addControlloOkOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_XSD_CONTROLLOTIPOAGGIORNAMENTO));
            }
        } else {
            // default METADATI
            versamento.getStrutturaUpdVers().setTipoAggiornamento(TipoAggiornamento.METADATI.name());
        }
        return true; // prosegui controllo successivo
    }

    protected boolean controllaVersioneXSD() {
        if (!versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getVersione()
                .equals(versamento.getVersioneWsChiamata())) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_VERSIONEXSD),
                    MessaggiWSBundle.UD_001_013,
                    versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getVersione());

            // aggiunta su controlli generali
            versamento.addEsitoControlloOnGeneraliBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_VERSIONEXSD), rispostaWs.getSeverity(),
                    TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_001_013,
                    versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getVersione());

            // se non coincidono questi elementi posso sempre
            // tentare di riscostruire la struttura, la chiave ed il tipo
            // e trasformare la sessione errata in una sessione fallita
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
        } else {
            // aggiunta su controlli generali
            versamento
                    .addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_VERSIONEXSD));
        }

        return true; // prosegui controllo successivo
    }

    protected boolean controllaStrutturaVersatore() {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        // verifica la struttura versante e versatore
        boolean utenteOk = true, strutturaOk = true;

        rispostaControlli = controlliSemantici.checkIdStrut(tagCSVersatore, TipiWSPerControlli.AGGIORNAMENTO_VERSAMENTO,
                versamento.getStrutturaUpdVers().getDataVersamento());
        if (rispostaControlli.getrLong() < 1) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            String cdControllo = ControlliWSBundle.CTRL_GENERIC_ERROR; // default generico

            // tipologie di errore -> relativo controllo
            switch (rispostaControlli.getCodErr()) {
            case MessaggiWSBundle.UD_001_001:
                // ambiente
                cdControllo = ControlliWSBundle.CTRL_INTS_AMBIENTE;
                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(cdControllo),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
                break;
            case MessaggiWSBundle.UD_001_002:
                // ente
                cdControllo = ControlliWSBundle.CTRL_INTS_ENTE;
                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(cdControllo),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());

                // check su ambiente superato
                // aggiunta su controlli generali
                versamento
                        .addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE));

                break;
            case MessaggiWSBundle.UD_001_003:
                // struttura
                cdControllo = ControlliWSBundle.CTRL_INTS_STRUTTURA;
                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(cdControllo),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
                // controllo struttura KO (non si effettuano ulteriori verifiche)
                strutturaOk = false;
                // check su ambiente/ente superato
                // aggiunta su controlli generali
                versamento
                        .addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE));
                versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ENTE));

                break;
            case MessaggiWSBundle.UD_001_015:
                // struttura
                cdControllo = ControlliWSBundle.CTRL_INTS_STRUTTURA;
                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(cdControllo),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
                // controllo struttura KO (non si effettuano ulteriori verifiche)
                strutturaOk = false;
                // check su ambiente/ente superato
                // aggiunta su controlli generali
                versamento
                        .addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE));
                versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ENTE));

                break;
            case MessaggiWSBundle.UD_001_019:
                // struttura
                cdControllo = ControlliWSBundle.CTRL_INTS_STRUTTURA;
                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(cdControllo),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());
                // controllo struttura KO (non si effettuano ulteriori verifiche)
                strutturaOk = false;
                // check su ambiente/ente superato
                // aggiunta su controlli generali
                versamento
                        .addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE));
                versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ENTE));

                break;
            }

            // esito generale
            rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(cdControllo), rispostaControlli.getCodErr(),
                    rispostaControlli.getDsErr());

            if (rispostaControlli.getrLongExtended() < 1) {
                // se anche questo controllo fallisce la sessione
                // è sempre errata, visto quanto dichiarato non esiste
                // e non ho alcun modo di determinare la struttura
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
                strutturaOk = false;
            } else {
                // se rLongExtended è valorizzato, la struttura esiste (ma è template):
                // posso sempre tentare di riscostruire la chiave ed il tipo UD
                // e trasformare la sessione errata in una sessione fallita
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
                // salvo idstruttura template
                strutturaUpdVers.setIdStruttura(rispostaControlli.getrLongExtended());
                // salvo idUtente
                strutturaUpdVers.setIdUser(versamento.getUtente().getIdUtente());
            }
        } else {
            // aggiunta su controlli generali
            versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_AMBIENTE));
            // aggiunta su controlli generali
            versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ENTE));

            // salvo idstruttura individuata
            strutturaUpdVers.setIdStruttura(rispostaControlli.getrLong());
            // salvo idUtente
            strutturaUpdVers.setIdUser(versamento.getUtente().getIdUtente());
        }

        // controlla che il valore del tag UserID coincida con con lo user specificato
        // nell’attivazione del servizio
        if (!versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getVersatore().getUserID()
                .equals(versamento.getLoginName())) {

            rispostaWs.setSeverity(SeverityEnum.ERROR);

            // esito generale
            rispostaWs.setEsitoWsErrBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ABILITAZIONEUTENTE),
                    MessaggiWSBundle.UD_001_005,
                    versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getVersatore().getUserID());

            // aggiunta su controlli generali
            versamento.addEsitoControlloOnGeneraliBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ABILITAZIONEUTENTE),
                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_001_005,
                    versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getVersatore().getUserID());

            utenteOk = false;
        }

        // sia controllo struttura che controllo sull'utente OK
        // adesso ... verifica se l'utente è autorizzato ad usare il WS sulla struttura
        if (strutturaOk && utenteOk) {
            versamento.getUtente().setIdOrganizzazioneFoglia(new BigDecimal(strutturaUpdVers.getIdStruttura()));
            rispostaControlli = controlliEjb.checkAuthWS(versamento.getUtente(), versamento.getDescrizione(),
                    TipiWSPerControlli.AGGIORNAMENTO_VERSAMENTO);
            if (!rispostaControlli.isrBoolean()) {

                // esito generale
                rispostaWs.setSeverity(SeverityEnum.ERROR);

                rispostaWs.setEsitoWsError(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ABILITAZIONEUTENTE),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ABILITAZIONEUTENTE),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());

                // sessione dubbia / da verificare
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);

                utenteOk = false;

            } else {
                // aggiunta su controlli generali
                versamento.addControlloOkOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_ABILITAZIONEUTENTE));
            }
        }

        // se struttura OK verifico tipo "sconosciuto" e "partizionamento"

        if (strutturaOk) {

            // verifica abilitazione struttura su tipi "Sconosciuti"
            boolean hasUnknownErr = false;
            // tipologia UD "Sconosciuto"
            rispostaControlli = controlliSemantici.checkTipologiaUD(UpdCostanti.TIPOUD_SCONOSCIUTO, descChiaveUD,
                    strutturaUpdVers.getIdStruttura());
            // tipologia REG "Sconosciuto"
            if (rispostaControlli.isrBoolean()) {
                // registro ID TIPOUD
                strutturaUpdVers.setIdTipoUDUnknown(rispostaControlli.getrLong());
                strutturaUpdVers.setDescTipologiaUnitaDocumentariaUnknown(rispostaControlli.getrStringExtended());
            } else {
                hasUnknownErr = true;
            }
            // tipo REG "Sconosciuto"
            rispostaControlli = controlliSemantici.checkTipoRegistro(UpdCostanti.TIPOREG_SCONOSCIUTO, descChiaveUD,
                    strutturaUpdVers.getIdStruttura());

            if (rispostaControlli.isrBoolean()) {
                // registro ID TIPOREG
                strutturaUpdVers.setIdTipoREGUnknown(rispostaControlli.getrLong());

                rispostaControlli = controlliSemantici.checkTipoDocumento(UpdCostanti.TIPDOC_PRINCIPALE_SCONOSCIUTO,
                        strutturaUpdVers.getIdStruttura(), true, descChiaveUD);
            } else {
                hasUnknownErr = true;
            }

            // tipo doc principale "Sconosciuto"
            rispostaControlli = controlliSemantici.checkTipoDocumento(UpdCostanti.TIPDOC_PRINCIPALE_SCONOSCIUTO,
                    strutturaUpdVers.getIdStruttura(), true, descChiaveUD);

            if (rispostaControlli.isrBoolean()) {
                // registro ID TIPODOCPRINCI
                strutturaUpdVers.setIdTipoDOCPRINCUnknown(rispostaControlli.getrLong());
            } else {
                hasUnknownErr = true;
            }

            // se si è verificato almeno UN errore
            if (hasUnknownErr) {

                // la sessione sarà dubbia in quanto la struttura non risulta abilitata su uno
                // dei tipi sconociuti / si prova successivamente a recuperare un tipo
                // documento/registro/doc. principale al fine di
                // trasformare la sessione errata in fallita
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);

                rispostaWs.setSeverity(SeverityEnum.ERROR);
                // esito generale
                rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_STRUTTURA),
                        MessaggiWSBundle.UD_001_018, versamento.getVersamento().getUnitaDocumentaria().getIntestazione()
                                .getVersatore().getStruttura());

                versamento.addEsitoControlloOnGeneraliBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_STRUTTURA), rispostaWs.getSeverity(),
                        TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_001_018, versamento.getVersamento()
                                .getUnitaDocumentaria().getIntestazione().getVersatore().getStruttura());

                strutturaOk = false;

            } else {
                // Test sugli "sconosciuti" OK
                // aggiunta su controlli generali
                versamento.addControlloOkOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_STRUTTURA));
            }

            // verifica il partizionamento corretto della struttura
            rispostaControlli = updVersamentoControlli.checkPartizioniStruttuaByAA(descChiaveUD,
                    strutturaUpdVers.getIdStruttura(), strutturaUpdVers.getChiaveNonVerificata().getAnno());

            if (!rispostaControlli.isrBoolean()) {
                // se il partizionamento è scorretto, la sessione di versamento è errata senza
                // possibilità di recupero (anche le sessioni fallite sono partizionate sul db)
                rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                // esito generale
                rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_PARTIZIONI),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_PARTIZIONI),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                        rispostaControlli.getDsErr());

                strutturaOk = false;

            } else {
                // aggiunta su controlli generali
                versamento.addControlloOkOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_PARTIZIONI));

            }

        }

        return strutturaOk && utenteOk;
    }

    protected boolean controlloChiaveECdKeyNorm() {
        boolean prosegui = controllaChiave();
        // se esito precedente positivo - verifico se calcolare chiave normalizzata (se
        // esiste già su sistema restituisco errore)
        if (prosegui) {
            prosegui = controllaCdKeyNormalized();
        }
        if (prosegui) {
            // aggiunta su controlli generali
            versamento.addControlloOkOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD));
        }

        return prosegui;
    }

    private boolean controllaChiave() {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();

        boolean prosegui = true;
        // controlla prensa chiave UD
        rispostaControlli = updVersamentoControlli.checkChiaveAndTipoDocPrinc(strutturaUpdVers.getChiaveNonVerificata(),
                strutturaUpdVers.getIdStruttura(), ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
        if (rispostaControlli.isrBoolean()) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);

            prosegui = false; // chiave non trovata

            // esito generale
            rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD),
                    rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            // aggiunta su controlli generali
            versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD),
                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                    rispostaControlli.getDsErr());

            // controlla registro
            rispostaControlli = controlliSemantici.checkTipoRegistro(tagCSChiave.getTipoRegistro(), descChiaveUD,
                    strutturaUpdVers.getIdStruttura());

            if (!rispostaControlli.isrBoolean()) {
                // raccolgo esito NEGATIVO solo per errori "gravi"
                if (rispostaControlli.getCodErr().contains(MessaggiWSBundle.ERR_666)) {
                    rispostaWs.setSeverity(SeverityEnum.ERROR);

                    // esito generale
                    rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

                    // aggiunta su ..... TODO: da determinare un controllo specifico?!

                }
            } else {
                // recupero registro
                strutturaUpdVers.setIdRegistro(rispostaControlli.getrLong());
            }

            // controlla tipo ud
            rispostaControlli = controlliSemantici.checkTipologiaUD(
                    strutturaUpdVers.getDescTipologiaUnitaDocumentariaNonVerificata(), descChiaveUD,
                    strutturaUpdVers.getIdStruttura());

            if (!rispostaControlli.isrBoolean()) {
                // raccolgo esito NEGATIVO solo per errori "gravi"
                if (rispostaControlli.getCodErr().contains(MessaggiWSBundle.ERR_666)) {
                    rispostaWs.setSeverity(SeverityEnum.ERROR);

                    // esito generale
                    rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

                    // aggiunta su ..... TODO: da determinare un controllo specifico?!
                }
            } else {
                // recupero tipo ud
                strutturaUpdVers.setIdTipoUDNonVerificata(rispostaControlli.getrLong());
                strutturaUpdVers.setDescTipologiaUnitaDocumentariaNonVerificata(rispostaControlli.getrStringExtended());
            }

            // controlla documento principale (se esiste)
            if (versamento.hasDocumentoPrincipaleToUpd()) {
                rispostaControlli = controlliSemantici.checkTipoDocumento(
                        strutturaUpdVers.getDescTipoDocPrincipaleNonVerificato(), strutturaUpdVers.getIdStruttura(),
                        true, "dummy doc");
                if (!rispostaControlli.isrBoolean()) {
                    // raccolgo esito NEGATIVO solo per errori "gravi"
                    if (rispostaControlli.getCodErr().contains(MessaggiWSBundle.ERR_666)) {
                        rispostaWs.setSeverity(SeverityEnum.ERROR);

                        // esito generale
                        rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

                        // aggiunta su ..... TODO: da determinare un controllo specifico?!
                    }
                } else {
                    // recupero tipo doc princ
                    strutturaUpdVers.setIdTipoDocPrincipale(rispostaControlli.getrLong());
                }
            }

        } /* UD trovata */ else {
            // recupera ID UD
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

            // forza collegamento
            strutturaUpdVers.setFlForzaCollegamento(
                    ((String) rispostaControlli.getrMap().get(RispostaControlli.ValuesOnrMap.FL_FORZA_COLL.name()))
                            .equals("1"));

            // recupero Stato Conservazione
            strutturaUpdVers.setStatoConservazioneUnitaDoc(
                    (CostantiDB.StatoConservazioneUnitaDoc) rispostaControlli.getrObject());

            // aggiunta cd key normalized
            strutturaUpdVers.setCdKeyNormalized(((String) rispostaControlli.getrMap()
                    .get(RispostaControlli.ValuesOnrMap.CD_KEY_NORMALIZED.name())));

            // aggiunta flag su riposta
            myEsito.getParametriAggiornamento().setForzaCollegamento(strutturaUpdVers.isFlForzaCollegamento());

        }

        return prosegui;
    }

    protected boolean controllaHashSIP(SyncFakeSessn sessione) {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        // dato che su DB verrà salvato il SIP prodotto dalla canonicalizzazione
        // si effettua il calcolo su quest'ultimo
        rispostaControlli = updVersamentoControlli.checkUltimoSIPWithHashBinary(descChiaveUD,
                versamento.getStrutturaUpdVers().getIdUd(), sessione.getDatiDaSalvareIndiceSip());

        if (!rispostaControlli.isrBoolean()) {
            // se risulta che l'hash dell'ultimo SIP aggiornato è il medesimo rispetto
            // l'attuale versamento
            // si restituisce errore
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_HASHSIP),
                    rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_HASHSIP), rispostaWs.getSeverity(),
                    TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

        } else {
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_HASHSIP));
            // per la sessione
            sessione.setHashIndiceSipXml(rispostaControlli.getrString());
            // per aggiornamento
            strutturaUpdVers.setHashIndiceSipXml(rispostaControlli.getrString());
            strutturaUpdVers.setAlgoritmoHashIndiceSIP(CostantiDB.TipiHash.SHA_256.descrivi());
            strutturaUpdVers.setEncodingHashIndiceSIP(CostantiDB.TipiEncBinari.HEX_BINARY.descrivi());

        }

        return true; // prosegui
    }

    protected boolean checkTipoUDAndAbilUpdMeta(CompRapportoUpdVers myEsito) {
        FlControlliUpd flControlliUpd = null;
        boolean prosegui = true;
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        /*
         * TODO: prima del controllo per struttura, il sistema controlla che la unità doc determinata in precedenza
         * abbia tipo di unità doc pari al valore del tag “TipologiaUnitaDocumentaria“ su SIP aggiornamento il tag in
         * questione non è presente
         */
        if (!versamento.getVersamento().getUnitaDocumentaria().getIntestazione().getTipologiaUnitaDocumentaria()
                .equalsIgnoreCase(strutturaUpdVers.getDescTipologiaUnitaDocumentariaNonVerificata())) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_TIPO),
                    MessaggiWSBundle.UD_015_001, descChiaveUD, versamento.getVersamento().getUnitaDocumentaria()
                            .getIntestazione().getTipologiaUnitaDocumentaria());

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_TIPO), rispostaWs.getSeverity(),
                    TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_015_001, descChiaveUD, versamento.getVersamento()
                            .getUnitaDocumentaria().getIntestazione().getTipologiaUnitaDocumentaria());

            prosegui = false;
        } else {
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_TIPO));

            // salvo l'id tipologia UD
            strutturaUpdVers.setIdTipologiaUnitaDocumentaria(strutturaUpdVers.getIdTipoUDNonVerificata()); // da
            // controllaChiave
            strutturaUpdVers.setDescTipologiaUnitaDocumentaria(
                    strutturaUpdVers.getDescTipologiaUnitaDocumentariaNonVerificata()); // da
            // controllaChiave

            // recupero DecTipoUnitaDoc per la costruzione dei flag
            rispostaControlli = updVersamentoControlli.getUpdFlagsFromTipoUDOrgStrut(
                    strutturaUpdVers.getIdTipologiaUnitaDocumentaria(), strutturaUpdVers.isFlForzaAggiornamento());

            /// 666P
            if (!rispostaControlli.isrBoolean()) {

                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

                return false; // non proseguo con i controlli
            }

            // recupero l'oggetto per impostare i flag (se esistono -> altrimenti da ricerca
            // nella struttura)

            flControlliUpd = (FlControlliUpd) rispostaControlli.getrObject();

            strutturaUpdVers.setFlControlliUpd(flControlliUpd);

            if (!flControlliUpd.isFlAbilitaUpdMeta()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                // esito generale
                rispostaWs.setEsitoWsErrBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_ABILAGGIORNAMENTO),
                        MessaggiWSBundle.UD_013_002, descChiaveUD);

                // aggiunta su controlli documento
                versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_ABILAGGIORNAMENTO),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_013_002, descChiaveUD);

                prosegui = false;
            } else {
                // aggiunta OK
                versamento.addControlloOkOnControlliUnitaDocumentaria(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_ABILAGGIORNAMENTO));
            }

            // aggiunta flag su riposta
            myEsito.getParametriAggiornamento().setAbilitaAggiornamento(flControlliUpd.isFlAbilitaUpdMeta());
            //
            myEsito.getParametriAggiornamento().setForzaAggiornamento(flControlliUpd.isFlForzaUpdMetaInark());
            //
            myEsito.getParametriAggiornamento()
                    .setAccettaAggiornamentoInArchivio(flControlliUpd.isFlAccettaUpdMetaInark());

        }

        return prosegui; // prosegui controlli successivi
    }

    protected boolean controllaAbilUserRegTipoUD() {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        // verifica abilitazione utente per registro/tipo UD
        rispostaControlli = updVersamentoControlli.checkTipoUDRegIamUserOrganizzazione(
                strutturaUpdVers.getChiaveNonVerificata(), strutturaUpdVers.getDescTipologiaUnitaDocumentaria(),
                strutturaUpdVers.getIdStruttura(), strutturaUpdVers.getIdUser(),
                strutturaUpdVers.getIdTipologiaUnitaDocumentaria(), strutturaUpdVers.getIdRegistro());

        if (!rispostaControlli.isrBoolean()) {
            // se risulta che l'hash dell'ultimo SIP aggiornato è il medesimo rispetto
            // l'attuale versamento
            // si restituisce errore
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_ABILUTENTE),
                    rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_ABILUTENTE), rispostaWs.getSeverity(),
                    TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

        } else {
            // aggiunta OK
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_ABILUTENTE));
        }

        return true; // prosegui controlli successivi
    }

    protected boolean controllaStatoConservazione() {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        FlControlliUpd flControlliUpd = strutturaUpdVers.getFlControlliUpd();
        boolean isWarn = flControlliUpd.isFlAccettaUpdMetaInark() && flControlliUpd.isFlForzaUpdMetaInark();
        //
        CostantiDB.StatoConservazioneUnitaDoc scud = strutturaUpdVers.getStatoConservazioneUnitaDoc();
        if (scud == CostantiDB.StatoConservazioneUnitaDoc.PRESA_IN_CARICO
                || scud == CostantiDB.StatoConservazioneUnitaDoc.AIP_GENERATO
                || scud == CostantiDB.StatoConservazioneUnitaDoc.AIP_IN_AGGIORNAMENTO
                || scud == CostantiDB.StatoConservazioneUnitaDoc.IN_VOLUME_DI_CONSERVAZIONE
                || scud == CostantiDB.StatoConservazioneUnitaDoc.AIP_FIRMATO) {

            // aggiunta OK
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_STATOCONSERVAZIONE));

        } else if ((scud == CostantiDB.StatoConservazioneUnitaDoc.IN_ARCHIVIO
                || scud == CostantiDB.StatoConservazioneUnitaDoc.IN_CUSTODIA) && isWarn) {
            // si restituisce warning (solo se in precedenza non si è verificato ERROR
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                rispostaWs.setSeverity(SeverityEnum.WARNING);

                // esito generale
                rispostaWs.setEsitoWsWarnBundle(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_STATOCONSERVAZIONE),
                        MessaggiWSBundle.UD_013_003, descChiaveUD, scud);
            }
            // aggiunta su controlli warning
            versamento.addControlloOnWarningsBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_STATOCONSERVAZIONE),
                    MessaggiWSBundle.UD_013_003, descChiaveUD, scud);

            // aggiunta su controlli ud
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_STATOCONSERVAZIONE), SeverityEnum.WARNING,
                    TipiEsitoErrore.WARNING, MessaggiWSBundle.UD_013_003, descChiaveUD, scud);

        } else {
            // si restituisce errore
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_STATOCONSERVAZIONE),
                    MessaggiWSBundle.UD_013_003, descChiaveUD, scud);

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_STATOCONSERVAZIONE),
                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_013_003, descChiaveUD,
                    scud);

        }

        return true; // prosegui controlli successivi
    }

    protected boolean controllaCollegamenti(CompRapportoUpdVers myEsito) {
        //
        UpdExecutePrsr controlloCollegamenti = new UpdExecutePrsr();
        return controlloCollegamenti.eseguiControlli(new UpdCollegamentiPrsr(versamento, rispostaWs, myEsito));

    }

    protected boolean controllaProfiloUnitaDocumentaria() {
        boolean profileHasError = false;
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();

        // se non sono presenti il profilo UD oppure non è presente la data o l'oggetto
        // verifico se sono opzionali, in base ai flag letti al punto precedente
        if (strutturaUpdVers.getFlControlliUpd().isFlProfiloUdObbOggetto()
                && (versamento.getVersamento().getUnitaDocumentaria().getProfiloUnitaDocumentaria() == null
                        || versamento.getVersamento().getUnitaDocumentaria().getProfiloUnitaDocumentaria()
                                .getOggetto() == null
                        || versamento.getVersamento().getUnitaDocumentaria().getProfiloUnitaDocumentaria().getOggetto()
                                .isEmpty())) {

            // si restituisce errore
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_PROFILOUD),
                    MessaggiWSBundle.UD_014_001, descChiaveUD);

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_PROFILOUD), rispostaWs.getSeverity(),
                    TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_014_001, descChiaveUD);

            profileHasError = true;

        }

        if (strutturaUpdVers.getFlControlliUpd().isFlProfiloUdObbData() && (versamento.getVersamento()
                .getUnitaDocumentaria().getProfiloUnitaDocumentaria() == null
                || versamento.getVersamento().getUnitaDocumentaria().getProfiloUnitaDocumentaria().getData() == null)) {

            // si restituisce errore
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_PROFILOUD),
                    MessaggiWSBundle.UD_014_002, descChiaveUD);

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_PROFILOUD), rispostaWs.getSeverity(),
                    TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.UD_014_002, descChiaveUD);

            profileHasError = true;
        }

        if (!profileHasError) {
            // aggiunta su controlli documento
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_PROFILOUD));
        }

        return true;
    }

    protected boolean controllaDatiSpecifici() {
        //
        // costruttore
        gestioneDatiSpec = new UpdGestioneDatiSpec(versamento);
        // verifico gli eventuali dati specifici e di migrazione
        RispostaControlliAttSpec tmpControlliAttSpec = gestioneDatiSpec.parseDatiSpec(TipiEntitaSacer.UNI_DOC,
                versamento.getVersamento().getUnitaDocumentaria().getDatiSpecifici(),
                versamento.getStrutturaUpdVers().getIdTipologiaUnitaDocumentaria(), descChiaveUD,
                versamento.getStrutturaUpdVers().getDescTipologiaUnitaDocumentaria());
        if (tmpControlliAttSpec.isrBoolean()) {
            versamento.getStrutturaUpdVers().setDatiSpecifici(tmpControlliAttSpec.getDatiSpecifici());
            versamento.getStrutturaUpdVers().setIdRecXsdDatiSpec(tmpControlliAttSpec.getIdRecXsdDatiSpec());

            // aggiunto esito positivo
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DATISPECIFICI));
        } else {
            // è un errore grave ...
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DATISPECIFICI),
                    tmpControlliAttSpec.getCodErr(), tmpControlliAttSpec.getDsErr());

            // aggiungo tra gli esiti del documento
            versamento.addEsitoControlloOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DATISPECIFICI), rispostaWs.getSeverity(),
                    TipiEsitoErrore.NEGATIVO, tmpControlliAttSpec.getCodErr(), tmpControlliAttSpec.getDsErr());
        }

        return true; // prosegui controlli successivi
    }

    protected boolean controllaDatiSpeificiMig() {

        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();

        if (StringUtils.isNotBlank(strutturaUpdVers.getTipoConservazione())
                && TipoConservazioneType.MIGRAZIONE.name().equalsIgnoreCase(strutturaUpdVers.getTipoConservazione())) {

            // costruttore
            gestioneDatiSpec = new UpdGestioneDatiSpec(versamento);
            // verifico gli eventuali dati specifici e di migrazione
            RispostaControlliAttSpec tmpControlliAttSpec = gestioneDatiSpec.parseDatiSpecMig(TipiEntitaSacer.UNI_DOC,
                    versamento.getVersamento().getUnitaDocumentaria().getDatiSpecificiMigrazione(),
                    versamento.getStrutturaUpdVers().getIdTipologiaUnitaDocumentaria(), descChiaveUD,
                    versamento.getStrutturaUpdVers().getDescTipologiaUnitaDocumentaria());

            if (tmpControlliAttSpec.isrBoolean()) {
                versamento.getStrutturaUpdVers().setDatiSpecificiMigrazione(tmpControlliAttSpec.getDatiSpecifici());
                versamento.getStrutturaUpdVers()
                        .setIdRecXsdDatiSpecMigrazione(tmpControlliAttSpec.getIdRecXsdDatiSpec());

                // aggiunto esito positivo
                versamento.addControlloOkOnControlliUnitaDocumentaria(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DATISPECIFICIMIGRAZ));

                // myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                // myEsito.getUnitaDocumentaria().getEsitoUnitaDocumentaria().setCorrispondenzaDatiSpecifici("POSITIVO");
            } else {
                // è un errore grave ...
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                // esito generale
                rispostaWs.setEsitoWsError(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DATISPECIFICIMIGRAZ),
                        tmpControlliAttSpec.getCodErr(), tmpControlliAttSpec.getDsErr());

                // aggiungo tra gli esiti del documento
                versamento.addEsitoControlloOnControlliUnitaDocumentaria(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DATISPECIFICIMIGRAZ),
                        rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, tmpControlliAttSpec.getCodErr(),
                        tmpControlliAttSpec.getDsErr());
            }
        } else {
            // esito negativo
            // si restituisce errore
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsErrBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DATISPECIFICIMIGRAZ),
                    MessaggiWSBundle.XSD_006_004);
            // aggiunta su controlli ud
            versamento.addEsitoControlloOnControlliUnitaDocumentariaBundle(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_DATISPECIFICIMIGRAZ),
                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, MessaggiWSBundle.XSD_006_004);
        }

        return true;
    }

    protected boolean controllaDocumenti(CompRapportoUpdVers myEsito) {
        //
        UpdExecutePrsr controlloDocumenti = new UpdExecutePrsr();
        return controlloDocumenti.eseguiControlli(new UpdDocumentiPrsr(versamento, rispostaWs, myEsito));
    }

    protected boolean controllaFascicoliSecondari() {
        // recupera e controlla fascicolo princ
        rispostaControlli = updVersamentoControlli.checkFascicoliSecondari(versamento);

        if (!rispostaControlli.isrBoolean()) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            // esito generale
            rispostaWs.setEsitoWsError(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_FASCICOLISECONDARIVSPRINCIPALE),
                    rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            // aggiunta su controlli documento
            versamento.addEsitoControlloOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_FASCICOLISECONDARIVSPRINCIPALE),
                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                    rispostaControlli.getDsErr());

        } else {
            // aggiunta OK
            versamento.addControlloOkOnControlliUnitaDocumentaria(
                    ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_UD_FASCICOLISECONDARIVSPRINCIPALE));
        }

        return true; // prosegui controlli successivi
    }

    private boolean controllaCdKeyNormalized() {
        StrutturaUpdVers strutturaUpdVers = versamento.getStrutturaUpdVers();
        CompRapportoUpdVers myEsito = rispostaWs.getCompRapportoUpdVers();

        boolean prosegui = true;
        // recupero parametro DB per verifica calcolo URN normalizzato
        rispostaControlli = controlliSemantici.getDtCalcInizioNuoviUrn();

        if (rispostaControlli.isrBoolean()) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);

            prosegui = false; // urn normalizzato già presente su sistema

            // esito generale
            rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD),
                    rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            // aggiunta su controlli generali
            versamento.addEsitoControlloOnGenerali(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD),
                    rispostaWs.getSeverity(), TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(),
                    rispostaControlli.getDsErr());

        } else {
            // dt calcolo nuovi urn
            strutturaUpdVers.setDtInizioCalcoloNewUrn(rispostaControlli.getrDate());
        }
        // se ho recuperato il parametro correttamente vado avanti
        if (prosegui) {
            // controllo e calcolo URN normalizzato
            rispostaControlli = controlliSemantici.checkUniqueCdKeyNormalized(strutturaUpdVers.getChiaveNonVerificata(),
                    strutturaUpdVers.getIdRegistro(), strutturaUpdVers.getIdUd(), strutturaUpdVers.getCdKeyNormalized(),
                    strutturaUpdVers.getDtInizioCalcoloNewUrn());

            if (rispostaControlli.isrBoolean()) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);

                prosegui = false; // urn normalizzato già presente su sistema

                // esito generale
                rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD),
                        rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

                // aggiunta su controlli generali
                versamento.addEsitoControlloOnGenerali(
                        ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_INTS_CHIAVEUD), rispostaWs.getSeverity(),
                        TipiEsitoErrore.NEGATIVO, rispostaControlli.getCodErr(), rispostaControlli.getDsErr());

            } else {
                // cd key normalized (se calcolato)
                if (StringUtils.isBlank(strutturaUpdVers.getCdKeyNormalized())) {
                    strutturaUpdVers.setCdKeyNormalized(rispostaControlli.getrString());
                }
                // dt vers max
                strutturaUpdVers.setDtVersMax(rispostaControlli.getrDate());
            }
        }

        return prosegui;
    }

}
