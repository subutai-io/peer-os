package org.safehaus.subutai.core.env.cli;


import java.util.UUID;

import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Destroys environment
 */
@Command( scope = "env", name = "destroy", description = "Command to destroy environment" )
public class DestroyEnvironmentCommand extends SubutaiShellCommandSupport
{
    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    /**
     * {@value environmentId} target environment id to destroy
     * {@code required = true}
     */
            String environmentId;

    @Argument( name = "async", description = "asynchronous destruction",
            index = 1, multiValued = false, required = false )
    /**
     * {@value async} execute destroy environment asynchronously
     * <p> {@code required = false}, {@code default = false}</p>
     */
            boolean async = false;

    @Argument( name = "force", description = "force metadata removal",
            index = 2, multiValued = false, required = false )
    /**
     * {@value forceMetadataRemoval} force metadata removal despite exception handled
     * <p> {@code required = false}, {@code default = false}</p>
     */
            boolean forceMetadataRemoval = false;


    private final EnvironmentManager environmentManager;


    public DestroyEnvironmentCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( environmentId ), "Invalid environment id" );

        environmentManager.destroyEnvironment( UUID.fromString( environmentId ), async, forceMetadataRemoval );

        System.out.println( "Environment destroyed" );

        return null;
    }
}
