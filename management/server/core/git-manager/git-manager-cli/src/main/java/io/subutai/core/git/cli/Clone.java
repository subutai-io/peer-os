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
 * Clones remote master repo
 */
@Command( scope = "git", name = "clone", description = "Clone master repo" )
public class Clone extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Clone.class.getName() );

    @Argument( index = 0, name = "new branch name", required = true, multiValued = false,
            description = "name of branch to create" )
    String newBranchName;
    @Argument( index = 1, name = "target directory", required = true, multiValued = false,
            description = "directory to clone to" )
    String targetDirectory;

    private final GitManager gitManager;


    public Clone( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {

        try
        {
            gitManager.clone( newBranchName, targetDirectory );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
