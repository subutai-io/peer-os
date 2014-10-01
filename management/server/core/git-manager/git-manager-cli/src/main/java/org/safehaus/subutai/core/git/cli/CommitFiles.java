package org.safehaus.subutai.core.git.cli;


import java.util.ArrayList;
import java.util.Collection;

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
 * Commits file(s)
 */
@Command(scope = "git", name = "commit-files", description = "Commit files")
public class CommitFiles extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( CommitFiles.class.getName() );

    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
    String repoPath;
    @Argument(index = 2, name = "message", required = true, multiValued = false, description = "commit message")
    String message;
    @Argument(index = 3, name = "file(s)", required = true, multiValued = true, description = "file(s) to commit")
    Collection<String> files;
    @Argument(index = 4, name = "conflict resolution", required = false, multiValued = false,
            description = "commit after conflict resolution (true/false = default)")
    boolean afterConflictResolved;

    private final GitManager gitManager;
    private final AgentManager agentManager;


    public CommitFiles( final GitManager gitManager, final AgentManager agentManager )
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


    public void setFiles( final Collection<String> files )
    {
        this.files = files;
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
                String commitId =
                        gitManager.commit( agent, repoPath, new ArrayList<>( files ), message, afterConflictResolved );

                System.out.println( String.format( "Commit ID : %s", commitId ) );
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
