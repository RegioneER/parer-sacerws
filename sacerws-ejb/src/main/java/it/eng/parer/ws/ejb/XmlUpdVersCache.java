package it.eng.parer.ws.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import it.eng.parer.ws.xml.versUpdReq.DatiSpecificiType;
import it.eng.parer.ws.xml.versUpdReq.IndiceSIPAggiornamentoUnitaDocumentaria;
import it.eng.parer.ws.xml.versUpdResp.EsitoAggiornamento;
import it.eng.parerxml.xsd.FileXSD;
import it.eng.parerxml.xsd.FileXSDUtil;

/**
 * Contains the JAXBContext for versamento ws
 *
 * @author sinatti_s
 */
@Singleton
@LocalBean
@Startup
public class XmlUpdVersCache {

    private static final Logger log = LoggerFactory.getLogger(XmlUpdVersCache.class);

    // <editor-fold defaultstate="collapsed" desc="Context versamento request">
    JAXBContext versReqCtx_UDAggiornamento;
    JAXBContext versReqCtx_DatiSpecifici;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Context versamento response">
    //
    JAXBContext versRespCtx_EsitoAggiornamentoVersamento;

    // </editor-fold>

    //
    Schema aggVersReqSchema;
    Schema aggVersRespSchema;

    @PostConstruct
    protected void initSingleton() {
        log.info("Inizializzazione singleton XmlUpdVersContext...");

        try {
            //
            versReqCtx_UDAggiornamento = JAXBContext.newInstance(IndiceSIPAggiornamentoUnitaDocumentaria.class);

            //
            versRespCtx_EsitoAggiornamentoVersamento = JAXBContext.newInstance(EsitoAggiornamento.class);

            //
            versReqCtx_DatiSpecifici = JAXBContext.newInstance(DatiSpecificiType.class);

            //
            SchemaFactory schemaFctry = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            aggVersReqSchema = schemaFctry.newSchema(FileXSDUtil.getURLFileXSD(FileXSD.AGG_VERS_REQ_XSD));
            aggVersRespSchema = schemaFctry.newSchema(FileXSDUtil.getURLFileXSD(FileXSD.AGG_VERS_RESP_XSD));

            log.info("Inizializzazione singleton XmlUpdVersContext... completata.");
        } catch (JAXBException | SAXException ex) {
            // log.fatal("Inizializzazione singleton XmlUpdVersContext fallita! ", ex);
            log.error("Inizializzazione singleton XmlUpdVersContext fallita! ", ex);
            throw new RuntimeException(ex);
        }
    }

    // Returns JAXBContext of Request
    @Lock(LockType.READ)
    public JAXBContext getVersReqCtxforAggiornamentoUD() {
        return versReqCtx_UDAggiornamento;
    }

    @Lock(LockType.READ)
    public JAXBContext getVersRespCtxforEsitoAggiornamentoVersamento() {
        return versRespCtx_EsitoAggiornamentoVersamento;
    }

    @Lock(LockType.READ)
    public JAXBContext getVersReqCtxforDatiSpecifici() {
        return versReqCtx_DatiSpecifici;
    }

    @Lock(LockType.READ)
    public Schema getSchemaOfAggVersReq() {
        return aggVersReqSchema;
    }

    @Lock(LockType.READ)
    public Schema getSchemaOfAggVersResp() {
        return aggVersRespSchema;
    }
}