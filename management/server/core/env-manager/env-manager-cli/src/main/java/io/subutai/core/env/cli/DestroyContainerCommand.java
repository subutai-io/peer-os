package io.subutai.core.env.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Destroys environment container
 */
@Command( scope = "env", name = "destroy-container", description = "Command to destroy container" )
public class DestroyContainerCommand extends SubutaiShellCommandSupport
{
    @Argument( name = "conId", description = "Container id",
            index = 0, multiValued = false, required = true )
    /**
     * {@value containerIdStr} target environment container host to destroy
     * {@code required = true}
     */
            String containerIdStr;

    @Argument( name = "async", description = "asynchronous destruction",
            index = 1, multiValued = false, required = false )
    /**
     * {@value async} destroy environment asynchronously
     * <p> {@code required = false}, {@code default false} </p>
     */
            boolean async = false;

    @Argument( name = "force", description = "force metadata removal",
            index = 2, multiValued = false, required = false )
    /**
     * {@value forceMetadataRemoval} force remove stored info about environment
     * <p> {@code required = false}, {@code default false} </p>
     */
            boolean forceMetadataRemoval = false;

    private final EnvironmentManager environmentManager;


    public DestroyContainerCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIdStr ), "Invalid container id" );


        for ( Environment environment : environmentManager.getEnvironments() )
        {
            for ( ContainerHost containerHost : environment.getContainerHosts() )
            {
                if ( containerHost.getId().equals( containerIdStr ) )
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
