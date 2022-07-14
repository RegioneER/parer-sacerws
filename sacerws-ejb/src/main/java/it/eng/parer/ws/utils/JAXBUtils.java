package it.eng.parer.ws.utils;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sinatti_s
 */
public class JAXBUtils {

    // standard di estrazione valore stringa di un elemento opzionale con nillable=true
    // default null if black or null or not present
    public static String getStringValFromJAXBElement(JAXBElement<String> node) {
        return node != null && !node.isNil() && StringUtils.isNotBlank(node.getValue()) ? node.getValue() : null;

    }

}
