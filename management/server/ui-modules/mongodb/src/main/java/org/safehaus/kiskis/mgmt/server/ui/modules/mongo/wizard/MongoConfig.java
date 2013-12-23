/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import java.util.ArrayList;
import java.util.List;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class MongoConfig {
    
    private String clusterName = "";
    private String replicaSetName = "";
    private List<Agent> configServers = new ArrayList<Agent>();
    private List<Agent> routerServers = new ArrayList<Agent>();
    private List<Agent> shards = new ArrayList<Agent>();
    
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
    
    public List<Agent> getConfigServers() {
        return configServers;
    }
    
    public void setConfigServers(List<Agent> configServers) {
        if (configServers != null) {
            this.configServers = configServers;
        }
    }
    
    public List<Agent> getRouterServers() {
        return routerServers;
    }
    
    public void setRouterServers(List<Agent> routerServers) {
        if (routerServers != null) {
            this.routerServers = routerServers;
        }
    }
    
    public List<Agent> getShards() {
        return shards;
    }
    
    public void setShards(List<Agent> shards) {
        if (shards != null) {
            this.shards = shards;
        }
    }
    
    @Override
    public String toString() {
        return "MongoConfig{" + "clusterName=" + clusterName + ", configServers=" + configServers + ", routerServers=" + routerServers + ", shards=" + shards + '}';
    }
    
}
