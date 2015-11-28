package io.subutai.common.metric;


import io.subutai.common.host.HostId;


/**
 * Common alert class
 */
public class CommonAlert extends BaseAlert implements CommonAlertValue
{
    private HostId hostId;
    private String description;


    public CommonAlert( final HostId hostId, final String description )
    {
        this.hostId = hostId;
        this.description = description;
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public HostId getHostId()
    {
        return hostId;
    }


    @Override
    public String toString()
    {
        return String.format( "%s %s", super.toString(), description );
    }
}
