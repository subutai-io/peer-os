/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.product.impl;

import java.util.HashMap;
import java.util.Map;
import org.safehaus.kiskis.mgmt.product.Product;

/**
 *
 * @author dilshat
 */
public class Hadoop implements Product {

    private Map<String, String> commands = new HashMap<String, String>();

    public Hadoop() {
        commands.put("install", "{\"command\":{\"type\":\"EXECUTE_REQUEST\",\"source\":\"@source@\"\"uuid\":\"@uuid@\",\"requestSequenceNumber\":@reqNo@,\"workingDirectory\":\"/home\",\"program\":\"/usr/bin/dpkg\",\"stdOut\":\"RETURN\",\"stdErr\":\"RETURN\",\"runAs\":\"root\",\"args\":[\"-i\",\"hadoop-package.deb\"],\"timeout\":600}}");
    }

    public String getName() {
        return "Hadoop Product";
    }

    public String getDescription() {
        return "Hadoop Description";
    }

    public String getContextPath() {
        return "/hadoop";
    }

    public Map<String, String> getCommands() {
        return commands;
    }
}
