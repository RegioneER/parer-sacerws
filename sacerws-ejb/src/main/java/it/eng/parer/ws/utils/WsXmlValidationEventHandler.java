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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * classe che implementa un event handler per l'unmarshaller di JAXB. Questo handler provoca l'interruzione della
 * verifica e dell'unmarshall al primo errore, che viene scritto nella variabile 'messaggio'
 *
 * @author fioravanti_f
 */
public class WsXmlValidationEventHandler implements ValidationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(WsXmlValidationEventHandler.class);

    String messaggio = null;

    public String getMessaggio() {
        return messaggio;
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {

        log.debug("\nErrore di validazione JAXB");
        log.debug("SEVERITY:  {}", event.getSeverity());
        log.debug("MESSAGE:  {}", event.getMessage());
        log.debug("LINKED EXCEPTION: {0}", event.getLinkedException());
        log.debug("LOCATOR");
        log.debug("    LINE NUMBER:  {}", event.getLocator().getLineNumber());
        log.debug("    COLUMN NUMBER:  {}", event.getLocator().getColumnNumber());
        log.debug("    OFFSET:  {}", event.getLocator().getOffset());
        log.debug("    OBJECT:  {}", event.getLocator().getObject());
        log.debug("    NODE:  {}", event.getLocator().getNode());
        log.debug("    URL:  {}", event.getLocator().getURL());

        messaggio = event.getMessage();

        return false;
    }

}
