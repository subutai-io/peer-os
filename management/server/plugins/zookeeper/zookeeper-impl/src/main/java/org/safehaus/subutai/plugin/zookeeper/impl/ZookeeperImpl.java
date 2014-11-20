package org.safehaus.subutai.plugin.zookeeper.impl;


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
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDao;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


//TODO: Add parameter validation
public class ZookeeperImpl implements Zookeeper
{

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Tracker tracker;
    private ContainerManager containerManager;
    private EnvironmentManager environmentManager;
    private Hadoop hadoopManager;
    private Commands commands;
    private ExecutorService executor;
    private static final Logger LOG = LoggerFactory.getLogger( ZookeeperImpl.class.getName() );

    private PluginDao pluginDAO;
    private DataSource dataSource;


    public ZookeeperImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public PluginDao getPluginDAO()
    {
        return pluginDAO;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public void setCommands( final Commands commands )
    {
        this.commands = commands;
    }


    public void setHadoopManager( final Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setCommandRunner( final CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
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


    public UUID installCluster( ZookeeperClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public List<ZookeeperClusterConfig> getClusters()
    {

        return pluginDAO.getInfo( ZookeeperClusterConfig.PRODUCT_KEY, ZookeeperClusterConfig.class );
    }


    @Override
    public ZookeeperClusterConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        return pluginDAO.getInfo( ZookeeperClusterConfig.PRODUCT_KEY, clusterName, ZookeeperClusterConfig.class );
    }


    public UUID installCluster( ZookeeperClusterConfig config, HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( config, "Accumulo configuration is null" );
        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop configuration is null" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config, hadoopClusterConfig );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID startNode( String clusterName, String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StartNodeOperationHandler( this, clusterName, lxcHostName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID stopNode( String clusterName, String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new StopNodeOperationHandler( this, clusterName, lxcHostName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID checkNode( String clusterName, String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new CheckNodeOperationHandler( this, clusterName, lxcHostName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID addNode( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );


        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID addNode( String clusterName, String lxcHostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( String clusterName, String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addProperty( String clusterName, String fileName, String propertyName, String propertyValue )
    {

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
    public UUID removeProperty( String clusterName, String fileName, String propertyName )
    {
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
                                                         final TrackerOperation po )
    {
        Preconditions.checkNotNull( config, "Zookeeper cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );
        if ( config.getSetupType() != SetupType.OVER_HADOOP )
        {
            Preconditions.checkNotNull( environment, "Environment is null" );
        }

        if ( config.getSetupType() == SetupType.STANDALONE )
        {
            //this is a standalone ZK cluster setup
            return new ZookeeperStandaloneSetupStrategy( environment, config, po, this );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            //this is a with-Hadoop ZK cluster setup
            return new ZookeeperWithHadoopSetupStrategy( environment, config, po, this );
        }
        else
        {
            //this is an over-Hadoop ZK cluster setup
            return new ZookeeperOverHadoopSetupStrategy( config, po, this );
        }
    }


    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( ZookeeperClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Zookeeper cluster config is null" );


        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName(
                String.format( "%s-%s", ZookeeperClusterConfig.PRODUCT_KEY, UUIDUtil.generateTimeBasedUUID() ) );

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
