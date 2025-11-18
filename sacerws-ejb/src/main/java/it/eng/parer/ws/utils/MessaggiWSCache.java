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
package it.eng.parer.ws.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecErrSacer;
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;

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
                errorMap.put(err.getCdErr(), StringEscapeUtils.unescapeJava(err.getDsErr())); // Unescape
                // java-like
                // every
                // message
            }
        } catch (RuntimeException ex) {
            throw new SacerWsRuntimeException(ex, SacerWsErrorCategory.INTERNAL_ERROR);
        }
        log.info("Inizializzazione singleton MessaggiWSCache... completata.");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getString(String key) {
        return errorMap.get(key);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getString(String key, Object... params) {
        return MessageFormat.format(errorMap.get(key), cleanTextContent(params));
    }

    /*
     * Clean up placeholder (from XML)
     */
    private Object[] cleanTextContent(Object... params) {
        List<Object> cleanedTxtParams = new ArrayList<>();
        Stream.of(params).filter(Objects::nonNull).forEach(param -> {
            String sparam = param.toString(); // force toString()
            // erases all the ASCII control characters
            sparam = sparam.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "?");
            // removes non-printable characters from Unicode
            sparam = sparam.replaceAll("\\p{C}", "?");
            // unescape XML of sparam
            sparam = StringEscapeUtils.unescapeXml(sparam);
            // final result
            cleanedTxtParams.add(sparam);
        });
        return cleanedTxtParams.toArray(new Object[0]);
    }

}
