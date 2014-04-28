/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.accumulo;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dilshat
 */
public class Config {

    public static final String PRODUCT_KEY = "Accumulo";
    private String clusterName = "";
    private Agent masterNode;
    private Agent gcNode;
    private Set<Agent> tracers;
    private Set<Agent> monitors;
    private Set<Agent> loggers;
    private Set<Agent> tabletServers;

    public Set<Agent> getAllNodes() {
        Set<Agent> allNodes = new HashSet<Agent>();

        allNodes.add(masterNode);
        allNodes.add(gcNode);
        allNodes.addAll(tracers);
        allNodes.addAll(monitors);
        allNodes.addAll(loggers);
        allNodes.addAll(tabletServers);

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

    public Set<Agent> getTracers() {
        return tracers;
    }

    public void setTracers(Set<Agent> tracers) {
        this.tracers = tracers;
    }

    public Set<Agent> getMonitors() {
        return monitors;
    }

    public void setMonitors(Set<Agent> monitors) {
        this.monitors = monitors;
    }

    public Set<Agent> getLoggers() {
        return loggers;
    }

    public void setLoggers(Set<Agent> loggers) {
        this.loggers = loggers;
    }

    public Set<Agent> getTabletServers() {
        return tabletServers;
    }

    public void setTabletServers(Set<Agent> tabletServers) {
        this.tabletServers = tabletServers;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void reset() {
        clusterName = "";
        masterNode = null;
        gcNode = null;
        tracers = null;
        monitors = null;
        loggers = null;
        tabletServers = null;
    }

    @Override
    public String toString() {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", masterNode=" + masterNode +
                ", gcNode=" + gcNode +
                ", tracers=" + tracers +
                ", monitors=" + monitors +
                ", loggers=" + loggers +
                ", tabletServers=" + tabletServers +
                '}';
    }
}
