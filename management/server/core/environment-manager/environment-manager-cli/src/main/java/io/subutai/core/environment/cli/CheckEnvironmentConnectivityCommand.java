package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Check environment connectivity
 */
@Command( scope = "environment", name = "check-connectivity", description = "Command to check environment "
        + "connectivity" )
public class CheckEnvironmentConnectivityCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )

    String environmentId;

    private final EnvironmentManager environmentManager;


    public CheckEnvironmentConnectivityCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        System.out.println( String.format( "Environment connectivity state: %s",
                environmentManager.checkEnvironmentConnectivity( environmentId ) ) );

        return null;
    }
}
