package it.eng.parer.ws.versamentoMM.utils;

import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliMM;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.XmlVersCache;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.CostantiDB.TipiEncBinari;
import it.eng.parer.ws.utils.CostantiDB.TipiHash;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.RispostaWS;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamento.ejb.RapportoVersBuilder;
import it.eng.parer.ws.versamentoMM.dto.ComponenteMM;
import it.eng.parer.ws.versamentoMM.dto.StrutturaIndiceMM;
import it.eng.parer.ws.versamentoMM.dto.VersamentoMMExt;
import it.eng.parer.ws.xml.versReqMultiMedia.ComponenteType;
import it.eng.parer.ws.xml.versReqMultiMedia.IndiceMM;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;
import it.eng.parer.ws.xml.versResp.ECEsitoPosNegType;
import it.eng.spagoLite.security.User;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fioravanti_F
 */
public class IndiceMMPrsr {

    private static final Logger log = LoggerFactory.getLogger(IndiceMMPrsr.class);
    private VersamentoMMExt versamento;
    private RispostaWS rispostaWs;
    private RispostaControlli rispostaControlli;
    // l'istanza dell'unità documentaria decodificata dall'XML di versamento
    IndiceMM parsedIndice = null;
    // stateless ejb per i controlli sul db
    ControlliSemantici controlliSemantici = null;
    // stateless ejb per i controlli sul db
    ControlliMM controlliMM = null;
    // singleton ejb di gestione cache dei parser Castor
    XmlVersCache xmlVersCache = null;
    // percorso completo e verificato del container .ZIP
    String zipFilePath = null;

    RapportoVersBuilder rapportoVersBuilder = null;

    public VersamentoMMExt getVersamento() {
        return versamento;
    }

    public RispostaWS getRispostaWs() {
        return rispostaWs;
    }

    public IndiceMMPrsr(VersamentoMMExt vers, RispostaWS risp) {
        versamento = vers;
        rispostaWs = risp;
        rispostaControlli = new RispostaControlli();
    }

