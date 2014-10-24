package org.safehaus.subutai.core.git.cli;


import java.util.ArrayList;
import java.util.Collection;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Adds file(s) to commit
 */
@Command( scope = "git", name = "add-files", description = "Add files to commit" )
public class AddFiles extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( AddFiles.class.getName() );


    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname" )
    String hostname;
    @Argument( index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 2, name = "file(s)", required = true, multiValued = true, description = "file(s) to add" )
    Collection<String> files;
    private final GitManager gitManager;
    private final AgentManager agentManager;


    public AddFiles( final GitManager gitManager, final AgentManager agentManager )
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


    public void setRepoPath( final String repoPath )
    {
        this.repoPath = repoPath;
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
                gitManager.add( agent, repoPath, new ArrayList<>( files ) );
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
