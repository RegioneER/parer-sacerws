package it.eng.parer.ws.utils;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;
import java.time.ZonedDateTime;
import static it.eng.parer.util.DateUtilsConverter.convert;

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
     * @param date
     *            data da convertire
     * @param zone
     *            timezone
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
            throw new SacerWsRuntimeException("Exception in conversion of Date to XMLGregorianCalendar", e,
                    SacerWsErrorCategory.VALIDATION_ERROR);
        }

        return xmlGregorianCalendar;
    }

    /**
     *
     * @param date
     *            da convertire
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
                throw new SacerWsRuntimeException("Exception in conversion of Date to XMLGregorianCalendar", e,
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
                xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            } catch (Exception e) {
                throw new SacerWsRuntimeException("Exception in conversion of Date to XMLGregorianCalendar", e,
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

    public static ZonedDateTime xmlGregorianCalendarToZonedDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar != null) {
            return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime();
        } else {
            return null;
        }
    }

}
