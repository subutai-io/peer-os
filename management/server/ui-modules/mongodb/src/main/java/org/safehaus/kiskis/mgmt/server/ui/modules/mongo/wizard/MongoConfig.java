/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import java.util.ArrayList;
import java.util.List;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class MongoConfig {

    private List<Agent> configServers = new ArrayList<Agent>();
    private List<Agent> routerServers = new ArrayList<Agent>();
    private List<Agent> shards = new ArrayList<Agent>();

    public List<Agent> getConfigServers() {
        return configServers;
    }

    public void setConfigServers(List<Agent> configServers) {
        this.configServers = configServers;
    }

    public List<Agent> getRouterServers() {
        return routerServers;
    }

    public void setRouterServers(List<Agent> routerServers) {
        this.routerServers = routerServers;
    }

    public List<Agent> getShards() {
        return shards;
    }

    public void setShards(List<Agent> shards) {
        this.shards = shards;
    }

}
