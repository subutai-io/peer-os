package org.safehaus.subutai.plugin.sqoop.impl;


import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDaoNew;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class SqoopBase implements Sqoop
{

    static final Logger LOG = LoggerFactory.getLogger( SqoopImpl.class );

    protected CommandRunner commandRunner;
    protected AgentManager agentManager;
    protected Tracker tracker;
    protected DbManager dbManager;
    protected PluginDaoNew pluginDAO;
    protected Hadoop hadoopManager;
    protected ContainerManager containerManager;
    protected EnvironmentManager environmentManager;
    public DataSource dataSource;

    protected ExecutorService executor;


    public void init()
    {
        try
        {
            this.pluginDAO = new PluginDaoNew( dataSource );
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


    public DbManager getDbManager()
    {
        return dbManager;
    }


    public void setDbManager( DbManager dbManager )
    {
        this.dbManager = dbManager;
    }


    public PluginDaoNew getPluginDao()
    {
        return pluginDAO;
    }


    public void setPluginDao( PluginDaoNew pluginDao )
    {
        this.pluginDAO = pluginDao;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
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


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( ExecutorService executor )
    {
        this.executor = executor;
    }


    public Logger getLogger()
    {
        return LOG;
    }
}
