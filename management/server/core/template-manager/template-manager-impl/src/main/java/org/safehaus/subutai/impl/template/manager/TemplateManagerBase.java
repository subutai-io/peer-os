package org.safehaus.subutai.impl.template.manager;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.aptrepositorymanager.AptRepositoryManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.template.manager.TemplateManager;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

public abstract class TemplateManagerBase implements TemplateManager {

	CommandRunner commandRunner;
	AgentManager agentManager;
	TemplateRegistryManager templateRegistry;
	AptRepositoryManager repoManager;

	ScriptExecutor scriptExecutor;

	public CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void setCommandRunner(CommandRunner commandRunner) {
		this.commandRunner = commandRunner;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public TemplateRegistryManager getTemplateRegistry() {
		return templateRegistry;
	}

	public void setTemplateRegistry(TemplateRegistryManager templateRegistry) {
		this.templateRegistry = templateRegistry;
	}

	public AptRepositoryManager getRepoManager() {
		return repoManager;
	}

	public void setRepoManager(AptRepositoryManager repoManager) {
		this.repoManager = repoManager;
	}

	public void init() {
		scriptExecutor = new ScriptExecutor(commandRunner);
	}

	public void destroy() {

	}

}
