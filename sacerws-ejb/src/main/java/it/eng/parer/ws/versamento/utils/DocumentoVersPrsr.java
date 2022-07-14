package it.eng.parer.ws.versamento.utils;

import it.eng.parer.entity.DecTipoRapprComp;
import it.eng.parer.entity.constraint.AroCompUrnCalc.TiUrn;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.BinEncUtility;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.Costanti.ModificatoriWS;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.RispostaControlliAttSpec;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamentoTpi.utils.FileServUtils;
import it.eng.parer.ws.xml.versReq.DatiSpecificiType;
import it.eng.parer.ws.xml.versResp.ECComponenteType;
import it.eng.parer.ws.xml.versResp.ECConfigurazioneType;
import it.eng.parer.ws.xml.versResp.ECDocumentoType;
import it.eng.parer.ws.xml.versResp.ECSottoComponenteType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegType;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegWarType;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fioravanti_F
 */
public class DocumentoVersPrsr {

    private static final Logger log = LoggerFactory.getLogger(DocumentoVersPrsr.class);
    private AbsVersamentoExt versamento; // nel costruttore
    private IRispostaWS rispostaWs; // nel costruttore
    private RispostaControlli rispostaControlli; // interno allocare nel costruttore
    // classe di supporto per la verifica e l'estrazione dei dati specifici
    GestioneDatiSpec gestioneDatiSpec = null; // allocata nel costruttore
    // stateless ejb per i controlli sul db
    ControlliSemantici controlliSemantici = null; // allocare nel costruttore
    //
    Set<Long> ordiniDiPresentazioneUnici;

