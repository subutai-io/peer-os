package io.subutai.core.git.cli;


import java.util.List;

import io.subutai.core.git.api.GitChangedFile;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Diffs branches
 */
@Command( scope = "git", name = "diff-branches", description = "Diff branches to see changed files" )
public class DiffBranches extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( DiffBranches.class.getName() );


    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "branch name 1", required = true, multiValued = false,
            description = "branch name 1" )
    String branchName1;
    @Argument( index = 2, name = "branch name 2", required = false, multiValued = false,
            description = "branch name 2 (master = default)" )
    String branchName2;
    private final GitManager gitManager;


    public DiffBranches( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            List<GitChangedFile> changedFileList;
            if ( branchName2 != null )
            {
                changedFileList = gitManager.diffBranches( repoPath, branchName1, branchName2 );
            }
            else
            {
                changedFileList = gitManager.diffBranches( repoPath, branchName1 );
            }

            for ( GitChangedFile gf : changedFileList )
            {
                System.out.println( gf.toString() );
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
