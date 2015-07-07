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
 * Commits all files
 */
@Command( scope = "git", name = "commit-all", description = "Commit all files" )
public class CommitAll extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( CommitAll.class.getName() );


    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "message", required = true, multiValued = false, description = "commit message" )
    String message;

    private final GitManager gitManager;


    public CommitAll( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {

        try
        {
            String commitId = gitManager.commitAll( repoPath, message );

            System.out.println( String.format( "Commit ID : %s", commitId ) );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
