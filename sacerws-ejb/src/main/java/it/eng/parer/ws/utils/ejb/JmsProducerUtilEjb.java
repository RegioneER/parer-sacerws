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
package it.eng.parer.ws.utils.ejb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.parer.util.Constants;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author DiLorenzo_F
 */
@Stateless(mappedName = "JmsProducerUtilEjb")
@LocalBean
public class JmsProducerUtilEjb {

    private static final Logger log = LoggerFactory.getLogger(JmsProducerUtilEjb.class);

    public RispostaControlli inviaMessaggioInFormatoJson(ConnectionFactory connectionFactory, Queue queue,
            Object objectToSerializeInJson, String tipoPayload) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try (Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
                MessageProducer messageProducer = session.createProducer(queue)) {
            TextMessage textMessage = session.createTextMessage();
            // app selector
            textMessage.setStringProperty(Costanti.JMSMsgProperties.MSG_K_APP, Constants.SACERWS);
            textMessage.setStringProperty("tipoPayload", tipoPayload);
            ObjectMapper jsonMapper = new ObjectMapper();
            textMessage.setText(jsonMapper.writeValueAsString(objectToSerializeInJson));
            log.debug("JmsProducer [JSON] {}", textMessage.getText());
            messageProducer.send(textMessage);
            log.debug("JmsProducer messaggio inviato");
            tmpRispostaControlli.setrBoolean(true);
        } catch (JMSException ex) {
            tmpRispostaControlli.setCodErr("ERR");
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append(String.format("Errore nell'invio del messaggio in coda: ")).append(ex.getMessage());
            log.error(errorMessage.toString(), ex);
            tmpRispostaControlli.setDsErr(errorMessage.toString());
            tmpRispostaControlli.setrBoolean(false);

            // Gestione degli errori sulle close()
            for (Throwable suppressed : ex.getSuppressed()) {
                log.error("Errore (trappato) JMS durante la chiusura delle risorse: ", suppressed);
                errorMessage.append("\nErrore (trappato) JMS durante la chiusura delle risorse: ")
                        .append(suppressed.getMessage());
                tmpRispostaControlli.setDsErr(errorMessage.toString());
                tmpRispostaControlli.setrBoolean(false);
            }
        } catch (JsonProcessingException ex) {
            tmpRispostaControlli.setCodErr("ERR");
            tmpRispostaControlli
                    .setDsErr("Errore nella serializzazione in JSON del messaggio per la coda: " + ex.getMessage());
            log.error("Errore nella serializzazione in JSON del messaggio per la coda: ", ex);
            tmpRispostaControlli.setrBoolean(false);
        }
        return tmpRispostaControlli;
    }

    // MAC#27513
    public RispostaControlli manageMessageGroupingInFormatoJson(ConnectionFactory connectionFactory, Queue queue,
            Object objectToSerializeInJson, String tipoPayload, String groupId) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        try (Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
                MessageProducer messageProducer = session.createProducer(queue)) {
            TextMessage textMessage = session.createTextMessage();
            textMessage.setStringProperty("JMSXGroupID", groupId);
            // app selector
            textMessage.setStringProperty(Costanti.JMSMsgProperties.MSG_K_APP, Constants.SACERWS);
            textMessage.setStringProperty("tipoPayload", tipoPayload);
            ObjectMapper jsonMapper = new ObjectMapper();
            textMessage.setText(jsonMapper.writeValueAsString(objectToSerializeInJson));
            log.debug(String.format("JmsProducer [JSON] %s", textMessage.getText()));
            messageProducer.send(textMessage);
            log.debug(String.format("JmsProducer messaggio inviato con groupId %s", groupId));
            tmpRispostaControlli.setrBoolean(true);
        } catch (JMSException ex) {
            tmpRispostaControlli.setCodErr("ERR");
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append(String.format("Errore nell'invio del messaggio con groupId %s in coda: ", groupId))
                    .append(ex.getMessage());
            log.error(errorMessage.toString(), ex);
            tmpRispostaControlli.setDsErr(errorMessage.toString());
            tmpRispostaControlli.setrBoolean(false);

            // Gestione degli errori sulle close()
            for (Throwable suppressed : ex.getSuppressed()) {
                log.error("Errore (trappato) JMS durante la chiusura delle risorse: ", suppressed);
                errorMessage.append("\nErrore (trappato) JMS durante la chiusura delle risorse: ")
                        .append(suppressed.getMessage());
                tmpRispostaControlli.setDsErr(errorMessage.toString());
                tmpRispostaControlli.setrBoolean(false);
            }

        } catch (JsonProcessingException ex) {
            tmpRispostaControlli.setCodErr("ERR");
            tmpRispostaControlli
                    .setDsErr("Errore nella serializzazione in JSON del messaggio per la coda: " + ex.getMessage());
            log.error("Errore nella serializzazione in JSON del messaggio per la coda: ", ex);
            tmpRispostaControlli.setrBoolean(false);
        }

        return tmpRispostaControlli;
    }
    // MAC#27513

}
