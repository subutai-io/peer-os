package io.subutai.core.git.cli;


import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Reverts the specified commit
 */
@Command( scope = "git", name = "revert-commit",
        description = "Reverts the repo to the state previous to the specified commit" )
public class RevertCommit extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( RevertCommit.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "commit id", required = true, multiValued = false,
            description = "commit id to revert" )
    String commitId;

    private final GitManager gitManager;


    public RevertCommit( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            gitManager.revertCommit( repoPath, commitId );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
