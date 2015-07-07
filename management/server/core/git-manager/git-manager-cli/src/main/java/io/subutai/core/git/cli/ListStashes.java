package io.subutai.core.git.cli;


import java.util.List;

import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Displays stashes
 */
@Command( scope = "git", name = "list-stashes", description = "Display stashes" )
public class ListStashes extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( ListStashes.class.getName() );


    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;

    private final GitManager gitManager;


    public ListStashes( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            List<String> stashes = gitManager.listStashes( repoPath );
            for ( String stash : stashes )
            {
                System.out.println( stash );
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
