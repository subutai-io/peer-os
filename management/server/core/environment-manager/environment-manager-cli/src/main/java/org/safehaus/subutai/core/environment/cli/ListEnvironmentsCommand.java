package org.safehaus.subutai.core.environment.cli;


import java.util.List;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 6/21/14.
 */
@Command( scope = "environment", name = "ls", description = "Command to list environments",
        detailedDescription = "Command to list environments" )
public class ListEnvironmentsCommand extends OsgiCommandSupport
{

    EnvironmentManager environmentManager;


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<Environment> environments = environmentManager.getEnvironments();
        if ( environments != null )
        {
            if ( !environments.isEmpty() )
            {
                for ( Environment environment : environments )
                {
                    System.out.println( environment.getName() );
                }
            }
            else
            {
                System.out.println( "No environments found." );
            }
        }
        else
        {
            System.out.println( "No environments found." );
        }
        return null;
    }
}
