/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.api;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.Set;

/**
 * @author dilshat
 */
public class ZookeeperClusterConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Zookeeper2";
    private String clusterName = "";
    private String zkName = "";
    private int numberOfNodes = 3;
    private Set<Agent> nodes;
    private boolean isStandalone;

    public boolean isStandalone() {
        return isStandalone;
    }

    public void setStandalone(boolean isStandalone) {
        this.isStandalone = isStandalone;
    }

    public String getZkName() {
        return zkName;
    }

    public void setZkName(String zkName) {
        this.zkName = zkName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public Set<Agent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Agent> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", zkName='" + zkName + '\'' +
                ", numberOfNodes=" + numberOfNodes +
                ", nodes=" + nodes +
                ", isStandalone=" + isStandalone +
                '}';
    }
}
