package io.subutai.core.git.cli;


import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Brings current branch to the state of the specified remote branch, effectively undoing all local changes
 */
@Command( scope = "git", name = "undo-hard",
        description = "Bring current branch to the state of the specified remote branch, "
                + "effectively undoing all local changes" )
public class UndoHard extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( UndoHard.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "branch name", required = false, multiValued = false,
            description = "name of remote branch whose state to restore current branch to (master = default)" )
    String branchName;

    private final GitManager gitManager;


    public UndoHard( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            if ( branchName != null )
            {
                gitManager.undoHard( repoPath, branchName );
            }
            else
            {
                gitManager.undoHard( repoPath );
            }
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
