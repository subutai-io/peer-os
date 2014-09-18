package org.safehaus.subutai.core.git.cli;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Clones remote master repo
 */
@Command( scope = "git", name = "clone", description = "Clone master repo" )
public class Clone extends OsgiCommandSupport {

    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname" )
    String hostname;
    @Argument( index = 1, name = "new branch name", required = true, multiValued = false,
            description = "name of branch to create" )
    String newBranchName;
    @Argument( index = 2, name = "target directory", required = true, multiValued = false,
            description = "directory to clone to" )
    String targetDirectory;
    private AgentManager agentManager;
    private GitManager gitManager;


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setGitManager( final GitManager gitManager )
    {
        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {

        Agent agent = agentManager.getAgentByHostname( hostname );

        try
        {
            gitManager.clone( agent, newBranchName, targetDirectory );
        }
        catch ( GitException e )
        {
            System.out.println( e );
        }

        return null;
    }
}
