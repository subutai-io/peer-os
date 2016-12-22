package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * List all existing environments
 */
@Command( scope = "environment", name = "list-deleted-on-hub", description = "Command to view deleted Hub "
        + "environments" )
public class ListDeletedHubEnvironmentsCommand extends SubutaiShellCommandSupport
{

    private final EnvironmentManager environmentManager;


    public ListDeletedHubEnvironmentsCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        for ( String environmentId : environmentManager.getDeletedEnvironmentsFromHub() )
        {
            System.out.println( String.format( "Environment id %s", environmentId ) );
        }

        return null;
    }
}
