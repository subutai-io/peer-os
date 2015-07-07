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
 * Stashes all changes in current branch and reverts it to HEAD commit
 */
@Command( scope = "git", name = "stash", description = "Stash all changes in current branch and revert to HEAD commit" )
public class Stash extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Stash.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;

    private final GitManager gitManager;


    public Stash( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            gitManager.stash( repoPath );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
