/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;

import com.google.gson.annotations.Expose;

//import org.safehaus.subutai.common.protocol.Agent;


/**
 * Holds a single mongo cluster configuration settings
 */
public class MongoClusterConfigImpl implements MongoClusterConfig
{
    @Expose
    public static final String PRODUCT_KEY = "MongoDB";

    @Expose
    public static final String PRODUCT_NAME = "mongo";

    @Expose
    private String templateName = PRODUCT_NAME;

    @Expose
    private String clusterName = "";
    @Expose
    private String replicaSetName = "repl";
    @Expose
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    @Expose
    private int numberOfConfigServers = 3;
    @Expose
    private int numberOfRouters = 2;
    @Expose
    private int numberOfDataNodes = 3;
    @Expose
    private int cfgSrvPort = 27019;
    @Expose
    private int routerPort = 27018;
    @Expose
    private int dataNodePort = 27017;

    @Expose
    private Set<MongoConfigNodeImpl> configServersImpl = new HashSet<>();

    @Expose
    private Set<MongoRouterNodeImpl> routerServersImpl = new HashSet<>();

    @Expose
    private Set<MongoDataNodeImpl> dataNodesImpl = new HashSet<>();

    private transient Set<MongoConfigNode> configServers = new HashSet<>();

    private transient Set<MongoRouterNode> routerServers = new HashSet<>();

    private transient Set<MongoDataNode> dataNodes = new HashSet<>();

    @Expose
    private UUID environmentId;


    public MongoClusterConfigImpl init( final EnvironmentManager environmentManager )
    {
        for ( final MongoConfigNodeImpl mongoConfigNode : configServersImpl )
        {
            mongoConfigNode.setContainerHost( environmentManager.getEnvironment( mongoConfigNode.getEnvironmentId() )
                                                                .getContainerHostById( UUID.fromString(
                                                                        mongoConfigNode.getContainerHostId() ) ) );

            this.configServers.add( mongoConfigNode );
        }
        for ( final MongoRouterNodeImpl mongoRouterNode : routerServersImpl )
        {
            mongoRouterNode.setContainerHost( environmentManager.getEnvironment( mongoRouterNode.getEnvironmentId() )
                                                                .getContainerHostById( UUID.fromString(
                                                                        mongoRouterNode.getContainerHostId() ) ) );
            this.routerServers.add( mongoRouterNode );
        }
        for ( final MongoDataNodeImpl mongoDataNode : dataNodesImpl )
        {
            mongoDataNode.setContainerHost( environmentManager.getEnvironment( mongoDataNode.getEnvironmentId() )
                                                              .getContainerHostById( UUID.fromString(
                                                                      mongoDataNode.getContainerHostId() ) ) );
            this.dataNodes.add( mongoDataNode );
        }
        return this;
    }


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


    @Override
    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    @Override
    public int getNumberOfConfigServers()
    {
        return numberOfConfigServers;
    }


    public void setNumberOfConfigServers( int numberOfConfigServers )
    {
        this.numberOfConfigServers = numberOfConfigServers;
    }


    @Override
    public int getNumberOfRouters()
    {
        return numberOfRouters;
    }


    public void setNumberOfRouters( int numberOfRouters )
    {
        this.numberOfRouters = numberOfRouters;
    }


    @Override
    public int getNumberOfDataNodes()
    {
        return numberOfDataNodes;
    }


    public void setNumberOfDataNodes( int numberOfDataNodes )
    {
        this.numberOfDataNodes = numberOfDataNodes;
    }


    public int getDataNodePort()
    {
        return dataNodePort;
    }


    public void setDataNodePort( int dataNodePort )
    {
        this.dataNodePort = dataNodePort;
    }



    public int getRouterPort()
    {
        return routerPort;
    }


    public void setRouterPort( int routerPort )
    {
        this.routerPort = routerPort;
    }


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( String domainName )
    {
        this.domainName = domainName;
    }


