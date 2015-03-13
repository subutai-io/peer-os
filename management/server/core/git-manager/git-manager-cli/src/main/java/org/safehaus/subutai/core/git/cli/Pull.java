package org.safehaus.subutai.core.git.cli;


import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Pulls from remote branch
 */
@Command( scope = "git", name = "pull", description = "Pull from remote branch" )
public class Pull extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Pull.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "branch name", required = false, multiValued = false,
            description = "branch name to pull from (master = default)" )
    String branchName;

    private final GitManager gitManager;


    public Pull( final GitManager gitManager )
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
                gitManager.pull( repoPath, branchName );
            }
            else
            {
                gitManager.pull( repoPath );
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
