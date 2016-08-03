package io.subutai.core.metric.cli;


import java.util.Calendar;
import java.util.Date;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.resource.Series;
import io.subutai.common.resource.SeriesBatch;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;


/**
 * List of resource hosts metrics command
 */
@Command( scope = "metric", name = "get", description = "Prints a specified host metrics" )
public class MetricCommand extends SubutaiShellCommandSupport
{
    private final Monitor monitor;
    private final LocalPeer localPeer;

    @Argument( index = 0, name = "Host ID", multiValued = false, required = true, description = "Host ID" )
    private String hostId;
    @Argument( index = 1, name = "Last hours", multiValued = false, required = true, description = "Number of last "
            + "hours for which to collect metrics" )
    private int lastHours;


    public MetricCommand( final LocalPeer localPeer, final Monitor monitor )
    {
        Preconditions.checkNotNull( localPeer, "Local peer is null" );
        Preconditions.checkNotNull( monitor, "Monitor is null" );

        this.localPeer = localPeer;
        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Host host = localPeer.bindHost( hostId );

        //calculate start date (current date - given # of hours)
        Calendar calendar = Calendar.getInstance();
        Date current = new Date( calendar.getTime().getTime() - Calendar.getInstance().getTimeZone().getRawOffset() );
        calendar.add( Calendar.HOUR, ( -lastHours ) );
        Date start = new Date( calendar.getTime().getTime() - Calendar.getInstance().getTimeZone().getRawOffset() );

        HistoricalMetrics metrics = monitor.getMetricsSeries( host, start, current );

        for ( SeriesBatch batch : metrics.getMetrics() )
        {
            for ( Series series : batch.getSeries() )
            {
                System.out.println( series );
            }

            if ( !Strings.isNullOrEmpty( batch.getMessages() ) )
            {
                System.err.println( batch.getMessages() );
            }
        }

        return null;
    }
}
