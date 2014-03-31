/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.mahout;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class Config {

    public static final String PRODUCT_KEY = "Mahout";
    private String clusterName = "";

    private Set<Agent> nodes;

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

}
