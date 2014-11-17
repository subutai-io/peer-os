package org.safehaus.subutai.plugin.flume.impl;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

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
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.flume.impl.handler.NodeOperationHandler;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FlumeImpl implements Flume
{

    private static final Logger LOG = LoggerFactory.getLogger( FlumeImpl.class.getName() );
    private Tracker tracker;
    private PluginDAO pluginDao;
    private EnvironmentManager environmentManager;
    private Hadoop hadoopManager;
    private DataSource dataSource;
    private ExecutorService executor;


    public FlumeImpl( final DataSource dataSource, final Tracker tracker, final EnvironmentManager environmentManager,
                      final Hadoop hadoopManager )
    {
        this.dataSource = dataSource;
        this.tracker = tracker;
        this.environmentManager = environmentManager;
        this.hadoopManager = hadoopManager;
    }

    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public PluginDAO getPluginDao()
    {
        return pluginDao;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }

    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void init()
    {
        try
        {
            this.pluginDao = new PluginDAO( dataSource );
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


    @Override
    public UUID installCluster( final FlumeConfig config )
    {
        ClusterOperationHandler h = new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        return null;
    }


    @Override
    public List<FlumeConfig> getClusters()
    {
        return pluginDao.getInfo( FlumeConfig.PRODUCT_KEY, FlumeConfig.class );
    }


    @Override
    public FlumeConfig getCluster( String clusterName )
    {
        return pluginDao.getInfo( FlumeConfig.PRODUCT_KEY, clusterName, FlumeConfig.class );
    }


    @Override
    public UUID installCluster( FlumeConfig config, HadoopClusterConfig hadoopConfig )
    {
        ClusterOperationHandler h = new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        h.setHadoopConfig( hadoopConfig );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID startNode( final String clusterName, final String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.START );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID stopNode( final String clusterName, final String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.STOP );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID checkNode( final String clusterName, final String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.STATUS );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID checkServiceStatus( final String clusterName, final String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.STATUS );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID addNode( final String clusterName, final String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.INSTALL );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID destroyNode( final String clusterName, final String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.UNINSTALL);
        executor.execute( h );
        return h.getTrackerId();
    }

    @Override
    public UUID uninstallCluster(FlumeConfig config) {
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.DESTROY );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }

    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint(FlumeConfig config) {
        EnvironmentBlueprint blueprint = new EnvironmentBlueprint();

        blueprint.setName( String.format( "%s-%s", config.getProductKey(), UUIDUtil.generateTimeBasedUUID() ) );
        blueprint.setExchangeSshKeys( true );
        blueprint.setLinkHosts( true );
        blueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );

        NodeGroup ng = new NodeGroup();
        ng.setName( "Default" );
        ng.setNumberOfNodes( config.getNodes().size() ); // master +slaves
        ng.setTemplateName( FlumeConfig.TEMPLATE_NAME );
        ng.setPlacementStrategy( new PlacementStrategy( "MORE_RAM" ) );
        blueprint.setNodeGroups( Sets.newHashSet(ng) );

        return blueprint;

    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, FlumeConfig config, TrackerOperation po )
    {
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            return new OverHadoopSetupStrategy( this, config, po, env );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            WithHadoopSetupStrategy s = new WithHadoopSetupStrategy( this, config, po );
            s.setEnvironment( env );
            return s;
        }
        return null;
    }
}
