package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.AddPropertyOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.RemovePropertyOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;


public class ZookeeperImpl implements Zookeeper {

    private final CommandRunner commandRunner;
    private final AgentManager agentManager;
    private final DbManager dbManager;
    private final Tracker tracker;
    private final ContainerManager containerManager;
    private final EnvironmentManager environmentManager;
    private final Hadoop hadoopManager;
    private ExecutorService executor;


    public ZookeeperImpl( final CommandRunner commandRunner, final AgentManager agentManager, final DbManager dbManager,
                          final Tracker tracker, final ContainerManager containerManager,
                          final EnvironmentManager environmentManager, final Hadoop hadoopManager ) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.containerManager = containerManager;
        this.environmentManager = environmentManager;
        this.hadoopManager = hadoopManager;

        Commands.init( commandRunner );
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public Hadoop getHadoopManager() {
        return hadoopManager;
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


    public ContainerManager getContainerManager() {
        return containerManager;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public UUID installCluster( ZookeeperClusterConfig config ) {

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID installCluster( ZookeeperClusterConfig config, HadoopClusterConfig hadoopClusterConfig ) {

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config, hadoopClusterConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( String clusterName ) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startNode( String clusterName, String lxcHostName ) {

        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( String clusterName, String lxcHostName ) {

        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( String clusterName, String lxcHostName ) {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( String clusterName, String lxcHostName ) {

        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addProperty( String clusterName, String fileName, String propertyName, String propertyValue ) {

        AbstractOperationHandler operationHandler =
                new AddPropertyOperationHandler( this, clusterName, fileName, propertyName, propertyValue );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID removeProperty( String clusterName, String fileName, String propertyName ) {

        AbstractOperationHandler operationHandler =
                new RemovePropertyOperationHandler( this, clusterName, fileName, propertyName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final ZookeeperClusterConfig config, ProductOperation po ) {
        if ( config.getSetupType() == SetupType.STANDALONE ) {
            //this is a standalone ZK cluster setup
            return new ZookeeperStandaloneSetupStrategy( config, po, this );
        }
        else {
            //this is an over-Hadoop ZK cluster setup
            return new ZookeeperOverHadoopSetupStrategy( config, po, this );
        }
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final HadoopClusterConfig hadoopConfig,
                                                         final ZookeeperClusterConfig zkConfig,
                                                         final ProductOperation po ) {
        //this is a with-Hadoop ZK cluster setup
        return new ZookeeperWithHadoopSetupStrategy( hadoopConfig, zkConfig, po, this );
    }


    public UUID addNode( String clusterName ) {

        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( String clusterName, String lxcHostname ) {

        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<ZookeeperClusterConfig> getClusters() {

        return dbManager.getInfo( ZookeeperClusterConfig.PRODUCT_KEY, ZookeeperClusterConfig.class );
    }


    @Override
    public ZookeeperClusterConfig getCluster( String clusterName ) {
        return dbManager.getInfo( ZookeeperClusterConfig.PRODUCT_KEY, clusterName, ZookeeperClusterConfig.class );
    }
}
