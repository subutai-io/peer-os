package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Commands for Monitor
 */
public class Commands
{


    public RequestBuilder getReadContainerHostMetricCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai monitor %s", hostname ) );
    }


    public RequestBuilder getReadResourceHostMetricCommand()
    {
        return new RequestBuilder( "subutai monitor -p" );
    }
}
