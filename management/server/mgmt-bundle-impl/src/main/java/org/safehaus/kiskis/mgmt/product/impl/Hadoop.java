/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.product.impl;

import java.util.Arrays;
import java.util.List;
import org.safehaus.kiskis.mgmt.product.Product;

/**
 *
 * @author dilshat
 */
public class Hadoop implements Product {

    public String getName() {
        return "Hadoop Product";
    }

    public String getDescription() {
        return "Hadoop Description";
    }

    public String getContextPath() {
        return "/hadoop";
    }

    public List<String> getCommands() {
        return Arrays.asList("{\"hadoop\":{\"type\":\"install hadoop\"}}");
    }
}
