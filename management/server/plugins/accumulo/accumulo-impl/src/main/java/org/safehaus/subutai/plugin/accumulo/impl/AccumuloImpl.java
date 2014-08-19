package org.safehaus.subutai.plugin.accumulo.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.api.SetupType;
import org.safehaus.subutai.plugin.accumulo.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.AddPropertyOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.CheckNodeOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.RemovePropertyOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.StartClusterOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.StopClusterOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;


//TODO: add parameter validation
public class AccumuloImpl implements Accumulo {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private Hadoop hadoopManager;
    private Zookeeper zkManager;
    private EnvironmentManager environmentManager;
    private ExecutorService executor;


    public AccumuloImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
                         Hadoop hadoopManager, Zookeeper zkManager, EnvironmentManager environmentManager ) {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.dbManager = dbManager;
        this.tracker = tracker;
        this.hadoopManager = hadoopManager;
        this.zkManager = zkManager;
        this.environmentManager = environmentManager;

        Commands.init( commandRunner );
    }


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
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


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public Zookeeper getZkManager() {
        return zkManager;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public UUID installCluster( final AccumuloClusterConfig accumuloClusterConfig ) {
        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, accumuloClusterConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID installCluster( final AccumuloClusterConfig accumuloClusterConfig,
                                final HadoopClusterConfig hadoopClusterConfig,
                                final ZookeeperClusterConfig zookeeperClusterConfig ) {
        AbstractOperationHandler operationHandler =
                new InstallOperationHandler( this, accumuloClusterConfig, hadoopClusterConfig, zookeeperClusterConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<AccumuloClusterConfig> getClusters() {

        return dbManager.getInfo( AccumuloClusterConfig.PRODUCT_KEY, AccumuloClusterConfig.class );
    }


    public AccumuloClusterConfig getCluster( String clusterName ) {
        return dbManager.getInfo( AccumuloClusterConfig.PRODUCT_KEY, clusterName, AccumuloClusterConfig.class );
    }


    public UUID startCluster( final String clusterName ) {

        AbstractOperationHandler operationHandler = new StartClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopCluster( final String clusterName ) {

        AbstractOperationHandler operationHandler = new StopClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostName ) {

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( final String clusterName, final String lxcHostname, final NodeType nodeType ) {

        AbstractOperationHandler operationHandler =
                new AddNodeOperationHandler( this, clusterName, lxcHostname, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostName, final NodeType nodeType ) {

        AbstractOperationHandler operationHandler =
                new DestroyNodeOperationHandler( this, clusterName, lxcHostName, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addProperty( final String clusterName, final String propertyName, final String propertyValue ) {

        AbstractOperationHandler operationHandler =
                new AddPropertyOperationHandler( this, clusterName, propertyName, propertyValue );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID removeProperty( final String clusterName, final String propertyName ) {

        AbstractOperationHandler operationHandler =
                new RemovePropertyOperationHandler( this, clusterName, propertyName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment,
                                                         AccumuloClusterConfig accumuloClusterConfig,
                                                         ProductOperation po ) {
        if ( accumuloClusterConfig.getSetupType() == SetupType.OVER_HADOOP_N_ZK ) {
            return new AccumuloOverZkNHadoopSetupStrategy( accumuloClusterConfig, po, this );
        }
        else {
            return new AccumuloWithZkNHadoopSetupStrategy( environment, accumuloClusterConfig, po, this );
        }
    }
}
