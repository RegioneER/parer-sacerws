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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sinatti_s
 */
public class CollectionUtility {

    private CollectionUtility() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> boolean hasDuplicate(Iterable<T> all) {
        Set<T> set = new HashSet<>();
        // Set#add returns false if the set does not change, which
        // indicates that a duplicate element has been added.
        for (T each : all)
            if (!set.add(each)) {
                return true;
            }
        return false;
    }

    public static <T> Set<T> findDuplicates(Iterable<T> all) {
        final Set<T> setToReturn = new HashSet<>();
        final Set<T> set1 = new HashSet<>();

        for (T el : all) {
            if (!set1.add(el)) {
                setToReturn.add(el);
            }
        }
        return setToReturn;
    }

}
