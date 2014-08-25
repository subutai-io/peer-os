package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.common.PluginDAO;
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
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.shared.protocol.NodeGroup;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


//TODO: Add parameter validation
public class ZookeeperImpl implements Zookeeper {

    private final CommandRunner commandRunner;
    private final AgentManager agentManager;
    private final Tracker tracker;
    private final ContainerManager containerManager;
    private final EnvironmentManager environmentManager;
    private final Hadoop hadoopManager;
    private ExecutorService executor;
    private PluginDAO pluginDAO;


    public ZookeeperImpl( final CommandRunner commandRunner, final AgentManager agentManager, final DbManager dbManager,
                          final Tracker tracker, final ContainerManager containerManager,
                          final EnvironmentManager environmentManager, final Hadoop hadoopManager ) {

        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );
        Preconditions.checkNotNull( dbManager, "Db Manager is null" );
        Preconditions.checkNotNull( tracker, "Tracker is null" );
        Preconditions.checkNotNull( containerManager, "Container manager is null" );
        Preconditions.checkNotNull( environmentManager, "Environment manager is null" );
        Preconditions.checkNotNull( hadoopManager, "Hadoop manager is null" );

        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
        this.tracker = tracker;
        this.containerManager = containerManager;
        this.environmentManager = environmentManager;
        this.hadoopManager = hadoopManager;
        this.pluginDAO = new PluginDAO( dbManager );

        Commands.init( commandRunner );
    }


    public PluginDAO getPluginDAO() {
        return pluginDAO;
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
        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<ZookeeperClusterConfig> getClusters() {

        try {
            return pluginDAO.getInfo( ZookeeperClusterConfig.PRODUCT_KEY, ZookeeperClusterConfig.class );
        }
        catch ( DBException e ) {
            return Collections.emptyList();
        }
    }


    @Override
    public ZookeeperClusterConfig getCluster( String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        try {
            return pluginDAO.getInfo( ZookeeperClusterConfig.PRODUCT_KEY, clusterName, ZookeeperClusterConfig.class );
        }
        catch ( DBException e ) {
            return null;
        }
    }


    public UUID installCluster( ZookeeperClusterConfig config, HadoopClusterConfig hadoopClusterConfig ) {
        Preconditions.checkNotNull( config, "Accumulo configuration is null" );
        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop configuration is null" );


        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config, hadoopClusterConfig );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startNode( String clusterName, String lxcHostName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( String clusterName, String lxcHostName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( String clusterName, String lxcHostName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( String clusterName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( String clusterName, String lxcHostname ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, lxcHostname );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( String clusterName, String lxcHostName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addProperty( String clusterName, String fileName, String propertyName, String propertyValue ) {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( fileName ), "File name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyValue ), "Property value is null or empty" );

        AbstractOperationHandler operationHandler =
                new AddPropertyOperationHandler( this, clusterName, fileName, propertyName, propertyValue );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID removeProperty( String clusterName, String fileName, String propertyName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( fileName ), "File name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );

        AbstractOperationHandler operationHandler =
                new RemovePropertyOperationHandler( this, clusterName, fileName, propertyName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment,
                                                         final ZookeeperClusterConfig config,
                                                         final ProductOperation po ) {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Zookeeper cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        if ( config.getSetupType() == SetupType.STANDALONE ) {
            //this is a standalone ZK cluster setup
            return new ZookeeperStandaloneSetupStrategy( environment, config, po, this );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP ) {
            //this is a with-Hadoop ZK cluster setup
            return new ZookeeperWithHadoopSetupStrategy( environment, config, po, this );
        }
        else {
            //this is an over-Hadoop ZK cluster setup
            return new ZookeeperOverHadoopSetupStrategy( config, po, this );
        }
    }


    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( ZookeeperClusterConfig config ) {
        Preconditions.checkNotNull( config, "Zookeeper cluster config is null" );


        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", ZookeeperClusterConfig.PRODUCT_KEY, UUID.randomUUID() ) );

        //node group
        NodeGroup nodesGroup = new NodeGroup();
        nodesGroup.setName( "DEFAULT" );
        nodesGroup.setNumberOfNodes( config.getNumberOfNodes() );
        nodesGroup.setTemplateName( config.getTemplateName() );
        nodesGroup.setPlacementStrategy( ZookeeperStandaloneSetupStrategy.getNodePlacementStrategy() );


        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodesGroup ) );

        return environmentBlueprint;
    }
}
