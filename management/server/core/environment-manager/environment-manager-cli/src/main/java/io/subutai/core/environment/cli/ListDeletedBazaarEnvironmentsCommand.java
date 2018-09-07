package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "environment", name = "list-deleted-on-bazaar", description = "Command to view deleted Bazaar"
        + "environments" )
public class ListDeletedBazaarEnvironmentsCommand extends SubutaiShellCommandSupport
{

    private final EnvironmentManager environmentManager;


    public ListDeletedBazaarEnvironmentsCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        for ( String environmentId : environmentManager.getDeletedEnvironmentsFromBazaar() )
        {
            System.out.println( String.format( "Environment id %s", environmentId ) );
        }

        return null;
    }
}
