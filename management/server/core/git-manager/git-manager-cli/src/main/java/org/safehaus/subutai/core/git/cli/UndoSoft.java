package org.safehaus.subutai.core.git.cli;


import java.util.ArrayList;
import java.util.Collection;

import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Undoes all uncommitted changes to specified files
 */
@Command( scope = "git", name = "undo-soft", description = "Undo all uncommitted changes to specified files" )
public class UndoSoft extends OsgiCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( UndoSoft.class.getName() );

    @Argument( index = 0, name = "repoPath", required = true, multiValued = false, description = "path to git repo" )
    String repoPath;
    @Argument( index = 1, name = "file(s)", required = true, multiValued = true,
            description = "file(s) to undo changes to" )
    Collection<String> files;

    private final GitManager gitManager;


    public UndoSoft( final GitManager gitManager )
    {
        Preconditions.checkNotNull( gitManager, "Git Manager is null" );

        this.gitManager = gitManager;
    }


    protected Object doExecute()
    {
        try
        {

            gitManager.undoSoft( repoPath, new ArrayList<>( files ) );
        }
        catch ( GitException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
