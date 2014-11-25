package org.safehaus.subutai.core.container.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.monitor.api.Monitoring;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.template.api.TemplateManager;

//import org.safehaus.subutai.core.db.api.DbManager;


public abstract class ContainerManagerBase implements ContainerManager
{

    AgentManager agentManager;
    QuotaManager quotaManager;
    CommandRunner commandRunner;
    TemplateManager templateManager;
    TemplateRegistry templateRegistry;
    //    DbManager dbManager;
    Monitoring monitoring;
    StrategyManager strategyManager;
    List<ContainerPlacementStrategy> placementStrategies =
            Collections.synchronizedList( new ArrayList<ContainerPlacementStrategy>() );


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public QuotaManager getQuotaManager()
    {
        return quotaManager;
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


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    public void setTemplateRegistry( TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    /*public DbManager getDbManager()
    {
        return dbManager;
    }


    public void setDbManager( DbManager dbManager )
    {
        this.dbManager = dbManager;
    }*/


    public List<ContainerPlacementStrategy> getPlacementStrategies()
    {
        return placementStrategies;
    }


    public void setPlacementStrategies( List<ContainerPlacementStrategy> placementStrategies )
    {
        this.placementStrategies = placementStrategies;
    }


    public Monitoring getMonitoring()
    {
        return monitoring;
    }


    public void setMonitoring( final Monitoring monitoring )
    {
        this.monitoring = monitoring;
    }


    public StrategyManager getStrategyManager()
    {
        return strategyManager;
    }


    public void setStrategyManager( final StrategyManager strategyManager )
    {
        this.strategyManager = strategyManager;
    }


    public abstract void destroy( String hostName, Set<String> cloneNames ) throws ContainerDestroyException;
}
