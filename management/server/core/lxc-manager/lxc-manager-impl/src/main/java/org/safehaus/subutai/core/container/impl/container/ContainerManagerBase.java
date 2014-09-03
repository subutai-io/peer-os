package org.safehaus.subutai.core.container.impl.container;

import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.template.api.TemplateManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;

public abstract class ContainerManagerBase implements ContainerManager {

	LxcManager lxcManager;
	AgentManager agentManager;
	CommandRunner commandRunner;
	TemplateManager templateManager;
	TemplateRegistryManager templateRegistry;
	DbManager dbManager;

	public LxcManager getLxcManager() {
		return lxcManager;
	}

	public void setLxcManager(LxcManager lxcManager) {
		this.lxcManager = lxcManager;
	}

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

}
