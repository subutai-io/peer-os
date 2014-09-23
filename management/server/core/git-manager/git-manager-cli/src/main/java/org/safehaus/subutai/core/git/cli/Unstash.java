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


/**
 * Applies all stashed changes to current branch
 */
@Command(scope = "git", name = "unstash", description = "Apply all stashed changes to current branch")
public class Unstash extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Unstash.class.getName() );

    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
    String repoPath;
    @Argument(index = 2, name = "stashName", required = true, multiValued = false,
            description = "stash name to apply")
    String stashName;

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
            gitManager.unstash( agent, repoPath, stashName );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
        }

        return null;
    }
}
