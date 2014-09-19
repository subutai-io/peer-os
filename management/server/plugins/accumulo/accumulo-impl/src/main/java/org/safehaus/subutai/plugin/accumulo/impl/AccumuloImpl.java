package org.safehaus.subutai.plugin.accumulo.impl;


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
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
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
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class AccumuloImpl implements Accumulo {

    protected Commands commands;
    protected AgentManager agentManager;
    private CommandRunner commandRunner;
    private Tracker tracker;
    private Hadoop hadoopManager;
    private Zookeeper zkManager;
    private EnvironmentManager environmentManager;
    private ExecutorService executor;
    private PluginDAO pluginDAO;


    public AccumuloImpl( CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker,
                         Hadoop hadoopManager, Zookeeper zkManager, EnvironmentManager environmentManager ) {

        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );
        Preconditions.checkNotNull( tracker, "Tracker is null" );
        Preconditions.checkNotNull( hadoopManager, "Hadoop manager is null" );
        Preconditions.checkNotNull( zkManager, "Zookeeper manager is null" );
        Preconditions.checkNotNull( environmentManager, "Environment manager is null" );

        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.tracker = tracker;
        this.hadoopManager = hadoopManager;
        this.zkManager = zkManager;
        this.environmentManager = environmentManager;
        this.pluginDAO = new PluginDAO( dbManager );
        this.commands = new Commands( commandRunner );

        Commands.init( commandRunner );
    }


    public PluginDAO getPluginDAO() {
        return pluginDAO;
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


    public Commands getCommands() {
        return commands;
    }


    public UUID installCluster( final AccumuloClusterConfig accumuloClusterConfig ) {
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, accumuloClusterConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName ) {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<AccumuloClusterConfig> getClusters() {

        return pluginDAO.getInfo( AccumuloClusterConfig.PRODUCT_KEY, AccumuloClusterConfig.class );
    }


    public AccumuloClusterConfig getCluster( String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        return pluginDAO.getInfo( AccumuloClusterConfig.PRODUCT_KEY, clusterName, AccumuloClusterConfig.class );
    }


    public UUID installCluster( final AccumuloClusterConfig accumuloClusterConfig,
                                final HadoopClusterConfig hadoopClusterConfig,
                                final ZookeeperClusterConfig zookeeperClusterConfig ) {
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster configuration is null" );
        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop cluster configuration is null" );
        Preconditions.checkNotNull( zookeeperClusterConfig, "Zookeeper cluster configuration is null" );

        AbstractOperationHandler operationHandler =
                new InstallOperationHandler( this, accumuloClusterConfig, hadoopClusterConfig, zookeeperClusterConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startCluster( final String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new StartClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopCluster( final String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new StopClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( final String clusterName, final String lxcHostname, final NodeType nodeType ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );

        AbstractOperationHandler operationHandler =
                new AddNodeOperationHandler( this, clusterName, lxcHostname, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostName, final NodeType nodeType ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );

        AbstractOperationHandler operationHandler =
                new DestroyNodeOperationHandler( this, clusterName, lxcHostName, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addProperty( final String clusterName, final String propertyName, final String propertyValue ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyValue ), "Property value is null or empty" );

        AbstractOperationHandler operationHandler =
                new AddPropertyOperationHandler( this, clusterName, propertyName, propertyValue );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID removeProperty( final String clusterName, final String propertyName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );

        AbstractOperationHandler operationHandler =
                new RemovePropertyOperationHandler( this, clusterName, propertyName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment,
                                                         AccumuloClusterConfig accumuloClusterConfig,
                                                         ProductOperation po ) {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        if ( accumuloClusterConfig.getSetupType() == SetupType.OVER_HADOOP_N_ZK )
        {
            return new AccumuloOverZkNHadoopSetupStrategy( accumuloClusterConfig, po, this );
        }
        else
        {
            return new AccumuloWithZkNHadoopSetupStrategy( environment, accumuloClusterConfig, po, this );
        }
    }
}
