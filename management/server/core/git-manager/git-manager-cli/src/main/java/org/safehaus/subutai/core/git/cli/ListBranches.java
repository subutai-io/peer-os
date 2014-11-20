package org.safehaus.subutai.core.git.cli;


import java.util.List;

import org.safehaus.subutai.core.git.api.GitBranch;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Displays branches
 */
@Command( scope = "git", name = "list-branches", description = "List local/remote branches" )
public class ListBranches extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( ListBranches.class.getName() );


    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "remote", required = false, multiValued = false,
            description = "list remote branches (true/false = default)" )
    boolean remote;

    private final GitManager gitManager;


    public ListBranches( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            List<GitBranch> branches = gitManager.listBranches( repoPath, remote );
            for ( GitBranch branch : branches )
            {
                System.out.println( branch.toString() );
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
