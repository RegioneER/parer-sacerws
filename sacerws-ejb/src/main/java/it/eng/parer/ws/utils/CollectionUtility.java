/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
