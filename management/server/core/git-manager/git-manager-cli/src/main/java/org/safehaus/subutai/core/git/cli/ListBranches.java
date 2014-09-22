package org.safehaus.subutai.core.git.cli;


import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitBranch;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays branches
 */
@Command(scope = "git", name = "list-branches", description = "List local/remote branches")
public class ListBranches extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( ListBranches.class.getName() );


    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
    String repoPath;
    @Argument(index = 2, name = "remote", required = false, multiValued = false,
            description = "list remote branches (true/false = default)")
    boolean remote;
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
            List<GitBranch> branches = gitManager.listBranches( agent, repoPath, remote );
            for ( GitBranch branch : branches )
            {
                System.out.println( branch.toString() );
            }
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
        }

        return null;
    }
}
