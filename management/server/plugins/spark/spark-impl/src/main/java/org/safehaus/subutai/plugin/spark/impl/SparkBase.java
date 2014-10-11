package org.safehaus.subutai.plugin.spark.impl;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;


public abstract class SparkBase
{

    CommandRunner commandRunner;
    AgentManager agentManager;
    Tracker tracker;
    DbManager dbManager;
    EnvironmentManager environmentManager;
    Hadoop hadoopManager;
    ContainerManager containerManager;

    ExecutorService executor;
    PluginDAO pluginDAO;
    Commands commands;


    public void init()
    {
        commands = new Commands( commandRunner );
        pluginDAO = new PluginDAO( dbManager );
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


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public PluginDAO getPluginDAO()
    {
        return pluginDAO;
    }
}
