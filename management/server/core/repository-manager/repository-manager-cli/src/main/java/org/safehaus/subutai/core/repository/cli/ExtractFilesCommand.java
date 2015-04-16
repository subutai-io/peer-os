package org.safehaus.subutai.core.repository.cli;


import java.util.Set;

import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


@Command( scope = "repo", name = "extract-files", description = "Extracts package files to /tmp" )
public class ExtractFilesCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ExtractFilesCommand.class.getName() );

    @Argument( index = 0, name = "package name", required = true, multiValued = false,
            description = "name of package" )
    String packageName;
    @Argument( index = 1, name = "file path(s)", required = true, multiValued = true,
            description = "relative file path(s) inside package to read" )
    Set<String> filesPaths;

    private final RepositoryManager repositoryManager;


    public ExtractFilesCommand( final RepositoryManager repositoryManager )
    {

        Preconditions.checkNotNull( repositoryManager, "Repo manager is null" );

        this.repositoryManager = repositoryManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            repositoryManager.extractPackageFiles( packageName, filesPaths );
        }
        catch ( RepositoryException e )
        {
            LOG.error( "Error in ExtractFilesCommand", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
