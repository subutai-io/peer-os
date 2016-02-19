package io.subutai.core.metric.cli;


import java.util.Collection;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.metric.BaseMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;


/**
 * List of resource hosts metrics command
 */
@Command( scope = "metric", name = "list", description = "Lists resource host metrics" )
public class MetricListCommand extends SubutaiShellCommandSupport
{
    private final Monitor monitor;


    public MetricListCommand( final Monitor monitor )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );

        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        ResourceHostMetrics metrics = monitor.getResourceHostMetrics();
        System.out.println( "List of resource host metrics:" );
        for ( BaseMetric metric : metrics.getResources() )
        {
            System.out.println( metric );
        }

        return null;
    }
}
