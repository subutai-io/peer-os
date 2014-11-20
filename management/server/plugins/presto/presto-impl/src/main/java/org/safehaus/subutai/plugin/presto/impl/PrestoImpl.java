package org.safehaus.subutai.plugin.presto.impl;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.plugin.presto.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.handler.NodeOperationHanler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class PrestoImpl implements Presto
{
    private static final Logger LOG = LoggerFactory.getLogger( PrestoImpl.class.getName() );
    private Tracker tracker;
    private PluginDAO pluginDAO;
    private EnvironmentManager environmentManager;
    private Hadoop hadoopManager;
    private DataSource dataSource;
    private ExecutorService executor;
    Commands commands;

    public PrestoImpl( final DataSource dataSource, final Tracker tracker, final EnvironmentManager environmentManager,
                       final Hadoop hadoopManager )
    {

        this.dataSource = dataSource;
        this.tracker = tracker;
        this.environmentManager = environmentManager;
        this.hadoopManager = hadoopManager;
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
        this.commands = new Commands();

        executor = Executors.newCachedThreadPool();
    }

    public Commands getCommands()
    {
        return commands;
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( PluginDAO pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }


    @Override
    public UUID installCluster( final PrestoClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }

    @Override
    public UUID installCluster( PrestoClusterConfig config, HadoopClusterConfig hadoopConfig )
    {
        ClusterOperationHandler h = new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        h.setHadoopConfig( hadoopConfig );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final PrestoClusterConfig config )
    {
        AbstractOperationHandler operationHandler = new ClusterOperationHandler( this, config, ClusterOperationType.DESTROY );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        return null;
    }


    @Override
    public List<PrestoClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( PrestoClusterConfig.PRODUCT_KEY, PrestoClusterConfig.class );
    }


    @Override
    public PrestoClusterConfig getCluster( String clusterName )
    {
        return pluginDAO.getInfo( PrestoClusterConfig.PRODUCT_KEY, clusterName, PrestoClusterConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String agentHostName )
    {
        return null;
    }



    @Override
    public UUID addWorkerNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler = new NodeOperationHanler( this, clusterName, lxcHostname,
                NodeOperationType.INSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroyWorkerNode( final String clusterName, final String lxcHostname )
    {

        AbstractOperationHandler operationHandler =
                new NodeOperationHanler( this, clusterName, lxcHostname, NodeOperationType.UNINSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }

    @Override
    public UUID startNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new NodeOperationHanler( this, clusterName, lxcHostname, NodeOperationType.START );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID stopNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new NodeOperationHanler( this, clusterName, lxcHostname, NodeOperationType.STOP );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID checkNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new NodeOperationHanler( this, clusterName, lxcHostname, NodeOperationType.STATUS );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final TrackerOperation po, final PrestoClusterConfig config,
                                                         final Environment environment )
    {

        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            return new SetupStrategyOverHadoop( po, this, config, environment );
        }

        /*if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            SetupStrategyWithHadoop s = new SetupStrategyWithHadoop( po, this, config );
            s.setEnvironment( environment );
            return s;
        }*/

        return null;
    }
}
