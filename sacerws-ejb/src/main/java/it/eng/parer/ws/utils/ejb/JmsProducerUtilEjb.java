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

    private final static Logger log = LoggerFactory.getLogger(JmsProducerUtilEjb.class);

    public RispostaControlli inviaMessaggioInFormatoJson(ConnectionFactory connectionFactory, Queue queue,
            Object objectToSerializeInJson, String tipoPayload) {
        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(false);

        MessageProducer messageProducer = null;
        TextMessage textMessage = null;
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            messageProducer = session.createProducer(queue);
            textMessage = session.createTextMessage();
            // app selector
            textMessage.setStringProperty(Costanti.JMSMsgProperties.MSG_K_APP, Constants.SACERWS);
            textMessage.setStringProperty("tipoPayload", tipoPayload);
            ObjectMapper jsonMapper = new ObjectMapper();
            textMessage.setText(jsonMapper.writeValueAsString(objectToSerializeInJson));
            log.debug(String.format("JmsProducer [JSON] %s", textMessage.getText()));
            messageProducer.send(textMessage);
            log.debug("JmsProducer messaggio inviato");
            tmpRispostaControlli.setrBoolean(true);
        } catch (JMSException ex) {
            tmpRispostaControlli.setCodErr("ERR");
            tmpRispostaControlli.setDsErr("Errore nell'invio del messaggio in coda: " + ex.getMessage());
            log.error("Errore nell'invio del messaggio in coda: ", ex);
            tmpRispostaControlli.setrBoolean(false);
        } catch (JsonProcessingException ex) {
            tmpRispostaControlli.setCodErr("ERR");
            tmpRispostaControlli
                    .setDsErr("Errore nella serializzazione in JSON del messaggio per la coda: " + ex.getMessage());
            log.error("Errore nella serializzazione in JSON del messaggio per la coda: ", ex);
            tmpRispostaControlli.setrBoolean(false);
        } finally {
            try {
                if (messageProducer != null) {
                    messageProducer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException ex) {
                tmpRispostaControlli.setCodErr("ERR");
                tmpRispostaControlli
                        .setDsErr("Errore (trappato) JMS durante la chiusura delle risorse: " + ex.getMessage());
                log.error("Errore (trappato) JMS durante la chiusura delle risorse: ", ex);
                tmpRispostaControlli.setrBoolean(false);
            }
        }

        return tmpRispostaControlli;
    }
}
