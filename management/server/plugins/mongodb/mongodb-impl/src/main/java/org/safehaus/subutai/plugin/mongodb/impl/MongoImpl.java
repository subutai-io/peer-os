/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.NodeType;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.safehaus.subutai.plugin.mongodb.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.mongodb.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Implementation of Mongo interface. Implements all backend logic for mongo cluster management
 */

public class MongoImpl implements Mongo {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Tracker tracker;
    private ContainerManager containerManager;
    private EnvironmentManager environmentManager;
    private ExecutorService executor;
    private PluginDAO pluginDAO;


    public MongoImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
                      ContainerManager containerManager, EnvironmentManager environmentManager ) {

        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );
        Preconditions.checkNotNull( tracker, "Tracker is null" );
        Preconditions.checkNotNull( containerManager, "Container manager is null" );
        Preconditions.checkNotNull( environmentManager, "Environment manager is null" );

        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.tracker = tracker;
        this.containerManager = containerManager;
        this.environmentManager = environmentManager;
        this.pluginDAO = new PluginDAO( dbManager );

        Commands.init( commandRunner );
    }


    public PluginDAO getPluginDAO() {
        return pluginDAO;
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public UUID installCluster( MongoClusterConfig config ) {

        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<MongoClusterConfig> getClusters() {

        try {
            return pluginDAO.getInfo( MongoClusterConfig.PRODUCT_KEY, MongoClusterConfig.class );
        }
        catch ( DBException e ) {
            return Collections.emptyList();
        }
    }


    @Override
    public MongoClusterConfig getCluster( String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        try {
            return pluginDAO.getInfo( MongoClusterConfig.PRODUCT_KEY, clusterName, MongoClusterConfig.class );
        }
        catch ( DBException e ) {
            return null;
        }
    }


    public UUID addNode( final String clusterName, final NodeType nodeType ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );


        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostname ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startNode( final String clusterName, final String lxcHostname ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( final String clusterName, final String lxcHostname ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostname ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final MongoClusterConfig config,
                                                         final ProductOperation po ) {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Mongo cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        return new MongoDbSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( MongoClusterConfig config ) {
        Preconditions.checkNotNull( config, "Mongo cluster config is null" );

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", MongoClusterConfig.PRODUCT_KEY, UUID.randomUUID() ) );
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

        return environmentBlueprint;
    }
}
