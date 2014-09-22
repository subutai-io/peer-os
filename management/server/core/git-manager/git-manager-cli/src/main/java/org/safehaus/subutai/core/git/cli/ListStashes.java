package org.safehaus.subutai.core.git.cli;


import java.util.List;

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
 * Displays stashes
 */
@Command(scope = "git", name = "list-stashes", description = "Display stashes")
public class ListStashes extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( ListStashes.class.getName() );

    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
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
            List<String> stashes = gitManager.listStashes( agent, repoPath );
            for ( String stash : stashes )
            {
                LOG.info( stash.toString() );
            }
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
        }

        return null;
    }
}
