package org.safehaus.subutai.core.container.impl;

import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.monitor.api.Monitor;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.template.api.TemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public abstract class ContainerManagerBase implements ContainerManager {

	AgentManager agentManager;
	CommandRunner commandRunner;
	TemplateManager templateManager;
	TemplateRegistryManager templateRegistry;
	DbManager dbManager;
    Monitor monitor;
    StrategyManager strategyManager;
    List<ContainerPlacementStrategy> placementStrategies = Collections.synchronizedList(new ArrayList<ContainerPlacementStrategy>());

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void setCommandRunner(CommandRunner commandRunner) {
		this.commandRunner = commandRunner;
	}

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public TemplateRegistryManager getTemplateRegistry() {
		return templateRegistry;
	}

	public void setTemplateRegistry(TemplateRegistryManager templateRegistry) {
		this.templateRegistry = templateRegistry;
	}

	public DbManager getDbManager() {
		return dbManager;
	}

	public void setDbManager(DbManager dbManager) {
		this.dbManager = dbManager;
	}

    public List<ContainerPlacementStrategy> getPlacementStrategies() {
        return placementStrategies;
    }

    public void setPlacementStrategies(List<ContainerPlacementStrategy> placementStrategies) {
        this.placementStrategies = placementStrategies;
    }


    public Monitor getMonitor()
    {
        return monitor;
    }


    public void setMonitor( final Monitor monitor )
    {
        this.monitor = monitor;
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
