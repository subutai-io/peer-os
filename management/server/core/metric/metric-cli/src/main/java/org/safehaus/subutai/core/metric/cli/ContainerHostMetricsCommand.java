package org.safehaus.subutai.core.metric.cli;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.Monitor;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * ResourceHostMetricsCommand
 */
@Command( scope = "metric", name = "container-host-metrics", description = "Lists container host metrics" )
public class ContainerHostMetricsCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "environment id", required = true, multiValued = false,
            description = "environment id (uuid)" )
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

        UUID environmentId = UUID.fromString( environmentIdString );

        try
        {
            Environment environment = environmentManager.findEnvironment( environmentId );
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
