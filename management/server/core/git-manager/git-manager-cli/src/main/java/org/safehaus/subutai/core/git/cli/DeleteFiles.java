package org.safehaus.subutai.core.git.cli;


import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Deletes file(s) from working directory and index
 */
@Command(scope = "git", name = "delete-files", description = "Delete files from repo")
public class DeleteFiles extends OsgiCommandSupport
{

    protected static final Logger LOG = Logger.getLogger( DeleteFiles.class.getName() );


    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
    String repoPath;
    @Argument(index = 2, name = "file(s)", required = true, multiValued = true, description = "file(s) to delete")
    Collection<String> files;
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
            gitManager.delete( agent, repoPath, new ArrayList<>( files ) );
        }
        catch ( GitException e )
        {
            LOG.log( Level.SEVERE, "Error in doExecute", e );
        }

        return null;
    }
}
