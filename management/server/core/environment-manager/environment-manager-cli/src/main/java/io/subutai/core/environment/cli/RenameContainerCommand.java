package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Rename environment container name
 */
@Command( scope = "environment", name = "rename-container", description = "Command to rename environment container" )
public class RenameContainerCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    String environmentId;

    @Argument( name = "hostName", description = "Host name",
            index = 1, multiValued = false, required = true )
    String hostname;

    @Argument( name = "newHostName", description = "New Host name",
            index = 2, multiValued = false, required = true )
    String newHostname;

    private final EnvironmentManager environmentManager;


    public RenameContainerCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Environment environment = environmentManager.loadEnvironment( environmentId );

        System.out.println( String.format( "Environment name %s", environment.getName() ) );

        final EnvironmentContainerHost c = environment.getContainerHostByHostname( hostname );

        c.setHostname( newHostname );

        return null;
    }
}
