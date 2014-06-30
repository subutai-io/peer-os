package org.safehaus.subutai.impl.manager;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.manager.TemplateManager;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

public abstract class TemplateManagerBase implements TemplateManager {

    CommandRunner commandRunner;
    AgentManager agentManager;
    TemplateRegistryManager templateRegistry;

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

    public void init() {
        scriptExecutor = new ScriptExecutor(commandRunner);
    }

    public void destroy() {

    }

}
