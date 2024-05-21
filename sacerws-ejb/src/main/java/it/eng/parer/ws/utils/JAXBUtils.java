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

package it.eng.parer.ws.utils;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sinatti_s
 */
public class JAXBUtils {

    private JAXBUtils() {
        throw new IllegalStateException("Utility class");
    }

    // standard di estrazione valore stringa di un elemento opzionale con
    // nillable=true
    // default null if black or null or not present
    public static String getStringValFromJAXBElement(JAXBElement<String> node) {
        return node != null && !node.isNil() && StringUtils.isNotBlank(node.getValue()) ? node.getValue() : null;

    }

}
