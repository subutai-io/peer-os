package org.safehaus.subutai.core.template.impl;


import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;
import org.safehaus.subutai.core.template.api.TemplateManager;


public abstract class TemplateManagerBase implements TemplateManager
{

    CommandRunner commandRunner;
    AgentManager agentManager;
    TemplateRegistryManager templateRegistry;
    AptRepositoryManager repoManager;

    ScriptExecutor scriptExecutor;


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


    public TemplateRegistryManager getTemplateRegistry()
    {
        return templateRegistry;
    }


    public void setTemplateRegistry( TemplateRegistryManager templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public AptRepositoryManager getRepoManager()
    {
        return repoManager;
    }


    public void setRepoManager( AptRepositoryManager repoManager )
    {
        this.repoManager = repoManager;
    }


    public void init()
    {
        scriptExecutor = new ScriptExecutor( commandRunner );
    }


    public void destroy()
    {

    }
}
