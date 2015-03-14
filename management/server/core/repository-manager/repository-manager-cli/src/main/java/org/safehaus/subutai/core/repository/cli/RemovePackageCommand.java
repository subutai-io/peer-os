package org.safehaus.subutai.core.repository.cli;


import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.repository.api.RepositoryException;
import org.safehaus.subutai.core.repository.api.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


@Command( scope = "repo", name = "remove", description = "Removes package from repository by name" )
public class RemovePackageCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( RemovePackageCommand.class.getName() );

    @Argument( index = 0, name = "package name", required = true, multiValued = false,
            description = "name of package" )
    String packageName;

    private final RepositoryManager repositoryManager;


    public RemovePackageCommand( final RepositoryManager repositoryManager )
    {

        Preconditions.checkNotNull( repositoryManager, "Repo manager is null" );

        this.repositoryManager = repositoryManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            repositoryManager.removePackageByName( packageName );
        }
        catch ( RepositoryException e )
        {
            LOG.error( "Error in removePackageByName", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
