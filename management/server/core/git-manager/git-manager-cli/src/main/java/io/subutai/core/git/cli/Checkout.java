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
 * Checkouts a remote branch (or creates a local branch)
 */
@Command( scope = "git", name = "checkout", description = "Checkout remote branch/create local branch" )
public class Checkout extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Checkout.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "branch name", required = true, multiValued = false,
            description = "branch name to switch to or create" )
    String branchName;
    @Argument( index = 2, name = "create branch", required = false, multiValued = false,
            description = "create branch (true/false = default)" )
    boolean create;

    private GitManager gitManager;


    public Checkout( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {

        try
        {
            gitManager.checkout( repoPath, branchName, create );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
