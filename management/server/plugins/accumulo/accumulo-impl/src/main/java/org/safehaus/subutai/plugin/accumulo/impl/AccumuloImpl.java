package org.safehaus.subutai.plugin.accumulo.impl;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.handler.AddPropertyOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.NodeOperationHandler;
import org.safehaus.subutai.plugin.accumulo.impl.handler.RemovePropertyOperationHandler;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AccumuloImpl implements Accumulo
{
    private static final Logger LOG = LoggerFactory.getLogger( AccumuloImpl.class.getName() );
    protected Commands commands;
    private Tracker tracker;
    private Hadoop hadoopManager;
    private Zookeeper zkManager;
    private EnvironmentManager environmentManager;
    private ExecutorService executor;
    private PluginDAO pluginDAO;
    private DataSource dataSource;


    public AccumuloImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }

    public void setPluginDAO(final PluginDAO pluginDAO)
    {
        this.pluginDAO = pluginDAO;
    }

    public PluginDAO getPluginDAO()
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


    public UUID installCluster( final AccumuloClusterConfig accumuloClusterConfig )
    {
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster configuration is null" );
        HadoopClusterConfig hadoopClusterConfig =
                hadoopManager.getCluster( accumuloClusterConfig.getHadoopClusterName() );
        ZookeeperClusterConfig zookeeperClusterConfig =
                zkManager.getCluster( accumuloClusterConfig.getZookeeperClusterName() );
        AbstractOperationHandler h =
                new ClusterOperationHandler( this, accumuloClusterConfig, hadoopClusterConfig, zookeeperClusterConfig,
                        ClusterOperationType.INSTALL );
        executor.execute( h );
        return h.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        AccumuloClusterConfig accumuloClusterConfig = getCluster( clusterName );
        HadoopClusterConfig hadoopClusterConfig =
                hadoopManager.getCluster( accumuloClusterConfig.getHadoopClusterName() );
        ZookeeperClusterConfig zookeeperClusterConfig =
                zkManager.getCluster( accumuloClusterConfig.getZookeeperClusterName() );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, accumuloClusterConfig, hadoopClusterConfig, zookeeperClusterConfig,
                        ClusterOperationType.UNINSTALL );
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


    public UUID startCluster( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        AccumuloClusterConfig accumuloClusterConfig = getCluster( clusterName );
        HadoopClusterConfig hadoopClusterConfig =
                hadoopManager.getCluster( accumuloClusterConfig.getHadoopClusterName() );
        ZookeeperClusterConfig zookeeperClusterConfig =
                zkManager.getCluster( accumuloClusterConfig.getZookeeperClusterName() );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, accumuloClusterConfig, hadoopClusterConfig, zookeeperClusterConfig,
                        ClusterOperationType.START_ALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID stopCluster( final String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        AccumuloClusterConfig accumuloClusterConfig = getCluster( clusterName );
        HadoopClusterConfig hadoopClusterConfig =
                hadoopManager.getCluster( accumuloClusterConfig.getHadoopClusterName() );
        ZookeeperClusterConfig zookeeperClusterConfig =
                zkManager.getCluster( accumuloClusterConfig.getZookeeperClusterName() );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, accumuloClusterConfig, hadoopClusterConfig, zookeeperClusterConfig,
                        ClusterOperationType.STOP_ALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String lxcHostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopManager, zkManager, clusterName, lxcHostName,
                        NodeOperationType.STATUS, null );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID addNode( final String clusterName, final String lxcHostname, final NodeType nodeType )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopManager, zkManager, clusterName, lxcHostname,
                        NodeOperationType.INSTALL, nodeType );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID destroyNode( final String clusterName, final String lxcHostName, final NodeType nodeType )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostName ), "Lxc hostname is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );

        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, hadoopManager, zkManager, clusterName, lxcHostName,
                        NodeOperationType.UNINSTALL, nodeType );

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
        nodeGroup.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        nodeGroup.setNumberOfNodes( config.getAllNodes().size() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        environmentBuildTask.setEnvironmentBlueprint( environmentBlueprint );

        return environmentBuildTask;
    }
}