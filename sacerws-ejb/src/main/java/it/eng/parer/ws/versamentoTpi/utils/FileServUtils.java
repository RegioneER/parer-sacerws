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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoTpi.utils;

import it.eng.parer.ws.utils.KeySizeUtility;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fioravanti_f
 */
public class FileServUtils {

    private static final Logger log = LoggerFactory.getLogger(FileServUtils.class);
    private static String POSIX_SET = "^[A-Za-z0-9_][A-Za-z0-9\\. _-]*$";
    private static long MAX_DISK_FILL = 85;

    public boolean controllaSpazioLibero(String partitionName, long bytesNeeded) {
        File file = new File(partitionName);

        log.debug("*************** controllaSpazioLibero ");

        log.debug("Partition name {}", partitionName);
        log.debug("Bytes needed {}", bytesNeeded);

        double rawTotalSpace = file.getTotalSpace(); // total disk space in bytes.
        double rawUsableSpace = file.getUsableSpace(); // /usable space in bytes,
        double rawFreeSpace = file.getFreeSpace(); // raw free disk space in bytes.

        log.debug("getTotalSpace {}", rawTotalSpace);
        log.debug("getUsableSpace  {}", rawUsableSpace);
        log.debug("getFreeSpace {}", rawFreeSpace);

        double usableSpace = rawFreeSpace < rawUsableSpace ? rawFreeSpace : rawUsableSpace;
        double usedPerc = 100d - (usableSpace / rawTotalSpace * 100d);

        log.debug("usableSpace {}", usableSpace);
        log.debug("usedPerc {}", usedPerc);

        return (bytesNeeded < usableSpace && usedPerc < MAX_DISK_FILL);
    }

    public boolean controllaSubPath(String subpath) {
        return (subpath.matches(POSIX_SET));
    }

    public boolean controllaPath(String path) {
        return (path.length() <= KeySizeUtility.MAX_LEN_FILENAME_ARK);
    }
}
