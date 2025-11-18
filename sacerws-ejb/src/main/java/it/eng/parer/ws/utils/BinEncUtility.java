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

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;

/**
 *
 * @author fioravanti_f
 */
public class BinEncUtility {

    private static final Logger logger = LoggerFactory.getLogger(BinEncUtility.class);

    private BinEncUtility() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isBase64String(String utf8str) {
        return Base64.isBase64(utf8str);
    }

    public static String encodeUTF8Base64String(byte[] barray) {
        if (barray != null && barray.length > 0) {
            try {
                return new String(Base64.encodeBase64(barray), StandardCharsets.UTF_8.name());
            } catch (Exception ex) {
                /* critical error */
                throw new SacerWsRuntimeException(ex, SacerWsErrorCategory.INTERNAL_ERROR);
            }
        } else {
            return StringUtils.EMPTY;
        }
    }

    public static byte[] decodeUTF8Base64String(String utf8str) {
        try {
            return Base64.decodeBase64(utf8str.getBytes(StandardCharsets.UTF_8.name()));
        } catch (Exception ex) {
            /* managed error */
            logger.warn("Encoding UTF-8 non supportato", ex);
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public static boolean isHexString(String utf8str) {
        return (utf8str.length() > 1 && utf8str.length() % 2 == 0)
                && utf8str.matches("^[0-9a-fA-F]+$");
    }

    public static String encodeUTF8HexString(byte[] barray) {
        if (barray != null && barray.length > 0) {
            return Hex.encodeHexString(barray);
        } else {
            return StringUtils.EMPTY;
        }
    }

    public static byte[] decodeUTF8HexString(String hexstr) {
        try {
            return Hex.decodeHex(hexstr.toCharArray());
        } catch (Exception ex) {
            /* managed error */
            logger.warn("La stringa non rappresenta un numero in base 16", ex);
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

}
