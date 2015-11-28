package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostId;


/**
 * Base alert class
 */
public abstract class BaseAlert implements AlertValue
{
    @JsonProperty( "hostId" )
    protected HostId hostId;

    @Override
    public HostId getHostId()
    {
        return hostId;
    }


    @Override
    public String toString()
    {
        return String.format( "%s", hostId );
    }
}
