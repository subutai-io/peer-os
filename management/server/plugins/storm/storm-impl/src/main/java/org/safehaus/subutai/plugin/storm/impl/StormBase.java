package org.safehaus.subutai.plugin.storm.impl;


import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDao;
import org.safehaus.subutai.plugin.storm.api.Storm;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class StormBase implements Storm
{

    private static final Logger LOG = LoggerFactory.getLogger( StormImpl.class.getName() );
    protected CommandRunner commandRunner;
    protected AgentManager agentManager;
    protected Tracker tracker;
    protected Zookeeper zookeeperManager;
    protected ContainerManager containerManager;
    protected EnvironmentManager environmentManager;

    protected PluginDao pluginDao;
    protected ExecutorService executor;
    protected DataSource dataSource;


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


    public Zookeeper getZookeeperManager()
    {
        return zookeeperManager;
    }


    public void setZookeeperManager( Zookeeper zookeeperManager )
    {
        this.zookeeperManager = zookeeperManager;
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


    public PluginDao getPluginDao()
    {
        return pluginDao;
    }


    public void setPluginDao( PluginDao pluginDao )
    {
        this.pluginDao = pluginDao;
    }


    public Logger getLogger()
    {
        return LOG;
    }
}
