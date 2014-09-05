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

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.handler.AddWorkerNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.ChangeCoordinatorNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.DestroyWorkerNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.PrestoSetupStrategy;
import org.safehaus.subutai.plugin.presto.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.UninstallOperationHandler;

import com.google.common.base.Preconditions;


/**
 * @author dilshat
 */
public class PrestoImpl implements Presto {

    private static CommandRunner commandRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;
    private static Tracker tracker;
    private static ExecutorService executor;
    private static PluginDAO pluginDAO;


    public PrestoImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker ) {
        PrestoImpl.commandRunner = commandRunner;
        PrestoImpl.agentManager = agentManager;
        PrestoImpl.dbManager = dbManager;
        PrestoImpl.tracker = tracker;
        pluginDAO = new PluginDAO( dbManager );

        Commands.init( commandRunner );
    }


    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public static AgentManager getAgentManager() {
        return agentManager;
    }


    public static DbManager getDbManager() {
        return dbManager;
    }


    public static Tracker getTracker() {
        return tracker;
    }


    public static PluginDAO getPluginDAO() {
        return pluginDAO;
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
        Preconditions.checkNotNull( prestoClusterConfig, "Presto cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        return new PrestoSetupStrategy( po, this, prestoClusterConfig );
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final ProductOperation po,
                                                         final PrestoClusterConfig prestoClusterConfig,
                                                         final Environment environment ) {

        return new PrestoSetupStrategy( environment, po, this, prestoClusterConfig );
    }
}
