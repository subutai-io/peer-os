package org.safehaus.subutai.core.repository.cli;


import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


@Command( scope = "repo", name = "extract", description = "Extracts package contents to /tmp" )
public class ExtractPackageCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ExtractPackageCommand.class.getName() );

    @Argument( index = 0, name = "package name", required = true, multiValued = false,
            description = "name of package" )
    String packageName;

    private final RepositoryManager repositoryManager;


    public ExtractPackageCommand( final RepositoryManager repositoryManager )
    {

        Preconditions.checkNotNull( repositoryManager, "Repo manager is null" );

        this.repositoryManager = repositoryManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            repositoryManager.extractPackageByName( packageName );
        }
        catch ( RepositoryException e )
        {
            LOG.error( "Error in extractPackageByName", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
