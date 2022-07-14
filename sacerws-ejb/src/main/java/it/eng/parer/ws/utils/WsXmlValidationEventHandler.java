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
