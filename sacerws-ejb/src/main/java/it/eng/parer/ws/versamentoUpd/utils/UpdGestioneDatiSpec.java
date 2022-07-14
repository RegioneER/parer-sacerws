/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.utils;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.XmlUpdVersCache;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiUsoDatiSpec;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.XmlValidationEventHandler;
import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.RispostaControlliAttSpec;
import it.eng.parer.ws.versamentoUpd.ext.UpdVersamentoExt;
import it.eng.parer.ws.xml.versUpdReq.DatiSpecificiType;

public class UpdGestioneDatiSpec {

    private static final Logger log = LoggerFactory.getLogger(UpdGestioneDatiSpec.class);

    private RispostaControlli rispostaControlli;
    private String sistemaDiMigrazione = null;
    private long idStruttura;
    // stateless ejb per i controlli sul db
    private ControlliSemantici controlliSemantici = null;
    // singleton ejb di gestione cache dei parser Castor
    private XmlUpdVersCache xmlVersCache = null;

    public UpdGestioneDatiSpec(UpdVersamentoExt versamento) {
        this.sistemaDiMigrazione = versamento.getStrutturaUpdVers().getSistemaDiMigrazione();
        this.idStruttura = versamento.getStrutturaUpdVers().getIdStruttura();
    }

    public RispostaControlliAttSpec parseDatiSpec(TipiEntitaSacer tipoEntita,
            JAXBElement<DatiSpecificiType> datiSpecificiElement, long idTipoElemento, String desElemento,
            String desTipoElemento) {

        DatiSpecificiType datiSpecifici;
        DatoSpecifico tmpAttSpecAtteso;
        SchemaFactory tmpSchemaFactoryValidazSpec = null;
        Schema tmpSchemaValidazSpec = null;
        String versione;
        long tmpIdVersioneXsd;
        Date tmpIstanteCorrente = new Date();

        RispostaControlliAttSpec rispostaControlliAttSpec = new RispostaControlliAttSpec();
        rispostaControlliAttSpec.setrBoolean(false);

        // recupera l'ejb dei controlli, se possibile - altrimenti segnala errore
        try {
            controlliSemantici = (ControlliSemantici) new InitialContext().lookup("java:module/ControlliSemantici");
        } catch (NamingException ex) {
            rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdGestioneDatiSpec.init.ControlliSemantici: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB dei controlli semantici in UpdGestioneDatiSpec ", ex);
            return rispostaControlliAttSpec;
        }

        // recupera l'ejb singleton, se possibile - altrimenti segnala errore
        try {
            xmlVersCache = (XmlUpdVersCache) new InitialContext().lookup("java:module/XmlUpdVersCache");
        } catch (NamingException ex) {
            rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdGestioneDatiSpec.init.XmlUpdVersCache: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB singleton XMLContext ", ex);
            return rispostaControlliAttSpec;
        }

        if (datiSpecificiElement == null || datiSpecificiElement.isNil()) {
            // verifica che esistano dati specifici attesi per idTipoEntita + tipoentita
            // alla data corrente
            rispostaControlli = controlliSemantici.checkPresenzaDatiSpec(TipiUsoDatiSpec.VERS, tipoEntita,
                    this.sistemaDiMigrazione, this.idStruttura, idTipoElemento, tmpIstanteCorrente);
            if (rispostaControlli.getCodErr() != null)/* 666 */ {
                // è un errore grave ...
                rispostaControlliAttSpec.setCodErr(rispostaControlli.getCodErr());
                rispostaControlliAttSpec.setDsErr(rispostaControlli.getDsErr());
                rispostaControlliAttSpec.setrBoolean(false);
                return rispostaControlliAttSpec;

            }
            if (rispostaControlli.isrBoolean()) {
                // errore, perché non ha inserito il tag datispecifici e questo era richiesto
                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPEC_001_002);
                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_002,
                        tipoEntita.descrivi(), desElemento, desTipoElemento));
                rispostaControlliAttSpec.setrBoolean(false);
                return rispostaControlliAttSpec;
            }
        } else {
            datiSpecifici = datiSpecificiElement.getValue();
            versione = datiSpecifici.getVersioneDatiSpecifici();

            // NOTA: questo errore non può essere mai restituito perché il tag
            // VersioneDatiSpecifici è definito
            // nell'XSD principale del versamento come stringa avente lunghezza minima di 1
            // char
            //
            // if (versione == null || "".equals(versione)) {
            // //Errore nella verifica dei dati specifici. La versione dei dati specifici è
            // obbligatoria.
            // this.setRispostaError(MessaggiWSBundle.DATISPEC_001_003,
            // MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_003,
            // tipoEntita.descrivi(), desElemento, desTipoElemento));
            // return;
            // }
            rispostaControlli = controlliSemantici.checkXSDDatiSpec(TipiUsoDatiSpec.VERS, tipoEntita,
                    this.sistemaDiMigrazione, this.idStruttura, idTipoElemento, tmpIstanteCorrente, versione);
            if (rispostaControlli.getCodErr() != null) {
                // è un errore grave ...
                rispostaControlliAttSpec.setCodErr(rispostaControlli.getCodErr());
                rispostaControlliAttSpec.setDsErr(rispostaControlli.getDsErr());
                rispostaControlliAttSpec.setrBoolean(false);
                return rispostaControlliAttSpec;
            }

            if (rispostaControlli.getrString() == null || rispostaControlli.getrString().length() == 0) {
                // errore, perché non ha trovato il validatore dei dati specifici per il tipo
                // doc e versione indicato
                // cerco se per caso quella versione di dati spec esiste ma è disattivata
                RispostaControlli rc = controlliSemantici.checkPresenzaVersioneDatiSpec(TipiUsoDatiSpec.VERS,
                        tipoEntita, this.sistemaDiMigrazione, this.idStruttura, idTipoElemento, versione);
                if (rc.getCodErr() != null) {
                    // è un errore grave ...
                    rispostaControlliAttSpec.setCodErr(rispostaControlli.getCodErr());
                    rispostaControlliAttSpec.setDsErr(rispostaControlli.getDsErr());
                    rispostaControlliAttSpec.setrBoolean(false);
                    return rispostaControlliAttSpec;
                }
                if (rc.isrBoolean()) {
                    // la versione è disattivata
                    // errore, perché non ha inserito il tag datispecifici e questo era richiesto
                    rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPEC_001_004);
                    rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_004,
                            tipoEntita.descrivi(), desElemento, versione, desTipoElemento));
                    rispostaControlliAttSpec.setrBoolean(false);
                } else {
                    // Errore nella verifica dei dati specifici.
                    // Non sono ammissibili dati per il tipo elemento e la versione indicati.
                    // errore, perché non ha inserito il tag datispecifici e DATISPEC_001_001 era richiesto
                    rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPEC_001_001);
                    rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_001,
                            tipoEntita.descrivi(), desElemento, versione, desTipoElemento));
                    rispostaControlliAttSpec.setrBoolean(false);
                }
                return rispostaControlliAttSpec;
            }

            tmpIdVersioneXsd = rispostaControlli.getrLong();

            // compilazione schema
            // 1. Lookup a factory for the W3C XML Schema language
            tmpSchemaFactoryValidazSpec = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // anche in questo caso l'eccezione non deve mai verificarsi, a meno di non aver
            // caricato
            // nel database un xsd danneggiato...
            try {
                // 2. Compile the schema.
                tmpSchemaValidazSpec = tmpSchemaFactoryValidazSpec
                        .newSchema(new StreamSource(new StringReader(rispostaControlli.getrString())));
            } catch (SAXException e) {
                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "AggGestioneDatiSpec.parseDatiSpec.il validatore xsd specifico è errato: " + e.getMessage()));
                rispostaControlliAttSpec.setrBoolean(false);
                log.error(
                        "Errore interno: il validatore xsd specifico è errato. Eccezione: " + e.getLocalizedMessage());
                return rispostaControlliAttSpec;
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
                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPEC_003_001);
                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_003_001,
                        tipoEntita.descrivi(), desElemento, events.getMessage()));
                rispostaControlliAttSpec.setrBoolean(false);
                return rispostaControlliAttSpec;
            } catch (Exception e) {
                // anche in questo caso l'eccezione non deve mai verificarsi
                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "UpdGestioneDatiSpec.parseDatiSpec.problemi nella validazione dei dati spec: "
                                + e.getMessage()));
                rispostaControlliAttSpec.setrBoolean(false);
                log.error("Errore interno: problemi nella validazione dei dati spec. Eccezione: ", e);
                return rispostaControlliAttSpec;
            }

            // leggi la tabella dei dati spec attesi
            rispostaControlliAttSpec = controlliSemantici.checkDatiSpecifici(TipiUsoDatiSpec.VERS, tipoEntita,
                    this.sistemaDiMigrazione, this.idStruttura, idTipoElemento, tmpIdVersioneXsd);
            if (rispostaControlliAttSpec.getCodErr() != null) {
                rispostaControlliAttSpec.setrBoolean(false);
                return rispostaControlliAttSpec;
            }

            // impostazione dell'Id dell'Xsd
            rispostaControlliAttSpec.setIdRecXsdDatiSpec(tmpIdVersioneXsd);

            for (Element element : datiSpecifici.getAny()) {
                String tmpChiave = element.getLocalName();
                tmpAttSpecAtteso = rispostaControlliAttSpec.getDatiSpecifici().get(tmpChiave);
                if (tmpAttSpecAtteso != null) {
                    if (tmpAttSpecAtteso.getValore() == null) {

                        if (element.getFirstChild() != null) {
                            String tmpValore = element.getFirstChild().getNodeValue();
                            if (tmpValore.length() <= UpdCostanti.MAXLEN_DATOSPEC) {
                                tmpAttSpecAtteso.setValore(tmpValore);
                            } else {
                                // "Errore nei dati specifici: uno o più valori superano
                                // la lunghezza di " + MAXLEN_DATOSPEC + " caratteri"
                                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPEC_002_001);
                                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(
                                        MessaggiWSBundle.DATISPEC_002_001, tipoEntita.descrivi(), desElemento,
                                        tmpChiave, UpdCostanti.MAXLEN_DATOSPEC));
                                rispostaControlliAttSpec.setrBoolean(false);
                                return rispostaControlliAttSpec;
                            }
                        } else {
                            tmpAttSpecAtteso.setValore(null);
                        }
                    } else {
                        // "Errore nei dati specifici: uno o più dati risultano duplicati");
                        rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPEC_002_002);
                        rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_002_002,
                                tipoEntita.descrivi(), desElemento, tmpChiave));
                        rispostaControlliAttSpec.setrBoolean(false);
                        return rispostaControlliAttSpec;
                    }
                } else {
                    rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
                    rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "UpdGestioneDatiSpec.parseDatiSpec.i dati specifici attesi "
                                    + "non coincidono con l'XSD "));
                    rispostaControlliAttSpec.setrBoolean(false);
                    log.error("Errore interno: i dati specifici attesi non coincidono con l'XSD");
                    return rispostaControlliAttSpec;
                }
            }
        }
        rispostaControlliAttSpec.setrBoolean(true);
        return rispostaControlliAttSpec;

    }

    // dati di migrazione, non c'è l'id tipo ma si valuta il sistema migrante
    public RispostaControlliAttSpec parseDatiSpecMig(TipiEntitaSacer tipoEntita,
            JAXBElement<DatiSpecificiType> datiSpecificiElement, long idTipoElemento, String desElemento,
            String desTipoElemento) {
        DatiSpecificiType datiSpecifici;
        DatoSpecifico tmpAttSpecAtteso;
        SchemaFactory tmpSchemaFactoryValidazSpec = null;
        Schema tmpSchemaValidazSpec = null;
        String versione;
        long tmpIdVersioneXsd;
        boolean tmpSistMigDef = true;
        Date tmpIstanteCorrente = new Date();

        RispostaControlliAttSpec rispostaControlliAttSpec = new RispostaControlliAttSpec();
        rispostaControlliAttSpec.setrBoolean(false);

        // recupera l'ejb dei controlli, se possibile - altrimenti segnala errore
        try {
            controlliSemantici = (ControlliSemantici) new InitialContext().lookup("java:module/ControlliSemantici");
        } catch (NamingException ex) {
            rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdGestioneDatiSpec.init.ControlliSemantici: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB dei controlli semantici in UpdGestioneDatiSpec ", ex);
            return rispostaControlliAttSpec;
        }

        // recupera l'ejb singleton, se possibile - altrimenti segnala errore
        try {
            xmlVersCache = (XmlUpdVersCache) new InitialContext().lookup("java:module/XmlUpdVersCache");
        } catch (NamingException ex) {
            rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdGestioneDatiSpec.init.XmlUpdVersCache: " + ex.getMessage()));
            log.error("Errore nel recupero dell'EJB singleton XMLContext ", ex);
            return rispostaControlliAttSpec;
        }

        rispostaControlliAttSpec = new RispostaControlliAttSpec();

        // verifica che il sistema migrante sia definito
        if (this.sistemaDiMigrazione == null || this.sistemaDiMigrazione.isEmpty()) {
            tmpSistMigDef = false;
        }

        if (tmpSistMigDef) {
            datiSpecifici = datiSpecificiElement.getValue();
            versione = datiSpecifici.getVersioneDatiSpecifici();

            // NOTA: questo errore non può essere mai restituito perché il tag
            // VersioneDatiSpecifici è definito
            // nell'XSD principale del versamento come stringa avente lunghezza minima di 1
            // char
            //
            // if (versione == null || "".equals(versione)) {
            // //Errore nella verifica dei dati specifici. La versione dei dati specifici è
            // obbligatoria.
            // this.setRispostaError(MessaggiWSBundle.DATISPECM_001_003,
            // MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_001_003,
            // tipoEntita.descrivi(), desElemento, desTipoElemento));
            // return;
            // }
            rispostaControlli = controlliSemantici.checkXSDDatiSpec(TipiUsoDatiSpec.MIGRAZ, tipoEntita,
                    this.sistemaDiMigrazione, this.idStruttura, 0, tmpIstanteCorrente, versione);
            if (rispostaControlli.getCodErr() != null) {
                rispostaControlliAttSpec.setCodErr(rispostaControlli.getCodErr());
                rispostaControlliAttSpec.setDsErr(rispostaControlli.getDsErr());
                rispostaControlliAttSpec.setrBoolean(false);
                return rispostaControlliAttSpec;
            }

            if (rispostaControlli.getrString() == null || rispostaControlli.getrString().length() == 0) {
                // errore, perché non ha trovato il validatore dei dati specifici per il tipo
                // doc e versione indicato
                // cerco se per caso quella versione di dati spec esiste ma è disattivata
                RispostaControlli rc = controlliSemantici.checkPresenzaVersioneDatiSpec(TipiUsoDatiSpec.MIGRAZ,
                        tipoEntita, this.sistemaDiMigrazione, this.idStruttura, 0, versione);
                if (rc.getCodErr() != null) {
                    rispostaControlliAttSpec.setCodErr(rispostaControlli.getCodErr());
                    rispostaControlliAttSpec.setDsErr(rispostaControlli.getDsErr());
                    rispostaControlliAttSpec.setrBoolean(false);
                    return rispostaControlliAttSpec;
                }
                if (rc.isrBoolean()) {
                    // la versione è disattivata
                    rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPECM_001_004);
                    rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_004,
                            tipoEntita.descrivi(), desElemento, versione, desTipoElemento));
                    rispostaControlliAttSpec.setrBoolean(false);
                } else {
                    // Errore nella verifica dei dati specifici.
                    // Non sono ammissibili dati per il tipo elemento e la versione indicati.
                    rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPECM_001_001);
                    rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_001_001,
                            tipoEntita.descrivi(), desElemento, versione, desTipoElemento));
                    rispostaControlliAttSpec.setrBoolean(false);
                }
                return rispostaControlliAttSpec;
            }

            tmpIdVersioneXsd = rispostaControlli.getrLong();

            // compilazione schema
            // 1. Lookup a factory for the W3C XML Schema language
            tmpSchemaFactoryValidazSpec = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // anche in questo caso l'eccezione non deve mai verificarsi, a meno di non aver
            // caricato
            // nel database un xsd danneggiato...
            try {
                // 2. Compile the schema.
                tmpSchemaValidazSpec = tmpSchemaFactoryValidazSpec
                        .newSchema(new StreamSource(new StringReader(rispostaControlli.getrString())));
            } catch (SAXException e) {
                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "UpdGestioneDatiSpec.parseDatiSpecMig.il validatore xsd specifico è errato: "
                                + e.getMessage()));
                rispostaControlliAttSpec.setrBoolean(false);
                log.error("Errore interno: il validatore xsd  di migrazione è errato. Eccezione: "
                        + e.getLocalizedMessage());
                return rispostaControlliAttSpec;
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
                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPECM_003_001);
                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_003_001,
                        tipoEntita.descrivi(), desElemento, events.getMessage()));
                rispostaControlliAttSpec.setrBoolean(false);
                return rispostaControlliAttSpec;
            } catch (Exception e) {
                // anche in questo caso l'eccezione non deve mai verificarsi
                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "UpdGestioneDatiSpec.parseDatiSpecMig.problemi nella validazione dei dati spec: "
                                + e.getMessage()));
                rispostaControlliAttSpec.setrBoolean(false);
                log.error("Errore interno: problemi nella validazione dei dati spec. di migrazione Eccezione: ", e);
                return rispostaControlliAttSpec;
            }

            // leggi la tabella dei dati spec attesi
            rispostaControlliAttSpec = controlliSemantici.checkDatiSpecifici(TipiUsoDatiSpec.MIGRAZ, tipoEntita,
                    this.sistemaDiMigrazione, this.idStruttura, 0, tmpIdVersioneXsd);
            if (rispostaControlliAttSpec.getCodErr() != null) {
                rispostaControlliAttSpec.setrBoolean(false);
                return rispostaControlliAttSpec;
            }

            // impostazione dell'Id dell'Xsd
            rispostaControlliAttSpec.setIdRecXsdDatiSpec(tmpIdVersioneXsd);

            // estrazione dati nella collection dei dati spec attesi
            for (Element element : datiSpecifici.getAny()) {
                String tmpChiave = element.getLocalName();
                tmpAttSpecAtteso = rispostaControlliAttSpec.getDatiSpecifici().get(tmpChiave);
                if (tmpAttSpecAtteso != null) {
                    if (tmpAttSpecAtteso.getValore() == null) {
                        if (element.getFirstChild() != null) {
                            String tmpValore = element.getFirstChild().getNodeValue();
                            if (tmpValore.length() <= UpdCostanti.MAXLEN_DATOSPEC) {
                                tmpAttSpecAtteso.setValore(tmpValore);
                            } else {
                                // "Errore nei dati specifici: uno o più valori superano
                                // la lunghezza di " + MAXLEN_DATOSPEC + " caratteri"
                                rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPECM_002_001);
                                rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(
                                        MessaggiWSBundle.DATISPECM_002_001, tipoEntita.descrivi(), desElemento,
                                        tmpChiave, UpdCostanti.MAXLEN_DATOSPEC));
                                rispostaControlliAttSpec.setrBoolean(false);
                                return rispostaControlliAttSpec;
                            }
                        } else {
                            tmpAttSpecAtteso.setValore(null);
                        }
                    } else {
                        // "Errore nei dati specifici: uno o più dati risultano duplicati");
                        rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.DATISPECM_002_002);
                        rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_002_002,
                                tipoEntita.descrivi(), desElemento, tmpChiave));
                        rispostaControlliAttSpec.setrBoolean(false);
                        return rispostaControlliAttSpec;
                    }
                } else {
                    rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
                    rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "GestioneDatiSpec.parseDatiSpecMig.i dati specifici attesi"
                                    + " non coincidono con l'XSD "));
                    rispostaControlliAttSpec.setrBoolean(false);
                    log.error("Errore interno: i dati specifici attesi di migrazione non coincidono con l'XSD");
                    return rispostaControlliAttSpec;
                }
            }
        } else {
            /*
             * nota questo è un problema, perché questo controllo deve essere già stato fatto nella classe
             * VerificaVersione
             */
            rispostaControlliAttSpec.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlliAttSpec.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "UpdGestioneDatiSpec.parseDatiSpecMig. Presenti dati specifici di migrazione "
                            + "e sistema migrante non specificato "));
            rispostaControlliAttSpec.setrBoolean(false);
            log.error("Errore interno: Presenti dati specifici di migrazione e sistema migrante non specificato");
            return rispostaControlliAttSpec;
        }

        rispostaControlliAttSpec.setrBoolean(true);
        return rispostaControlliAttSpec;
    }

}
