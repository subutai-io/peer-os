/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author dilshat
 */
public class Util {

    public static boolean isStringEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isCollectionEmpty(Collection col) {
        return col == null || col.isEmpty();
    }

    public static Set retainValues(Set col1, Set col2) {
        if (col1 == null || col2 == null) {
            return null;
        } else {
            col1.retainAll(col2);
            return col1;
        }
    }
}
