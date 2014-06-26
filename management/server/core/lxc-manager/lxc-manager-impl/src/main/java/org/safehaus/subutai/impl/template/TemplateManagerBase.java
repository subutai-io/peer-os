package org.safehaus.subutai.impl.template;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.template.TemplateManager;

public abstract class TemplateManagerBase implements TemplateManager {

    CommandRunner commandRunner;
    AgentManager agentManager;
    LxcManager lxcManager;

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

    public LxcManager getLxcManager() {
        return lxcManager;
    }

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public void init() {
        scriptExecutor = new ScriptExecutor(commandRunner);
    }

    public void destroy() {

    }

}
