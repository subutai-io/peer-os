package org.safehaus.subutai.core.git.cli;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitBranch;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Displays the current git branch
 */
@Command(scope = "git", name = "get-current-branch", description = "Get current branch")
public class GetCurrentBranch extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( GetCurrentBranch.class.getName() );


    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
    String repoPath;

    private final GitManager gitManager;
    private final AgentManager agentManager;


    public GetCurrentBranch( final GitManager gitManager, final AgentManager agentManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.gitManager = gitManager;
        this.agentManager = agentManager;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    protected Object doExecute()
    {

        Agent agent = agentManager.getAgentByHostname( hostname );
        if ( agent == null )
        {
            System.out.println( "Agent not connected" );
        }
        else
        {
            try
            {
                GitBranch gitBranch = gitManager.currentBranch( agent, repoPath );
                System.out.println( gitBranch.toString() );
            }
            catch ( GitException e )
            {
                LOG.error( "Error in doExecute", e );
                System.out.println( e.getMessage() );
            }
        }

        return null;
    }
}
