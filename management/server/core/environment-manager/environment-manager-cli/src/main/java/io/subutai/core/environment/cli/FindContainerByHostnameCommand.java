package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * List all existing environments
 */
@Command( scope = "environment", name = "find-container", description = "Find container by hostname" )
public class FindContainerByHostnameCommand extends SubutaiShellCommandSupport
{

    private final EnvironmentManager environmentManager;

    @Argument( name = "container hostname", description = "Container hostname",
            index = 0, multiValued = false, required = true )
    String containerHostname;


    public FindContainerByHostnameCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        for ( Environment environment : environmentManager.getEnvironments() )
        {
            try
            {
                ContainerHost containerHost = environment.getContainerHostByHostname( containerHostname );

                System.out.format( "Id: %s, State: %s%n", containerHost.getId(), containerHost.getState() );

                return null;
            }
            catch ( ContainerHostNotFoundException e )
            {
                //ignore
            }
        }

        System.out.println( "Container not found" );

        return null;
    }
}
