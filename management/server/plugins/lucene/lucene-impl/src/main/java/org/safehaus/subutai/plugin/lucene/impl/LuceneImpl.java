package org.safehaus.subutai.plugin.lucene.impl;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDaoNew;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.lucene.api.Lucene;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;
import org.safehaus.subutai.plugin.lucene.api.SetupType;
import org.safehaus.subutai.plugin.lucene.impl.handler.AddNodeOperationHandler;
import org.safehaus.subutai.plugin.lucene.impl.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.plugin.lucene.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.lucene.impl.handler.UninstallOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class LuceneImpl implements Lucene
{

    protected Commands commands;
    private static final Logger LOG = LoggerFactory.getLogger( LuceneImpl.class.getName() );
    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Tracker tracker;
    private Hadoop hadoopManager;
    private ExecutorService executor;
    private EnvironmentManager environmentManager;
    private ContainerManager containerManager;
    private DataSource dataSource;
    private PluginDaoNew pluginDao;


    public LuceneImpl( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public Commands getCommands()
    {
        return commands;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public PluginDaoNew getPluginDao()
    {
        return pluginDao;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setCommandRunner( final CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
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


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public void init()
    {
        try
        {
            this.pluginDao = new PluginDaoNew( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
        this.commands = new Commands( commandRunner );
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    @Override
    public UUID installCluster( final LuceneConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public List<LuceneConfig> getClusters()
    {
        return pluginDao.getInfo( LuceneConfig.PRODUCT_KEY, LuceneConfig.class );
    }


    @Override
    public LuceneConfig getCluster( String clusterName )
    {
        return pluginDao.getInfo( LuceneConfig.PRODUCT_KEY, clusterName, LuceneConfig.class );
    }


    @Override
    public UUID installCluster( LuceneConfig config, HadoopClusterConfig hadoopConfig )
    {
        InstallOperationHandler operationHandler = new InstallOperationHandler( this, config );
        operationHandler.setHadoopConfig( hadoopConfig );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID addNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroyNode( final String clusterName, final String lxcHostname )
    {
        AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler( this, clusterName, lxcHostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( Environment env, LuceneConfig config, TrackerOperation po )
    {
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            return new OverHadoopSetupStrategy( this, config, po );
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
