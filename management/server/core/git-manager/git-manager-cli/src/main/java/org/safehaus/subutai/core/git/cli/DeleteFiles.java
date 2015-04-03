package org.safehaus.subutai.core.git.cli;


import java.util.List;

import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Deletes file(s) from working directory and index
 */
@Command( scope = "git", name = "delete-files", description = "Delete files from repo" )
public class DeleteFiles extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( DeleteFiles.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "file(s)", required = true, multiValued = true, description = "file(s) to delete" )
    List<String> files;

    private final GitManager gitManager;


    public DeleteFiles( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {

        try
        {
            gitManager.delete( repoPath, files );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
