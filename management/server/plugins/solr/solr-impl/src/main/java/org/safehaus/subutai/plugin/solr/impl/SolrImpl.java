package org.safehaus.subutai.plugin.solr.impl;


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
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.solr.impl.handler.NodeOperationHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class SolrImpl implements Solr
{

    private static final Logger LOG = LoggerFactory.getLogger( SolrImpl.class.getName() );
    private Tracker tracker;
    private EnvironmentManager environmentManager;
    private ExecutorService executor;
    private PluginDAO pluginDAO;
    private DataSource dataSource;


    public SolrImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
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


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }

    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public UUID installCluster( final SolrClusterConfig solrClusterConfig )
    {

        Preconditions.checkNotNull( solrClusterConfig, "Configuration is null" );

        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, solrClusterConfig, ClusterOperationType.INSTALL );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        return null;
    }


    @Override
    public UUID uninstallCluster( final SolrClusterConfig config )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( config.getClusterName() ), "Cluster name is null or empty" );

        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.UNINSTALL );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public List<SolrClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( SolrClusterConfig.PRODUCT_KEY, SolrClusterConfig.class );
    }


    @Override
    public SolrClusterConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );

        return pluginDAO.getInfo( SolrClusterConfig.PRODUCT_KEY, clusterName, SolrClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxcHostname )
    {
        return null;
    }


    public UUID startNode( final String clusterName, final String hostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new NodeOperationHandler( this, clusterName, hostName, NodeOperationType.START );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID stopNode( final String clusterName, final String hostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new NodeOperationHandler( this, clusterName, hostName, NodeOperationType.STOP );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }


    public UUID checkNode( final String clusterName, final String hostName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostName ), "Lxc hostname is null or empty" );


        AbstractOperationHandler operationHandler = new NodeOperationHandler( this, clusterName, hostName, NodeOperationType.STATUS );

        executor.execute( operationHandler );

        return operationHandler.getTrackerId();
    }

    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final SolrClusterConfig config,
                                                         final TrackerOperation po )
    {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Solr cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );

        return new SolrSetupStrategy( this, po, config, environment );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( SolrClusterConfig config )
    {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint
                .setName( String.format( "%s-%s", SolrClusterConfig.PRODUCT_KEY, UUIDUtil.generateTimeBasedUUID() ) );

        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        environmentBlueprint.setExchangeSshKeys( true );

        //1 node group
        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        nodeGroup.setNumberOfNodes( config.getNumberOfNodes() );

        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        return environmentBlueprint;
    }
    public UUID configureEnvironmentCluster( final SolrClusterConfig config )
    {
        return null;
    }
}
