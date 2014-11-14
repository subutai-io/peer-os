/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;


/**
 * Holds a single mongo cluster configuration settings
 */
public class MongoClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "MongoDB";
    public static final String PRODUCT_NAME = "mongo";
    private String templateName = PRODUCT_NAME;
    private String clusterName = "";
    private String replicaSetName = "repl";
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    private int numberOfConfigServers = 3;
    private int numberOfRouters = 2;
    private int numberOfDataNodes = 3;
    private int cfgSrvPort = 27019;
    private int routerPort = 27018;
    private int dataNodePort = 27017;

    private Set<MongoConfigNode> configServers;
    private Set<MongoRouterNode> routerServers;
    private Set<MongoDataNode> dataNodes;
    private UUID environmentId;


    public Set<MongoNode> getAllNodes()
    {
        Set<MongoNode> nodes = new HashSet<>();
        if ( configServers != null )
        {
            nodes.addAll( configServers );
        }
        if ( dataNodes != null )
        {
            nodes.addAll( dataNodes );
        }
        if ( routerServers != null )
        {
            nodes.addAll( routerServers );
        }

        return nodes;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( String domainName )
    {
        this.domainName = domainName;
    }


    public int getNumberOfConfigServers()
    {
        return numberOfConfigServers;
    }


    public void setNumberOfConfigServers( int numberOfConfigServers )
    {
        this.numberOfConfigServers = numberOfConfigServers;
    }


    public int getNumberOfRouters()
    {
        return numberOfRouters;
    }


    public void setNumberOfRouters( int numberOfRouters )
    {
        this.numberOfRouters = numberOfRouters;
    }


    public int getNumberOfDataNodes()
    {
        return numberOfDataNodes;
    }


    public void setNumberOfDataNodes( int numberOfDataNodes )
    {
        this.numberOfDataNodes = numberOfDataNodes;
    }


    public String getReplicaSetName()
    {
        return replicaSetName;
    }


    public void setReplicaSetName( String replicaSetName )
    {
        this.replicaSetName = replicaSetName;
    }


    public String getClusterName()
    {
        return clusterName;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    @Override
    public String getProductName()
    {
        return PRODUCT_NAME;
    }


    @Override
    public String getProductKey()
    {
        return PRODUCT_KEY;
    }


    public NodeType getNodeType( Agent node )
    {
        NodeType nodeType = null;

        if ( getRouterServers().contains( node ) )
        {
            nodeType = NodeType.ROUTER_NODE;
        }
        else if ( getConfigServers().contains( node ) )
        {
            nodeType = NodeType.CONFIG_NODE;
        }
        else if ( getDataNodes().contains( node ) )
        {
            nodeType = NodeType.DATA_NODE;
        }

        return nodeType;
    }


    public Set<MongoRouterNode> getRouterServers()
    {
        return routerServers;
    }


    public void setRouterServers( Set<MongoRouterNode> routerServers )
    {
        this.routerServers = routerServers;
    }


    public Set<MongoConfigNode> getConfigServers()
    {
        return configServers;
    }


    public void setConfigServers( Set<MongoConfigNode> configServers )
    {
        this.configServers = configServers;
    }


    public Set<MongoDataNode> getDataNodes()
    {
        return dataNodes;
    }


    public void setDataNodes( Set<MongoDataNode> dataNodes )
    {
        this.dataNodes = dataNodes;
    }


    public int getNodePort( Agent node )
    {

        if ( getRouterServers().contains( node ) )
        {
            return getRouterPort();
        }
        else if ( getConfigServers().contains( node ) )
        {
            return getCfgSrvPort();
        }

        return getDataNodePort();
    }


    public int getRouterPort()
    {
        return routerPort;
    }


    public void setRouterPort( int routerPort )
    {
        this.routerPort = routerPort;
    }


    public int getCfgSrvPort()
    {
        return cfgSrvPort;
    }


    public void setCfgSrvPort( int cfgSrvPort )
    {
        this.cfgSrvPort = cfgSrvPort;
    }


    public int getDataNodePort()
    {
        return dataNodePort;
    }


    public void setDataNodePort( int dataNodePort )
    {
        this.dataNodePort = dataNodePort;
    }


    public void addNode( final MongoNode host, final NodeType nodeType )
    {
        switch ( nodeType )
        {
            case DATA_NODE:
                getDataNodes().add( ( MongoDataNode ) host );
                setNumberOfDataNodes( getNumberOfDataNodes() + 1 );
                break;
            case ROUTER_NODE:
                getRouterServers().add( ( MongoRouterNode ) host );
                setNumberOfRouters( getNumberOfRouters() + 1 );
                break;
            case CONFIG_NODE:
                getConfigServers().add( ( MongoConfigNode ) host );
                setNumberOfConfigServers( getNumberOfConfigServers() + 1 );
                break;
        }
    }


    @Override
    public String toString()
    {
        return "ClusterConfig{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", domainName="
                + domainName + ", numberOfConfigServers=" + numberOfConfigServers + ", numberOfRouters="
                + numberOfRouters + ", numberOfDataNodes=" + numberOfDataNodes + ", cfgSrvPort=" + cfgSrvPort
                + ", routerPort=" + routerPort + ", dataNodePort=" + dataNodePort + ", configServers=" + configServers
                + ", routerServers=" + routerServers + ", dataNodes=" + dataNodes + '}';
    }
}
