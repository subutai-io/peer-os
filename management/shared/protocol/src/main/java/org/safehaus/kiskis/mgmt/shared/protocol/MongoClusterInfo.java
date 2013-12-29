/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public class MongoClusterInfo {

    public static final String TABLE_NAME = "mongo_cluster_info",
            CLUSTER_NAME = "cluster_name",
            REPLICA_SET_NAME = "replica_set_name",
            CONFIG_SERVERS_NAME = "config_servers",
            ROUTERS_NAME = "routers",
            SHARDS_NAME = "shards";

    private String clusterName;
    private String replicaSetName;
    private List<UUID> configServers;
    private List<UUID> routers;
    private List<UUID> shards;

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

    public List<UUID> getShards() {
        return shards;
    }

    public void setShards(List<UUID> shards) {
        this.shards = shards;
    }

    @Override
    public String toString() {
        return "MongoClusterInfo{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", configServers=" + configServers + ", routers=" + routers + ", shards=" + shards + '}';
    }

}
