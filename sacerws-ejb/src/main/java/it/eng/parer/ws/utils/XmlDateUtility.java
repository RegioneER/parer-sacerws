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

package it.eng.parer.ws.utils;

import static it.eng.parer.util.DateUtilsConverter.convert;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;

/**
 *
 * @author fioravanti_f
 */
public class XmlDateUtility {

    private XmlDateUtility() {
	throw new IllegalStateException("Utility class");
    }

    /**
     *
     * @param date data da convertire
     * @param zone timezone
     *
     * @return restituisce XMLGregorianCalendar
     */
    public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date, TimeZone zone) {
	XMLGregorianCalendar xmlGregorianCalendar = null;
	GregorianCalendar gregorianCalendar = new GregorianCalendar();
	gregorianCalendar.setTime(date);
	gregorianCalendar.setTimeZone(zone);
	try {
	    DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
	    xmlGregorianCalendar = dataTypeFactory.newXMLGregorianCalendar(gregorianCalendar);
	} catch (Exception e) {
	    throw new SacerWsRuntimeException(
		    "Exception in conversion of Date to XMLGregorianCalendar", e,
		    SacerWsErrorCategory.VALIDATION_ERROR);
	}

	return xmlGregorianCalendar;
    }

    /**
     *
     * @param date da convertire
     *
     * @return restituisce XMLGregorianCalendar
     *
     */
    public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) {
	if (date != null) {
	    XMLGregorianCalendar xmlGregorianCalendar = null;
	    GregorianCalendar gregorianCalendar = new GregorianCalendar();
	    gregorianCalendar.setTime(date);
	    try {
		DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
		xmlGregorianCalendar = dataTypeFactory.newXMLGregorianCalendar(gregorianCalendar);
	    } catch (Exception e) {
		throw new SacerWsRuntimeException(
			"Exception in conversion of Date to XMLGregorianCalendar", e,
			SacerWsErrorCategory.VALIDATION_ERROR);
	    }
	    return xmlGregorianCalendar;
	} else {
	    return null;
	}
    }

    public static XMLGregorianCalendar dateToXMLGregorianCalendar(ZonedDateTime zonedDateTime) {
	if (zonedDateTime != null) {
	    XMLGregorianCalendar xmlGregorianCalendar = null;
	    try {
		GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);
		xmlGregorianCalendar = DatatypeFactory.newInstance()
			.newXMLGregorianCalendar(gregorianCalendar);
	    } catch (Exception e) {
		throw new SacerWsRuntimeException(
			"Exception in conversion of Date to XMLGregorianCalendar", e,
			SacerWsErrorCategory.VALIDATION_ERROR);
	    }
	    return xmlGregorianCalendar;
	} else {
	    return null;
	}

    }

    public static Date xmlGregorianCalendarToDate(XMLGregorianCalendar xmlGregorianCalendar) {
	if (xmlGregorianCalendar != null) {
	    ZonedDateTime zdt = xmlGregorianCalendarToZonedDateTime(xmlGregorianCalendar);
	    return convert(zdt);
	} else {
	    return null;
	}
    }

    public static ZonedDateTime xmlGregorianCalendarToZonedDateTime(
	    XMLGregorianCalendar xmlGregorianCalendar) {
	if (xmlGregorianCalendar != null) {
	    return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime();
	} else {
	    return null;
	}
    }

}
