package it.eng.parer.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Classe di utilità per la generica conversione delle date.
 *
 *
 * @author Snidero_L
 */
public class DateUtilsConverter {

    private DateUtilsConverter() {
        throw new IllegalStateException(
                "Classe di utility per aiutarti con la conversione delle date dopo l'introduzione di java.time.*");
    }

    /**
     * Conversione della data espressa con il fuso orario in data locale.
     *
     * @param zdt
     *            data in input
     *
     * @return oggetto java.util.Date oppure null
     */
    public static Date convert(ZonedDateTime zdt) {
        if (zdt != null) {
            return Date.from(zdt.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * Conversione della data locale ad una data con fuso orario.
     *
     * @param dt
     *            data in input
     *
     * @return oggetto java.time.ZonedDateTime oppure null
     */
    public static ZonedDateTime convert(Date dt) {
        if (dt != null) {
            return ZonedDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault());
        }
        return null;
    }

    /**
     * Conversione in stringa della data con il formato {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME}. Questo formato è
     * completo e non contiene caratteri potenzialmente "pericolosi" per, ad esempio, i metadati dell'object storage.
     *
     * @param dt
     *            data in input
     *
     * @return Data correttamente formatta o null.
     */
    public static String format(ZonedDateTime dt) {
        if (dt != null) {
            return dt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        return null;
    }

}
