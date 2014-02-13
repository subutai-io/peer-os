/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class MongoClusterInfo implements Serializable {

    public static final String TABLE_NAME = "mongo_cluster_info",
            CLUSTER_NAME = "cluster_name",
            REPLICA_SET_NAME = "replica_set_name",
            CONFIG_SERVERS_NAME = "config_servers",
            ROUTERS_NAME = "routers",
            DATA_NODES_NAME = "data_nodes";

    private String clusterName;
    private String replicaSetName;
    private List<UUID> configServers;
    private List<UUID> routers;
    private List<UUID> dataNodes;

    public MongoClusterInfo() {
    }

    public MongoClusterInfo(String clusterName, String replicaSetName,
            Set<Agent> configServerAgents, Set<Agent> routerAgents, Set<Agent> dataNodeAgents) {
        this.clusterName = clusterName;
        this.replicaSetName = replicaSetName;

        configServers = new ArrayList<UUID>();
        for (Agent configAgent : configServerAgents) {
            configServers.add(configAgent.getUuid());
        }
        routers = new ArrayList<UUID>();
        for (Agent routerAgent : routerAgents) {
            routers.add(routerAgent.getUuid());
        }
        dataNodes = new ArrayList<UUID>();
        for (Agent dataNodeAgent : dataNodeAgents) {
            dataNodes.add(dataNodeAgent.getUuid());
        }
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public void setReplicaSetName(String replicaSetName) {
        this.replicaSetName = replicaSetName;
    }

    public List<UUID> getConfigServers() {
        return configServers;
    }

    public void setConfigServers(List<UUID> configServers) {
        this.configServers = configServers;
    }

    public List<UUID> getRouters() {
        return routers;
    }

    public void setRouters(List<UUID> routers) {
        this.routers = routers;
    }

    public List<UUID> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(List<UUID> shards) {
        this.dataNodes = shards;
    }

    @Override
    public String toString() {
        return "MongoClusterInfo{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", configServers=" + configServers + ", routers=" + routers + ", dataNodes=" + dataNodes + '}';
    }

}
