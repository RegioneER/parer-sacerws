package it.eng.parer.ws.versamento.utils;

/**
 *
 * @author Fioravanti_F
 */
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.XmlVersCache;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiUsoDatiSpec;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.XmlValidationEventHandler;
import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.RispostaControlliAttSpec;
import it.eng.parer.ws.xml.versReq.DatiSpecificiType;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

public class GestioneDatiSpec {

    private static final Logger log = LoggerFactory.getLogger(GestioneDatiSpec.class);
    private IRispostaWS rispostaWs;
    private RispostaControlli rispostaControlli;
    private RispostaControlliAttSpec rispostaControlliAttSpec = null;
    private String sistemaMig = null;
    private long idOrgStrutt;
    // stateless ejb per i controlli sul db
    ControlliSemantici controlliSemantici = null;
    // singleton ejb di gestione cache dei parser Castor
    XmlVersCache xmlVersCache = null;
    //
    public final int MAXLEN_DATOSPEC = 4000;

    public IRispostaWS getRispostaWs() {
        return rispostaWs;
    }

    public RispostaControlliAttSpec getRispostaControlliAttSpec() {
        return rispostaControlliAttSpec;
    }

    public GestioneDatiSpec(String sistemaDiMigrazione, long idStrut, IRispostaWS risp) {
        this.sistemaMig = sistemaDiMigrazione;
        this.idOrgStrutt = idStrut;
        rispostaWs = risp;
        rispostaControlliAttSpec = new RispostaControlliAttSpec();

        // recupera l'ejb dei controlli, se possibile - altrimenti segnala errore
        try {
            controlliSemantici = (ControlliSemantici) new InitialContext().lookup("java:module/ControlliSemantici");
        } catch (NamingException ex) {
            this.setRispostaError(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "GestioneDatiSpec.init.ControlliSemantici: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB dei controlli semantici in GestioneDatiSpec ", ex);
        }

        // recupera l'ejb singleton, se possibile - altrimenti segnala errore
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            try {
                xmlVersCache = (XmlVersCache) new InitialContext().lookup("java:module/XmlVersCache");
            } catch (NamingException ex) {
                this.setRispostaError(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "GestioneDatiSpec.init.XmlVersCache: " + ex.getMessage()));
                log.error("Errore nel recupero dell'EJB singleton XMLContext ", ex);
            }
        }
    }

    public void parseDatiSpec(TipiEntitaSacer tipoEntita, JAXBElement<DatiSpecificiType> datiSpecificiElement,
            long idTipoElemento, String desElemento, String desTipoElemento) {
        DatiSpecificiType datiSpecifici;
        DatoSpecifico tmpAttSpecAtteso;
        SchemaFactory tmpSchemaFactoryValidazSpec = null;
        Schema tmpSchemaValidazSpec = null;
        String versione;
        long tmpIdVersioneXsd;
        Date tmpIstanteCorrente = new Date();

        rispostaControlliAttSpec = new RispostaControlliAttSpec();

        if (datiSpecificiElement == null || datiSpecificiElement.isNil()) {
            // verifica che esistano dati specifici attesi per idTipoEntita + tipoentita alla data corrente
            rispostaControlli = controlliSemantici.checkPresenzaDatiSpec(TipiUsoDatiSpec.VERS, tipoEntita,
                    this.sistemaMig, this.idOrgStrutt, idTipoElemento, tmpIstanteCorrente);
            if (rispostaControlli.getCodErr() != null) {
                this.setRispostaError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                return;
            }
            if (rispostaControlli.isrBoolean()) {
                // errore, perché non ha inserito il tag datispecifici e questo era richiesto
                this.setRispostaError(MessaggiWSBundle.DATISPEC_001_002, MessaggiWSBundle.getString(
                        MessaggiWSBundle.DATISPEC_001_002, tipoEntita.descrivi(), desElemento, desTipoElemento));
                return;
            }
        } else {
            datiSpecifici = datiSpecificiElement.getValue();
            versione = datiSpecifici.getVersioneDatiSpecifici();

            // NOTA: questo errore non può essere mai restituito perché il tag VersioneDatiSpecifici è definito
            // nell'XSD principale del versamento come stringa avente lunghezza minima di 1 char
            //
            // if (versione == null || "".equals(versione)) {
            // //Errore nella verifica dei dati specifici. La versione dei dati specifici è obbligatoria.
            // this.setRispostaError(MessaggiWSBundle.DATISPEC_001_003,
            // MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_003,
            // tipoEntita.descrivi(), desElemento, desTipoElemento));
            // return;
            // }
            rispostaControlli = controlliSemantici.checkXSDDatiSpec(TipiUsoDatiSpec.VERS, tipoEntita, this.sistemaMig,
                    this.idOrgStrutt, idTipoElemento, tmpIstanteCorrente, versione);
            if (rispostaControlli.getCodErr() != null) {
                this.setRispostaError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                return;
            }

            if (rispostaControlli.getrString() == null || rispostaControlli.getrString().length() == 0) {
                // errore, perché non ha trovato il validatore dei dati specifici per il tipo doc e versione indicato
                // cerco se per caso quella versione di dati spec esiste ma è disattivata
                RispostaControlli rc = controlliSemantici.checkPresenzaVersioneDatiSpec(TipiUsoDatiSpec.VERS,
                        tipoEntita, this.sistemaMig, this.idOrgStrutt, idTipoElemento, versione);
                if (rc.getCodErr() != null) {
                    this.setRispostaError(rc.getCodErr(), rc.getDsErr());
                    return;
                }
                if (rc.isrBoolean()) {
                    // la versione è disattivata
                    this.setRispostaError(MessaggiWSBundle.DATISPEC_001_004,
                            MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_004, tipoEntita.descrivi(),
                                    desElemento, versione, desTipoElemento));
                } else {
                    // Errore nella verifica dei dati specifici.
                    // Non sono ammissibili dati per il tipo elemento e la versione indicati.
                    this.setRispostaError(MessaggiWSBundle.DATISPEC_001_001,
                            MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_001, tipoEntita.descrivi(),
                                    desElemento, versione, desTipoElemento));
                }
                return;
            }

            tmpIdVersioneXsd = rispostaControlli.getrLong();

            // compilazione schema
            // 1. Lookup a factory for the W3C XML Schema language
            tmpSchemaFactoryValidazSpec = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // anche in questo caso l'eccezione non deve mai verificarsi, a meno di non aver caricato
            // nel database un xsd danneggiato...
            try {
                // 2. Compile the schema.
                tmpSchemaValidazSpec = tmpSchemaFactoryValidazSpec
                        .newSchema(new StreamSource(new StringReader(rispostaControlli.getrString())));
            } catch (SAXException e) {
                this.setRispostaError(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "GestioneDatiSpec.parseDatiSpec.il validatore xsd specifico è errato: " + e.getMessage()));
                log.error("Errore interno: il validatore xsd specifico è errato. Eccezione: {}",
                        e.getLocalizedMessage());
                return;
            }

            XmlValidationEventHandler handler = new XmlValidationEventHandler();
            try {
                Marshaller m = xmlVersCache.getVersReqCtxforDatiSpecifici().createMarshaller();
                m.setSchema(tmpSchemaValidazSpec);
                m.setEventHandler(handler);
                // 3. Marshall and validate
                m.marshal(datiSpecificiElement, new StringWriter());
            } catch (JAXBException e) {
                ValidationEvent events = handler.getFirstErrorValidationEvent();

                // Errore nella verifica dei dati specifici.
                this.setRispostaError(MessaggiWSBundle.DATISPEC_003_001, MessaggiWSBundle.getString(
                        MessaggiWSBundle.DATISPEC_003_001, tipoEntita.descrivi(), desElemento, events.getMessage()));
                return;
            } catch (Exception e) {
                // anche in questo caso l'eccezione non deve mai verificarsi
                this.setRispostaError(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "GestioneDatiSpec.parseDatiSpec.problemi nella validazione dei dati spec: " + e.getMessage()));
                log.error("Errore interno: problemi nella validazione dei dati spec. Eccezione: ", e);
                return;
            }

            // leggi la tabella dei dati spec attesi
            rispostaControlliAttSpec = controlliSemantici.checkDatiSpecifici(TipiUsoDatiSpec.VERS, tipoEntita,
                    this.sistemaMig, this.idOrgStrutt, idTipoElemento, tmpIdVersioneXsd);
            if (rispostaControlliAttSpec.getCodErr() != null) {
                this.setRispostaError(rispostaControlliAttSpec.getCodErr(), rispostaControlliAttSpec.getDsErr());
                return;
            }

            // impostazione dell'Id dell'Xsd
            rispostaControlliAttSpec.setIdRecXsdDatiSpec(tmpIdVersioneXsd);

            for (Element element : datiSpecifici.getAny()) {
                if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                    String tmpChiave = element.getLocalName();

                    tmpAttSpecAtteso = rispostaControlliAttSpec.getDatiSpecifici().get(tmpChiave);
                    if (tmpAttSpecAtteso != null) {
                        if (tmpAttSpecAtteso.getValore() == null) {

                            if (element.getFirstChild() != null) {
                                String tmpValore = element.getFirstChild().getNodeValue();
                                if (tmpValore.length() <= MAXLEN_DATOSPEC) {
                                    tmpAttSpecAtteso.setValore(tmpValore);
                                } else {
                                    // "Errore nei dati specifici: uno o più valori superano
                                    // la lunghezza di " + MAXLEN_DATOSPEC + " caratteri"
                                    this.setRispostaError(MessaggiWSBundle.DATISPEC_002_001,
                                            MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_002_001,
                                                    tipoEntita.descrivi(), desElemento, tmpChiave, MAXLEN_DATOSPEC));
                                    return;
                                }
                            } else {
                                tmpAttSpecAtteso.setValore(null);
                            }
                        } else {
                            // "Errore nei dati specifici: uno o più dati risultano duplicati");
                            this.setRispostaError(MessaggiWSBundle.DATISPEC_002_002, MessaggiWSBundle.getString(
                                    MessaggiWSBundle.DATISPEC_002_002, tipoEntita.descrivi(), desElemento, tmpChiave));
                            return;
                        }
                    } else {
                        this.setRispostaError(MessaggiWSBundle.ERR_666,
                                MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                                        "GestioneDatiSpec.parseDatiSpec.i dati specifici attesi "
                                                + "non coincidono con l'XSD "));
                        log.error("Errore interno: i dati specifici attesi non coincidono con l'XSD");
                        return;
                    }
                }
            }
        }
    }

    // dati di migrazione, non c'è l'id tipo ma si valuta il sistema migrante
    public void parseDatiSpecMig(TipiEntitaSacer tipoEntita, JAXBElement<DatiSpecificiType> datiSpecificiElement,
            long idTipoElemento, String desElemento, String desTipoElemento) {
        DatiSpecificiType datiSpecifici;
        DatoSpecifico tmpAttSpecAtteso;
        SchemaFactory tmpSchemaFactoryValidazSpec = null;
        Schema tmpSchemaValidazSpec = null;
        Validator tmpValidatorSpec = null;
        String versione;
        long tmpIdVersioneXsd;
        boolean tmpSistMigDef = true;
        Date tmpIstanteCorrente = new Date();

        rispostaControlliAttSpec = new RispostaControlliAttSpec();

        // verifica che il sistema migrante sia definito
        if (this.sistemaMig == null || this.sistemaMig.isEmpty()) {
            tmpSistMigDef = false;
        }

        if (datiSpecificiElement == null || datiSpecificiElement.isNil()) {
            if (tmpSistMigDef) {
                // verifica che esistano dati specifici attesi per sistema migrante + tipoentita alla data corrente
                rispostaControlli = controlliSemantici.checkPresenzaDatiSpec(TipiUsoDatiSpec.MIGRAZ, tipoEntita,
                        this.sistemaMig, this.idOrgStrutt, 0, tmpIstanteCorrente);
                if (rispostaControlli.getCodErr() != null) {
                    this.setRispostaError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                    return;
                }
                if (rispostaControlli.isrBoolean()) {
                    // errore, perché non ha inserito il tag datispecifici e questo era richiesto
                    this.setRispostaError(MessaggiWSBundle.DATISPECM_001_002, MessaggiWSBundle.getString(
                            MessaggiWSBundle.DATISPECM_001_002, tipoEntita.descrivi(), desElemento, desTipoElemento));
                    return;
                }
            }
        } else {
            if (tmpSistMigDef) {
                datiSpecifici = datiSpecificiElement.getValue();
                versione = datiSpecifici.getVersioneDatiSpecifici();

                // NOTA: questo errore non può essere mai restituito perché il tag VersioneDatiSpecifici è definito
                // nell'XSD principale del versamento come stringa avente lunghezza minima di 1 char
                //
                // if (versione == null || "".equals(versione)) {
                // //Errore nella verifica dei dati specifici. La versione dei dati specifici è obbligatoria.
                // this.setRispostaError(MessaggiWSBundle.DATISPECM_001_003,
                // MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_001_003,
                // tipoEntita.descrivi(), desElemento, desTipoElemento));
                // return;
                // }
                rispostaControlli = controlliSemantici.checkXSDDatiSpec(TipiUsoDatiSpec.MIGRAZ, tipoEntita,
                        this.sistemaMig, this.idOrgStrutt, 0, tmpIstanteCorrente, versione);
                if (rispostaControlli.getCodErr() != null) {
                    this.setRispostaError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr());
                    return;
                }

                if (rispostaControlli.getrString() == null || rispostaControlli.getrString().length() == 0) {
                    // errore, perché non ha trovato il validatore dei dati specifici per il tipo doc e versione
                    // indicato
                    // cerco se per caso quella versione di dati spec esiste ma è disattivata
                    RispostaControlli rc = controlliSemantici.checkPresenzaVersioneDatiSpec(TipiUsoDatiSpec.MIGRAZ,
                            tipoEntita, this.sistemaMig, this.idOrgStrutt, 0, versione);
                    if (rc.getCodErr() != null) {
                        this.setRispostaError(rc.getCodErr(), rc.getDsErr());
                        return;
                    }
                    if (rc.isrBoolean()) {
                        // la versione è disattivata
                        this.setRispostaError(MessaggiWSBundle.DATISPECM_001_004,
                                MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_004, tipoEntita.descrivi(),
                                        desElemento, versione, desTipoElemento));
                    } else {
                        // Errore nella verifica dei dati specifici.
                        // Non sono ammissibili dati per il tipo elemento e la versione indicati.
                        this.setRispostaError(MessaggiWSBundle.DATISPECM_001_001,
                                MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_001_001, tipoEntita.descrivi(),
                                        desElemento, versione, desTipoElemento));
                    }
                    return;
                }

                tmpIdVersioneXsd = rispostaControlli.getrLong();

                // compilazione schema
                // 1. Lookup a factory for the W3C XML Schema language
                tmpSchemaFactoryValidazSpec = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                // anche in questo caso l'eccezione non deve mai verificarsi, a meno di non aver caricato
                // nel database un xsd danneggiato...
                try {
                    // 2. Compile the schema.
                    tmpSchemaValidazSpec = tmpSchemaFactoryValidazSpec
                            .newSchema(new StreamSource(new StringReader(rispostaControlli.getrString())));
                } catch (SAXException e) {
                    this.setRispostaError(MessaggiWSBundle.ERR_666,
                            MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                                    "GestioneDatiSpec.parseDatiSpecMig.il validatore xsd specifico è errato: "
                                            + e.getMessage()));
                    log.error("Errore interno: il validatore xsd  di migrazione è errato. Eccezione: {}",
                            e.getLocalizedMessage());
                    return;
                }

                XmlValidationEventHandler handler = new XmlValidationEventHandler();
                try {
                    Marshaller m = xmlVersCache.getVersReqCtxforDatiSpecifici().createMarshaller();
                    m.setSchema(tmpSchemaValidazSpec);
                    m.setEventHandler(handler);
                    // 3. Marshall and validate
                    m.marshal(datiSpecificiElement, new StringWriter());
                } catch (JAXBException e) {
                    ValidationEvent events = handler.getFirstErrorValidationEvent();

                    // Errore nella verifica dei dati specifici.
                    this.setRispostaError(MessaggiWSBundle.DATISPECM_003_001,
                            MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_003_001, tipoEntita.descrivi(),
                                    desElemento, events.getMessage()));
                    return;
                } catch (Exception e) {
                    // anche in questo caso l'eccezione non deve mai verificarsi
                    this.setRispostaError(MessaggiWSBundle.ERR_666,
                            MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                                    "GestioneDatiSpec.parseDatiSpecMig.problemi nella validazione dei dati spec: "
                                            + e.getMessage()));
                    log.error("Errore interno: problemi nella validazione dei dati spec. di migrazione Eccezione: ", e);
                    return;
                }

                // leggi la tabella dei dati spec attesi
                rispostaControlliAttSpec = controlliSemantici.checkDatiSpecifici(TipiUsoDatiSpec.MIGRAZ, tipoEntita,
                        this.sistemaMig, this.idOrgStrutt, 0, tmpIdVersioneXsd);
                if (rispostaControlliAttSpec.getCodErr() != null) {
                    this.setRispostaError(rispostaControlliAttSpec.getCodErr(), rispostaControlliAttSpec.getDsErr());
                    return;
                }

                // impostazione dell'Id dell'Xsd
                rispostaControlliAttSpec.setIdRecXsdDatiSpec(tmpIdVersioneXsd);

                // estrazione dati nella collection dei dati spec attesi
                for (Element element : datiSpecifici.getAny()) {
                    if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
                        String tmpChiave = element.getLocalName();

                        tmpAttSpecAtteso = rispostaControlliAttSpec.getDatiSpecifici().get(tmpChiave);
                        if (tmpAttSpecAtteso != null) {
                            if (tmpAttSpecAtteso.getValore() == null) {
                                if (element.getFirstChild() != null) {
                                    String tmpValore = element.getFirstChild().getNodeValue();
                                    if (tmpValore.length() <= MAXLEN_DATOSPEC) {
                                        tmpAttSpecAtteso.setValore(tmpValore);
                                    } else {
                                        // "Errore nei dati specifici: uno o più valori superano
                                        // la lunghezza di " + MAXLEN_DATOSPEC + " caratteri"
                                        this.setRispostaError(MessaggiWSBundle.DATISPECM_002_001,
                                                MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_002_001,
                                                        tipoEntita.descrivi(), desElemento, tmpChiave,
                                                        MAXLEN_DATOSPEC));
                                        return;
                                    }
                                } else {
                                    tmpAttSpecAtteso.setValore(null);
                                }
                            } else {
                                // "Errore nei dati specifici: uno o più dati risultano duplicati");
                                this.setRispostaError(MessaggiWSBundle.DATISPECM_002_002,
                                        MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_002_002,
                                                tipoEntita.descrivi(), desElemento, tmpChiave));
                                return;
                            }
                        } else {
                            this.setRispostaError(MessaggiWSBundle.ERR_666,
                                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                                            "GestioneDatiSpec.parseDatiSpecMig.i dati specifici attesi"
                                                    + " non coincidono con l'XSD "));
                            log.error("Errore interno: i dati specifici attesi di migrazione non coincidono con l'XSD");
                            return;
                        }
                    }
                }
            } else {
                /*
                 * nota questo è un problema, perché questo controllo deve essere già stato fatto nella classe
                 * VerificaVersione
                 */
                this.setRispostaError(MessaggiWSBundle.ERR_666,
                        MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                                "GestioneDatiSpec.parseDatiSpecMig. Presenti dati specifici di migrazione "
                                        + "e sistema migrante non specificato "));
                log.error("Errore interno: Presenti dati specifici di migrazione e sistema migrante non specificato");
                return;
            }
        }
    }

    private void setRispostaError(String codErr, String dsErr) {
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        rispostaWs.setErrorCode(codErr);
        rispostaWs.setErrorMessage(dsErr);
        rispostaControlliAttSpec.setrBoolean(false);
        rispostaControlliAttSpec.setCodErr(codErr);
        rispostaControlliAttSpec.setDsErr(dsErr);
    }
}