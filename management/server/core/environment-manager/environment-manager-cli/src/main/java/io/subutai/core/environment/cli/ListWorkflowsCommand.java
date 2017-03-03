package io.subutai.core.environment.cli;


import java.util.Map;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "environment", name = "list-workflows", description = "Lists all active environment workflows" )
public class ListWorkflowsCommand extends SubutaiShellCommandSupport
{

    private final EnvironmentManager environmentManager;


    public ListWorkflowsCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Map<String, CancellableWorkflow> activeWorkflows = environmentManager.getActiveWorkflows();

        System.out.format( "Found %d active workflow(s)%n", activeWorkflows.size() );

        for ( Map.Entry<String, CancellableWorkflow> workflowEntry : activeWorkflows.entrySet() )
        {
            System.out.printf( "Environment %s\tWorkflow %s%n", workflowEntry.getKey(),
                    workflowEntry.getValue().getClass().getSimpleName() );
        }

        return null;
    }
}
