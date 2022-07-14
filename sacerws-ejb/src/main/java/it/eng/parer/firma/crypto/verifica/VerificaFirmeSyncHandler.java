/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.crypto.verifica;

import it.eng.parer.firma.crypto.helper.VerificaFirmeSyncHelper;
import it.eng.spagoLite.security.auth.AuthenticationHandlerConstants;
import static it.eng.spagoLite.security.auth.AuthenticationHandlerConstants.QNAME_WSSE_HEADER;
import static it.eng.spagoLite.security.auth.AuthenticationHandlerConstants.WSSE_XSD_URI;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handler per il WS SOAP di Verifica Firme Sync
 */
@Stateless
public class VerificaFirmeSyncHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger log = LoggerFactory.getLogger(VerificaFirmeSyncHandler.class);

    @EJB
    private VerificaFirmeSyncHelper helper;

    @Override
    public boolean handleMessage(SOAPMessageContext msgCtx) {
        Boolean outbound = (Boolean) msgCtx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        String ipAddress = "NON_CALCOLATO";
        if (!outbound) {
            Object tmpRequest = msgCtx.get(MessageContext.SERVLET_REQUEST);
            if (tmpRequest != null && tmpRequest instanceof HttpServletRequest) {
                ipAddress = ((HttpServletRequest) tmpRequest).getHeader("X-FORWARDED-FOR");
                if (ipAddress == null || ipAddress.isEmpty()) {
                    ipAddress = ((HttpServletRequest) tmpRequest).getRemoteAddr();
                }
            }
            log.debug("VerificaFirmeSyncHandler attivato. Client IP Address: " + ipAddress);

            try {
                NodeList usernameEl = (NodeList) msgCtx.getMessage().getSOAPHeader()
                        .getElementsByTagNameNS(WSSE_XSD_URI, "Username");
                NodeList passwordEl = (NodeList) msgCtx.getMessage().getSOAPHeader()
                        .getElementsByTagNameNS(WSSE_XSD_URI, "Password");
                Node userNode = null;
                Node passNode = null;
                if (usernameEl != null && passwordEl != null && (userNode = usernameEl.item(0)) != null
                        && (passNode = passwordEl.item(0)) != null) {
                    String username = userNode.getFirstChild().getNodeValue();
                    String password = passNode.getFirstChild().getNodeValue();

                    boolean authorized = helper.checkAuthorization(username, password);

                    if (!authorized) {
                        try {
                            SOAPFactory fac = SOAPFactory.newInstance();
                            SOAPFault sfault = fac.createFault();
                            sfault.setFaultCode("ERR_USER");
                            sfault.setFaultString("Utente mancante o non autorizzato per invocare il servizio");
                            throw new SOAPFaultException(sfault);
                        } catch (SOAPException e1) {
                            log.error("Errore durante la creazione dell'eccezione SOAP", e1);
                            throw new ProtocolException(e1);
                        }
                    }
                    msgCtx.put(AuthenticationHandlerConstants.AUTHN_STAUTS, java.lang.Boolean.TRUE);
                    msgCtx.put(AuthenticationHandlerConstants.USER, username);
                    msgCtx.put(AuthenticationHandlerConstants.PWD, password);
                } else {
                    throw new ProtocolException("Username e password sono obbligatorie");
                }

            } catch (DOMException | SOAPException e) {
                throw new ProtocolException(e);
            }
            msgCtx.setScope(AuthenticationHandlerConstants.AUTHN_STAUTS, MessageContext.Scope.APPLICATION);
            msgCtx.setScope(AuthenticationHandlerConstants.USER, MessageContext.Scope.APPLICATION);
            msgCtx.setScope(AuthenticationHandlerConstants.PWD, MessageContext.Scope.APPLICATION);
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        HashSet<QName> headers = new HashSet<QName>();
        headers.add(QNAME_WSSE_HEADER);
        return headers;
    }

}
