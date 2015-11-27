package io.subutai.common.metric;


import io.subutai.common.host.HostInfo;


/**
 * Base metrics
 */
public class BaseMetric
{
    protected String peerId;
    protected HostInfo hostInfo;


    public BaseMetric()
    {
    }


    public BaseMetric( final String peerId, final HostInfo hostInfo )
    {
        this.peerId = peerId;
        this.hostInfo = hostInfo;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public HostInfo getHostInfo()
    {
        return hostInfo;
    }
}
