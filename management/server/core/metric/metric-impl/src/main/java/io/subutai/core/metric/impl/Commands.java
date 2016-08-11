package io.subutai.core.metric.impl;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;


/**
 * Commands for Monitor
 */
public class Commands
{

    public RequestBuilder getCurrentMetricCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai stats system %s", hostname ) );
    }


    //subutai metrics management -s "2015-11-30 03:00:00" -e "2015-11-30 20:00:00"
    public RequestBuilder getHistoricalMetricCommand( Host host, Date start, Date end )
    {
        DateFormat df = new SimpleDateFormat( "YYYY-MM-dd HH:mm:ss" );
        String startTimestamp = df.format( start );
        String endTimestamp = df.format( end );
        return new RequestBuilder(
                String.format( "subutai metrics %s -s \"%s\" -e \"%s\"", host.getHostname(), startTimestamp,
                        endTimestamp ) );
    }


    public RequestBuilder getProcessResourceUsageCommand( String hostname, int pid )
    {
        return new RequestBuilder( String.format( "subutai monitor -i %s %s", pid, hostname ) );
    }
}
