/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.presto.impl;


import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.common.PluginDAO;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.handler.AddWorkerNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.ChangeCoordinatorNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.DestroyWorkerNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import com.google.common.base.Preconditions;


/**
 * @author dilshat
 */
public class PrestoImpl implements Presto {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;
    private static PluginDAO pluginDAO;


    public PrestoImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker ) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        pluginDAO = new PluginDAO( dbManager );

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


    public UUID installCluster( final PrestoClusterConfig config ) {

        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<PrestoClusterConfig> getClusters() {
        try {
            return pluginDAO.getInfo( PrestoClusterConfig.PRODUCT_KEY, PrestoClusterConfig.class );
        }
        catch ( DBException e ) {
            return Collections.emptyList();
        }
    }


    @Override
    public PrestoClusterConfig getCluster( String clusterName ) {

        try {
            return pluginDAO.getInfo( PrestoClusterConfig.PRODUCT_KEY, clusterName, PrestoClusterConfig.class );
        }
        catch ( DBException e ) {
            return null;
        }
    }


    public UUID addWorkerNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new AddWorkerNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyWorkerNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler =
                new DestroyWorkerNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID changeCoordinatorNode( final String clusterName, final String newCoordinatorHostname ) {

        AbstractOperationHandler operationHandler =
                new ChangeCoordinatorNodeOperationHandler( this, clusterName, newCoordinatorHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final ProductOperation po,
                                                         final PrestoClusterConfig prestoClusterConfig ) {
        return null;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final ProductOperation po,
                                                         final PrestoClusterConfig prestoClusterConfig,
                                                         final Environment environment ) {
        return null;
    }
}
