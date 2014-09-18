package org.safehaus.subutai.core.container.impl;


import java.util.Set;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.monitor.api.Monitor;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.template.api.TemplateManager;


public abstract class ContainerManagerBase implements ContainerManager {

    public StrategyManager strategyManager;
    AgentManager agentManager;
    CommandRunner commandRunner;
    TemplateManager templateManager;
    TemplateRegistryManager templateRegistry;
    DbManager dbManager;
    Monitor monitor;


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void setCommandRunner( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public TemplateManager getTemplateManager()
    {
        return templateManager;
    }


    public void setTemplateManager( TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    public TemplateRegistryManager getTemplateRegistry()
    {
        return templateRegistry;
    }


    public void setTemplateRegistry( TemplateRegistryManager templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public DbManager getDbManager()
    {
        return dbManager;
    }


    public void setDbManager( DbManager dbManager )
    {
        this.dbManager = dbManager;
    }


    public StrategyManager getStrategyManager()
    {
        return strategyManager;
    }


    public void setStrategyManager( final StrategyManager strategyManager )
    {
        this.strategyManager = strategyManager;
    }


    public Monitor getMonitor()
    {
        return monitor;
    }


    public void setMonitor( final Monitor monitor )
    {
        this.monitor = monitor;
    }


    public abstract void destroy( String hostName, Set<String> cloneNames ) throws ContainerDestroyException;
}
