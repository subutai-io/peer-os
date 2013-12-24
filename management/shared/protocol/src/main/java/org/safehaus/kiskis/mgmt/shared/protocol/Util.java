/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.Collection;

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
}
