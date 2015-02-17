package org.safehaus.subutai.core.env.cli;


import java.util.UUID;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Destroys environment container
 */
@Command( scope = "env", name = "destroy-container", description = "Command to destroy container" )
public class DestroyContainerCommand extends OsgiCommandSupport
{
    @Argument( name = "conId", description = "Container id",
            index = 0, multiValued = false, required = true )
    /**
     * {@value containerIdStr} target environment container host to destroy
     * {@code required = true}
     */ private String containerIdStr;

    @Argument( name = "async", description = "asynchronous destruction",
            index = 1, multiValued = false, required = false )
    /**
     * {@value async} destroy environment asynchronously
     * <p> {@code required = false}, {@code default false} </p>
     */ private boolean async = false;

    @Argument( name = "force", description = "force metadata removal",
            index = 2, multiValued = false, required = false )
    /**
     * {@value forceMetadataRemoval} force remove stored info about environment
     * <p> {@code required = false}, {@code default false} </p>
     */ private boolean forceMetadataRemoval = false;

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
                    environmentManager.destroyContainer( containerHost, async, forceMetadataRemoval );

                    System.out.println( "Container destroyed" );

                    return null;
                }
            }
        }

        System.out.println( "Container environment not found" );

        return null;
    }
}
