/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.lucene;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ConfigBase;

import java.util.Set;

/**
 * @author dilshat
 */
public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Lucene";

    private String clusterName = "";

    private Set<Agent> nodes;

    @Override
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Set<Agent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Agent> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", nodes=" + nodes + '}';
    }

    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }
}
