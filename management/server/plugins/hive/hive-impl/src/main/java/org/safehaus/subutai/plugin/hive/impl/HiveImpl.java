package org.safehaus.subutai.plugin.hive.impl;


import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;
import org.safehaus.subutai.plugin.hive.impl.handler.NodeOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HiveImpl implements Hive
{
    private static final Logger LOG = LoggerFactory.getLogger( HiveImpl.class.getName() );
    private Tracker tracker;
    private ExecutorService executor;
    private EnvironmentManager environmentManager;
    private PluginDAO pluginDAO;
    private DataSource dataSource;
    private Hadoop hadoopManager;


    public HiveImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
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


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
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


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    @Override
    public UUID installCluster( HiveConfig config )
    {
//        AbstractOperationHandler h = new InstallHandler( this, config );
//        executor.execute( h );
//        return h.getTrackerId();
        return null;
    }


    @Override
    public UUID uninstallCluster( String clusterName )
    {
//        AbstractOperationHandler h = new UninstallHandler( this, clusterName );
//        executor.execute( h );
//        return h.getTrackerId();
        return null;
    }


    @Override
    public List<HiveConfig> getClusters()
    {
        return pluginDAO.getInfo( HiveConfig.PRODUCT_KEY, HiveConfig.class );
    }


    @Override
    public HiveConfig getCluster( String clusterName )
    {
        return pluginDAO.getInfo( HiveConfig.PRODUCT_KEY, clusterName, HiveConfig.class );
    }



    @Override
    public UUID installCluster( HiveConfig config, HadoopClusterConfig hc )
    {
//        InstallHandler h = new InstallHandler( this, config );
//        h.setHadoopConfig( hc );
//        executor.execute( h );
//        return h.getTrackerId();
        return null;
    }


    @Override
    public UUID statusCheck( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.STATUS );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID startNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.START );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID stopNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new NodeOperationHandler( this, clusterName, hostname, NodeOperationType.STOP );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID restartNode( String clusterName, String hostname )
    {
//        AbstractOperationHandler h = new RestartHandler( this, clusterName, hostname );
//        executor.execute( h );
//        return h.getTrackerId();
        return null;
    }


    @Override
    public UUID addNode( String clusterName, String hostname )
    {
//        AbstractOperationHandler h = new AddNodeHandler( this, clusterName, hostname );
//        executor.execute( h );
//        return h.getTrackerId();
        return null;
    }


    @Override
    public UUID destroyNode( String clusterName, String hostname )
    {
//        AbstractOperationHandler h = new DestroyNodeHandler( this, clusterName, hostname );
//        executor.execute( h );
//        return h.getTrackerId();
        return null;
    }


    @Override
    public Map<Agent, Boolean> isInstalled( Set<Agent> nodes )
    {
//        CheckInstallHandler h = new CheckInstallHandler( this );
//        return h.check( Product.HIVE, nodes );
        return null;
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, HiveConfig config, TrackerOperation po )
    {
//        if ( config.getSetupType() == SetupType.OVER_HADOOP )
//        {
//            return new SetupStrategyOverHadoop( env, this, config, po );
//        }
//        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
//        {
//            SetupStrategyWithHadoop s = new SetupStrategyWithHadoop( env, this, config, po );
//            s.setEnvironment( env );
//            return s;
//        }
        return null;
    }
}
