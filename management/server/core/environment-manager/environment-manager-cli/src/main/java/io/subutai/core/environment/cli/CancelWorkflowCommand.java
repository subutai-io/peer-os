package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Cancels any active environment workflow
 */
@Command( scope = "environment", name = "cancel-workflow", description = "Cancels active environment workflow" )
public class CancelWorkflowCommand extends SubutaiShellCommandSupport
{
    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )

    String environmentId;


    private final EnvironmentManager environmentManager;


    public CancelWorkflowCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        environmentManager.cancelEnvironmentWorkflow( environmentId );

        System.out.println( "Active environment workflow cancelled" );

        return null;
    }
}
