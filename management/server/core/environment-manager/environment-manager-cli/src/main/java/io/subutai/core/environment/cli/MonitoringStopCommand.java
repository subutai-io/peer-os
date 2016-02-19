package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Stop monitoring command
 */
@Command( scope = "environment", name = "remove-alert-handler", description = "Removes alert handler from specified "
        + "environment." )
public class MonitoringStopCommand extends SubutaiShellCommandSupport
{
    private final EnvironmentManager environmentManager;

    @Argument( index = 0, name = "handlerId", multiValued = false, description = "Alert handler ID" )
    protected String handlerId;

    @Argument( index = 1, name = "priority", multiValued = false, description = "Alert handler priority" )
    protected String priority;

    @Argument( index = 2, name = "environmentId", multiValued = false, description = "Environment ID" )
    protected String environmentId;


    public MonitoringStopCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager, "Environment manager is null" );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        environmentManager.stopMonitoring( handlerId, AlertHandlerPriority.valueOf( priority ), environmentId );
        return null;
    }
}
