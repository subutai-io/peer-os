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
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.handler.AddOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.NodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.impl.handler.RemoveNodeOperationHandler;
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


    public void setCommands( final Commands commands )
    {
        this.commands = commands;
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


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    @Override
    public UUID installCluster( final HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.INSTALL, null );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.UNINSTALL, null );
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
    public UUID uninstallCluster( final String clustername )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clustername ), "Cluster name is null or empty" );
        HadoopClusterConfig hadoopClusterConfig = getCluster( clustername );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.UNINSTALL, null );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.START_ALL,
                        NodeType.NAMENODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.STOP_ALL,
                        NodeType.NAMENODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.STATUS_ALL,
                        NodeType.NAMENODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusSecondaryNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.STATUS_ALL,
                        NodeType.SECONDARY_NAMENODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname, NodeOperationType.START,
                        NodeType.DATANODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname, NodeOperationType.STOP,
                        NodeType.DATANODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusDataNode( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname,
                        NodeOperationType.STATUS, NodeType.DATANODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.START_ALL,
                        NodeType.JOBTRACKER );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.STOP_ALL,
                        NodeType.JOBTRACKER );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.STATUS_ALL,
                        NodeType.JOBTRACKER );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname, NodeOperationType.START,
                        NodeType.TASKTRACKER );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname, NodeOperationType.STOP,
                        NodeType.TASKTRACKER );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusTaskTracker( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname,
                        NodeOperationType.STATUS, NodeType.TASKTRACKER );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
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
    public UUID destroyNode( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new RemoveNodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkDecomissionStatus( HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, hadoopClusterConfig, ClusterOperationType.DECOMISSION_STATUS, null );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID excludeNode( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname,
                        NodeOperationType.EXCLUDE, NodeType.SLAVE_NODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID includeNode( HadoopClusterConfig hadoopClusterConfig, String hostname )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Configuration is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ),
                "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopClusterConfig.getClusterName(), hostname,
                        NodeOperationType.INCLUDE, NodeType.SLAVE_NODE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public ClusterSetupStrategy getClusterSetupStrategy( Environment environment,
                                                         HadoopClusterConfig hadoopClusterConfig, TrackerOperation po )
    {
        return new HadoopSetupStrategy( environment, hadoopClusterConfig, po, this );
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
//        Set<NodeGroup> nodeGroups = new HashSet<>( INITIAL_CAPACITY );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setName( "Hadoop node group" );
        nodeGroup.setLinkHosts( true );
        nodeGroup.setExchangeSshKeys( true );
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        nodeGroup.setNumberOfNodes(
                HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY + config.getCountOfSlaveNodes() );

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
