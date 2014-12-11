package org.safehaus.subutai.plugin.cassandra.impl;


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
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.dao.PluginDAO;
import org.safehaus.subutai.plugin.cassandra.impl.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import org.safehaus.subutai.plugin.common.PluginDao;
//
//import org.safehaus.subutai.core.agent.api.AgentManager;


public class CassandraImpl implements Cassandra
{

    private static final Logger LOG = LoggerFactory.getLogger( CassandraImpl.class.getName() );
    private Tracker tracker;
    protected ExecutorService executor;
    private EnvironmentManager environmentManager;
    private PluginDAO pluginDAO;
    private DataSource dataSource;
    //    private ServiceLocator serviceLocator;


    public CassandraImpl( DataSource dataSource )
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


    /*public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }*/


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void init()
    {
        try
        {
            //            this.serviceLocator = new ServiceLocator();
            //            this.tracker = serviceLocator.getService( Tracker.class );
            //            this.environmentManager = serviceLocator.getService( EnvironmentManager.class );
            this.pluginDAO = new PluginDAO( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }

        executor = Executors.newSingleThreadExecutor();
    }


    public void destroy()
    {
        //        this.serviceLocator = null;
        this.tracker = null;
        this.environmentManager = null;
        this.pluginDAO = null;
        this.dataSource = null;
        this.executor.shutdown();
        this.executor = null;
    }


    public UUID installCluster( final CassandraClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new InstallClusterHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public UUID uninstallCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new UninstallClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public List<CassandraClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( CassandraClusterConfig.PRODUCT_KEY, CassandraClusterConfig.class );
    }


    @Override
    public CassandraClusterConfig getCluster( String clusterName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        return pluginDAO.getInfo( CassandraClusterConfig.PRODUCT_KEY, clusterName, CassandraClusterConfig.class );
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    @Override
    public UUID startCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new StartClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new CheckClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new StopClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID startService( final String clusterName, final UUID containerId )
    {
        AbstractOperationHandler operationHandler = new StartServiceHandler( this, clusterName, containerId );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopService( final String clusterName, final UUID containerId )
    {
        AbstractOperationHandler operationHandler = new StopServiceHandler( this, clusterName, containerId );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID statusService( final String clusterName, final UUID containerId )
    {
        AbstractOperationHandler operationHandler = new CheckServiceHandler( this, clusterName, containerId );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( final String clusterName, final String nodetype )
    {
        // TODO
        return null;
    }


    @Override
    public UUID destroyNode( final String clusterName, UUID containerId )
    {
        // TODO
        return null;
    }


    @Override
    public UUID checkNode( final String clusterName, final UUID containerId )
    {
        AbstractOperationHandler operationHandler = new CheckNodeHandler( this, clusterName, containerId );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment,
                                                         final CassandraClusterConfig config,
                                                         final TrackerOperation po )
    {
        return new CassandraSetupStrategy( environment, config, po, this );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final CassandraClusterConfig config )
    {

        EnvironmentBlueprint blueprint = new EnvironmentBlueprint();
        blueprint.setName( String.format( "%s-%s", config.getProductKey(), UUIDUtil.generateTimeBasedUUID() ) );

        blueprint.setLinkHosts( true );
        blueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        blueprint.setExchangeSshKeys( true );

        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setName( config.PRODUCT_NAME );
        nodeGroup.setLinkHosts( true );
        nodeGroup.setExchangeSshKeys( true );
        nodeGroup.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        nodeGroup.setTemplateName( config.getTemplateName() );
        nodeGroup.setPlacementStrategy( new PlacementStrategy( "ROUND_ROBIN" ) );
        nodeGroup.setNumberOfNodes( config.getNumberOfNodes() );

        blueprint.setNodeGroups( Sets.newHashSet( nodeGroup ) );

        return blueprint;
    }


    public UUID configureEnvironmentCluster( final CassandraClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new ConfigureEnvironmentClusterHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }
}
