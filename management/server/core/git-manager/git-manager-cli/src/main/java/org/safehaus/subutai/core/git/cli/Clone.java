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
 * Clones remote master repo
 */
@Command(scope = "git", name = "clone", description = "Clone master repo")
public class Clone extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Clone.class.getName() );


    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "new branch name", required = true, multiValued = false,
            description = "name of branch to create")
    String newBranchName;
    @Argument(index = 2, name = "target directory", required = true, multiValued = false,
            description = "directory to clone to")
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
            LOG.error( "Error in doExecute", e );
        }

        return null;
    }
}
