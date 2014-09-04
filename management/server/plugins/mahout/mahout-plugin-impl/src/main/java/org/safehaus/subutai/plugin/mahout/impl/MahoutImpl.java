/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mahout.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mahout.api.Mahout;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;
import org.safehaus.subutai.plugin.mahout.impl.handler.AddNodeHandler;
import org.safehaus.subutai.plugin.mahout.impl.handler.DestroyNodeHandler;
import org.safehaus.subutai.plugin.mahout.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.mahout.impl.handler.UninstallHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * @author dilshat
 */
public class MahoutImpl implements Mahout {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;


    public MahoutImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker ) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;

        Commands.init( commandRunner );
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public DbManager getDbManager() {
        return dbManager;
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


    public UUID installCluster( final MahoutClusterConfig config ) {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new InstallHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {
        AbstractOperationHandler operationHandler = new UninstallHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public List<MahoutClusterConfig> getClusters() {
        return dbManager.getInfo( MahoutClusterConfig.PRODUCT_KEY, MahoutClusterConfig.class );
    }


    @Override
    public MahoutClusterConfig getCluster( String clusterName ) {
        return dbManager.getInfo( MahoutClusterConfig.PRODUCT_KEY, clusterName, MahoutClusterConfig.class );
    }


    public UUID addNode( final String clusterName, final String lxcHostname ) {
        AbstractOperationHandler operationHandler = new AddNodeHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostname ) {
        AbstractOperationHandler operationHandler = new DestroyNodeHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkNode( final String clustername, final String lxchostname ) {
        return null;
    }


    @Override
    public UUID stopCluster( final String clusterName ) {
        return null;
    }


    @Override
    public UUID startCluster( final String clusterName ) {
        return null;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final MahoutClusterConfig config,
                                                         final ProductOperation po ) {
        return new MahoutSetupStrategy( this, po, config );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final MahoutClusterConfig config ) {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", config.PRODUCT_KEY, UUID.randomUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        nodeGroup.setNumberOfNodes( config.getNodes().size() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        return environmentBlueprint;
    }
}
