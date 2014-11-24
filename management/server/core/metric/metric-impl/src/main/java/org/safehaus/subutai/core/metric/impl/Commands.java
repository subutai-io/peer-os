package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Commands for Monitor
 */
public class Commands
{


    public RequestBuilder getCurrentMetricCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai monitor %s", hostname ) );
    }
}
