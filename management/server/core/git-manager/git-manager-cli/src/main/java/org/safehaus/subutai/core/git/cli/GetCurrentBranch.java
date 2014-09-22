package org.safehaus.subutai.core.git.cli;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitBranch;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the current git branch
 */
@Command( scope = "git", name = "get-current-branch", description = "Get current branch" )
public class GetCurrentBranch extends OsgiCommandSupport
{
    protected static final Logger LOG = Logger.getLogger( GetCurrentBranch.class.getName() );


    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname" )
    String hostname;
    @Argument( index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
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
            GitBranch gitBranch = gitManager.currentBranch( agent, repoPath );
            LOG.info( gitBranch.toString() );
        }
        catch ( GitException e )
        {
            LOG.log( Level.SEVERE, "Error in doExecute", e );
        }

        return null;
    }
}
