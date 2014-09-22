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
 * Diffs file between branches
 */
@Command(scope = "git", name = "diff-file", description = "Diff file between branches")
public class DiffFile extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( DiffFile.class.getName() );

    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    @Argument(index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo")
    String repoPath;
    @Argument(index = 2, name = "relative file path from repo root", required = true, multiValued = false,
            description = "file path")
    String filePath;
    @Argument(index = 3, name = "branch name 1", required = true, multiValued = false,
            description = "branch name 1")
    String branchName1;
    @Argument(index = 4, name = "branch name 2", required = false, multiValued = false,
            description = "branch name 2 (master = default)")
    String branchName2;
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
            String diff = null;
            if ( branchName2 != null )
            {
                diff = gitManager.diffFile( agent, repoPath, branchName1, branchName2, filePath );
            }
            else
            {
                diff = gitManager.diffFile( agent, repoPath, branchName1, filePath );
            }

            System.out.println( diff );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
        }

        return null;
    }
}
