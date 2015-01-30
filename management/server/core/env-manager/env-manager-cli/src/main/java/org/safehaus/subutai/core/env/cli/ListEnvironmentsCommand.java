package org.safehaus.subutai.core.env.cli;


import java.util.Date;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "env", name = "list", description = "Command to view environment" )
public class ListEnvironmentsCommand extends OsgiCommandSupport
{

    private final EnvironmentManager environmentManager;


    public ListEnvironmentsCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        for ( Environment environment : environmentManager.getEnvironments() )
        {
            System.out.println( String.format( "Environment id %s", environment.getId() ) );
            System.out.println( String.format( "Environment name %s", environment.getName() ) );
            System.out.println(
                    String.format( "Environment creation time %s", new Date( environment.getCreationTimestamp() ) ) );
            System.out.println( String.format( "Environment status %s", environment.getStatus() ) );
            System.out.println("-----------------------------------------------------------------");
        }

        return null;
    }
}
