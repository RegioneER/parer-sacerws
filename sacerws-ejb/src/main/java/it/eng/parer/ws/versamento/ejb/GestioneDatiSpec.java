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
package it.eng.parer.ws.versamento.ejb;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
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
import java.util.logging.Level;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

@Stateless(mappedName = "GestioneDatiSpec")
@LocalBean
public class GestioneDatiSpec {

    private static final Logger log = LoggerFactory.getLogger(GestioneDatiSpec.class);
    // stateless ejb per i controlli sul db
    @EJB
    ControlliSemantici controlliSemantici;
    // singleton ejb di gestione cache dei parser Castor
    @EJB
    XmlVersCache xmlVersCache;
    //
    private static final int MAXLEN_DATOSPEC = 4000;

    public RispostaControlliAttSpec parseDatiSpec(TipiEntitaSacer tipoEntita,
            JAXBElement<DatiSpecificiType> datiSpecificiElement, long idTipoElemento, String desElemento,
            String desTipoElemento, String sistemaMig, long idOrgStrutt, IRispostaWS rispostaWs) {

        DatiSpecificiType datiSpecifici;
        DatoSpecifico tmpAttSpecAtteso;
        SchemaFactory tmpSchemaFactoryValidazSpec = null;
        Schema tmpSchemaValidazSpec = null;
        String versione;
        long tmpIdVersioneXsd;
        Date tmpIstanteCorrente = new Date();

        RispostaControlliAttSpec rispostaControlliAttSpec = new RispostaControlliAttSpec();

        if (datiSpecificiElement == null || datiSpecificiElement.isNil()) {
            // verifica che esistano dati specifici attesi per idTipoEntita + tipoentita alla data corrente
            RispostaControlli rispostaControlli = controlliSemantici.checkPresenzaDatiSpec(TipiUsoDatiSpec.VERS,
                    tipoEntita, sistemaMig, idOrgStrutt, idTipoElemento, tmpIstanteCorrente);
            if (rispostaControlli.getCodErr() != null) {
                this.setRispostaError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr(), rispostaWs,
                        rispostaControlliAttSpec);
                return rispostaControlliAttSpec;
            }
            if (rispostaControlli.isrBoolean()) {
                // errore, perché non ha inserito il tag datispecifici e questo era richiesto
                this.setRispostaError(
                        MessaggiWSBundle.DATISPEC_001_002, MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_002,
                                tipoEntita.descrivi(), desElemento, desTipoElemento),
                        rispostaWs, rispostaControlliAttSpec);
                return rispostaControlliAttSpec;
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
            RispostaControlli rispostaControlli = controlliSemantici.checkXSDDatiSpec(TipiUsoDatiSpec.VERS, tipoEntita,
                    sistemaMig, idOrgStrutt, idTipoElemento, tmpIstanteCorrente, versione);
            if (rispostaControlli.getCodErr() != null) {
                this.setRispostaError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr(), rispostaWs,
                        rispostaControlliAttSpec);
                return rispostaControlliAttSpec;
            }

