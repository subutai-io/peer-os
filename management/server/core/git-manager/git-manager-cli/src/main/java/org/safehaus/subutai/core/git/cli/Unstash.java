package org.safehaus.subutai.core.git.cli;


import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Applies all stashed changes to current branch
 */
@Command( scope = "git", name = "unstash", description = "Apply all stashed changes to current branch" )
public class Unstash extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( Unstash.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "stashName", required = true, multiValued = false,
            description = "stash name to apply" )
    String stashName;

    private final GitManager gitManager;


    public Unstash( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {
            gitManager.unstash( repoPath, stashName );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
