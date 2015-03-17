package org.safehaus.subutai.core.git.cli;


import org.safehaus.subutai.core.git.api.GitBranch;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Displays the current git branch
 */
@Command( scope = "git", name = "get-current-branch", description = "Get current branch" )
public class GetCurrentBranch extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( GetCurrentBranch.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;

    private final GitManager gitManager;


    public GetCurrentBranch( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            GitBranch gitBranch = gitManager.currentBranch( repoPath );
            System.out.println( gitBranch.toString() );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
