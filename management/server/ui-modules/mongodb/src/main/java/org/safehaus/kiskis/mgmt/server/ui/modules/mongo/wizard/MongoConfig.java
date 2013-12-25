/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class MongoConfig {

    private String clusterName = "";
    private String replicaSetName = "";
    private Set<Agent> configServers;
    private Set<Agent> routerServers;
    private Set<Agent> shards;
    private Set<Agent> selectedAgents;

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

    public Set<Agent> getShards() {
        return shards;
    }

    public void setShards(Set<Agent> shards) {
        this.shards = shards;
    }

    public Set<Agent> getSelectedAgents() {
        return selectedAgents;
    }

    public void setSelectedAgents(Set<Agent> selectedAgents) {
        this.selectedAgents = selectedAgents;
    }

    @Override
    public String toString() {
        return "MongoConfig{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", configServers=" + configServers + ", routerServers=" + routerServers + ", shards=" + shards + ", selectedAgents=" + selectedAgents + '}';
    }

    public void reset() {
        clusterName = "";
        replicaSetName = "";
        configServers = null;
        routerServers = null;
        shards = null;
        selectedAgents = null;
    }

}
