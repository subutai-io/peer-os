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
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDao;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;
import org.safehaus.subutai.plugin.hive.impl.handler.AddNodeHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.CheckInstallHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.DestroyNodeHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.RestartHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.StartHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.StatusHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.StopHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.UninstallHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HiveImpl implements Hive
{

    protected CommandRunner commandRunner;
    protected AgentManager agentManager;
    protected Tracker tracker;
    protected ContainerManager containerManager;
    protected EnvironmentManager environmentManager;
    protected Hadoop hadoopManager;
    private static final Logger LOG = LoggerFactory.getLogger( HiveImpl.class.getName() );
    private DataSource dataSource;
    private PluginDao pluginDao;
    protected ExecutorService executor;


    public HiveImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public void init()
    {
        try
        {
            this.pluginDao = new PluginDao( dataSource );
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


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( ContainerManager containerManager )
    {
        this.containerManager = containerManager;
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


    public PluginDao getPluginDao()
    {
        return pluginDao;
    }


    @Override
    public UUID installCluster( HiveConfig config )
    {
        AbstractOperationHandler h = new InstallHandler( this, config );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( String clusterName )
    {
        AbstractOperationHandler h = new UninstallHandler( this, clusterName );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public List<HiveConfig> getClusters()
    {
        return pluginDao.getInfo( HiveConfig.PRODUCT_KEY, HiveConfig.class );
    }


    @Override
    public HiveConfig getCluster( String clusterName )
    {
        return pluginDao.getInfo( HiveConfig.PRODUCT_KEY, clusterName, HiveConfig.class );
    }


    @Override
    public UUID installCluster( HiveConfig config, HadoopClusterConfig hc )
    {
        InstallHandler h = new InstallHandler( this, config );
        h.setHadoopConfig( hc );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID statusCheck( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StatusHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID startNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StartHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID stopNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new StopHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID restartNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new RestartHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID addNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new AddNodeHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public UUID destroyNode( String clusterName, String hostname )
    {
        AbstractOperationHandler h = new DestroyNodeHandler( this, clusterName, hostname );
        executor.execute( h );
        return h.getTrackerId();
    }


    @Override
    public Map<Agent, Boolean> isInstalled( Set<Agent> nodes )
    {
        CheckInstallHandler h = new CheckInstallHandler( this );
        return h.check( Product.HIVE, nodes );
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, HiveConfig config, TrackerOperation po )
    {
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            return new SetupStrategyOverHadoop( this, config, po );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            SetupStrategyWithHadoop s = new SetupStrategyWithHadoop( this, config, po );
            s.setEnvironment( env );
            return s;
        }
        return null;
    }
}
