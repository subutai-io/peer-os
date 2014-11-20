package org.safehaus.subutai.plugin.nutch.impl;


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
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.nutch.api.Nutch;
import org.safehaus.subutai.plugin.nutch.api.NutchConfig;
import org.safehaus.subutai.plugin.nutch.api.SetupType;
import org.safehaus.subutai.plugin.nutch.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.nutch.impl.handler.NodeOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class NutchImpl implements Nutch
{

    protected Commands commands;
    private static final Logger LOG = LoggerFactory.getLogger( NutchImpl.class.getName() );
    private Tracker tracker;
    private Hadoop hadoopManager;
    private ExecutorService executor;
    private PluginDAO pluginDao;
    private EnvironmentManager environmentManager;
    private DataSource dataSource;


    public NutchImpl( final DataSource dataSource, final Tracker tracker, final EnvironmentManager environmentManager,
                      final Hadoop hadoopManager )
    {
        this.dataSource = dataSource;
        this.tracker = tracker;
        this.environmentManager = environmentManager;
        this.hadoopManager = hadoopManager;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }

    public Commands getCommands()
    {
        return commands;
    }

    public Tracker getTracker()
    {
        return tracker;
    }

    public PluginDAO getPluginDao()
    {
        return pluginDao;
    }

    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }

    public void setCommands( final Commands commands )
    {
        this.commands = commands;
    }

    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }

    public void setHadoopManager( final Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
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
    public UUID installCluster( final NutchConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        return null;
    }


    @Override
    public List<NutchConfig> getClusters()
    {
        return pluginDao.getInfo( NutchConfig.PRODUCT_KEY, NutchConfig.class );
    }


    @Override
    public NutchConfig getCluster( String clusterName )
    {
        return pluginDao.getInfo( NutchConfig.PRODUCT_KEY, clusterName, NutchConfig.class );
    }


    @Override
    public UUID installCluster( NutchConfig config, HadoopClusterConfig hadoopConfig )
    {
        ClusterOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        operationHandler.setHadoopConfig( hadoopConfig );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new NodeOperationHandler( this, clusterName, lxcHostname, NodeOperationType.INSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new NodeOperationHandler( this, clusterName, lxcHostname, NodeOperationType.UNINSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final NutchConfig config )
    {
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.DESTROY );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final NutchConfig config )
    {
        EnvironmentBlueprint blueprint = new EnvironmentBlueprint();

        blueprint.setName( String.format( "%s-%s", config.getProductKey(), UUIDUtil.generateTimeBasedUUID() ) );
        blueprint.setExchangeSshKeys( true );
        blueprint.setLinkHosts( true );
        blueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );

        NodeGroup ng = new NodeGroup();
        ng.setName( "Default" );
        ng.setNumberOfNodes( config.getNodes().size() ); // master +slaves
        ng.setTemplateName( NutchConfig.TEMPLATE_NAME );
        ng.setPlacementStrategy( new PlacementStrategy( "MORE_RAM" ) );
        blueprint.setNodeGroups( Sets.newHashSet( ng ) );


        return blueprint;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, NutchConfig config, TrackerOperation po )
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