    public String getReplicaSetName()
    {
        return replicaSetName;
    }


    public void setReplicaSetName( String replicaSetName )
    {
        this.replicaSetName = replicaSetName;
    }


    public int getCfgSrvPort()
    {
        return cfgSrvPort;
    }


    public void setCfgSrvPort( int cfgSrvPort )
    {
        this.cfgSrvPort = cfgSrvPort;
        this.routerServers.clear();
        this.routerServers.addAll( routerServers );
    }


    public Set<MongoConfigNode> getConfigServers()
    {
        return configServers;
    }


    public void setConfigServers( Set<MongoConfigNode> configServers )
    {
        this.configServers.clear();
        this.configServers.addAll( configServers );
    }


    public Set<MongoDataNode> getDataNodes()
    {
        return dataNodes;
    }


    public void setDataNodes( Set<MongoDataNode> dataNodes )
    {
        this.dataNodes = dataNodes;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
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


    public Set<MongoRouterNode> getRouterServers()
    {
        return routerServers;
    }


    public void setRouterServers( Set<MongoRouterNode> routerServers )
    {
        //        Set<MongoRouterNodeImpl> routers = new HashSet<>();
        //        for ( MongoRouterNode node : routerServers )
        //        {
        //            routers.add( new MongoRouterNodeImpl( node ) );
        //        }
        this.routerServers = routerServers;
    }


    public NodeType getNodeType( MongoNode node )
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


    public String getClusterName()
    {
        return clusterName;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
    }


    @Override
    public MongoDataNode findPrimaryNode() throws MongoException
    {
        String primaryNodeName = getDataNodes().iterator().next().getPrimaryNodeName();

        return ( MongoDataNode ) findNode( primaryNodeName );
    }


    @Override
    public MongoNode findNode( final String lxcHostname )
    {
        MongoNode result = null;
        Iterator<MongoConfigNode> i1 = configServers.iterator();
        while ( result == null && i1.hasNext() )
        {
            MongoNode n = i1.next();
            if ( n.getHostname().equals( lxcHostname ) )
            {
                result = n;
            }
        }

        Iterator<MongoRouterNode> i2 = routerServers.iterator();
        while ( result == null && i2.hasNext() )
        {
            MongoNode n = i2.next();
            if ( n.getHostname().equals( lxcHostname ) )
            {
                result = n;
            }
        }

        Iterator<MongoDataNode> i3 = dataNodes.iterator();
        while ( result == null && i3.hasNext() )
        {
            MongoNode n = i3.next();
            if ( n.getHostname().equals( lxcHostname ) )
            {
                result = n;
            }
        }

        return result;
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


    public int getNodePort( ContainerHost node )
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


    @Override
    public String toString()
    {
        return "ClusterConfig{" + "clusterName=" + clusterName + ", replicaSetName=" + replicaSetName + ", domainName="
                + domainName + ", numberOfConfigServers=" + numberOfConfigServers + ", numberOfRouters="
                + numberOfRouters + ", numberOfDataNodes=" + numberOfDataNodes + ", cfgSrvPort=" + cfgSrvPort
                + ", routerPort=" + routerPort + ", dataNodePort=" + dataNodePort + ", configServers=" + configServers
                + ", routerServers=" + routerServers + ", dataNodes=" + dataNodes + '}';
    }


    @Override
    public Object prepare()
    {
        for ( final MongoConfigNode configServer : configServers )
        {
            this.configServersImpl.add( ( MongoConfigNodeImpl ) configServer );
        }
        for ( final MongoRouterNode routerServer : routerServers )
        {
            this.routerServersImpl.add( ( MongoRouterNodeImpl ) routerServer );
        }
        for ( final MongoDataNode dataNode : dataNodes )
        {
            this.dataNodesImpl.add( ( MongoDataNodeImpl ) dataNode );
        }
        return this;
    }
}
