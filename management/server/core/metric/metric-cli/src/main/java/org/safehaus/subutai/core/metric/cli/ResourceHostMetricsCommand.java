package org.safehaus.subutai.core.metric.cli;


import java.util.Set;

import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * ResourceHostMetricsCommand
 */
@Command( scope = "monitor", name = "resource-host-metrics", description = "Lists resource host metrics" )
public class ResourceHostMetricsCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostMetricsCommand.class.getName() );

    private final Monitor monitor;


    public ResourceHostMetricsCommand( final Monitor monitor )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );

        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<ResourceHostMetric> metrics = monitor.getResourceHostMetrics();
        for ( ResourceHostMetric metric : metrics )
        {
            System.out.println( metric );
        }

        return null;
    }
}
