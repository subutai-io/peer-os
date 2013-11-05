/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.product;

import java.util.Map;

/**
 *
 * @author dilshat
 */
public interface Product {

    public String getName();

    public String getDescription();

    public String getContextPath();

    public Map<String, String> getCommands();
}
