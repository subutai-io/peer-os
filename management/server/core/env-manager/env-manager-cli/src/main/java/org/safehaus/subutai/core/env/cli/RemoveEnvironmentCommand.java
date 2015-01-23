package org.safehaus.subutai.core.env.cli;


import java.util.UUID;

import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "env", name = "remove", description = "Command to remove environment from database" )
public class RemoveEnvironmentCommand extends OsgiCommandSupport
{
    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    private String environmentId;

    private final EnvironmentManager environmentManager;


    public RemoveEnvironmentCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( environmentId ), "Invalid environment id" );

        environmentManager.removeEnvironment( UUID.fromString( environmentId ) );

        System.out.println( "Environment removed from database" );

        return null;
    }
}