    public void parseXML(SyncFakeSessn sessione) {
        EsitoVersamento myEsito = rispostaWs.getIstanzaEsito();
        AvanzamentoWs myAvanzamentoWs = rispostaWs.getAvanzamento();
        StringReader tmpReader;
        parsedIndice = null;

        // stateless ejb per i controlli sul db
        controlliSemantici = null;

        log.debug("Recupera ejb di controllo ");
        // recupera l'ejb dei controlli, se possibile - altrimenti segnala errore
        try {
            controlliSemantici = (ControlliSemantici) new InitialContext().lookup("java:module/ControlliSemantici");
        } catch (NamingException ex) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                    "Errore nel recupero dell'EJB dei controlli semantici " + ex.getMessage());
            log.error("Errore nel recupero dell'EJB dei controlli semantici ", ex);
        }

        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            try {
                controlliMM = (ControlliMM) new InitialContext().lookup("java:module/ControlliMM");
            } catch (NamingException ex) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore nel recupero dell'EJB ControlliMM " + ex.getMessage());
                log.error("Errore nel recupero dell'EJB ControlliMM ", ex);
            }
        }

        // recupera l'ejb singleton, se possibile - altrimenti segnala errore
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            try {
                xmlVersCache = (XmlVersCache) new InitialContext().lookup("java:module/XmlVersCache");
            } catch (NamingException ex) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore nel recupero dell'EJB singleton XMLContext " + ex.getMessage());
                log.error("Errore nel recupero dell'EJB singleton XMLContext ", ex);
            }
        }

        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            try {
                rapportoVersBuilder = (RapportoVersBuilder) new InitialContext()
                        .lookup("java:module/RapportoVersBuilder");
            } catch (NamingException ex) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore nel recupero dell'EJB RapportoVersBuilder " + ex.getMessage());
                log.error("Errore nel recupero dell'EJB RapportoVersBuilder ", ex);
            }
        }

        versamento.setDatiXmlIndice(sessione.getDatiPackInfoSipXml());

        /*
         * produco la versione canonicalizzata del SIP. Gestisco l'eventuale errore relativo all'encoding indicato in
         * maniera errata (es. "ISO8859/1" oppure "utf8"), non rilevato dalla verifica precedente.
         */
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            rispostaControlli = rapportoVersBuilder.canonicalizzaPackInfoSipXml(sessione);
            if (!rispostaControlli.isrBoolean()) {
                setRispostaWsError();
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                myEsito.getEsitoXSD().setControlloStrutturaXML(rispostaControlli.getDsErr());
            }
        }

        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            log.debug("Unmarshall ");
            tmpReader = new StringReader(sessione.getDatiC14NPackInfoSipXml());
            try {
                myAvanzamentoWs.setFase("Unmarshall XML").logAvanzamento();
                Unmarshaller tmpUnmarshaller = xmlVersCache.getVersReqMMCtxforIndiceMM().createUnmarshaller();
                parsedIndice = (IndiceMM) tmpUnmarshaller.unmarshal(tmpReader);
                versamento.setIndiceMM(parsedIndice);
            } catch (UnmarshalException e) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.MM_XSD_001_001, e.getMessage());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setControlloStrutturaXML(
                        "Errore: XML malformato nel blocco di dati indice multimedia. Eccezione: " + e.getMessage());
            } catch (ValidationException e) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.MM_XSD_001_002, e.getMessage());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setControlloStrutturaXML(
                        "Errore di validazione del blocco di dati indice multimedia. Eccezione: " + e.getMessage());
            } catch (Exception e) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.MM_XSD_001_001, e.getMessage());
                myEsito.getEsitoXSD().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
                myEsito.getEsitoXSD().setControlloStrutturaXML(
                        "Errore: XML malformato nel blocco di dati indice multimedia. Eccezione: " + e.getMessage());
            }
        }

        // *********************************************************************************************
        String prefissoPathPerApp = "";
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            log.debug("Carica root path ");
            rispostaControlli = controlliMM.caricaRootPath(parsedIndice.getApplicativoChiamante(),
                    ControlliMM.TipiRootPath.In);
            if (rispostaControlli.isrBoolean()) {
                prefissoPathPerApp = rispostaControlli.getrString();
                versamento.setPrefissoPathPerApp(prefissoPathPerApp);
            } else {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
            }
        }

        // verifica se presente un riferimento ad un eventuale container .ZIP
        // verifica se il file ZIP è presente sul disco
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            File tmpFile;
            if (parsedIndice.getPathArchivioZip() != null && parsedIndice.getPathArchivioZip().length() > 0) {
                log.debug("Verifica presenza file zip ");
                // verifica esistenza del file zip
                zipFilePath = prefissoPathPerApp + parsedIndice.getPathArchivioZip();
                tmpFile = new File(zipFilePath);
                versamento.setContainerZip(true);
                versamento.setPathContainerZip(zipFilePath);
                if (!tmpFile.isFile() || !tmpFile.canRead()) {
                    rispostaWs.setSeverity(SeverityEnum.ERROR);
                    // Il file ZIP {0} dichiarato nell'indiceMM non esiste o non è raggiungibile
                    rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.MM_FILE_001_001, zipFilePath);
                }
            }
        }

        // verifica che non ci siano ID duplicati *
        // verifica che forzaformato sia definito solo se Verifica = false
        // verifica che forzahash sia definito solo se Calcola = false
        // verifica e calcola l'id del formato standard
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            log.debug("Verifica id duplicati ");
            versamento.setStrutturaIndiceMM(new StrutturaIndiceMM());
            versamento.getStrutturaIndiceMM().setComponenti(new HashMap<String, ComponenteMM>());

            boolean trovatoDuplicati = false;
            HashMap<String, ComponenteMM> tmpMap = versamento.getStrutturaIndiceMM().getComponenti();

            for (ComponenteType tmpComponente : parsedIndice.getComponenti().getComponente()) {
                if (tmpComponente.getID() == null || tmpComponente.getID().length() == 0
                        || tmpMap.containsKey(tmpComponente.getID())) {
                    trovatoDuplicati = true;
                } else {
                    if (tmpComponente.isVerificaFirmeFormati() && tmpComponente.getForzaFormato() != null) {
                        // Componente Multimedia <ID> {0}: Tag <ForzaFormato> presente ma tag <VerificaFirmeFormati> =
                        // true
                        versamento.listErrAddErrorUd(tmpComponente.getID(), MessaggiWSBundle.MM_COMP_001_001,
                                tmpComponente.getID());
                    }
                    //
                    if (tmpComponente.isCalcolaHash() && tmpComponente.getForzaHash() != null) {
                        // Componente Multimedia <ID> {0}: Tag <ForzaHash> presente ma tag <CalcolaHash> = true
                        versamento.listErrAddErrorUd(tmpComponente.getID(), MessaggiWSBundle.MM_COMP_002_001,
                                tmpComponente.getID());
                    }
                    //
                    ComponenteMM tmpComponenteMM = new ComponenteMM();
                    tmpComponenteMM.setId(tmpComponente.getID());
                    tmpComponenteMM.setMyComponenteMM(tmpComponente);
                    tmpComponenteMM.setForzaFirmeFormati(!tmpComponente.isVerificaFirmeFormati());
                    tmpComponenteMM.setForzaHash(!tmpComponente.isCalcolaHash());
                    tmpMap.put(tmpComponente.getID(), tmpComponenteMM);
                    //
                    if (tmpComponente.getForzaFormato() != null) {
                        rispostaControlli = controlliSemantici
                                .checkFormatoFileStandard(tmpComponente.getForzaFormato().getFormatoStandard());
                        if (rispostaControlli.isrBoolean() == false) {
                            // Componente Multimedia <ID> {0}: Il formato [{1}] non \u00e8 presente nel DB
                            versamento.listErrAddErrorUd(tmpComponente.getID(), MessaggiWSBundle.MM_COMP_003_001,
                                    tmpComponente.getID(), tmpComponente.getForzaFormato().getFormatoStandard());
                        } else {
                            tmpComponenteMM.setIdFormatoFileCalc(rispostaControlli.getrLong());
                        }
                    }
                    //
                    if (tmpComponente.getForzaHash() != null) {
                        boolean hashForzatoErr = false;
                        if (TipiHash.evaluateByDesc(tmpComponente.getForzaHash().getAlgoritmo())
                                .equals(TipiHash.SCONOSCIUTO)) {
                            hashForzatoErr = true;
                        }
                        if (TipiEncBinari.evaluateByDesc(tmpComponente.getForzaHash().getEncoding())
                                .equals(TipiEncBinari.SCONOSCIUTO)) {
                            hashForzatoErr = true;
                        }
                        try {
                            tmpComponenteMM.setHashForzato(
                                    Hex.decodeHex(tmpComponente.getForzaHash().getHash().toCharArray()));
                        } catch (DecoderException e) {
                            hashForzatoErr = true;
                        }
                        if (hashForzatoErr) {
                            // Componente Multimedia <ID> {0}: L'hash forzato non \u00e8 valido (sono ammessi solo hash
                            // SHA-256 in formato hexBinary)
                            versamento.listErrAddErrorUd(tmpComponente.getID(), MessaggiWSBundle.MM_COMP_004_001,
                                    tmpComponente.getID());
                        }
                    }
                    //
                    String tmpFilePath = tmpComponente.getURNFile().trim();
                    if (tmpFilePath == null || tmpFilePath.length() == 0) {
                        // Componente Multimedia <ID> {0}: Il tag <URNFile> obbligatorio e non valorizzato
                        versamento.listErrAddErrorUd(tmpComponente.getID(), MessaggiWSBundle.MM_COMP_005_001,
                                tmpComponente.getID());
                    }
                }
            }
            if (trovatoDuplicati) {
                // Controllare che i tag <ID> siano stati valorizzati correttamente. I valori devono essere univoci
                // entro l''indice Multimedia
                versamento.listErrAddErrorUd("", MessaggiWSBundle.MM_XSD_002_001);
            }
            //
            VoceDiErrore tmpVdE = versamento.calcolaErrorePrincipale();
            if (tmpVdE != null) {
                rispostaWs.setSeverity(tmpVdE.getSeverity());
                if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                    rispostaWs.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                } else if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                    rispostaWs.setEsitoWsWarning(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                }
            }
        }

        // .....................verifica che il file dichiarato nel campo URNFile esista
        // .....................verifica che il file dichiarato nel campo URNFile esista nello zip dichiarato
        if (rispostaWs.getSeverity() == SeverityEnum.OK) {
            log.debug("Verifica presenza file ");
            String tmpFilePath;
            File tmpFile;
            try {
                for (ComponenteType tmpComponente : parsedIndice.getComponenti().getComponente()) {
                    tmpFilePath = tmpComponente.getURNFile().trim();
                    if (tmpFilePath != null && tmpFilePath.length() > 0) {
                        if (zipFilePath == null) {
                            // verifica esistenza del file
                            tmpFile = new File(prefissoPathPerApp + tmpFilePath);
                            if (!tmpFile.isFile() || !tmpFile.canRead()) {
                                // Il file {0} riferito nel componente Multimedia <ID> {1} non esiste o non \u00e8
                                // raggiungibile
                                versamento.listErrAddErrorUd(tmpComponente.getID(), MessaggiWSBundle.MM_FILE_002_001,
                                        tmpFilePath, tmpComponente.getID());
                            }
                        } else {
                            if (!this.verificaPresenzaFileInZip(zipFilePath, tmpFilePath)) {
                                // Il file {0} riferito nel componente Multimedia <ID> {1} non \u00e8 presente nel file
                                // ZIP {2}
                                versamento.listErrAddErrorUd(tmpComponente.getID(), MessaggiWSBundle.MM_FILE_003_001,
                                        tmpFilePath, tmpComponente.getID(), zipFilePath);
                            }
                        }
                    }
                }
                //
                VoceDiErrore tmpVdE = versamento.calcolaErrorePrincipale();
                if (tmpVdE != null) {
                    rispostaWs.setSeverity(tmpVdE.getSeverity());
                    if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                        rispostaWs.setEsitoWsError(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                    } else if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                        rispostaWs.setEsitoWsWarning(tmpVdE.getErrorCode(), tmpVdE.getErrorMessage());
                    }
                }
            } catch (IOException ex) {
                rispostaWs.setSeverity(SeverityEnum.ERROR);
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "IndiceMMPrsr - verifica file in zip: " + ex.getMessage());
            }
        }
    }

    private boolean verificaPresenzaFileInZip(String zipPathName, String fileInZip) throws IOException {
        boolean tmpResult = false;
        log.debug("verifica presenza file in zip. Inizio. {}", fileInZip);
        ZipArchiveEntry tmpZipArchiveEntry;
        ZipFile tmpFile = null;
        try {
            tmpFile = new ZipFile(zipPathName);
            tmpZipArchiveEntry = tmpFile.getEntry(fileInZip);
            if (tmpZipArchiveEntry != null) {
                tmpResult = true;
            }
        } finally {
            ZipFile.closeQuietly(tmpFile);
        }
        log.debug("verifica presenza file in zip. Fine. {}", fileInZip);
        return tmpResult;
    }

    private void setRispostaWsError() {
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        rispostaWs.setErrorCode(rispostaControlli.getCodErr());
        rispostaWs.setErrorMessage(rispostaControlli.getDsErr());
    }
}
