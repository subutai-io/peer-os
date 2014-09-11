package org.safehaus.subutai.impl.containermanager;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.containermanager.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.strategymanager.ContainerPlacementStrategy;
import org.safehaus.subutai.api.template.manager.TemplateManager;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ContainerManagerBase implements ContainerManager {

	AgentManager agentManager;
	CommandRunner commandRunner;
	TemplateManager templateManager;
	TemplateRegistryManager templateRegistry;
	DbManager dbManager;
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
}
