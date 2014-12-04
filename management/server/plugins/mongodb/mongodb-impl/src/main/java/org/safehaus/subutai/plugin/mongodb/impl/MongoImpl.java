/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.GsonInterfaceAdapter;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.safehaus.subutai.plugin.mongodb.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.UninstallOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;


/**
 * Implementation of Mongo interface. Implements all backend logic for mongo cluster management
 */

public class MongoImpl implements Mongo
{

    private static final Logger LOG = LoggerFactory.getLogger( MongoImpl.class.getName() );
    private Tracker tracker;
    //    private ContainerManager containerManager;
    private EnvironmentManager environmentManager;
    private ExecutorService executor;
    private Commands commands;
    private PluginDAO pluginDAO;
    private DataSource dataSource;
    private PeerManager peerManager;


    public MongoImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public void setCommands( final Commands commands )
    {
        this.commands = commands;
    }


    public void init()
    {
        try
        {

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter( MongoDataNode.class, new GsonInterfaceAdapter<MongoDataNode>() ).create();
            gsonBuilder.registerTypeAdapter( MongoConfigNode.class, new GsonInterfaceAdapter<MongoConfigNode>() )
                       .create();
            gsonBuilder.registerTypeAdapter( MongoRouterNode.class, new GsonInterfaceAdapter<MongoRouterNode>() )
                       .create();

            this.pluginDAO = new PluginDAO( dataSource, gsonBuilder );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        this.commands = new Commands();

        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public UUID installCluster( MongoClusterConfig config )
    {

        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<MongoClusterConfig> getClusters()
    {
        List<MongoClusterConfigImpl> r =
                pluginDAO.getInfo( MongoClusterConfig.PRODUCT_KEY, MongoClusterConfigImpl.class );

        List<MongoClusterConfig> result = new ArrayList<>();
        result.addAll( r );
        return result;
    }


    @Override
    public MongoClusterConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        return pluginDAO.getInfo( MongoClusterConfig.PRODUCT_KEY, clusterName, MongoClusterConfigImpl.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    public UUID addNode( final String clusterName, final NodeType nodeType )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );


        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startNode( final String clusterName, final String lxcHostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( final String clusterName, final String lxcHostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final MongoClusterConfig config,
                                                         final TrackerOperation po )
    {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Mongo cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        return new MongoDbSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( MongoClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Mongo cluster config is null" );
        //        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint
                .setName( String.format( "%s-%s", MongoClusterConfig.PRODUCT_KEY, UUIDUtil.generateTimeBasedUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );

        //config servers
        NodeGroup cfgServersGroup = new NodeGroup();
        cfgServersGroup.setName( NodeType.CONFIG_NODE.name() );
        cfgServersGroup.setNumberOfNodes( config.getNumberOfConfigServers() );
        cfgServersGroup.setTemplateName( config.getTemplateName() );
        cfgServersGroup.setPlacementStrategy(
                MongoDbSetupStrategy.getNodePlacementStrategyByNodeType( NodeType.CONFIG_NODE ) );

        //routers
        NodeGroup routersGroup = new NodeGroup();
        routersGroup.setName( NodeType.ROUTER_NODE.name() );
        routersGroup.setNumberOfNodes( config.getNumberOfRouters() );
        routersGroup.setTemplateName( config.getTemplateName() );
        routersGroup.setPlacementStrategy(
                MongoDbSetupStrategy.getNodePlacementStrategyByNodeType( NodeType.ROUTER_NODE ) );

        //data nodes
        NodeGroup dataNodesGroup = new NodeGroup();
        dataNodesGroup.setName( NodeType.DATA_NODE.name() );
        dataNodesGroup.setNumberOfNodes( config.getNumberOfDataNodes() );
        dataNodesGroup.setTemplateName( config.getTemplateName() );
        dataNodesGroup
                .setPlacementStrategy( MongoDbSetupStrategy.getNodePlacementStrategyByNodeType( NodeType.DATA_NODE ) );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( cfgServersGroup, routersGroup, dataNodesGroup ) );

        //        environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );
        return environmentBlueprint;
    }


    @Override
    public MongoClusterConfig newMongoClusterConfigInstance()
    {
        return new MongoClusterConfigImpl();
    }
}
