package org.safehaus.subutai.impl.container;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.manager.TemplateManager;

public abstract class ContainerManagerBase implements ContainerManager {

    LxcManager lxcManager;
    AgentManager agentManager;
    CommandRunner commandRunner;
    TemplateManager templateManager;
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

    public DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

}
