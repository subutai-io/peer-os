package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostId;
import io.subutai.common.peer.HostType;


/**
 * Base alert class
 */
public class BaseAlert
{
    @JsonProperty( "hostId" )
    protected HostId hostId;
    @JsonProperty( "hostType" )
    private HostType hostType;
    @JsonProperty( "value" )
    private AlertValue value;


    public BaseAlert()
    {
    }


    public BaseAlert( final HostId hostId, final HostType hostType, final AlertValue value )
    {
        this.hostId = hostId;
        this.hostType = hostType;
        this.value = value;
    }


    public HostId getHostId()
    {
        return hostId;
    }


    public HostType getHostType()
    {
        return hostType;
    }


    public AlertValue getValue()
    {
        return value;
    }


    @Override
    public String toString()
    {
        return String.format( "%s", hostId );
    }
}
