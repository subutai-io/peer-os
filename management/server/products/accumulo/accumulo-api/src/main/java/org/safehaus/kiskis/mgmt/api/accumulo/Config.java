/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.accumulo;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ConfigBase;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dilshat
 */
public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Accumulo";
    private String clusterName = "";
    private Agent masterNode;
    private Agent gcNode;
    private Agent monitor;
    private Set<Agent> tracers;
    private Set<Agent> slaves;

    public Set<Agent> getAllNodes() {
        Set<Agent> allNodes = new HashSet<Agent>();

        allNodes.add(masterNode);
        allNodes.add(gcNode);
        allNodes.add(monitor);
        allNodes.addAll(tracers);
        allNodes.addAll(slaves);

        return allNodes;
    }

    public Agent getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(Agent masterNode) {
        this.masterNode = masterNode;
    }

    public Agent getGcNode() {
        return gcNode;
    }

    public void setGcNode(Agent gcNode) {
        this.gcNode = gcNode;
    }

    public Agent getMonitor() {
        return monitor;
    }

    public void setMonitor(Agent monitor) {
        this.monitor = monitor;
    }

    public Set<Agent> getTracers() {
        return tracers;
    }

    public void setTracers(Set<Agent> tracers) {
        this.tracers = tracers;
    }

    public Set<Agent> getSlaves() {
        return slaves;
    }

    public void setSlaves(Set<Agent> slaves) {
        this.slaves = slaves;
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


    public void reset() {
        clusterName = "";
        masterNode = null;
        gcNode = null;
        monitor = null;
        tracers = null;
        slaves = null;
    }

    @Override
    public String toString() {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", masterNode=" + masterNode +
                ", gcNode=" + gcNode +
                ", monitor=" + monitor +
                ", tracers=" + tracers +
                ", slaves=" + slaves +
                '}';
    }
}
