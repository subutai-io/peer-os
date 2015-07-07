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
 * Pushes to remote branch
 */
@Command( scope = "git", name = "push", description = "Push to remote branch" )
public class Push extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Push.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "branch name", required = true, multiValued = false,
            description = "branch name to push to" )
    String branchName;

    private final GitManager gitManager;


    public Push( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {

        try
        {
            gitManager.push( repoPath, branchName );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }
        return null;
    }
}
