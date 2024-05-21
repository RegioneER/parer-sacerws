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

import java.text.MessageFormat;
import java.util.HashMap;
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
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;
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
            throw new SacerWsRuntimeException(ex, SacerWsErrorCategory.INTERNAL_ERROR);
        }
        log.info("Inizializzazione singleton ControlliWSCache... completata.");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getString(String cdControllo) {
        return StringEscapeUtils.unescapeJava(controlliMap.get(cdControllo).toString());
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getString(String cdControllo, Object... params) {
        return StringEscapeUtils.unescapeJava(MessageFormat.format(controlliMap.get(cdControllo).toString(), params));
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
