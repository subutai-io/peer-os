package org.safehaus.subutai.plugin.hbase.impl;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.SetupType;
import org.safehaus.subutai.plugin.hbase.impl.handler.CheckClusterHandler;
import org.safehaus.subutai.plugin.hbase.impl.handler.CheckNodeHandler;
import org.safehaus.subutai.plugin.hbase.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.hbase.impl.handler.StartClusterHandler;
import org.safehaus.subutai.plugin.hbase.impl.handler.StopClusterHandler;
import org.safehaus.subutai.plugin.hbase.impl.handler.UninstallHandler;

import com.google.common.base.Preconditions;


public class HBaseImpl implements HBase
{

    AgentManager agentManager;
    Hadoop hadoopManager;
    DbManager dbManager;
    Tracker tracker;
    ExecutorService executor;
    CommandRunner commandRunner;
    EnvironmentManager environmentManager;
    ContainerManager containerManager;
    PluginDAO pluginDAO;
    Commands commands;


    public HBaseImpl()
    {

    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }


    public void setPluginDAO( final PluginDAO pluginDAO )
    {
        this.pluginDAO = pluginDAO;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public DbManager getDbManager()
    {
        return dbManager;
    }


    public void setDbManager( DbManager dbManager )
    {
        this.dbManager = dbManager;
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


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public void init()
    {
        this.pluginDAO = new PluginDAO( dbManager );
        this.commands = new Commands( commandRunner );

        commands = new Commands( commandRunner );
        executor = Executors.newCachedThreadPool();
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


    public UUID installCluster( final HBaseClusterConfig config )
    {
        Preconditions.checkNotNull( config, "Configuration is null" );
        AbstractOperationHandler operationHandler = new InstallHandler( this, config );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    public List<HBaseClusterConfig> getClusters()
    {
        return pluginDAO.getInfo( HBaseClusterConfig.PRODUCT_KEY, HBaseClusterConfig.class );
    }


    @Override
    public UUID startCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new StartClusterHandler( this, clusterName );
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
    public UUID checkCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new CheckClusterHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( final Environment environment, final HBaseClusterConfig config,
                                                         final ProductOperation po )
    {
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            return new OverHadoopSetupStrategy( this, po, config );
        }
        else
        {
            return new WithHadoopSetupStrategy( environment, this, po, config );
        }
    }


    @Override
    public EnvironmentBuildTask getDefaultEnvironmentBlueprint( final HBaseClusterConfig config )
    {
        return null;
    }


    @Override
    public UUID checkNode( final String clustername, final String lxchostname )
    {
        AbstractOperationHandler operationHandler = new CheckNodeHandler( this, clustername, lxchostname );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public UUID destroyNode( final String clustername, final String lxchostname, final String nodetype )
    {
        return null;
    }


    @Override
    public UUID addNode( final String clustername, final String lxchostname, final String nodetype )
    {
        return null;
    }


    @Override
    public UUID destroyCluster( final String clusterName )
    {
        return null;
    }


    public UUID uninstallCluster( final String clusterName )
    {
        AbstractOperationHandler operationHandler = new UninstallHandler( this, clusterName );
        executor.execute( operationHandler );
        return operationHandler.getTrackerId();
    }


    @Override
    public HBaseClusterConfig getCluster( String clusterName )
    {
        return pluginDAO.getInfo( HBaseClusterConfig.PRODUCT_KEY, clusterName, HBaseClusterConfig.class );
    }
}
