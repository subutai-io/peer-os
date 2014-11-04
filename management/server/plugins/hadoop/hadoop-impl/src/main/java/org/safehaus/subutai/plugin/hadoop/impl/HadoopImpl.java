package org.safehaus.subutai.plugin.hadoop.impl;


import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;
import org.safehaus.subutai.plugin.hadoop.impl.common.HadoopSetupStrategy;
import org.safehaus.subutai.plugin.hadoop.impl.handler.AddOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.RemoveNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.dao.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StartNameNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StatusDataNodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.namenode.StopNameNodeOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class HadoopImpl implements Hadoop
{
    public static final int INITIAL_CAPACITY = 2;
    private static final Logger LOG = LoggerFactory.getLogger( HadoopImpl.class.getName() );
    private Tracker tracker;
    private ContainerManager containerManager;
    private NetworkManager networkManager;
    private ExecutorService executor;
    private EnvironmentManager environmentManager;
    private Commands commands;


    private PluginDAO pluginDAO;
    private DataSource dataSource;


    public HadoopImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public void init()
    {
        try
        {
            this.pluginDAO = new PluginDAO( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public NetworkManager getNetworkManager()
    {
        return networkManager;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public void setNetworkManager( final NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void setCommands( final Commands commands )
    {
        this.commands = commands;
    }


    @Override
    public UUID installCluster( final HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, hadoopClusterConfig );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public List<HadoopClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( HadoopClusterConfig.PRODUCT_KEY, HadoopClusterConfig.class );
    }


    @Override
    public HadoopClusterConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        return pluginDAO.getInfo( HadoopClusterConfig.PRODUCT_KEY, clusterName, HadoopClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    @Override
    public UUID startNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new StartNameNodeOperationHandler( this, hadoopClusterConfig.getClusterName() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );        AbstractOperationHandler operationHandler =
                new StopNameNodeOperationHandler( this, hadoopClusterConfig.getClusterName() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID restartNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//
//        AbstractOperationHandler operationHandler =
//                new RestartNameNodeOperationHandler( this, hadoopClusterConfig.getClusterName() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID statusNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new StatusNameNodeOperationHandler( this, hadoopClusterConfig.getClusterName() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID statusSecondaryNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new StatusSecondaryNameNodeOperationHandler( this, hadoopClusterConfig.getClusterName() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID startDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//
//        AbstractOperationHandler operationHandler =
//                new StartDataNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID stopDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//
//        AbstractOperationHandler operationHandler =
//                new StopDataNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID statusDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new StatusDataNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
//        return  null;
    }


    @Override
    public UUID startJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new StartJobTrackerOperationHandler( this, hadoopClusterConfig.getClusterName() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return  null;
    }


    @Override
    public UUID stopJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new StopJobTrackerOperationHandler( this, hadoopClusterConfig.getClusterName() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID restartJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new RestartJobTrackerOperationHandler( this, hadoopClusterConfig.getClusterName() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID statusJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new StatusJobTrackerOperationHandler( this, hadoopClusterConfig.getClusterName() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID startTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        AbstractOperationHandler operationHandler =
//                new StartTaskTrackerOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID stopTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        AbstractOperationHandler operationHandler =
//                new StopTaskTrackerOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID statusTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new StatusTaskTrackerOperationHandler( this, hadoopClusterConfig.getClusterName(),
//                        agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID addNode( String clusterName, int nodeCount )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new AddOperationHandler( this, clusterName, nodeCount );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( String clusterName )
    {
        return addNode( clusterName, 1 );
    }


    @Override
    public UUID blockDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new BlockDataNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID destroyNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new RemoveNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkDecomissionStatus( HadoopClusterConfig hadoopClusterConfig )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new CheckDecommissionStatusOperationHandler( this, hadoopClusterConfig.getClusterName() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID excludeNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new ExcludeNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID includeNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new IncludeNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return  null;
    }


    @Override
    public UUID blockTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new BlockTaskTrackerOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return  null;
    }


    @Override
    public UUID unblockDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new UnblockDataNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return null;
    }


    @Override
    public UUID unblockTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
//                "Cluster name is null or empty" );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( agent.getHostname() ), "Lxc hostname is null or empty" );
//
//        AbstractOperationHandler operationHandler =
//                new UnblockTaskTrackerOperationHandler( this, hadoopClusterConfig.getClusterName(),
//                        agent.getHostname() );
//        executor.execute( operationHandler );
//        return operationHandler.getTrackerId();
        return  null;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( TrackerOperation po, HadoopClusterConfig hadoopClusterConfig )
    {
        return new HadoopSetupStrategy( po, this, hadoopClusterConfig );
    }


    public ClusterSetupStrategy getClusterSetupStrategy( TrackerOperation po, HadoopClusterConfig hadoopClusterConfig,
                                                         Environment environment )
    {
        return new HadoopSetupStrategy( po, this, hadoopClusterConfig, environment );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final HadoopClusterConfig config )
            throws ClusterSetupException
    {

        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint
                .setName( String.format( "%s-%s", HadoopClusterConfig.PRODUCT_KEY, UUIDUtil.generateTimeBasedUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setExchangeSshKeys( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        Set<NodeGroup> nodeGroups = new HashSet<>( INITIAL_CAPACITY );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
        nodeGroup.setNumberOfNodes( HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY + config.getCountOfSlaveNodes() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );


//        //hadoop master nodes
//        NodeGroup mastersGroup = new NodeGroup();
//        mastersGroup.setName( NodeType.MASTER_NODE.name() );
//        mastersGroup.setNumberOfNodes( HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY );
//        mastersGroup.setTemplateName( config.getTemplateName() );
//        mastersGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN );
//        //        mastersGroup.setPhysicalNodes( convertAgent2Hostname() );
//        nodeGroups.add( mastersGroup );
//
//        //hadoop slave nodes
//        NodeGroup slavesGroup = new NodeGroup();
//        slavesGroup.setName( NodeType.SLAVE_NODE.name() );
//        slavesGroup.setNumberOfNodes( config.getCountOfSlaveNodes() );
//        slavesGroup.setTemplateName( config.getTemplateName() );
//        slavesGroup.setPlacementStrategy( PlacementStrategy.ROUND_ROBIN);
//        //        slavesGroup.setPhysicalNodes( convertAgent2Hostname() );
//        nodeGroups.add( slavesGroup );
//
//        environmentBlueprint.setNodeGroups( nodeGroups );

        return environmentBlueprint;
    }
}
