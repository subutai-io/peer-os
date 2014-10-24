package org.safehaus.subutai.core.monitor.cli;


import java.util.Date;
import java.util.List;

import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.monitor.api.MonitorException;
import org.safehaus.subutai.core.monitor.api.Monitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


@Command( scope = "monitor", name = "all-metrics",
        description = "List all metrics for given host within given number of last days up to given limit" )
public class AllMetricsCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( AllMetricsCommand.class.getName() );


    @Argument( index = 0, name = "hostname", required = true, multiValued = false )
    protected String hostname;
    @Argument( index = 1, name = "days", required = false, multiValued = false )
    protected int days = 1;
    @Argument( index = 2, name = "limit", required = false, multiValued = false )
    protected int limit = 100;

    private final Monitoring monitoring;


    public AllMetricsCommand( final Monitoring monitoring )
    {
        Preconditions.checkNotNull( monitoring, "Monitoring is null" );

        this.monitoring = monitoring;
    }


    protected Object doExecute()
    {

        Date endDate = new Date();
        Date startDate = DateUtils.addDays( endDate, -days );

        try
        {
            List<Metric> metrics = monitoring
                    .getMetrics( Sets.newHashSet( hostname ), Sets.newHashSet( MetricType.values() ), startDate,
                            endDate, limit );

            for ( Metric metric : metrics )
            {
                System.out.println( metric );
            }
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
