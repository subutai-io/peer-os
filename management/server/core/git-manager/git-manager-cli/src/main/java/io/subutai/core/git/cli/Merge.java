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
 * Merges current branch with specified branch
 */
@Command( scope = "git", name = "merge", description = "Merge current branch with specified branch" )
public class Merge extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Merge.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "branch name", required = false, multiValued = false,
            description = "branch name to merge with (master = default)" )
    String branchName;

    private final GitManager gitManager;


    public Merge( final GitManager gitManager )
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
                gitManager.merge( repoPath, branchName );
            }
            else
            {
                gitManager.merge( repoPath );
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
