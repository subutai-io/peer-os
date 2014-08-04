/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;
import org.safehaus.subutai.shared.protocol.settings.Common;


/**
 * Holds a single mongo cluster configuration settings
 */
public class MongoClusterConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "MongoDB2";
    private String templateName = "mongodb";
    private String clusterName = "";
    private String replicaSetName = "repl";
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    private int numberOfConfigServers = 3;
    private int numberOfRouters = 2;
    private int numberOfDataNodes = 3;
    private int cfgSrvPort = 27019;
    private int routerPort = 27018;
    private int dataNodePort = 27017;

    private Set<Agent> configServers;
    private Set<Agent> routerServers;
    private Set<Agent> dataNodes;


    public Set<Agent> getAllNodes() {
        Set<Agent> nodes = new HashSet<>();
        nodes.addAll( configServers );
        nodes.addAll( dataNodes );
        nodes.addAll( routerServers );

        return nodes;
    }


    public String getTemplateName() {
        return templateName;
    }


    public void setTemplateName( final String templateName ) {
        this.templateName = templateName;
    }


    public String getDomainName() {
        return domainName;
    }


    public void setDomainName( String domainName ) {
        this.domainName = domainName;
    }


    public int getCfgSrvPort() {
        return cfgSrvPort;
    }


    public void setCfgSrvPort( int cfgSrvPort ) {
        this.cfgSrvPort = cfgSrvPort;
    }


    public int getRouterPort() {
        return routerPort;
    }


    public void setRouterPort( int routerPort ) {
        this.routerPort = routerPort;
    }


    public int getDataNodePort() {
        return dataNodePort;
    }


    public void setDataNodePort( int dataNodePort ) {
        this.dataNodePort = dataNodePort;
    }


    public int getNumberOfConfigServers() {
        return numberOfConfigServers;
    }


    public void setNumberOfConfigServers( int numberOfConfigServers ) {
        this.numberOfConfigServers = numberOfConfigServers;
    }


    public int getNumberOfRouters() {
        return numberOfRouters;
    }


    public void setNumberOfRouters( int numberOfRouters ) {
        this.numberOfRouters = numberOfRouters;
    }


    public int getNumberOfDataNodes() {
        return numberOfDataNodes;
    }


    public void setNumberOfDataNodes( int numberOfDataNodes ) {
        this.numberOfDataNodes = numberOfDataNodes;
    }


    public String getReplicaSetName() {
        return replicaSetName;
    }


    public void setReplicaSetName( String replicaSetName ) {
        this.replicaSetName = replicaSetName;
    }


    public String getClusterName() {
        return clusterName;
    }


    public void setClusterName( String clusterName ) {
        this.clusterName = clusterName;
    }


    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }


    public Set<Agent> getConfigServers() {
        return configServers;
    }


    public void setConfigServers( Set<Agent> configServers ) {
        this.configServers = configServers;
    }


    public Set<Agent> getRouterServers() {
        return routerServers;
    }


    public void setRouterServers( Set<Agent> routerServers ) {
        this.routerServers = routerServers;
    }


    public Set<Agent> getDataNodes() {
        return dataNodes;
    }


    public void setDataNodes( Set<Agent> dataNodes ) {
        this.dataNodes = dataNodes;
    }


    public NodeType getNodeType( Agent node ) {
        NodeType nodeType = null;

        if ( getRouterServers().contains( node ) ) {
            nodeType = NodeType.ROUTER_NODE;
        }
        else if ( getConfigServers().contains( node ) ) {
            nodeType = NodeType.CONFIG_NODE;
        }
        else if ( getDataNodes().contains( node ) ) {
            nodeType = NodeType.DATA_NODE;
        }

        return nodeType;
    }


    public int getNodePort( Agent node ) {

        if ( getRouterServers().contains( node ) ) {
            return getRouterPort();
        }
        else if ( getConfigServers().contains( node ) ) {
            return getCfgSrvPort();
        }

        return getDataNodePort();
    }


    @Override
    public String toString() {
        return "ClusterConfig{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", domainName="
                + domainName + ", numberOfConfigServers=" + numberOfConfigServers + ", numberOfRouters="
                + numberOfRouters + ", numberOfDataNodes=" + numberOfDataNodes + ", cfgSrvPort=" + cfgSrvPort
                + ", routerPort=" + routerPort + ", dataNodePort=" + dataNodePort + ", configServers=" + configServers
                + ", routerServers=" + routerServers + ", dataNodes=" + dataNodes + '}';
    }
}
