package io.subutai.core.environment.cli;


import java.util.Collection;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.AlertHandler;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * List all registered alert handlers
 */
@Command( scope = "environment", name = "registered-alert-handlers", description = "Command to list all registered alert "
        + "handlers" )
public class ListRegisteredAlertHandlersCommand extends SubutaiShellCommandSupport
{

    private final EnvironmentManager environmentManager;


    public ListRegisteredAlertHandlersCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        final Collection<AlertHandler> registeredAlertHandlers = environmentManager.getRegisteredAlertHandlers();
        System.out.println( String.format( "List of registered alert handlers. Found %d handler(s)",
                registeredAlertHandlers.size() ) );
        for ( AlertHandler alertHandler : registeredAlertHandlers )
        {
            System.out.println( String.format( "%s\t%s", alertHandler.getId(), alertHandler.getDescription() ) );
        }

        return null;
    }
}
