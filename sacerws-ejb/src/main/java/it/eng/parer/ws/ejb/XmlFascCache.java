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
package it.eng.parer.ws.ejb;

import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.ws.xml.versfascicolo.IndiceSIPFascicolo;
import it.eng.parer.ws.xml.versfascicoloresp.EsitoVersamentoFascicolo;

/**
 *
 * @author fioravanti_f
 */
@Singleton
@LocalBean
@Startup
public class XmlFascCache {

    private static final Logger log = LoggerFactory.getLogger(XmlFascCache.class);

    private final String URL_SCHEMA_REQUEST_FASCICOLO = "/it/eng/parer/ws/xml/versfascicolo/WSRequestIndiceSIPFascicolo.xsd";

    //
    JAXBContext versReqFascicoloCtx;
    JAXBContext versRespFascicoloCtx;

    Schema versReqFascicoloSchema;

    @PostConstruct
    protected void initSingleton() {
	InputStream tmpInputStream = null;
	try {
	    log.info("Inizializzazione singleton XmlFascCache...");

	    versReqFascicoloCtx = JAXBContext.newInstance(IndiceSIPFascicolo.class);

	    versRespFascicoloCtx = JAXBContext.newInstance(EsitoVersamentoFascicolo.class);

	    ClassLoader tmpClassLoader = getClass().getClassLoader();
	    tmpInputStream = tmpClassLoader.getResourceAsStream(URL_SCHEMA_REQUEST_FASCICOLO);
	    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
	    sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
	    versReqFascicoloSchema = sf.newSchema(new StreamSource(tmpInputStream));
	    log.info("Inizializzazione singleton XmlFascCache... completata.");
	} catch (Exception ex) {
	    log.error("Inizializzazione singleton XmlFascCache fallita! ", ex);
	    throw new RuntimeException(ex);
	} finally {
	    IOUtils.closeQuietly(tmpInputStream);
	}
    }

    @Lock(LockType.READ)
    public JAXBContext getVersReqFascicoloCtx() {
	return versReqFascicoloCtx;
    }

    @Lock(LockType.READ)
    public JAXBContext getVersRespFascicoloCtx() {
	return versRespFascicoloCtx;
    }

    @Lock(LockType.READ)
    public Schema getVersReqFascicoloSchema() {
	return versReqFascicoloSchema;
    }

}