            if (rispostaControlli.getrString() == null || rispostaControlli.getrString().length() == 0) {
                // errore, perché non ha trovato il validatore dei dati specifici per il tipo doc e versione indicato
                // cerco se per caso quella versione di dati spec esiste ma è disattivata
                RispostaControlli rc = controlliSemantici.checkPresenzaVersioneDatiSpec(TipiUsoDatiSpec.VERS,
                        tipoEntita, sistemaMig, idOrgStrutt, idTipoElemento, versione);
                if (rc.getCodErr() != null) {
                    this.setRispostaError(rc.getCodErr(), rc.getDsErr(), rispostaWs, rispostaControlliAttSpec);
                    return rispostaControlliAttSpec;
                }
                if (rc.isrBoolean()) {
                    // la versione è disattivata
                    this.setRispostaError(MessaggiWSBundle.DATISPEC_001_004,
                            MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_004, tipoEntita.descrivi(),
                                    desElemento, versione, desTipoElemento),
                            rispostaWs, rispostaControlliAttSpec);
                } else {
                    // Errore nella verifica dei dati specifici.
                    // Non sono ammissibili dati per il tipo elemento e la versione indicati.
                    this.setRispostaError(MessaggiWSBundle.DATISPEC_001_001,
                            MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_001, tipoEntita.descrivi(),
                                    desElemento, versione, desTipoElemento),
                            rispostaWs, rispostaControlliAttSpec);
                }
                return rispostaControlliAttSpec;
            }

            tmpIdVersioneXsd = rispostaControlli.getrLong();

            // compilazione schema
            // 1. Lookup a factory for the W3C XML Schema language
            tmpSchemaFactoryValidazSpec = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                tmpSchemaFactoryValidazSpec.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                tmpSchemaFactoryValidazSpec.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            } catch (SAXException ex) {
                java.util.logging.Logger.getLogger(GestioneDatiSpec.class.getName()).log(Level.SEVERE, null, ex);
            }
            // anche in questo caso l'eccezione non deve mai verificarsi, a meno di non aver caricato
            // nel database un xsd danneggiato...
            try {
                // 2. Compile the schema.
                tmpSchemaValidazSpec = tmpSchemaFactoryValidazSpec
                        .newSchema(new StreamSource(new StringReader(rispostaControlli.getrString())));
            } catch (SAXException e) {
                this.setRispostaError(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "GestioneDatiSpec.parseDatiSpec.il validatore xsd specifico è errato: " + e.getMessage()),
                        rispostaWs, rispostaControlliAttSpec);
                log.error("Errore interno: il validatore xsd specifico è errato. Eccezione: {}",
                        e.getLocalizedMessage());
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
                this.setRispostaError(
                        MessaggiWSBundle.DATISPEC_003_001, MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_003_001,
                                tipoEntita.descrivi(), desElemento, events.getMessage()),
                        rispostaWs, rispostaControlliAttSpec);
                return rispostaControlliAttSpec;
            } catch (Exception e) {
                // anche in questo caso l'eccezione non deve mai verificarsi
                this.setRispostaError(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "GestioneDatiSpec.parseDatiSpec.problemi nella validazione dei dati spec: " + e.getMessage()),
                        rispostaWs, rispostaControlliAttSpec);
                log.error("Errore interno: problemi nella validazione dei dati spec. Eccezione: ", e);
                return rispostaControlliAttSpec;
            }

            // leggi la tabella dei dati spec attesi
            rispostaControlliAttSpec = controlliSemantici.checkDatiSpecifici(TipiUsoDatiSpec.VERS, tipoEntita,
                    sistemaMig, idOrgStrutt, idTipoElemento, tmpIdVersioneXsd);
            if (rispostaControlliAttSpec.getCodErr() != null) {
                this.setRispostaError(rispostaControlliAttSpec.getCodErr(), rispostaControlliAttSpec.getDsErr(),
                        rispostaWs, rispostaControlliAttSpec);
                return rispostaControlliAttSpec;
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
                                                    tipoEntita.descrivi(), desElemento, tmpChiave, MAXLEN_DATOSPEC),
                                            rispostaWs, rispostaControlliAttSpec);
                                    return rispostaControlliAttSpec;
                                }
                            } else {
                                tmpAttSpecAtteso.setValore(null);
                            }
                        } else {
                            // "Errore nei dati specifici: uno o più dati risultano duplicati");
                            this.setRispostaError(MessaggiWSBundle.DATISPEC_002_002,
                                    MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_002_002, tipoEntita.descrivi(),
                                            desElemento, tmpChiave),
                                    rispostaWs, rispostaControlliAttSpec);
                            return rispostaControlliAttSpec;
                        }
                    } else {
                        // "I dati specifici attesi non coincidono con l'XSD");
                        // MAC#29906 - Correzione della mancata gestione dell'errore per dati specifici che non
                        // coincidono con l'XSD
                        this.setRispostaError(MessaggiWSBundle.DATISPEC_003_002,
                                MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_003_002, tipoEntita.descrivi(),
                                        desElemento, tmpChiave),
                                rispostaWs, rispostaControlliAttSpec);
                        /*
                         * this.setRispostaError(MessaggiWSBundle.ERR_666, MessaggiWSBundle
                         * .getString(MessaggiWSBundle.ERR_666,
                         * "GestioneDatiSpec.parseDatiSpec.i dati specifici attesi " + "non coincidono con l'XSD "),
                         * rispostaWs, rispostaControlliAttSpec);
                         */
                        log.error("Errore interno: i dati specifici attesi non coincidono con l'XSD", rispostaWs,
                                rispostaControlliAttSpec);
                        return rispostaControlliAttSpec;
                    }
                }
            }
        }

        return rispostaControlliAttSpec;
    }

    // dati di migrazione, non c'è l'id tipo ma si valuta il sistema migrante
    public RispostaControlliAttSpec parseDatiSpecMig(TipiEntitaSacer tipoEntita,
            JAXBElement<DatiSpecificiType> datiSpecificiElement, String desElemento, String desTipoElemento,
            String sistemaMig, long idOrgStrutt, IRispostaWS rispostaWs) {
        DatiSpecificiType datiSpecifici;
        DatoSpecifico tmpAttSpecAtteso;
        SchemaFactory tmpSchemaFactoryValidazSpec = null;
        Schema tmpSchemaValidazSpec = null;
        String versione;
        long tmpIdVersioneXsd;
        boolean tmpSistMigDef = true;
        Date tmpIstanteCorrente = new Date();

        RispostaControlliAttSpec rispostaControlliAttSpec = new RispostaControlliAttSpec();

        // verifica che il sistema migrante sia definito
        if (sistemaMig == null || sistemaMig.isEmpty()) {
            tmpSistMigDef = false;
        }

        if (datiSpecificiElement == null || datiSpecificiElement.isNil()) {
            if (tmpSistMigDef) {
                // verifica che esistano dati specifici attesi per sistema migrante + tipoentita alla data corrente
                RispostaControlli rispostaControlli = controlliSemantici.checkPresenzaDatiSpec(TipiUsoDatiSpec.MIGRAZ,
                        tipoEntita, sistemaMig, idOrgStrutt, 0, tmpIstanteCorrente);
                if (rispostaControlli.getCodErr() != null) {
                    this.setRispostaError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr(), rispostaWs,
                            rispostaControlliAttSpec);
                    return rispostaControlliAttSpec;
                }
                if (rispostaControlli.isrBoolean()) {
                    // errore, perché non ha inserito il tag datispecifici e questo era richiesto
                    this.setRispostaError(MessaggiWSBundle.DATISPECM_001_002,
                            MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_001_002, tipoEntita.descrivi(),
                                    desElemento, desTipoElemento),
                            rispostaWs, rispostaControlliAttSpec);
                    return rispostaControlliAttSpec;
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
                RispostaControlli rispostaControlli = controlliSemantici.checkXSDDatiSpec(TipiUsoDatiSpec.MIGRAZ,
                        tipoEntita, sistemaMig, idOrgStrutt, 0, tmpIstanteCorrente, versione);
                if (rispostaControlli.getCodErr() != null) {
                    this.setRispostaError(rispostaControlli.getCodErr(), rispostaControlli.getDsErr(), rispostaWs,
                            rispostaControlliAttSpec);
                    return rispostaControlliAttSpec;
                }

                if (rispostaControlli.getrString() == null || rispostaControlli.getrString().length() == 0) {
                    // errore, perché non ha trovato il validatore dei dati specifici per il tipo doc e versione
                    // indicato
                    // cerco se per caso quella versione di dati spec esiste ma è disattivata
                    RispostaControlli rc = controlliSemantici.checkPresenzaVersioneDatiSpec(TipiUsoDatiSpec.MIGRAZ,
                            tipoEntita, sistemaMig, idOrgStrutt, 0, versione);
                    if (rc.getCodErr() != null) {
                        this.setRispostaError(rc.getCodErr(), rc.getDsErr(), rispostaWs, rispostaControlliAttSpec);
                        return rispostaControlliAttSpec;
                    }
                    if (rc.isrBoolean()) {
                        // la versione è disattivata
                        this.setRispostaError(MessaggiWSBundle.DATISPECM_001_004,
                                MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_001_004, tipoEntita.descrivi(),
                                        desElemento, versione, desTipoElemento),
                                rispostaWs, rispostaControlliAttSpec);
                    } else {
                        // Errore nella verifica dei dati specifici.
                        // Non sono ammissibili dati per il tipo elemento e la versione indicati.
                        this.setRispostaError(MessaggiWSBundle.DATISPECM_001_001,
                                MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_001_001, tipoEntita.descrivi(),
                                        desElemento, versione, desTipoElemento),
                                rispostaWs, rispostaControlliAttSpec);
                    }
                    return rispostaControlliAttSpec;
                }

                tmpIdVersioneXsd = rispostaControlli.getrLong();

                // compilazione schema
                // 1. Lookup a factory for the W3C XML Schema language
                tmpSchemaFactoryValidazSpec = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                try {
                    tmpSchemaFactoryValidazSpec.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                    tmpSchemaFactoryValidazSpec.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                } catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
                    java.util.logging.Logger.getLogger(GestioneDatiSpec.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                                            + e.getMessage()),
                            rispostaWs, rispostaControlliAttSpec);
                    log.error("Errore interno: il validatore xsd  di migrazione è errato. Eccezione: {}",
                            e.getLocalizedMessage());
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
                    this.setRispostaError(MessaggiWSBundle.DATISPECM_003_001,
                            MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_003_001, tipoEntita.descrivi(),
                                    desElemento, events.getMessage()),
                            rispostaWs, rispostaControlliAttSpec);
                    return rispostaControlliAttSpec;
                } catch (Exception e) {
                    // anche in questo caso l'eccezione non deve mai verificarsi
                    this.setRispostaError(MessaggiWSBundle.ERR_666,
                            MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                                    "GestioneDatiSpec.parseDatiSpecMig.problemi nella validazione dei dati spec: "
                                            + e.getMessage()),
                            rispostaWs, rispostaControlliAttSpec);
                    log.error("Errore interno: problemi nella validazione dei dati spec. di migrazione Eccezione: ", e);
                    return rispostaControlliAttSpec;
                }

                // leggi la tabella dei dati spec attesi
                rispostaControlliAttSpec = controlliSemantici.checkDatiSpecifici(TipiUsoDatiSpec.MIGRAZ, tipoEntita,
                        sistemaMig, idOrgStrutt, 0, tmpIdVersioneXsd);
                if (rispostaControlliAttSpec.getCodErr() != null) {
                    this.setRispostaError(rispostaControlliAttSpec.getCodErr(), rispostaControlliAttSpec.getDsErr(),
                            rispostaWs, rispostaControlliAttSpec);
                    return rispostaControlliAttSpec;
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
                                                        tipoEntita.descrivi(), desElemento, tmpChiave, MAXLEN_DATOSPEC),
                                                rispostaWs, rispostaControlliAttSpec);
                                        return rispostaControlliAttSpec;
                                    }
                                } else {
                                    tmpAttSpecAtteso.setValore(null);
                                }
                            } else {
                                // "Errore nei dati specifici: uno o più dati risultano duplicati");
                                this.setRispostaError(MessaggiWSBundle.DATISPECM_002_002,
                                        MessaggiWSBundle.getString(MessaggiWSBundle.DATISPECM_002_002,
                                                tipoEntita.descrivi(), desElemento, tmpChiave),
                                        rispostaWs, rispostaControlliAttSpec);
                                return rispostaControlliAttSpec;
                            }
                        } else {
                            // this.setRispostaError(MessaggiWSBundle.ERR_666,
                            // MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            // "GestioneDatiSpec.parseDatiSpecMig.i dati specifici attesi"
                            // + " non coincidono con l'XSD "),
                            // rispostaWs, rispostaControlliAttSpec);
                            // "I dati specifici attesi non coincidono con l'XSD");
                            // MAC#29906 - Correzione della mancata gestione dell'errore per dati specifici che non
                            // coincidono con l'XSD
                            this.setRispostaError(MessaggiWSBundle.DATISPEC_003_002,
                                    MessaggiWSBundle.getString(MessaggiWSBundle.DATISPEC_003_002, tipoEntita.descrivi(),
                                            desElemento, tmpChiave),
                                    rispostaWs, rispostaControlliAttSpec);

                            log.error("Errore interno: i dati specifici attesi di migrazione non coincidono con l'XSD");
                            return rispostaControlliAttSpec;
                        }
                    }
                }
            } else {
                /*
                 * nota questo è un problema, perché questo controllo deve essere già stato fatto nella classe
                 * VerificaVersione
                 */
                this.setRispostaError(MessaggiWSBundle.ERR_666,
                        MessaggiWSBundle
                                .getString(MessaggiWSBundle.ERR_666,
                                        "GestioneDatiSpec.parseDatiSpecMig. Presenti dati specifici di migrazione "
                                                + "e sistema migrante non specificato "),
                        rispostaWs, rispostaControlliAttSpec);
                log.error("Errore interno: Presenti dati specifici di migrazione e sistema migrante non specificato");
                return rispostaControlliAttSpec;
            }
        }

        return rispostaControlliAttSpec;
    }

    private void setRispostaError(String codErr, String dsErr, IRispostaWS rispostaWs,
            RispostaControlliAttSpec rispostaControlliAttSpec) {
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        rispostaWs.setErrorCode(codErr);
        rispostaWs.setErrorMessage(dsErr);
        rispostaControlliAttSpec.setrBoolean(false);
        rispostaControlliAttSpec.setCodErr(codErr);
        rispostaControlliAttSpec.setDsErr(dsErr);
    }
}
