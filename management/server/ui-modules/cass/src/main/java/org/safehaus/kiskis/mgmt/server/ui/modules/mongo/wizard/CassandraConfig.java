/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class CassandraConfig {

    private String clusterName = "";
    private String replicaSetName = "";
    private Set<Agent> configServers = new HashSet<Agent>();
    private Set<Agent> routerServers = new HashSet<Agent>();
    private Set<Agent> shards = new HashSet<Agent>();
    private Set<Agent> selectedAgents = new HashSet<Agent>();

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public void setReplicaSetName(String replicaSetName) {
        if (!Util.isStringEmpty(clusterName)) {
            this.replicaSetName = replicaSetName;
        }
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        if (!Util.isStringEmpty(clusterName)) {
            this.clusterName = clusterName;
        }
    }

    public Set<Agent> getConfigServers() {
        return configServers;
    }

    public void setConfigServers(Set<Agent> configServers) {
        if (configServers != null) {
            this.configServers = configServers;
        }
    }

    public Set<Agent> getRouterServers() {
        return routerServers;
    }

    public void setRouterServers(Set<Agent> routerServers) {
        if (routerServers != null) {
            this.routerServers = routerServers;
        }
    }

    public Set<Agent> getShards() {
        return shards;
    }

    public void setShards(Set<Agent> shards) {
        if (shards != null) {
            this.shards = shards;
        }
    }

    public Set<Agent> getSelectedAgents() {
        return selectedAgents;
    }

    public void setSelectedAgents(Set<Agent> selectedAgents) {
        if (selectedAgents != null) {
            this.selectedAgents = selectedAgents;
        }
    }

    @Override
    public String toString() {
        return "MongoConfig{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", configServers=" + configServers + ", routerServers=" + routerServers + ", shards=" + shards + ", selectedAgents=" + selectedAgents + '}';
    }

}
