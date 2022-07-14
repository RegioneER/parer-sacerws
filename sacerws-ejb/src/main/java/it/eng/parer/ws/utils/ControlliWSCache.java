/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecControlloWs;
import it.eng.parer.ws.versamentoUpd.dto.ControlloWSResp;

/**
 *
 * @author sinatti_s
 */
@Startup
@Singleton(mappedName = "ControlliWSCache")
public class ControlliWSCache {

    private static Logger log = LoggerFactory.getLogger(ControlliWSCache.class);

    @EJB
    ControlliWSHelper controlliWSHelper;

    Map<String, ControlloWSResp> controlliMap;

    @PostConstruct
    public void initSingleton() {
        log.info("Inizializzazione singleton ControlliWSCache...");
        try {

            List<DecControlloWs> list = controlliWSHelper.caricaListaControlli();
            controlliMap = new HashMap<>();
            for (DecControlloWs cnt : list) {
                // String cdCategoria, String cdControllo, String cdFamiglia, String dsControllo, BigDecimal niOrd
                controlliMap.put(cnt.getCdControlloWs(),
                        new ControlloWSResp(cnt.getCdCategoriaControllo(), cnt.getCdControlloWs(),
                                cnt.getCdFamigliaControllo(), cnt.getDsControlloWs(), cnt.getNiOrdControllo()));
            }
        } catch (RuntimeException ex) {
            // log.fatal("Inizializzazione singleton ControlliWSCache fallita! ", ex);
            log.error("Inizializzazione singleton ControlliWSCache fallita! ", ex);
            throw ex;
        }
        log.info("Inizializzazione singleton ControlliWSCache... completata.");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getString(String cdControllo) {
        return StringEscapeUtils.unescapeJava(controlliMap.get(cdControllo).toString());
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getString(String cdControllo, Object... params) {
        return StringEscapeUtils.unescapeJava(MessageFormat.format(controlliMap.get(controlliMap).toString(), params));
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public ControlloWSResp getControllo(String cdControlloWs) {
        return controlliMap.get(cdControlloWs);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<ControlloWSResp> getControlli() {
        return controlliMap.values().stream().collect(Collectors.toList());
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<ControlloWSResp> getControlliByCdFamiglia(String cdFamiglia) {
        return getControlli().stream().filter(c -> c.getCdFamiglia().equalsIgnoreCase(cdFamiglia))
                .collect(Collectors.toList());
    }

}
