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
            versReqCtx_UDAggiornamento = JAXBContext
                    .newInstance(IndiceSIPAggiornamentoUnitaDocumentaria.class);

            //
            versRespCtx_EsitoAggiornamentoVersamento = JAXBContext
                    .newInstance(EsitoAggiornamento.class);

            //
            versReqCtx_DatiSpecifici = JAXBContext.newInstance(DatiSpecificiType.class);

            //
            SchemaFactory schemaFctry = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFctry.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            schemaFctry.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            aggVersReqSchema = schemaFctry
                    .newSchema(FileXSDUtil.getURLFileXSD(FileXSD.AGG_VERS_REQ_XSD));
            aggVersRespSchema = schemaFctry
                    .newSchema(FileXSDUtil.getURLFileXSD(FileXSD.AGG_VERS_RESP_XSD));

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
