package io.subutai.core.metric.cli;


import java.util.Set;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.common.metric.ContainerHostMetric;
import io.subutai.core.metric.api.Monitor;


/**
 * ResourceHostMetricsCommand
 */
@Command( scope = "metric", name = "container-host-metrics", description = "Lists container host metrics" )
public class ContainerHostMetricsCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "environment id", required = true, multiValued = false,
            description = "environment id " )
    String environmentIdString;


    private final Monitor monitor;
    private final EnvironmentManager environmentManager;


    public ContainerHostMetricsCommand( final Monitor monitor, final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );
        Preconditions.checkNotNull( environmentManager, "Environment manager is null" );

        this.monitor = monitor;
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentIdString );
            Set<ContainerHostMetric> metrics = monitor.getContainerHostsMetrics( environment );
            for ( ContainerHostMetric metric : metrics )
            {
                System.out.println( metric );
            }
        }
        catch ( EnvironmentNotFoundException e )
        {
            System.out.println( "Environment not found" );
        }

        return null;
    }
}
