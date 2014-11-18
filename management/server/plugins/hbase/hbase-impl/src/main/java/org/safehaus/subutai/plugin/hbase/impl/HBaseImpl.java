package org.safehaus.subutai.plugin.hbase.impl;


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
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.api.SetupType;
import org.safehaus.subutai.plugin.hbase.impl.handler.ClusterOperationHandler;
import org.safehaus.subutai.plugin.hbase.impl.handler.NodeOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class HBaseImpl implements HBase
{

    private static final Logger LOG = LoggerFactory.getLogger( HBaseImpl.class.getName() );
    private Hadoop hadoopManager;
    private Tracker tracker;
    private ExecutorService executor;
    private EnvironmentManager environmentManager;
    private PluginDAO pluginDAO;
    private DataSource dataSource;
    private Commands commands;


    public HBaseImpl( DataSource dataSource )
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


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
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
        this.executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public Commands getCommands()
    {
        return commands;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public UUID installCluster( final HBaseConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        Preconditions.checkNotNull( clusterName );
        HBaseConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, config, ClusterOperationType.UNINSTALL );
        return operationHandler.getTrackerId();
    }


    @Override
    public List<HBaseConfig> getClusters()
    {
        return pluginDAO.getInfo( HBaseConfig.PRODUCT_KEY, HBaseConfig.class );
    }


    @Override
    public HBaseConfig getCluster( String clusterName )
    {
        Preconditions.checkNotNull( clusterName );
        return pluginDAO.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
    }


    @Override
    public UUID addNode( final String clusterName, final String hostname )
    {
        Preconditions.checkNotNull( clusterName );
        Preconditions.checkNotNull( hostname );
        HBaseConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, config, hostname, OperationType.INCLUDE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }



    /*public UUID installCluster( final HBaseConfig config )
    {
        Preconditions.checkNotNull( config, "HBase configuration is null" );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( this, config, ClusterOperationType.INSTALL );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }*/


    @Override
    public UUID installCluster( final HBaseConfig config, final HadoopClusterConfig hadoopConfig )
    {
        return null;
    }


    @Override
    public UUID destroyNode( final String clusterName, final String hostname )
    {
        Preconditions.checkNotNull( clusterName );
        Preconditions.checkNotNull( hostname );
        HBaseConfig config = getCluster( clusterName );
        AbstractOperationHandler operationHandler =
                new NodeOperationHandler( this, config, hostname, OperationType.EXCLUDE );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final TrackerOperation po, final HBaseConfig config,
                                                         final Environment environment )
    {
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {

            return new OverHadoopSetupStrategy( this, config, environment, po );
        }
        else
        {
            return new WithHadoopSetupStrategy( this, config, environment, po );
        }
    }
}

