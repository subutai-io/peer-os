/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.spark;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class Config {

    public static final String PRODUCT_KEY = "Spark";
    private String clusterName = "";

    private Agent masterNode;
    private Set<Agent> nodes;

    public Agent getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(Agent masterNode) {
        this.masterNode = masterNode;
    }

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

    public Set<Agent> getAllNodes() {
        Set<Agent> allNodes = new HashSet<Agent>();
        if (nodes != null) {
            allNodes.addAll(nodes);
        }
        if (masterNode != null) {
            allNodes.add(masterNode);
        }

        return allNodes;
    }

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", masterNode=" + masterNode + ", nodes=" + nodes + '}';
    }

}
