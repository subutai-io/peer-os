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
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.api.CommandType;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.AddPropertyOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.RemovePropertyOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.ZookeeperClusterOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.ZookeeperNodeOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


//TODO: Add parameter validation
public class ZookeeperImpl implements Zookeeper
{

    private Tracker tracker;
    private EnvironmentManager environmentManager;
    private Hadoop hadoopManager;
    private Commands commands;
    private ExecutorService executor;
    private PeerManager peerManager;
    private static final Logger LOG = LoggerFactory.getLogger( ZookeeperImpl.class.getName() );

    private PluginDAO pluginDAO;
    private DataSource dataSource;


    public ZookeeperImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public PluginDAO getPluginDAO()
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


    public Tracker getTracker()
    {
        return tracker;
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


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
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


    public UUID installCluster( ZookeeperClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );

        AbstractOperationHandler operationHandler =
                new ZookeeperClusterOperationHandler( this, config, ClusterOperationType.INSTALL );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler =
                new ZookeeperClusterOperationHandler( this, getCluster( clusterName ), ClusterOperationType.UNINSTALL );
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


    public UUID startNode( String clusterName, String hostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new ZookeeperNodeOperationHandler( this, clusterName, hostname, NodeOperationType.START );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID stopNode( String clusterName, String hostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Hostname is null or empty" );


        AbstractOperationHandler operationHandler =
                new ZookeeperNodeOperationHandler( this, clusterName, hostname, NodeOperationType.STOP );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID checkNode( String clusterName, String hostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Hostname is null or empty" );


        AbstractOperationHandler operationHandler =
                new ZookeeperNodeOperationHandler( this, clusterName, hostname, NodeOperationType.STATUS );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID addNode( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        ZookeeperClusterConfig zookeeperClusterConfig = getCluster( clusterName );

        AbstractOperationHandler operationHandler =
                new ZookeeperClusterOperationHandler( this, zookeeperClusterConfig, ClusterOperationType.ADD );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID addNode( String clusterName, String lxcHostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );
        ZookeeperClusterConfig zookeeperClusterConfig = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new ZookeeperClusterOperationHandler( this, zookeeperClusterConfig, lxcHostname,
                        ClusterOperationType.ADD );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( String clusterName, String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new ZookeeperNodeOperationHandler( this, clusterName, lxcHostName, NodeOperationType.DESTROY );
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
            return new ZookeeperOverHadoopSetupStrategy( environment, config, po, this );
        }
    }


    @Override
    public String getCommand( final CommandType commandType )
    {
        switch ( commandType )
        {
            case INSTALL:
                return Commands.getInstallCommand();
            case UNINSTALL:
                return Commands.getUninstallCommand();
            case START:
                return Commands.getStartCommand();
            case STOP:
                return Commands.getStopCommand();
            case STATUS:
                return Commands.getStatusCommand();
        }
        return null;
    }


    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( ZookeeperClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Zookeeper cluster config is null" );


        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName(
                String.format( "%s-%s", ZookeeperClusterConfig.PRODUCT_KEY, UUIDUtil.generateTimeBasedUUID() ) );

        //node group
        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setName( "DEFAULT" );
        nodeGroup.setLinkHosts( false );
        nodeGroup.setExchangeSshKeys( false );
        nodeGroup.setNumberOfNodes( config.getNumberOfNodes() );
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( ZookeeperStandaloneSetupStrategy.getNodePlacementStrategy() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );


        return environmentBlueprint;
    }
}
