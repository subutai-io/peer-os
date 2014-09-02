/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.spark.impl;


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
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.handler.AddSlaveNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.ChangeMasterNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.DestroySlaveNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.SparkDbSetupStrategy;
import org.safehaus.subutai.plugin.spark.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.handler.UninstallOperationHandler;

import com.google.common.base.Preconditions;


/**
 * @author dilshat
 */
public class SparkImpl implements Spark {

    private static CommandRunner commandRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;
    private static Tracker tracker;
    private static ExecutorService executor;
    private static PluginDAO pluginDAO;


    public SparkImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker ) {
        SparkImpl.commandRunner = commandRunner;
        SparkImpl.agentManager = agentManager;
        SparkImpl.dbManager = dbManager;
        SparkImpl.tracker = tracker;
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


    public UUID installCluster( final SparkClusterConfig config ) {

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


    public List<SparkClusterConfig> getClusters() {
        try {
            return pluginDAO.getInfo( SparkClusterConfig.PRODUCT_KEY, SparkClusterConfig.class );
        }
        catch ( DBException e ) {
            return Collections.emptyList();
        }
    }


    @Override
    public SparkClusterConfig getCluster( String clusterName ) {
        try {
            return pluginDAO.getInfo( SparkClusterConfig.PRODUCT_KEY, clusterName, SparkClusterConfig.class );
        }
        catch ( DBException e ) {
            return null;
        }
    }


    public UUID addSlaveNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler = new AddSlaveNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroySlaveNode( final String clusterName, final String lxcHostname ) {

        AbstractOperationHandler operationHandler =
                new DestroySlaveNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID changeMasterNode( final String clusterName, final String newMasterHostname, final boolean keepSlave ) {

        AbstractOperationHandler operationHandler =
                new ChangeMasterNodeOperationHandler( this, clusterName, newMasterHostname, keepSlave );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startNode( final String clusterName, final String lxcHostname, final boolean master ) {

        AbstractOperationHandler operationHandler =
                new StartNodeOperationHandler( this, clusterName, lxcHostname, master );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( final String clusterName, final String lxcHostname, final boolean master ) {

        AbstractOperationHandler operationHandler =
                new StopNodeOperationHandler( this, clusterName, lxcHostname, master );

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
                                                         final SparkClusterConfig sparkClusterConfig ) {

        Preconditions.checkNotNull( sparkClusterConfig, "Spark cluster config is null" );
        Preconditions.checkNotNull( po, "Spark operation is null" );
        return new SparkDbSetupStrategy( po, this, sparkClusterConfig );
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final ProductOperation po,
                                                         final SparkClusterConfig sparkClusterConfig,
                                                         final Environment environment ) {
        Preconditions.checkNotNull( po, "Product operation is null" );
        Preconditions.checkNotNull( sparkClusterConfig, "Spark cluster config is null" );
        return new SparkDbSetupStrategy( environment, po, this, sparkClusterConfig );
    }
}
