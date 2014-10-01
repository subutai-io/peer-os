package org.safehaus.subutai.core.git.cli;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Brings current branch to the state of the specified remote branch, effectively undoing all local changes
 */
@Command(scope = "git", name = "undo-hard",
        description = "Bring current branch to the state of the specified remote branch, "
                + "effectively undoing all local changes")
public class UndoHard extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( UndoHard.class.getName() );


    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
    String repoPath;
    @Argument(index = 2, name = "branch name", required = false, multiValued = false,
            description = "name of remote branch whose state to restore current branch to (master = default)")
    String branchName;

    private final GitManager gitManager;
    private final AgentManager agentManager;


    public UndoHard( final GitManager gitManager, final AgentManager agentManager )
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


    public void setBranchName( final String branchName )
    {
        this.branchName = branchName;
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

                if ( branchName != null )
                {
                    gitManager.undoHard( agent, repoPath, branchName );
                }
                else
                {
                    gitManager.undoHard( agent, repoPath );
                }
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
