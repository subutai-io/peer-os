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
 * Diffs file between branches
 */
@Command( scope = "git", name = "diff-file", description = "Diff file between branches" )
public class DiffFile extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( DiffFile.class.getName() );


    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "relative file path from repo root", required = true, multiValued = false,
            description = "file path" )
    String filePath;
    @Argument( index = 2, name = "branch name 1", required = true, multiValued = false,
            description = "branch name 1" )
    String branchName1;
    @Argument( index = 3, name = "branch name 2", required = false, multiValued = false,
            description = "branch name 2 (master = default)" )
    String branchName2;

    private final GitManager gitManager;


    public DiffFile( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            String diff;
            if ( branchName2 != null )
            {
                diff = gitManager.diffFile( repoPath, branchName1, branchName2, filePath );
            }
            else
            {
                diff = gitManager.diffFile( repoPath, branchName1, filePath );
            }

            System.out.println( diff );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }
        return null;
    }
}
