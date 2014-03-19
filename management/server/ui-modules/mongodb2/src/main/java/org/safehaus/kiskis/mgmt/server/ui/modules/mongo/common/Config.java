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
public class Config {

    private String clusterName;
    private String replicaSetName;
    private String domainName;
    private int numberOfConfigServers;
    private int numberOfRouters;
    private int numberOfDataNodes;
    private int cfgSrvPort;
    private int routerPort;
    private int dataNodePort;

    private Set<Agent> configServers;
    private Set<Agent> routerServers;
    private Set<Agent> dataNodes;

    public Config() {
        reset();
    }

    public final void reset() {
        configServers = null;
        routerServers = null;
        dataNodes = null;
        clusterName = "";
        replicaSetName = "repl";
        domainName = "intra.lan";
        numberOfConfigServers = 3;
        numberOfRouters = 2;
        numberOfDataNodes = 3;
        cfgSrvPort = 27019;
        routerPort = 27018;
        dataNodePort = 27017;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public int getCfgSrvPort() {
        return cfgSrvPort;
    }

    public void setCfgSrvPort(int cfgSrvPort) {
        this.cfgSrvPort = cfgSrvPort;
    }

    public int getRouterPort() {
        return routerPort;
    }

    public void setRouterPort(int routerPort) {
        this.routerPort = routerPort;
    }

    public int getDataNodePort() {
        return dataNodePort;
    }

    public void setDataNodePort(int dataNodePort) {
        this.dataNodePort = dataNodePort;
    }

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

    @Override
    public String toString() {
        return "ClusterConfig{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", domainName=" + domainName + ", numberOfConfigServers=" + numberOfConfigServers + ", numberOfRouters=" + numberOfRouters + ", numberOfDataNodes=" + numberOfDataNodes + ", cfgSrvPort=" + cfgSrvPort + ", routerPort=" + routerPort + ", dataNodePort=" + dataNodePort + ", configServers=" + configServers + ", routerServers=" + routerServers + ", dataNodes=" + dataNodes + '}';
    }

}
