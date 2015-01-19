package org.safehaus.subutai.core.environment.cli;


import java.util.UUID;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "environment", name = "destroy", description = "Command to destroy environment",
        detailedDescription = "Command to destroy environment by name" )
public class DestroyEnvironmentCommand extends OsgiCommandSupport
{

    EnvironmentManager environmentManager;

    @Argument( name = "environmentId", index = 0, required = true, multiValued = false,
            description = "Environment id", valueToShowInHelp = "Environment name" )
    String environmentId;


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            environmentManager.destroyEnvironment( UUID.fromString( environmentId ) );
            System.out.println( "Environment destroyed successfully." );
        }
        catch ( EnvironmentDestroyException e )
        {
            System.out.println( String.format( "Environment destroy failed: %s", e ) );
            e.printStackTrace();
        }
        return null;
    }
}
