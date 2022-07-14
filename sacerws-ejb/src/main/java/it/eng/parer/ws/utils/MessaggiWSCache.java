/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import it.eng.parer.entity.DecErrSacer;
import it.eng.parer.exception.SacerWsRuntimeException;
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fioravanti_f
 */
@Startup
@Singleton(mappedName = "MessaggiWSCache")
public class MessaggiWSCache {

    private static Logger log = LoggerFactory.getLogger(MessaggiWSCache.class);

    @EJB
    MessaggiWSHelper messaggiWSHelper;

    Map<String, String> errorMap;

    @PostConstruct
    public void initSingleton() {
        log.info("Inizializzazione singleton MessaggiWSCache...");
        try {

            List<DecErrSacer> list = messaggiWSHelper.caricaListaErrori();
            errorMap = new HashMap<>();
            for (DecErrSacer err : list) {
                errorMap.put(err.getCdErr(), err.getDsErr());
            }
        } catch (RuntimeException ex) {
            throw new SacerWsRuntimeException(ex, SacerWsErrorCategory.INTERNAL_ERROR);
        }
        log.info("Inizializzazione singleton MessaggiWSCache... completata.");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getString(String key) {
        return StringEscapeUtils.unescapeJava(errorMap.get(key));
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getString(String key, Object... params) {
        return StringEscapeUtils.unescapeJava(MessageFormat.format(errorMap.get(key), params));
    }

}
