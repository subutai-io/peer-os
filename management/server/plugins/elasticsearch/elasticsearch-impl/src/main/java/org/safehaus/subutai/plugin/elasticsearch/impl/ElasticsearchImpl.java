package org.safehaus.subutai.plugin.elasticsearch.impl;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.impl.handler.NodeOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ElasticsearchImpl implements Elasticsearch
{
    private static final Logger LOG = LoggerFactory.getLogger( ElasticsearchImpl.class.getName() );
    private Tracker tracker;
    protected ExecutorService executor;
    private EnvironmentManager environmentManager;
    private PluginDAO pluginDAO;
    private DataSource dataSource;


    public ElasticsearchImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( final PluginDAO pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public void setDataSource( final DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public Tracker getTracker()
    {
        return tracker;
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


    public UUID installCluster( final ElasticsearchClusterConfiguration config )
    {
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( String clusterName )
    {
        return null;
    }


    @Override
    public List<ElasticsearchClusterConfiguration> getClusters()
    {
        return pluginDAO
                .getInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, ElasticsearchClusterConfiguration.class );
    }


    @Override
    public ElasticsearchClusterConfiguration getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        return pluginDAO.getInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, clusterName,
                ElasticsearchClusterConfiguration.class );
    }


    @Override
    public UUID startAllNodes( final ElasticsearchClusterConfiguration config )
    {
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.START_ALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkAllNodes( final ElasticsearchClusterConfiguration config )
    {
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.STATUS_ALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopAllNodes( final ElasticsearchClusterConfiguration config )
    {
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.STOP_ALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( final String clusterName, final String lxcHostname )
    {
        // TODO
        return null;
    }


    @Override
    public UUID checkNode( final String clusterName, final String hostname )
    {
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.STATUS );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startNode( final String clusterName, final String hostname )
    {
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.START );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNode( final String clusterName, final String hostname )
    {
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.STOP );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxcHostname )
    {
        // TODO
        return null;
    }


    @Override
    public UUID uninstallCluster( final ElasticsearchClusterConfiguration config )
    {
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.UNINSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment,
                                                         final ElasticsearchClusterConfiguration
                                                                 elasticsearchClusterConfiguration,
                                                         final TrackerOperation po )
    {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( elasticsearchClusterConfiguration, "Zookeeper cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        return new ESSetupStrategy( environment, elasticsearchClusterConfiguration, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final ElasticsearchClusterConfiguration config )
    {

        EnvironmentBlueprint blueprint = new EnvironmentBlueprint();
        blueprint.setName( String.format( "%s-%s", config.getProductKey(), UUIDUtil.generateTimeBasedUUID() ) );

        blueprint.setLinkHosts( true );
        blueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        blueprint.setExchangeSshKeys( true );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( new PlacementStrategy("ROUND_ROBIN") );
        nodeGroup.setNumberOfNodes( config.getNumberOfNodes() );

        blueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        return blueprint;
    }


    @Override
    public UUID configureEnvironmentCluster( final ElasticsearchClusterConfiguration config )
    {
        return null;
    }
}
