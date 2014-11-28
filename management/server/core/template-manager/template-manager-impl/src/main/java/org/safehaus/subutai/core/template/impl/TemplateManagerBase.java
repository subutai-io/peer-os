package org.safehaus.subutai.core.template.impl;


import org.safehaus.subutai.core.agent.api.AgentManager;
//import org.safehaus.subutai.core.apt.api.AptRepositoryManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.template.api.TemplateManager;


public abstract class TemplateManagerBase implements TemplateManager
{

    CommandRunner commandRunner;
    AgentManager agentManager;
    TemplateRegistry templateRegistry;
//    AptRepositoryManager repoManager;

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


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    public void setTemplateRegistry( TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


//    public AptRepositoryManager getRepoManager()
//    {
//        return repoManager;
//    }
//
//
//    public void setRepoManager( AptRepositoryManager repoManager )
//    {
//        this.repoManager = repoManager;
//    }


    public void init()
    {
        scriptExecutor = new ScriptExecutor( commandRunner );
    }


    public void setScriptExecutor( ScriptExecutor scriptExecutor )
    {
        this.scriptExecutor = scriptExecutor;
    }


    public void destroy()
    {

    }
}
