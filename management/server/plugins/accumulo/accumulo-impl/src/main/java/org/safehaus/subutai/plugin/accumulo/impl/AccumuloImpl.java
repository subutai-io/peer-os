package org.safehaus.subutai.plugin.accumulo.impl;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
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
import org.safehaus.subutai.plugin.accumulo.impl.handler.ConfigureEnvironmentClusterHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.RemovePropertyOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.StartClusterOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.StopClusterOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.common.PluginDao;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class AccumuloImpl implements Accumulo
{
    private static final Logger LOG = LoggerFactory.getLogger( AccumuloImpl.class.getName() );
    protected Commands commands;
    protected AgentManager agentManager;
    private CommandRunner commandRunner;
    private Tracker tracker;
    private Hadoop hadoopManager;
    private Zookeeper zkManager;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;
    private ExecutorService executor;
    private PluginDao pluginDAO;
    private DataSource dataSource;


    public AccumuloImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public PluginDao getPluginDAO()
    {
        return pluginDAO;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public Zookeeper getZkManager()
    {
        return zkManager;
    }


    public void setZkManager( final Zookeeper zkManager )
    {
        this.zkManager = zkManager;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public void init()
    {
        try
        {
            this.pluginDAO = new PluginDao( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        this.commands = new Commands( commandRunner );

        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public UUID installCluster( final AccumuloClusterConfig accumuloClusterConfig )
    {
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, accumuloClusterConfig );

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


    @Override
    public List<AccumuloClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( AccumuloClusterConfig.PRODUCT_KEY, AccumuloClusterConfig.class );
    }


    @Override
    public AccumuloClusterConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        return pluginDAO.getInfo( AccumuloClusterConfig.PRODUCT_KEY, clusterName, AccumuloClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    public UUID installCluster( final AccumuloClusterConfig accumuloClusterConfig,
                                final HadoopClusterConfig hadoopClusterConfig,
                                final ZookeeperClusterConfig zookeeperClusterConfig )
    {
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster configuration is null" );
        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop cluster configuration is null" );
        Preconditions.checkNotNull( zookeeperClusterConfig, "Zookeeper cluster configuration is null" );

        AbstractOperationHandler operationHandler =
                new InstallOperationHandler( this, accumuloClusterConfig, hadoopClusterConfig, zookeeperClusterConfig );


        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID startCluster( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new StartClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopCluster( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new StopClusterOperationHandler( this, clusterName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( final String clusterName, final String lxcHostname, final NodeType nodeType )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );

        AbstractOperationHandler operationHandler =
                new AddNodeOperationHandler( this, clusterName, lxcHostname, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostName, final NodeType nodeType )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );

        AbstractOperationHandler operationHandler =
                new DestroyNodeOperationHandler( this, clusterName, lxcHostName, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addProperty( final String clusterName, final String propertyName, final String propertyValue )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyValue ), "Property value is null or empty" );

        AbstractOperationHandler operationHandler =
                new AddPropertyOperationHandler( this, clusterName, propertyName, propertyValue );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID removeProperty( final String clusterName, final String propertyName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );

        AbstractOperationHandler operationHandler =
                new RemovePropertyOperationHandler( this, clusterName, propertyName );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public EnvironmentBuildTask getDefaultEnvironmentBlueprint( final AccumuloClusterConfig config )
    {

        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint
                .setName( String.format( "%s-%s", config.getProductKey(), UUIDUtil.generateTimeBasedUUID() ) );

        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        nodeGroup.setNumberOfNodes( config.getAllNodes().size() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );

        return environmentBuildTask;
    }


    public UUID configureEnvironmentCluster( final AccumuloClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new ConfigureEnvironmentClusterHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment,
                                                         AccumuloClusterConfig accumuloClusterConfig,
                                                         TrackerOperation po )
    {
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
