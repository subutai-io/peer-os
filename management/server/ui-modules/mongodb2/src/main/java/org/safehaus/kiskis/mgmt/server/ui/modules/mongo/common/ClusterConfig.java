/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class ClusterConfig {

    private String clusterName = "";
    private String replicaSetName = "";
    private int numberOfConfigServers = 3;
    private int numberOfRouters = 2;
    private int numberOfDataNodes = 3;

    private Set<Agent> configServers;
    private Set<Agent> routerServers;
    private Set<Agent> dataNodes;
    private Set<Agent> selectedAgents;

    public int getNumberOfConfigServers() {
        return numberOfConfigServers;
    }

    public void setNumberOfConfigServers(int numberOfConfigServers) {
        this.numberOfConfigServers = numberOfConfigServers;
    }

    public int getNumberOfRouters() {
        return numberOfRouters;
    }

    public void setNumberOfRouters(int numberOfRouters) {
        this.numberOfRouters = numberOfRouters;
    }

    public int getNumberOfDataNodes() {
        return numberOfDataNodes;
    }

    public void setNumberOfDataNodes(int numberOfDataNodes) {
        this.numberOfDataNodes = numberOfDataNodes;
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public void setReplicaSetName(String replicaSetName) {
        this.replicaSetName = replicaSetName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Set<Agent> getConfigServers() {
        return configServers;
    }

    public void setConfigServers(Set<Agent> configServers) {
        this.configServers = configServers;
    }

    public Set<Agent> getRouterServers() {
        return routerServers;
    }

    public void setRouterServers(Set<Agent> routerServers) {
        this.routerServers = routerServers;
    }

    public Set<Agent> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(Set<Agent> dataNodes) {
        this.dataNodes = dataNodes;
    }

    public Set<Agent> getSelectedAgents() {
        return selectedAgents;
    }

    public void setSelectedAgents(Set<Agent> selectedAgents) {
        this.selectedAgents = selectedAgents;
    }

    @Override
    public String toString() {
        return "ClusterConfig{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", numberOfConfigServers=" + numberOfConfigServers + ", numberOfRouters=" + numberOfRouters + ", numberOfDataNodes=" + numberOfDataNodes + ", configServers=" + configServers + ", routerServers=" + routerServers + ", dataNodes=" + dataNodes + ", selectedAgents=" + selectedAgents + '}';
    }

    public void reset() {
        clusterName = "";
        replicaSetName = "";
        configServers = null;
        routerServers = null;
        dataNodes = null;
        selectedAgents = null;
        numberOfConfigServers = 3;
        numberOfRouters = 2;
        numberOfDataNodes = 3;
    }

}