    public DocumentoVersPrsr(AbsVersamentoExt absVersamento, IRispostaWS iRisposta,
            ECConfigurazioneType configurazione) {
        versamento = absVersamento;
        rispostaWs = iRisposta;

        // istanzia la risposta controlli
        rispostaControlli = new RispostaControlli();

        // recupera l'ejb dei controlli, se possibile - altrimenti segnala errore
        try {
            controlliSemantici = (ControlliSemantici) new InitialContext().lookup("java:module/ControlliSemantici");
        } catch (NamingException ex) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                    "Errore nel recupero dell'EJB dei controlli semantici   " + ex.getMessage());
            log.error("Errore nel recupero dell'EJB dei controlli semantici ", ex);
        }

        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            // alloca la classe di verifica ed estrazione dei dati specifici e dei dati specifici di migrazione
            // NB: questa allocazione può fallire. Al termine devo verificare lo stato di RispostaWS e gestire il
            // problema
            gestioneDatiSpec = new GestioneDatiSpec(versamento.getStrutturaComponenti().getSistemaDiMigrazione(),
                    versamento.getStrutturaComponenti().getIdStruttura(), rispostaWs);
            if (rispostaWs.getSeverity() == SeverityEnum.ERROR) {
                rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
            }
        }

    }

    // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
    // INIZIO METODI DI VERIFICA §§§§§§§§§§§§§§§§§§§§§§§§§§§§§
    // §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
    public void parseDocumentoGen(DocumentoVers documento, ECDocumentoType esitoDoc) {
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();

        myAvanzamentoWs.setFase("Verifica semantica documento")
                .setDocumento(documento.getRifDocumento().getIDDocumento()).logAvanzamento();

        documento.setUrnPartDocumento(
                MessaggiWSFormat.formattaUrnPartDocumento(documento.getCategoriaDoc(), documento.getProgressivo()));

        // urn v2
        // DOC<progressivo doc>
        documento.setUrnPartDocumentoNiOrdDoc(
                MessaggiWSFormat.formattaUrnPartDocumento(CategoriaDocumento.Documento, documento.getNiOrdDoc(), true,
                        Costanti.UrnFormatter.DOC_FMT_STRING_V2, Costanti.UrnFormatter.PAD5DIGITS_FMT));

        // verifica il tipo documento
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            this.verificaTipoDoc(documento, esitoDoc);
        }

        // verifico la presenza dei DATI FISCALI - non devono essere presenti in alcun caso.
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            this.parseDatiFisc(documento, esitoDoc);
        }

        // verifico la corrispondenza dei DATI SPECIFICI del Documento
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            this.parseDatiSpecDoc(documento, esitoDoc);
        }

        // verifico tipo struttura
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            this.verificaTipoStruttura(documento, esitoDoc);
        }

        // creo il ramo dell'xml di risposta relativo ai controlli componenti
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            esitoDoc.setComponenti(new ECDocumentoType.Componenti());
        }

        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            this.verificaComponenti(documento, esitoDoc);
        }
    }

    private void verificaTipoDoc(DocumentoVers documento, ECDocumentoType esitoDoc) {
        rispostaControlli.reset();
        rispostaControlli = controlliSemantici.checkTipoDocumento(documento.getRifDocumento().getTipoDocumento(),
                versamento.getStrutturaComponenti().getIdStruttura(),
                (documento.getCategoriaDoc() == Costanti.CategoriaDocumento.Principale),
                documento.getRifDocumentoResp().getChiaveDoc());
        if (rispostaControlli.isrBoolean() == false) {
            setRispostaWsError();
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            esitoDoc.getEsitoDocumento().setVerificaTipoDocumento(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
        } else {
            // salvo l'id tipologia UD
            documento.setIdTipoDocumentoDB(rispostaControlli.getrLong());
            // salvo l'esito positivo
            esitoDoc.getEsitoDocumento().setVerificaTipoDocumento(ECEsitoPosNegType.POSITIVO);
        }
    }

    private void parseDatiFisc(DocumentoVers documento, ECDocumentoType esitoDoc) {
        if (documento.getRifDocumento().getDatiFiscali() != null) {
            // esiste il tag <DatiFiscali>: errore, perchè non dovrebbe esserci!
            setRispostaWsError();
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            esitoDoc.getEsitoDocumento().setCorrispondenzaDatiFiscali(ECEsitoPosNegType.NEGATIVO);
            // Documento {0}: L''utilizzo del Tag &lt;DatiFiscali&gt; è DEPRECATO dal 17 Ottobre 2013
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.DOC_009_001,
                    documento.getRifDocumentoResp().getChiaveDoc());
        } else {
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
            esitoDoc.getEsitoDocumento().setCorrispondenzaDatiFiscali(ECEsitoPosNegType.POSITIVO);
        }
    }

    private void verificaTipoStruttura(DocumentoVers documento, ECDocumentoType esitoDoc) {
        rispostaControlli.reset();
        rispostaControlli = controlliSemantici.checkTipoStruttura(
                documento.getRifDocumento().getStrutturaOriginale().getTipoStruttura(),
                versamento.getStrutturaComponenti().getIdStruttura(), documento.getRifDocumentoResp().getChiaveDoc());
        if (rispostaControlli.isrBoolean() == false) {
            setRispostaWsError();
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            esitoDoc.getEsitoDocumento().setVerificaTipoStruttura(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
        } else {
            // salvo l'id di tipo struttura originale
            documento.setIdTipoStrutturaDB(rispostaControlli.getrLong());
            // salvo l'esito positivo
            esitoDoc.getEsitoDocumento().setVerificaTipoStruttura(ECEsitoPosNegType.POSITIVO);

        }
    }

    private void verificaComponenti(DocumentoVers documento, ECDocumentoType esitoDoc) {
        ControlliSemantici.TipologieComponente tipologiaComponente = ControlliSemantici.TipologieComponente.COMPONENTE;
        FileServUtils fileServUtils = new FileServUtils();

        ordiniDiPresentazioneUnici = new HashSet<>();

        // loop sui componenti (e sottocomponenti)
        for (ComponenteVers valueCompVers : documento.getFileAttesi()) {
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR && valueCompVers.getMySottoComponente() == null) {
                ECComponenteType componenteResp = new ECComponenteType();

                // aggiungo il componente
                esitoDoc.getComponenti().getComponente().add(componenteResp);
                ECComponenteType.EsitoComponente esitoComponente = new ECComponenteType.EsitoComponente();
                esitoComponente.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                esitoComponente.setVerificaTipoComponente(ECEsitoPosNegType.POSITIVO);
                componenteResp.setEsitoComponente(esitoComponente);

                // crea la chiave del componente
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    /*
                     * valueCompVers.setChiaveComp( MessaggiWSFormat.formattaChiaveComponente(
                     * documento.getRifDocumentoResp().getChiaveDoc(), 1,
                     * valueCompVers.getMyComponente().getOrdinePresentazione()));
                     */
                    valueCompVers.setChiaveComp(
                            MessaggiWSFormat.formattaChiaveComponente(documento.getRifDocumentoResp().getChiaveDoc(),
                                    valueCompVers.getMyComponente().getOrdinePresentazione(),
                                    Costanti.UrnFormatter.COMP_FMT_STRING_V2, Costanti.UrnFormatter.PAD5DIGITS_FMT));
                    //
                    valueCompVers.setUrnPartComponenteNiOrdDoc(
                            MessaggiWSFormat.formattaUrnPartComponente(documento.getUrnPartDocumentoNiOrdDoc(),
                                    valueCompVers.getMyComponente().getOrdinePresentazione(),
                                    Costanti.UrnFormatter.COMP_FMT_STRING_V2, Costanti.UrnFormatter.PAD5DIGITS_FMT));

                }

                // v.1.5
                if (versamento.getModificatoriWSCalc().contains(Costanti.ModificatoriWS.TAG_RAPPORTO_VERS_1_5)) {
                    // nome componente
                    componenteResp.setNomeComponente(valueCompVers.getMyComponente().getNomeComponente());
                    // urn versato
                    componenteResp.setUrnVersato(valueCompVers.getMyComponente().getUrnVersato());
                    // hash versato
                    componenteResp.setHashVersato(valueCompVers.getMyComponente().getHashVersato());
                    // id componente
                    componenteResp.setIDComponenteVersato(valueCompVers.getMyComponente().getIDComponenteVersato());
                }

                // se necessario, crea e verifica la validità del nome del file sul file system di destinazione
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR && versamento.getStrutturaComponenti()
                        .getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE) {
                    String tmpNomeFile = MessaggiWSFormat
                            .formattaNomeFileArk(valueCompVers.getUrnPartComponenteNiOrdDoc());
                    valueCompVers.setNomeFileArk(tmpNomeFile);

                    String tmpFilePath = MessaggiWSFormat.formattaFilePathArk(
                            versamento.getStrutturaComponenti().getTpiRootTpi(),
                            versamento.getStrutturaComponenti().getTpiRootArkVers(),
                            versamento.getStrutturaComponenti().getSubPathDataVers(),
                            versamento.getStrutturaComponenti().getSubPathVersatoreArk(),
                            versamento.getStrutturaComponenti().getSubPathUnitaDocArk(), tmpNomeFile);

                    if (!fileServUtils.controllaPath(tmpFilePath)) {
                        setRispostaWsError();
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        // il componente {0} non può essere archiviato su filesystem perchè il suo pathname supera i 255
                        // caratteri
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_002_001, valueCompVers.getChiaveComp());
                    }
                }

                // verifica ordine presentazione per valore > 0
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    // salvo il controlloUnivocitaOrdinePresentazione
                    if (valueCompVers.getMyComponente().getOrdinePresentazione() <= 0) {
                        // sollevo l'errore di connessione UnivocitaOrdinePresentazione
                        setRispostaWsError();
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        esitoDoc.getEsitoDocumento().setUnivocitaOrdinePresentazione(ECEsitoPosNegType.NEGATIVO);
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.DOC_007_002,
                                documento.getRifDocumentoResp().getChiaveDoc(),
                                valueCompVers.getMyComponente().getID());
                    }
                }

                // verifica univocità ordine presentazione
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    // salvo il controlloUnivocitaOrdinePresentazione
                    if (!ordiniDiPresentazioneUnici
                            .contains(Long.valueOf(valueCompVers.getMyComponente().getOrdinePresentazione()))) {
                        ordiniDiPresentazioneUnici
                                .add(Long.valueOf(valueCompVers.getMyComponente().getOrdinePresentazione()));
                        // salvo i parametri dell'esito
                        // ordinePresentazione
                        componenteResp.setOrdinePresentazione(
                                BigInteger.valueOf(valueCompVers.getMyComponente().getOrdinePresentazione()));
                    } else {
                        // sollevo l'errore di connessione UnivocitaOrdinePresentazione
                        setRispostaWsError();
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        esitoDoc.getEsitoDocumento().setUnivocitaOrdinePresentazione(ECEsitoPosNegType.NEGATIVO);
                        // "Documento {0}: <OrdinePresentazione>{1}</OrdinePresentazione> del componente <ID>{2}</ID>
                        // non sequenziale"
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.DOC_007_001,
                                documento.getRifDocumentoResp().getChiaveDoc(),
                                valueCompVers.getMyComponente().getOrdinePresentazione(),
                                valueCompVers.getMyComponente().getID());
                    }
                }

                // verifico TipoComponente
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    rispostaControlli.reset();
                    rispostaControlli = controlliSemantici.checkTipoComponente(
                            valueCompVers.getMyComponente().getTipoComponente(), documento.getIdTipoStrutturaDB(),
                            tipologiaComponente, valueCompVers.getChiaveComp());
                    if (rispostaControlli.isrBoolean() == false) {
                        setRispostaWsError();
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaTipoComponente(ECEsitoPosNegType.NEGATIVO);
                        rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                    } else {
                        // salvo l'id del tipo componente
                        valueCompVers.setIdTipoComponente(rispostaControlli.getrLong());
                        // salvo il tipo uso
                        valueCompVers.setTipoUso(rispostaControlli.getrString());
                        // tipoComponete
                        componenteResp.setTipoComponente(valueCompVers.getMyComponente().getTipoComponente());
                        // urn
                        componenteResp.setURN(valueCompVers.getMyComponente().getUrnVersato());
                        // memorizzo in ComponenteVers il riferimento alla classe di esito relativo a questo componente
                        valueCompVers.setRifComponenteResp(componenteResp);
                    }
                }

                // Errore: se il tag "TipoSupportoComponente" del XML di versamento è diverso da RIFERIMENTO, ed è
                // presente il tag "Riferimento"
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    if ((valueCompVers.isPresenteRifMeta())
                            && (valueCompVers.getTipoSupporto() != ComponenteVers.TipiSupporto.RIFERIMENTO)) {
                        rispostaWs.setSeverity(SeverityEnum.ERROR);
                        rispostaWs.setErrorCode("COMP-002");
                        rispostaWs.setErrorMessage(
                                "Il tag Riferimento non deve essere valorizzato per TipoSupportoComponente diverso da RIFERIMENTO");

                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaTipoSupportoComponente("NEGATIVO");
                        // "Il tag Riferimento non deve essere valorizzato per TipoSupportoComponente diverso da
                        // RIFERIMENTO"
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_002_001, valueCompVers.getChiaveComp());
                    } else {
                        componenteResp.getEsitoComponente().setVerificaTipoSupportoComponente("POSITIVO");
                    }
                }

                // Errore: se il tag "TipoSupportoComponente" del XML di versamento è RIFERIMENTO, e non è presente il
                // tag "Riferimento".
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    if ((!valueCompVers.isPresenteRifMeta())
                            && (valueCompVers.getTipoSupporto() == ComponenteVers.TipiSupporto.RIFERIMENTO)) {
                        rispostaWs.setSeverity(SeverityEnum.ERROR);
                        rispostaWs.setErrorCode("COMP-002");
                        rispostaWs.setErrorMessage(
                                "Per TipoSupportoComponente  = RIFERIMENTO è necessario valorizzare Riferimento");

                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaTipoSupportoComponente("NEGATIVO");
                        // Componente {0}: tag <Riferimento> obbligatorio e non valorizzato (il tag
                        // <TipoSupportoComponente> = "RIFERIMENTO")
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_002_002, valueCompVers.getChiaveComp());
                    } else {
                        componenteResp.getEsitoComponente().setVerificaTipoSupportoComponente("POSITIVO");
                    }
                }

                // verifico se il tipo supporto componente è diverso da file, NON deve avere sottocomponenti
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    if (valueCompVers.getTipoSupporto() != ComponenteVers.TipiSupporto.FILE
                            && valueCompVers.getMyComponente().getSottoComponenti() != null) {
                        rispostaWs.setSeverity(SeverityEnum.ERROR);
                        rispostaWs.setErrorCode("COMP-002");
                        rispostaWs.setErrorMessage(
                                "Se il tipo supporto componente è diverso da FILE, non è possibile definire sottocomponenti");

                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaTipoSupportoComponente("NEGATIVO");
                        // Componente {0}: Il tag <SottoComponenti> non deve essere indicato (tag
                        // <TipoSupportoComponente> diverso da "FILE")"
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_002_004, valueCompVers.getChiaveComp());
                    }
                }

                boolean trovatoTipoRappComp = false;

                // verifico TipoRappresentazioneComponente se valorizzato
                if ((rispostaWs.getSeverity() != SeverityEnum.ERROR)
                        && (valueCompVers.getMyComponente().getTipoRappresentazioneComponente() != null)
                        && (!valueCompVers.getMyComponente().getTipoRappresentazioneComponente().trim().equals(""))) {
                    rispostaControlli.reset();
                    rispostaControlli = controlliSemantici.checkTipoRappComponente(
                            valueCompVers.getMyComponente().getTipoRappresentazioneComponente(),
                            versamento.getStrutturaComponenti().getIdStruttura(), valueCompVers.getChiaveComp());
                    if (rispostaControlli.isrBoolean() == false) {
                        setRispostaWsError();
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaTipoRappresentazione(ECEsitoPosNegType.NEGATIVO);
                        rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                    } else {
                        DecTipoRapprComp decTipoRapprComp = (DecTipoRapprComp) rispostaControlli.getrObject();
                        if (decTipoRapprComp.getTiOutputRappr() != null
                                && decTipoRapprComp.getTiOutputRappr().equals(CostantiDB.TipiOutputRappr.DIP.name())) {
                            trovatoTipoRappComp = true;
                            // salvo l'id del tipo rappresentazione
                            valueCompVers.setIdTipoRappresentazioneComponente(decTipoRapprComp.getIdTipoRapprComp());
                            valueCompVers.setIdFormatoFileDocCont(
                                    decTipoRapprComp.getDecFormatoFileDocCont().getIdFormatoFileDoc());
                            if (decTipoRapprComp.getDecFormatoFileDocConv() != null) {
                                valueCompVers.setIdFormatoFileDocConv(
                                        decTipoRapprComp.getDecFormatoFileDocConv().getIdFormatoFileDoc());
                            }
                            valueCompVers.setNonAccettareForzaFormato(true);
                            if (decTipoRapprComp.getTiAlgoRappr() != null
                                    && !decTipoRapprComp.getTiAlgoRappr().isEmpty()) {
                                valueCompVers.setAlgoritmoRappr(
                                        CostantiDB.TipoAlgoritmoRappr.fromString(decTipoRapprComp.getTiAlgoRappr()));
                            }
                            // salvo l'esito positivo
                            componenteResp.getEsitoComponente()
                                    .setVerificaTipoRappresentazione(ECEsitoPosNegType.POSITIVO);
                        } else {
                            // il tipo output deve essere uguale a DIP
                            setRispostaWsError();
                            componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            componenteResp.getEsitoComponente()
                                    .setVerificaTipoRappresentazione(ECEsitoPosNegType.NEGATIVO);
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_003_002, valueCompVers.getChiaveComp(),
                                    valueCompVers.getMyComponente().getTipoRappresentazioneComponente());
                        }
                    }
                }

                // se presente tipo Rappresentazione Componente, verifico che il salvataggio avvenga su BLOB
                if ((rispostaWs.getSeverity() != SeverityEnum.ERROR && trovatoTipoRappComp)) {
                    if (versamento.getStrutturaComponenti()
                            .getTipoSalvataggioFile() != CostantiDB.TipoSalvataggioFile.BLOB) {
                        setRispostaWsError();
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaTipoRappresentazione(ECEsitoPosNegType.NEGATIVO);
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_003_004, valueCompVers.getChiaveComp());
                    }
                }

                // se presente tipo Rappresentazione Componente, verifico che il tipo supporto sia FILE
                if ((rispostaWs.getSeverity() != SeverityEnum.ERROR && trovatoTipoRappComp)) {
                    if (valueCompVers.getTipoSupporto() != ComponenteVers.TipiSupporto.FILE) {
                        setRispostaWsError();
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaTipoRappresentazione(ECEsitoPosNegType.NEGATIVO);
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_003_002, valueCompVers.getChiaveComp());
                    }
                }

                // se presente tipo Rappresentazione Componente, verifico che il tipo uso componente sia CONTENUTO
                if ((rispostaWs.getSeverity() != SeverityEnum.ERROR && trovatoTipoRappComp)) {
                    if (!valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.CONTENUTO)) {
                        setRispostaWsError();
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaTipoRappresentazione(ECEsitoPosNegType.NEGATIVO);
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_003_005, valueCompVers.getChiaveComp());
                    }
                }

                // verifico il NomeComponente se è presente per il tipo supporto = FILE
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    if (valueCompVers.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
                        if (valueCompVers.getMyComponente().getNomeComponente() == null
                                || valueCompVers.getMyComponente().getNomeComponente().isEmpty()) {
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setErrorCode("COMP-005");
                            rispostaWs.setErrorMessage(
                                    "Tipo supporto componente pari a FILE ma Nome Componente assente!");

                            componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            componenteResp.getEsitoComponente().setVerificaNomeComponente(ECEsitoPosNegType.NEGATIVO);
                            // Componente {0}: Il tag <NomeComponente> deve essere valorizzato (il componente ha un tipo
                            // di supporto pari a "FILE")
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_005_001,
                                    valueCompVers.getChiaveComp());
                        } else {
                            componenteResp.getEsitoComponente().setVerificaNomeComponente(ECEsitoPosNegType.POSITIVO);
                        }
                    }
                }

                // VerificaRiferimentoUnitaDocumentaria
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR
                        && (valueCompVers.getMyComponente().getRiferimento() != null)) {
                    rispostaControlli.reset();
                    CSChiave chiave = new CSChiave();
                    chiave.setAnno(Long.valueOf(valueCompVers.getMyComponente().getRiferimento().getAnno()));
                    chiave.setNumero(valueCompVers.getMyComponente().getRiferimento().getNumero());
                    chiave.setTipoRegistro(valueCompVers.getMyComponente().getRiferimento().getTipoRegistro());

                    rispostaControlli = controlliSemantici.checkChiave(chiave,
                            versamento.getStrutturaComponenti().getIdStruttura(),
                            ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
                    if (rispostaControlli.isrBoolean() || rispostaControlli.getrLong() == -1) {
                        rispostaWs.setSeverity(SeverityEnum.ERROR);
                        rispostaWs.setErrorCode("COMP-008");
                        rispostaWs.setErrorMessage("Tag Riferimento del componente definito, ma U.D. non presente");

                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente()
                                .setVerificaRiferimentoUnitaDocumentaria(ECEsitoPosNegType.NEGATIVO);
                        // Componente {0}: la UD identificata dal Riferimento {1} non è presente nel sistema
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_008_003, valueCompVers.getChiaveComp(),
                                MessaggiWSFormat.formattaUrnPartUnitaDoc(chiave));
                    } else {
                        // verifico che l'unità doc riferita sia composta da un solo componente e che questo sia di tipo
                        // CONVERTITORE
                        // che sia stata salvata su BLOB e che abbia tipo supporto uguale a FILE
                        long tmpIdUnitaDoc = rispostaControlli.getrLong();
                        rispostaControlli = controlliSemantici.checkRiferimentoUD(chiave, tmpIdUnitaDoc,
                                versamento.getStrutturaComponenti().getIdStruttura(),
                                ControlliSemantici.TipologieComponente.COMPONENTE, valueCompVers.getChiaveComp());
                        if (rispostaControlli.isrBoolean() == false) {
                            setRispostaWsError();
                            componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            componenteResp.getEsitoComponente()
                                    .setVerificaRiferimentoUnitaDocumentaria(ECEsitoPosNegType.NEGATIVO);
                            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                        } else {
                            valueCompVers.setIdUnitaDocRif(rispostaControlli.getrLong());
                            componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                            componenteResp.getEsitoComponente()
                                    .setVerificaRiferimentoUnitaDocumentaria(ECEsitoPosNegType.POSITIVO);
                        }
                    }
                }

                // verifico l'ammissibilità del formato per il tipo supporto = FILE
                // e successiva verifica del riconoscimento formato
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR
                        && valueCompVers.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
                    // nel caso il formato versato fosse "sconosciuto" (parer/unkonwn)
                    // lo tratto come se fosse nullo
                    String descFormatoFileVers = valueCompVers.getMyComponente().getFormatoFileVersato();
                    if (descFormatoFileVers != null && (descFormatoFileVers
                            .equals(versamento.getXmlDefaults().get(ParametroApplDB.FMT_UNKNOWN)))) {
                        descFormatoFileVers = null;
                    }
                    rispostaControlli = controlliSemantici.checkFormatoFileVersato(descFormatoFileVers,
                            versamento.getStrutturaComponenti().getIdStruttura(), valueCompVers.getIdTipoComponente());
                    if (rispostaControlli.isrBoolean()) {
                        // se è ammissibile, tutto OK e recupero l'ID del dec formato file doc
                        valueCompVers.setIdFormatoFileVers(rispostaControlli.getrLong());
                    } else {
                        // altrimenti, imposto la risposta a ERRORE in via preventiva
                        rispostaWs.setSeverity(SeverityEnum.ERROR);
                        valueCompVers.setFormatoFileVersNonAmmesso(true);
                        if (!valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.FIRMA)
                                && !valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.MARCA)) {
                            // se il componenete che sto verificando NON è una firma o una
                            // marca, provo a farlo versare comunque:
                            // verifico se è ammissibile il formato file "sconosciuto" (parer/unkonwn)
                            rispostaControlli = controlliSemantici.checkFormatoFileVersato(
                                    versamento.getXmlDefaults().get(ParametroApplDB.FMT_UNKNOWN),
                                    versamento.getStrutturaComponenti().getIdStruttura(),
                                    valueCompVers.getIdTipoComponente());
                            if (rispostaControlli.isrBoolean()) {
                                // se lo è, registro questo formato come se fosse stato versato
                                valueCompVers.setIdFormatoFileVers(rispostaControlli.getrLong());
                                // e correggo lo stato della risposta: non è più in errore
                                rispostaWs.setSeverity(SeverityEnum.WARNING);
                            }
                            // se non è ammissibile neanche il formato "sconosciuto" la risposta del ws resta ERRORE
                        }
                    }

                    // in ogni caso conservo una copia della descrizione
                    // originale del formato versato (eventualmente nulla)
                    valueCompVers.setDescFormatoFileVers(descFormatoFileVers);

                    // se il precedente test di ammissibilità è andato (in un qualche modo) bene...
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        // se presente tipo Rappresentazione Componente,
                        // verifico che il formato del file versato coincida con quello atteso dal convertitore
                        if (trovatoTipoRappComp
                                && valueCompVers.getIdFormatoFileVers() != valueCompVers.getIdFormatoFileDocCont()) {
                            setRispostaWsError();
                            componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            componenteResp.getEsitoComponente()
                                    .setVerificaAmmissibilitaFormato(ECEsitoPosNegType.NEGATIVO);
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_006_002, valueCompVers.getChiaveComp(),
                                    descFormatoFileVers);
                        } else {
                            componenteResp.getEsitoComponente()
                                    .setVerificaAmmissibilitaFormato(ECEsitoPosNegType.POSITIVO);
                        }
                    } else {
                        // ... altrimenti preparo la risposta di formato non ammesso
                        rispostaWs.setErrorCode("COMP-006");
                        rispostaWs.setErrorMessage("Formato file non ammesso per struttura versante e tipo componente");
                        componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                        componenteResp.getEsitoComponente().setVerificaAmmissibilitaFormato(ECEsitoPosNegType.NEGATIVO);
                        // Componente {0}: il Formato {1} non è presente nel DB per la struttura versante, il tipo
                        // struttura e il tipo componente
                        rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_006_001, valueCompVers.getChiaveComp(),
                                descFormatoFileVers);
                    }
                }

                // se è un componente di tipo FILE, verifica eventualmente il formato dell'hash versato
                // in seguito verrà verificato che l'hash in questione sia anche corretto...
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR
                        && valueCompVers.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE
                        && versamento.getStrutturaComponenti().isFlagVerificaHash()) {
                    String tmpCdErr = null;
                    String tmpDsErr = null;
                    if (valueCompVers.getMyComponente().getHashVersato() != null
                            && valueCompVers.getMyComponente().getHashVersato().length() > 0) {
                        this.controllaHash(valueCompVers, valueCompVers.getMyComponente().getHashVersato());
                        if (valueCompVers.getHashVersatoEncoding() == CostantiDB.TipiEncBinari.SCONOSCIUTO) {
                            tmpCdErr = MessaggiWSBundle.HASH_002_001;
                            tmpDsErr = MessaggiWSBundle.getString(MessaggiWSBundle.HASH_002_001,
                                    valueCompVers.getChiaveComp());
                        } else if (valueCompVers.getHashVersatoAlgoritmo() == CostantiDB.TipiHash.SCONOSCIUTO) {
                            tmpCdErr = MessaggiWSBundle.HASH_003_001;
                            tmpDsErr = MessaggiWSBundle.getString(MessaggiWSBundle.HASH_003_001,
                                    valueCompVers.getChiaveComp());
                        }
                    } else {
                        tmpCdErr = MessaggiWSBundle.HASH_001_001;
                        tmpDsErr = MessaggiWSBundle.getString(MessaggiWSBundle.HASH_001_001,
                                valueCompVers.getChiaveComp());
                    }

                    // imposto l'eventuale errore trovato come warning o come errore
                    if (tmpCdErr != null) {
                        // memorizzo, in ogni caso, che l'hash non è verificabile
                        valueCompVers.setHashVersNonDefinito(true);
                        if (versamento.getStrutturaComponenti().isFlagAccettaHashErrato()) {
                            versamento.addWarningDoc(documento, valueCompVers.getChiaveComp(), tmpCdErr, tmpDsErr);
                        } else {
                            versamento.addErrorDoc(documento, valueCompVers.getChiaveComp(), tmpCdErr, tmpDsErr);
                        }
                    }

                    // calcolo l'esito del ws in funzione di eventuali warning ed errori
                    VoceDiErrore tmpVdE = versamento.calcolaErrorePrincipale();
                    if (tmpVdE != null) {
                        rispostaWs.setSeverity(tmpVdE.getSeverity());
                        if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                            componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            rispostaWs.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                        } else if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                            rispostaWs.setEsitoWsWarning(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                        }
                    }

                }

                // verifico gli eventuali dati specifici e di migrazione
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    this.parseDatiSpecComp(valueCompVers, componenteResp);
                }

                /*
                 * verifico i sottocomponenti del componente
                 */
                if ((rispostaWs.getSeverity() != SeverityEnum.ERROR)) {
                    this.verificaSottoComponentiComp(valueCompVers, documento, esitoDoc);
                }
            }
        } // fine loop sui componenti
    }

    private void verificaSottoComponentiComp(ComponenteVers componentePadre, DocumentoVers documento,
            ECDocumentoType esitoDoc) {
        ControlliSemantici.TipologieComponente tipologiaComponente = ControlliSemantici.TipologieComponente.SOTTOCOMPONENTE;
        ECComponenteType componenteResp = componentePadre.getRifComponenteResp();
        FileServUtils fileServUtils = new FileServUtils();
        boolean trovatoTipoRappComp = false;
        boolean verificaFormatoConvertitore = false;
        int contaConvRapp = 0;

        if (componentePadre.getIdTipoRappresentazioneComponente() != 0) {
            trovatoTipoRappComp = true;
        }
        if (componentePadre.getIdFormatoFileDocConv() != 0) {
            verificaFormatoConvertitore = true;
        }

        // loop sui sottocomponenti
        if (componentePadre.getMyComponente().getSottoComponenti() != null) {
            // creo l'elemento SottoComponenti
            componenteResp.setSottoComponenti(new ECComponenteType.SottoComponenti());
            for (ComponenteVers valueCompVers : documento.getFileAttesi()) {
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR && valueCompVers.getMySottoComponente() != null
                        && valueCompVers.getRifComponenteVersPadre().getMyComponente().getID()
                                .equals(componentePadre.getId())) {
                    boolean trovatoConvRapp = false;
                    ECSottoComponenteType sottoComponenteResp = new ECSottoComponenteType();
                    // aggiungo il Sottocomponente
                    ECSottoComponenteType.EsitoSottoComponente componente = new ECSottoComponenteType.EsitoSottoComponente();
                    componente.setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    componente.setVerificaTipoComponente(ECEsitoPosNegType.POSITIVO);
                    sottoComponenteResp.setEsitoSottoComponente(componente);
                    componenteResp.getSottoComponenti().getSottoComponente().add(sottoComponenteResp);

                    // crea la chiave del componente
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        /*
                         * valueCompVers.setChiaveComp( MessaggiWSFormat.formattaChiaveComponente(
                         * documento.getRifDocumentoResp().getChiaveDoc(), 1,
                         * valueCompVers.getMySottoComponente().getOrdinePresentazione()));
                         */

                        valueCompVers.setChiaveComp(MessaggiWSFormat.formattaChiaveComponente(
                                componentePadre.getChiaveComp(),
                                valueCompVers.getMySottoComponente().getOrdinePresentazione(),
                                Costanti.UrnFormatter.COMP_FMT_STRING_V2, Costanti.UrnFormatter.PAD5DIGITS_FMT));
                        //
                        valueCompVers.setUrnPartComponenteNiOrdDoc(MessaggiWSFormat.formattaUrnPartComponente(
                                componentePadre.getUrnPartComponenteNiOrdDoc(),
                                valueCompVers.getMySottoComponente().getOrdinePresentazione(),
                                Costanti.UrnFormatter.COMP_FMT_STRING_V2, Costanti.UrnFormatter.PAD5DIGITS_FMT));
                    }

                    // v.1.5
                    if (versamento.getModificatoriWSCalc().contains(Costanti.ModificatoriWS.TAG_RAPPORTO_VERS_1_5)) {
                        // nome componente
                        componenteResp.setNomeComponente(valueCompVers.getMySottoComponente().getNomeComponente());
                        // urn versato
                        componenteResp.setUrnVersato(valueCompVers.getMySottoComponente().getUrnVersato());
                        // id componente
                        componenteResp
                                .setIDComponenteVersato(valueCompVers.getMySottoComponente().getIDComponenteVersato());
                    }

                    // se necessario, crea e verifica la validità del nome del file sul file system di destinazione
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR && versamento.getStrutturaComponenti()
                            .getTipoSalvataggioFile() == CostantiDB.TipoSalvataggioFile.FILE) {
                        String tmpNomeFile = MessaggiWSFormat
                                .formattaNomeFileArk(valueCompVers.getUrnPartComponenteNiOrdDoc());
                        valueCompVers.setNomeFileArk(tmpNomeFile);

                        String tmpFilePath = MessaggiWSFormat.formattaFilePathArk(
                                versamento.getStrutturaComponenti().getTpiRootTpi(),
                                versamento.getStrutturaComponenti().getTpiRootArkVers(),
                                versamento.getStrutturaComponenti().getSubPathDataVers(),
                                versamento.getStrutturaComponenti().getSubPathVersatoreArk(),
                                versamento.getStrutturaComponenti().getSubPathUnitaDocArk(), tmpNomeFile);

                        if (!fileServUtils.controllaPath(tmpFilePath)) {
                            setRispostaWsError();
                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            // il sottocomponente {0} non può essere archiviato su filesystem perchè il suo pathname
                            // supera i 255 caratteri
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FILESYS_002_002,
                                    valueCompVers.getChiaveComp());
                        }
                    }

                    // verifica ordine presentazione per valore > 0
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        // salvo il controlloUnivocitaOrdinePresentazione
                        if (valueCompVers.getMySottoComponente().getOrdinePresentazione() <= 0) {
                            // sollevo l'errore di connessione UnivocitaOrdinePresentazione
                            setRispostaWsError();
                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            esitoDoc.getEsitoDocumento().setUnivocitaOrdinePresentazione(ECEsitoPosNegType.NEGATIVO);
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.DOC_007_002,
                                    documento.getRifDocumentoResp().getChiaveDoc(),
                                    valueCompVers.getMySottoComponente().getID());
                        }
                    }

                    // verifica univocità ordine presentazione
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        // salvo il controlloUnivocitaOrdinePresentazione
                        if (!ordiniDiPresentazioneUnici.contains(
                                Long.valueOf(valueCompVers.getMySottoComponente().getOrdinePresentazione()))) {
                            ordiniDiPresentazioneUnici
                                    .add(Long.valueOf(valueCompVers.getMySottoComponente().getOrdinePresentazione()));
                            // salvo i parametri dell'esito
                            // ordinePresentazione
                            sottoComponenteResp.setOrdinePresentazione(
                                    BigInteger.valueOf(valueCompVers.getMySottoComponente().getOrdinePresentazione()));
                        } else {
                            // sollevo l'errore di connessione UnivocitaOrdinePresentazione
                            setRispostaWsError();
                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            esitoDoc.getEsitoDocumento().setUnivocitaOrdinePresentazione(ECEsitoPosNegType.NEGATIVO);
                            // "Documento {0}: <OrdinePresentazione>{1}</OrdinePresentazione> del componente
                            // <ID>{2}</ID> non sequenziale"
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.DOC_007_001,
                                    documento.getRifDocumentoResp().getChiaveDoc(),
                                    valueCompVers.getMySottoComponente().getOrdinePresentazione(),
                                    valueCompVers.getMySottoComponente().getID());
                        }
                    }

                    // verifico TipoComponente
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        rispostaControlli.reset();
                        rispostaControlli = controlliSemantici.checkTipoComponente(
                                valueCompVers.getMySottoComponente().getTipoComponente(),
                                documento.getIdTipoStrutturaDB(), tipologiaComponente, valueCompVers.getChiaveComp());
                        if (rispostaControlli.isrBoolean() == false) {
                            setRispostaWsError();
                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            sottoComponenteResp.getEsitoSottoComponente()
                                    .setVerificaTipoComponente(ECEsitoPosNegType.NEGATIVO);
                            rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                        } else {
                            // salvo l'id del tipo componente
                            valueCompVers.setIdTipoComponente(rispostaControlli.getrLong());
                            // salvo il tipo uso
                            valueCompVers.setTipoUso(rispostaControlli.getrString());
                            // tipoComponente
                            sottoComponenteResp
                                    .setTipoComponente(valueCompVers.getMySottoComponente().getTipoComponente());
                            // urn
                            sottoComponenteResp.setURN(valueCompVers.getMySottoComponente().getUrnVersato());
                            //
                            valueCompVers.setRifSottoComponenteResp(sottoComponenteResp);
                        }
                    }

                    // Errore: se il tag "TipoSupportoComponente" del XML di versamento è diverso da RIFERIMENTO, ed è
                    // presente il tag "Riferimento"
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        if ((valueCompVers.isPresenteRifMeta())
                                && (valueCompVers.getTipoSupporto() != ComponenteVers.TipiSupporto.RIFERIMENTO)) {
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setErrorCode("SUBCOMP-002");
                            rispostaWs.setErrorMessage(
                                    "Il tag Riferimento non deve essere valorizzato per TipoSupportoComponente diverso da RIFERIMENTO");

                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("NEGATIVO");
                            // "Il tag Riferimento non deve essere valorizzato per TipoSupportoComponente diverso da
                            // RIFERIMENTO"
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_002_001,
                                    valueCompVers.getChiaveComp());
                        } else {
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("POSITIVO");
                        }
                    }

                    // Errore: se il tag "TipoSupportoComponente" del XML di versamento è RIFERIMENTO, e non è presente
                    // il tag "Riferimento".
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        if ((!valueCompVers.isPresenteRifMeta())
                                && (valueCompVers.getTipoSupporto() == ComponenteVers.TipiSupporto.RIFERIMENTO)) {
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setErrorCode("SUBCOMP-002");
                            rispostaWs.setErrorMessage(
                                    "Per TipoSupportoComponente = RIFERIMENTO è necessario valorizzare Riferimento");

                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("NEGATIVO");
                            // " SottoComponente {0}: tag <Riferimento> obbligatorio e non valorizzato (il tag
                            // <TipoSupportoComponente> = "RIFERIMENTO")
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_002_002,
                                    valueCompVers.getChiaveComp());
                        } else {
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("POSITIVO");
                        }
                    }

                    // incremento il conteggio di convertitori e rappresentazioni..
                    // il loro numero mi serve per il controllo di coerenza alla fine del loop
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        if (valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.CONVERTITORE)
                                || valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.RAPPRESENTAZIONE)) {
                            contaConvRapp++;
                            trovatoConvRapp = true;
                        }
                    }

                    // Errore: se il tag "TipoSupportoComponente" del XML di versamento è diverso da FILE, e il tipo di
                    // uso è uguale a CONVERTITORE.
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        if ((valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.CONVERTITORE))
                                && (valueCompVers.getTipoSupporto() != ComponenteVers.TipiSupporto.FILE)) {
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setErrorCode("SUBCOMP-002");
                            rispostaWs.setErrorMessage(
                                    "TipoSupportoComponente è diverso da FILE e tipo uso uguale a CONVERTITORE");
                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("NEGATIVO");
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_002_005,
                                    valueCompVers.getChiaveComp());
                        }
                    }

                    // Errore: se il tag "TipoSupportoComponente" del XML di versamento è RIFERIMENTO, e il tipo di uso
                    // è diverso da RAPPRESENTAZIONE.
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        if ((!valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.RAPPRESENTAZIONE))
                                && (valueCompVers.getTipoSupporto() == ComponenteVers.TipiSupporto.RIFERIMENTO)) {
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setErrorCode("SUBCOMP-002");
                            rispostaWs.setErrorMessage(
                                    "TipoSupportoComponente=RIFERIMENTO e tipo uso diverso da RAPPRESENTAZIONE");

                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("NEGATIVO");
                            // SottoComponente {0}: tag <TipoSupportoComponente> valorizzato con "RIFERIMENTO" e tipo
                            // uso diverso da "RAPPRESENTAZIONE"
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_002_003,
                                    valueCompVers.getChiaveComp());
                        } else {
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("POSITIVO");
                        }
                    }

                    // Errore: se il tag "TipoSupportoComponente" del XML di versamento è diverso da RIFERIMENTO, e il
                    // tipo di uso è uguale a RAPPRESENTAZIONE.
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        if ((valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.RAPPRESENTAZIONE))
                                && (valueCompVers.getTipoSupporto() != ComponenteVers.TipiSupporto.RIFERIMENTO)) {
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setErrorCode("SUBCOMP-002");
                            rispostaWs.setErrorMessage(
                                    "TipoSupportoComponente è diverso da RIFERIMENTO e tipo uso uguale a RAPPRESENTAZIONE");

                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("NEGATIVO");
                            // SottoComponente {0}: tag <TipoSupportoComponente> diverso da "RIFERIMENTO" e tipo uso
                            // uguale a "RAPPRESENTAZIONE"
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_002_004,
                                    valueCompVers.getChiaveComp());
                        } else {
                            sottoComponenteResp.getEsitoSottoComponente().setVerificaTipoSupportoComponente("POSITIVO");
                        }
                    }

                    // verifico il NomeComponente se è presente per il tipo supporto = FILE
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        if (valueCompVers.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
                            if (valueCompVers.getMySottoComponente().getNomeComponente() == null
                                    || valueCompVers.getMySottoComponente().getNomeComponente().isEmpty()) {
                                rispostaWs.setSeverity(SeverityEnum.ERROR);
                                rispostaWs.setErrorCode("SUBCOMP-003");
                                rispostaWs.setErrorMessage(
                                        "Tipo supporto componente pari a FILE ma Nome Componente assente!");

                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setVerificaNomeComponente(ECEsitoPosNegType.NEGATIVO);
                                // SottoComponente {0}: Il tag <NomeComponente> deve essere valorizzato (il componente
                                // ha un tipo di supporto pari a "FILE")
                                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_003_001,
                                        valueCompVers.getChiaveComp());
                            } else {
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setVerificaNomeComponente(ECEsitoPosNegType.POSITIVO);
                            }
                        }
                    }

                    // VerificaRiferimentoUnitaDocumentaria
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR
                            && (valueCompVers.getMySottoComponente().getRiferimento() != null)) {
                        rispostaControlli.reset();
                        CSChiave chiave = new CSChiave();
                        chiave.setAnno(Long.valueOf(valueCompVers.getMySottoComponente().getRiferimento().getAnno()));
                        chiave.setNumero(valueCompVers.getMySottoComponente().getRiferimento().getNumero());
                        chiave.setTipoRegistro(valueCompVers.getMySottoComponente().getRiferimento().getTipoRegistro());

                        rispostaControlli = controlliSemantici.checkChiave(chiave,
                                versamento.getStrutturaComponenti().getIdStruttura(),
                                ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
                        if (rispostaControlli.isrBoolean() || rispostaControlli.getrLong() == -1) {
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            rispostaWs.setErrorCode("SUBCOMP-006");
                            rispostaWs.setErrorMessage("Tag Riferimento del componente definito, ma U.D. non presente");

                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            sottoComponenteResp.getEsitoSottoComponente()
                                    .setVerificaRiferimentoUnitaDocumentaria(ECEsitoPosNegType.NEGATIVO);
                            // SottoComponente {0}: la UD identificata dal Riferimento {1} non è presente nel sistema
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_006_003,
                                    valueCompVers.getChiaveComp(), MessaggiWSFormat.formattaUrnPartUnitaDoc(chiave));
                        } else {
                            // verifico che l'unità doc riferita sia composta da un solo componente e che questo sia di
                            // tipo CONVERTITORE,
                            // che sia stata salvata su BLOB e che abbia tipo supporto uguale a FILE
                            long tmpIdUnitaDoc = rispostaControlli.getrLong();
                            rispostaControlli = controlliSemantici.checkRiferimentoUD(chiave, tmpIdUnitaDoc,
                                    versamento.getStrutturaComponenti().getIdStruttura(),
                                    ControlliSemantici.TipologieComponente.SOTTOCOMPONENTE,
                                    valueCompVers.getChiaveComp());
                            if (rispostaControlli.isrBoolean() == false) {
                                setRispostaWsError();
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setVerificaRiferimentoUnitaDocumentaria(ECEsitoPosNegType.NEGATIVO);
                                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                            } else /*
                                    * se sto esaminando un sottocomponente di tipo uso CONV o RAPP, se è definito un
                                    * formato di file atteso per il convertitore, verifico che il formato del file
                                    * versato coincida
                                    */ if (trovatoConvRapp && verificaFormatoConvertitore
                                    && rispostaControlli.getrLong() != componentePadre.getIdFormatoFileDocConv()) {
                                setRispostaWsError();
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setVerificaRiferimentoUnitaDocumentaria(ECEsitoPosNegType.NEGATIVO);
                                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_006_006,
                                        valueCompVers.getChiaveComp(),
                                        MessaggiWSFormat.formattaUrnPartUnitaDoc(chiave));
                            } else {
                                valueCompVers.setIdUnitaDocRif(tmpIdUnitaDoc);
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setVerificaRiferimentoUnitaDocumentaria(ECEsitoPosNegType.POSITIVO);
                                valueCompVers.setNonAccettareForzaFormato(true);
                            }
                        }
                    }

                    // verifico l'ammissibilità del formato per il tipo supporto = FILE
                    // e successiva verifica del riconoscimento formato
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR
                            && valueCompVers.getTipoSupporto() == ComponenteVers.TipiSupporto.FILE) {
                        // nel caso il formato versato fosse "sconosciuto" (parer/unkonwn)
                        // lo tratto come se fosse nullo
                        String descFormatoFileVers = valueCompVers.getMySottoComponente().getFormatoFileVersato();
                        if (descFormatoFileVers != null && (descFormatoFileVers
                                .equals(versamento.getXmlDefaults().get(ParametroApplDB.FMT_UNKNOWN)))) {
                            descFormatoFileVers = null;
                        }
                        rispostaControlli = controlliSemantici.checkFormatoFileVersato(descFormatoFileVers,
                                versamento.getStrutturaComponenti().getIdStruttura(),
                                valueCompVers.getIdTipoComponente());
                        if (rispostaControlli.isrBoolean()) {
                            // se è ammissibile, tutto OK e recupero l'ID del dec formato file doc
                            valueCompVers.setIdFormatoFileVers(rispostaControlli.getrLong());
                        } else {
                            // altrimenti, imposto la risposta a ERRORE in via preventiva
                            rispostaWs.setSeverity(SeverityEnum.ERROR);
                            valueCompVers.setFormatoFileVersNonAmmesso(true);
                            if (!valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.FIRMA)
                                    && !valueCompVers.getTipoUso().equals(CostantiDB.TipoUsoComponente.MARCA)) {
                                // se il sottocomponenete che sto verificando NON è una firma o una
                                // marca, provo a farlo versare comunque:
                                // verifico se è ammissibile il formato file "sconosciuto" (parer/unkonwn)
                                rispostaControlli = controlliSemantici.checkFormatoFileVersato(
                                        versamento.getXmlDefaults().get(ParametroApplDB.FMT_UNKNOWN),
                                        versamento.getStrutturaComponenti().getIdStruttura(),
                                        valueCompVers.getIdTipoComponente());
                                if (rispostaControlli.isrBoolean()) {
                                    // se lo è, registro questo formato come se fosse stato versato
                                    valueCompVers.setIdFormatoFileVers(rispostaControlli.getrLong());
                                    // e correggo lo stato della risposta: non è più in errore
                                    rispostaWs.setSeverity(SeverityEnum.WARNING);
                                }
                                // se non è ammissibile neanche il formato "sconosciuto" la risposta del ws resta ERRORE
                            }
                        }

                        // in ogni caso conservo una copia della descrizione
                        // originale del formato versato (eventualmente nulla)
                        valueCompVers.setDescFormatoFileVers(descFormatoFileVers);

                        // se il precedente test di ammissibilità è andato (in un qualche modo) bene...
                        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                            /*
                             * se sto esaminando un sottocomponente di tipo uso CONV o RAPP, se è definito un formato di
                             * file atteso per il convertitore, verifico che il formato del file versato coincida
                             */
                            if (trovatoConvRapp && verificaFormatoConvertitore && valueCompVers
                                    .getIdFormatoFileVers() != componentePadre.getIdFormatoFileDocConv()) {
                                setRispostaWsError();
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setVerificaAmmissibilitaFormato(ECEsitoPosNegType.NEGATIVO);
                                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_004_002,
                                        valueCompVers.getChiaveComp(), descFormatoFileVers);
                            } else {
                                sottoComponenteResp.getEsitoSottoComponente()
                                        .setVerificaAmmissibilitaFormato(ECEsitoPosNegType.POSITIVO);
                                valueCompVers.setNonAccettareForzaFormato(true);
                            }
                        } else {
                            // ... altrimenti preparo la risposta di formato non ammesso
                            rispostaWs.setErrorCode("SUBCOMP-004");
                            rispostaWs.setErrorMessage(
                                    "Formato file non ammesso per struttura versante e tipo sottocomponente");
                            sottoComponenteResp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                            sottoComponenteResp.getEsitoSottoComponente()
                                    .setVerificaAmmissibilitaFormato(ECEsitoPosNegType.NEGATIVO);
                            // SottoComponente {0}: il Formato {1} non è presente nel DB per la struttura versante, il
                            // tipo struttura e il tipo componente
                            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.SUBCOMP_004_001,
                                    valueCompVers.getChiaveComp(), descFormatoFileVers);
                        }
                    }

                    // verifico gli eventuali dati specifici e di migrazione
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        this.parseDatiSpecSottoComp(valueCompVers, sottoComponenteResp);
                    }

                }
            }
        }
        // fine loop sui sottocomp
        /*
         * COMP: Se non è valorizzato Tipo Rappresentazione Componente non devono essere presenti sottocomponenti aventi
         * tipo uso CONVERTITORE o RAPPRESENTAZIONE
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR && (!trovatoTipoRappComp) && contaConvRapp > 0) {
            setRispostaWsError();
            componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            componenteResp.getEsitoComponente().setVerificaSottoComponenteRappresentazione("NEGATIVO");
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_004_001, componentePadre.getChiaveComp());
        }
        /*
         * COMP: Se è valorizzato Tipo Rappresentazione Componente deve essere presente uno ed uno solo sottocomponente
         * avente tipo uso CONVERTITORE o RAPPRESENTAZIONE
         */
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR && trovatoTipoRappComp && verificaFormatoConvertitore
                && contaConvRapp != 1) {
            setRispostaWsError();
            componenteResp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            componenteResp.getEsitoComponente().setVerificaSottoComponenteRappresentazione("NEGATIVO");
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.COMP_004_002, componentePadre.getChiaveComp());
        }
    }

    private void parseDatiSpecDoc(DocumentoVers documento, ECDocumentoType esitoDoc) {
        JAXBElement<DatiSpecificiType> tmpDatiSpecifici;
        JAXBElement<DatiSpecificiType> tmpDatiSpecificiMigrazione;
        RispostaControlliAttSpec tmpControlliAttSpec = null;

        tmpDatiSpecifici = documento.getRifDocumento().getDatiSpecifici();
        gestioneDatiSpec.parseDatiSpec(TipiEntitaSacer.DOC, tmpDatiSpecifici, documento.getIdTipoDocumentoDB(),
                esitoDoc.getChiaveDoc(), documento.getRifDocumento().getTipoDocumento());
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            tmpControlliAttSpec = gestioneDatiSpec.getRispostaControlliAttSpec();
            documento.setDatiSpecifici(tmpControlliAttSpec.getDatiSpecifici());
            documento.setIdRecXsdDatiSpec(tmpControlliAttSpec.getIdRecXsdDatiSpec());
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
            esitoDoc.getEsitoDocumento().setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.POSITIVO);
        } else {
            esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
            esitoDoc.getEsitoDocumento().setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.NEGATIVO);
            rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
        }

        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_MIGRAZIONE)
                && rispostaWs.getSeverity() != SeverityEnum.ERROR
                && versamento.getStrutturaComponenti().getSistemaDiMigrazione() != null) {
            tmpDatiSpecificiMigrazione = documento.getRifDocumento().getDatiSpecificiMigrazione();
            gestioneDatiSpec.parseDatiSpecMig(TipiEntitaSacer.DOC, tmpDatiSpecificiMigrazione,
                    documento.getIdTipoDocumentoDB(), esitoDoc.getChiaveDoc(),
                    documento.getRifDocumento().getTipoDocumento());
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                tmpControlliAttSpec = gestioneDatiSpec.getRispostaControlliAttSpec();
                documento.setDatiSpecificiMigrazione(tmpControlliAttSpec.getDatiSpecifici());
                documento.setIdRecXsdDatiSpecMigrazione(tmpControlliAttSpec.getIdRecXsdDatiSpec());
                esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                esitoDoc.getEsitoDocumento().setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.POSITIVO);
            } else {
                esitoDoc.getEsitoDocumento().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                esitoDoc.getEsitoDocumento().setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
            }
        }
    }

    private void controllaHash(ComponenteVers componente, String hashDaVerificare) {
        boolean trovato = false;
        byte[] tmpHash = null;
        componente.setHashVersatoEncoding(CostantiDB.TipiEncBinari.SCONOSCIUTO);
        componente.setHashVersatoAlgoritmo(CostantiDB.TipiHash.SCONOSCIUTO);
        if (BinEncUtility.isHexString(hashDaVerificare)) {
            componente.setHashVersatoEncoding(CostantiDB.TipiEncBinari.HEX_BINARY);
            tmpHash = BinEncUtility.decodeUTF8HexString(hashDaVerificare);
            trovato = this.impostaAlgoHash(componente, tmpHash.length);
        }
        if (!trovato && BinEncUtility.isBase64String(hashDaVerificare)) {
            componente.setHashVersatoEncoding(CostantiDB.TipiEncBinari.BASE64);
            tmpHash = BinEncUtility.decodeUTF8Base64String(hashDaVerificare);
            this.impostaAlgoHash(componente, tmpHash.length);
        }
        componente.setHashVersato(tmpHash);
    }

    private boolean impostaAlgoHash(ComponenteVers componente, int lunghezza) {
        boolean trovato = false;
        switch (TipiHash.evaluateByLenght(lunghezza)) {
        case MD5:
            componente.setHashVersatoAlgoritmo(CostantiDB.TipiHash.MD5);
            trovato = true;
            break;
        case SHA_1:
            componente.setHashVersatoAlgoritmo(CostantiDB.TipiHash.SHA_1);
            trovato = true;
            break;
        case SHA_224:
            componente.setHashVersatoAlgoritmo(CostantiDB.TipiHash.SHA_224);
            trovato = true;
            break;
        case SHA_256:
            componente.setHashVersatoAlgoritmo(CostantiDB.TipiHash.SHA_256);
            trovato = true;
            break;
        case SHA_384:
            componente.setHashVersatoAlgoritmo(CostantiDB.TipiHash.SHA_384);
            trovato = true;
            break;
        case SHA_512:
            componente.setHashVersatoAlgoritmo(CostantiDB.TipiHash.SHA_512);
            trovato = true;
            break;
        default:
            componente.setHashVersatoAlgoritmo(CostantiDB.TipiHash.SCONOSCIUTO);
            break;
        }
        return trovato;
    }

    private void parseDatiSpecComp(ComponenteVers componente, ECComponenteType esitoComp) {
        JAXBElement<DatiSpecificiType> tmpDatiSpecifici;
        JAXBElement<DatiSpecificiType> tmpDatiSpecificiMigrazione;
        RispostaControlliAttSpec tmpControlliAttSpec = null;

        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_DATISPEC_EXT)) {
            tmpDatiSpecifici = componente.getMyComponente().getDatiSpecifici();
            gestioneDatiSpec.parseDatiSpec(TipiEntitaSacer.COMP, tmpDatiSpecifici, componente.getIdTipoComponente(),
                    componente.getChiaveComp(), componente.getMyComponente().getTipoComponente());
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                tmpControlliAttSpec = gestioneDatiSpec.getRispostaControlliAttSpec();
                componente.setDatiSpecifici(tmpControlliAttSpec.getDatiSpecifici());
                componente.setIdRecXsdDatiSpec(tmpControlliAttSpec.getIdRecXsdDatiSpec());
                esitoComp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                esitoComp.getEsitoComponente().setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.POSITIVO);
            } else {
                esitoComp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                esitoComp.getEsitoComponente().setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.NEGATIVO);
                rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
            }

            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_MIGRAZIONE)
                    && rispostaWs.getSeverity() != SeverityEnum.ERROR
                    && versamento.getStrutturaComponenti().getSistemaDiMigrazione() != null) {
                tmpDatiSpecificiMigrazione = componente.getMyComponente().getDatiSpecificiMigrazione();
                gestioneDatiSpec.parseDatiSpecMig(TipiEntitaSacer.COMP, tmpDatiSpecificiMigrazione,
                        componente.getIdTipoComponente(), componente.getChiaveComp(),
                        componente.getMyComponente().getTipoComponente());
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    tmpControlliAttSpec = gestioneDatiSpec.getRispostaControlliAttSpec();
                    componente.setDatiSpecificiMigrazione(tmpControlliAttSpec.getDatiSpecifici());
                    componente.setIdRecXsdDatiSpecMigrazione(tmpControlliAttSpec.getIdRecXsdDatiSpec());
                    esitoComp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    esitoComp.getEsitoComponente().setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.POSITIVO);
                } else {
                    esitoComp.getEsitoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    esitoComp.getEsitoComponente().setCorrispondenzaDatiSpecifici(ECEsitoPosNegType.NEGATIVO);
                    rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
                }
            }
        }
    }

    private void parseDatiSpecSottoComp(ComponenteVers componente, ECSottoComponenteType esitoComp) {
        JAXBElement<DatiSpecificiType> tmpDatiSpecifici;
        JAXBElement<DatiSpecificiType> tmpDatiSpecificiMigrazione;
        RispostaControlliAttSpec tmpControlliAttSpec = null;

        if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_DATISPEC_EXT)) {
            tmpDatiSpecifici = componente.getMySottoComponente().getDatiSpecifici();
            gestioneDatiSpec.parseDatiSpec(TipiEntitaSacer.SUB_COMP, tmpDatiSpecifici, componente.getIdTipoComponente(),
                    componente.getChiaveComp(), componente.getMySottoComponente().getTipoComponente());
            if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                tmpControlliAttSpec = gestioneDatiSpec.getRispostaControlliAttSpec();
                componente.setDatiSpecifici(tmpControlliAttSpec.getDatiSpecifici());
                componente.setIdRecXsdDatiSpec(tmpControlliAttSpec.getIdRecXsdDatiSpec());
                esitoComp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                esitoComp.getEsitoSottoComponente().setCorrispondenzaDatiSpecifici("POSITIVO");
            } else {
                esitoComp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                esitoComp.getEsitoSottoComponente().setCorrispondenzaDatiSpecifici("NEGATIVO");
                rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
            }

            if (versamento.getModificatoriWSCalc().contains(ModificatoriWS.TAG_MIGRAZIONE)
                    && rispostaWs.getSeverity() != SeverityEnum.ERROR
                    && versamento.getStrutturaComponenti().getSistemaDiMigrazione() != null) {
                tmpDatiSpecificiMigrazione = componente.getMySottoComponente().getDatiSpecificiMigrazione();
                gestioneDatiSpec.parseDatiSpecMig(TipiEntitaSacer.SUB_COMP, tmpDatiSpecificiMigrazione,
                        componente.getIdTipoComponente(), componente.getChiaveComp(),
                        componente.getMySottoComponente().getTipoComponente());
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    tmpControlliAttSpec = gestioneDatiSpec.getRispostaControlliAttSpec();
                    componente.setDatiSpecificiMigrazione(tmpControlliAttSpec.getDatiSpecifici());
                    componente.setIdRecXsdDatiSpecMigrazione(tmpControlliAttSpec.getIdRecXsdDatiSpec());
                    esitoComp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.POSITIVO);
                    esitoComp.getEsitoSottoComponente().setCorrispondenzaDatiSpecifici("POSITIVO");
                } else {
                    esitoComp.getEsitoSottoComponente().setCodiceEsito(ECEsitoPosNegWarType.NEGATIVO);
                    esitoComp.getEsitoSottoComponente().setCorrispondenzaDatiSpecifici("NEGATIVO");
                    rispostaWs.setEsitoWsError(rispostaWs.getErrorCode(), rispostaWs.getErrorMessage());
                }
            }
        }
    }

    private void setRispostaWsError() {
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        rispostaWs.setErrorCode(rispostaControlli.getCodErr());
        rispostaWs.setErrorMessage(rispostaControlli.getDsErr());
    }
}
