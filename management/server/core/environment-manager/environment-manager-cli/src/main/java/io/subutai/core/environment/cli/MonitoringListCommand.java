package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * List alert handlers of the specified environment
 */
@Command( scope = "environment", name = "alert-handlers", description = "List alert handlers" )
public class MonitoringListCommand extends SubutaiShellCommandSupport
{
    private final EnvironmentManager environmentManager;

    @Argument( index = 0, name = "environmentId", multiValued = false, description = "Environment ID" )
    protected String environmentId;


    public MonitoringListCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager, "Environment manager is null" );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( "Priority\tHandler ID\tDescription" );

        EnvironmentAlertHandlers handlers =
                environmentManager.getEnvironmentAlertHandlers( new EnvironmentId( environmentId ) );
        for ( EnvironmentAlertHandler environmentAlertHandler : handlers.getAllHandlers().keySet() )
        {
            AlertHandler alertHandler = handlers.getHandler( environmentAlertHandler );
            if ( alertHandler == null )
            {
                System.out.println( String.format( "%s\t%s\tNOT AVAILABLE", environmentId,
                        environmentAlertHandler.getAlertHandlerPriority(),
                        environmentAlertHandler.getAlertHandlerId() ) );
            }
            else
            {
                System.out.println(
                        String.format( "%s\t%s\t%s", environmentId, environmentAlertHandler.getAlertHandlerPriority(),
                                environmentAlertHandler.getAlertHandlerId(), alertHandler.getDescription() ) );
            }
        }

        return null;
    }
}
