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

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;
import it.eng.parer.ws.xml.versReq.DatiSpecificiType;
import it.eng.parer.ws.xml.versReq.UnitaDocAggAllegati;
import it.eng.parer.ws.xml.versReq.UnitaDocumentaria;
import it.eng.parer.ws.xml.versReqMultiMedia.IndiceMM;
import it.eng.parer.ws.xml.versResp.EsitoVersAggAllegati;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;
import it.eng.parer.ws.xml.versResp.RapportoVersamento;
import it.eng.parerxml.xsd.FileXSD;
import it.eng.parerxml.xsd.FileXSDUtil;

/**
 * Contains the JAXBContext for versamento ws
 *
 * @author Moretti_Lu
 */
@Singleton
@LocalBean
@Startup
public class XmlVersCache {

    private static final Logger log = LoggerFactory.getLogger(XmlVersCache.class);

    // <editor-fold defaultstate="collapsed" desc="Context versamento request">
    JAXBContext versReqCtx_UD;
    JAXBContext versReqCtx_DatiSpecifici;
    JAXBContext versReqCtx_UDAggAllegati;

    JAXBContext versReqMMCtx_IndiceMM;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Context versamento response">
    JAXBContext versRespCtx_RappVersamento;
    JAXBContext versRespCtx_EsitoVersamento;
    JAXBContext versRespCtx_EsitoVersamentoAggAllegati;
    // </editor-fold>

    Schema versReqSchema;
    Schema versRespSchema;

    @PostConstruct
    protected void initSingleton() {
        log.info("Inizializzazione singleton XMLVersContext...");

        try {
            versReqCtx_UD = JAXBContext.newInstance(UnitaDocumentaria.class);
            versReqCtx_DatiSpecifici = JAXBContext.newInstance(DatiSpecificiType.class);
            versReqCtx_UDAggAllegati = JAXBContext.newInstance(UnitaDocAggAllegati.class);
            versReqMMCtx_IndiceMM = JAXBContext.newInstance(IndiceMM.class);

            versRespCtx_RappVersamento = JAXBContext.newInstance(RapportoVersamento.class);
            versRespCtx_EsitoVersamento = JAXBContext.newInstance(EsitoVersamento.class);
            versRespCtx_EsitoVersamentoAggAllegati = JAXBContext
                    .newInstance(EsitoVersAggAllegati.class);

            SchemaFactory schemaFctry = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFctry.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            schemaFctry.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            versReqSchema = schemaFctry.newSchema(FileXSDUtil.getURLFileXSD(FileXSD.VERS_REQ_XSD));
            versRespSchema = schemaFctry
                    .newSchema(FileXSDUtil.getURLFileXSD(FileXSD.VERS_RESP_XSD));

            log.info("Inizializzazione singleton XMLVersContext... completata.");
        } catch (JAXBException | SAXException ex) {
            throw new SacerWsRuntimeException(ex, SacerWsErrorCategory.INTERNAL_ERROR);
        }
    }

    // Returns JAXBContext of Request
    @Lock(LockType.READ)
    public JAXBContext getVersReqCtxforUD() {
        return versReqCtx_UD;
    }

    @Lock(LockType.READ)
    public JAXBContext getVersReqCtxforDatiSpecifici() {
        return versReqCtx_DatiSpecifici;
    }

    @Lock(LockType.READ)
    public JAXBContext getVersReqCtxforUDAggAllegati() {
        return versReqCtx_UDAggAllegati;
    }

    @Lock(LockType.READ)
    public JAXBContext getVersReqMMCtxforIndiceMM() {
        return versReqMMCtx_IndiceMM;
    }

    // Returns JAXBContext of Response
    @Lock(LockType.READ)
    public JAXBContext getVersRespCtxforRapportoVersamento() {
        return versRespCtx_RappVersamento;
    }

    @Lock(LockType.READ)
    public JAXBContext getVersRespCtxforEsitoVersamento() {
        return versRespCtx_EsitoVersamento;
    }

    @Lock(LockType.READ)
    public JAXBContext getVersRespCtxforEsitoVersamentoAggAllegati() {
        return versRespCtx_EsitoVersamentoAggAllegati;
    }

    // Returns Schema
    @Lock(LockType.READ)
    public Schema getSchemaOfVersReq() {
        return versReqSchema;
    }

    @Lock(LockType.READ)
    public Schema getSchemaOfVersResp() {
        return versRespSchema;
    }
}
