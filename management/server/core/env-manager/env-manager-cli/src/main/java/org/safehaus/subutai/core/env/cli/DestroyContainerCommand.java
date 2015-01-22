package org.safehaus.subutai.core.env.cli;


import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "env", name = "destroy-container", description = "Command to destroy container" )
public class DestroyContainerCommand extends OsgiCommandSupport
{
    @Argument( name = "conId", description = "Container id",
            index = 0, multiValued = false, required = true )
    private String containerIdStr;

    private final EnvironmentManager environmentManager;


    public DestroyContainerCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( containerIdStr ), "Invalid container id" );

        UUID containerId = UUID.fromString( containerIdStr );

        for ( Environment environment : environmentManager.getEnvironments() )
        {
            for ( ContainerHost containerHost : environment.getContainerHosts() )
            {
                if ( containerHost.getId().equals( containerId ) )
                {
                    environmentManager.destroyContainer( containerHost );

                    System.out.println( "Container destroyed" );

                    return null;
                }
            }
        }

        System.out.println( "Container environment not found" );

        return null;
    }
}
