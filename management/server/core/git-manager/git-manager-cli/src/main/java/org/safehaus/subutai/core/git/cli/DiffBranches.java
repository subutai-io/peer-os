package org.safehaus.subutai.core.git.cli;


import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Diffs branches
 */
@Command( scope = "git", name = "diff-branches", description = "Diff branches to see changed files" )
public class DiffBranches extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( DiffBranches.class.getName() );

    @Argument( index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname" )
    String hostname;
    @Argument( index = 1, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 2, name = "branch name 1", required = true, multiValued = false,
            description = "branch name 1" )
    String branchName1;
    @Argument( index = 3, name = "branch name 2", required = false, multiValued = false,
            description = "branch name 2 (master = default)" )
    String branchName2;
    private final GitManager gitManager;
    private final AgentManager agentManager;


    public DiffBranches( final GitManager gitManager, final AgentManager agentManager )
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


    public void setBranchName2( final String branchName2 )
    {
        this.branchName2 = branchName2;
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
                List<GitChangedFile> changedFileList;
                if ( branchName2 != null )
                {
                    changedFileList = gitManager.diffBranches( agent, repoPath, branchName1, branchName2 );
                }
                else
                {
                    changedFileList = gitManager.diffBranches( agent, repoPath, branchName1 );
                }

                for ( GitChangedFile gf : changedFileList )
                {
                    System.out.println( gf.toString() );
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
