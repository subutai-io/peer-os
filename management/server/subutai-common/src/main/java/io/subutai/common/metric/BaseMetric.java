package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInfoModel;


/**
 * Base metrics
 */
public class BaseMetric
{
    @JsonProperty( "peerId" )
    protected String peerId;
    @JsonProperty( "hostInfo" )
    protected HostInfoModel hostInfo;
    @JsonProperty( "connected" )
    private boolean connected;


    public BaseMetric()
    {
    }


    public BaseMetric( @JsonProperty( "peerId" ) final String peerId,
                       @JsonProperty( "hostInfo" ) final HostInfoModel hostInfo )
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


    public void setHostInfo( final HostInfoModel hostInfo )
    {
        this.hostInfo = hostInfo;
    }


    @Override
    public String toString()
    {
        if ( hostInfo != null )
        {
            return String
                    .format( "%s\t%s\t%s\t%s\t%s", peerId, hostInfo.getHostname(), hostInfo.getId(), hostInfo.getArch(),
                            connected );
        }
        else
        {
            return "NULL";
        }
    }


    public boolean isConnected()
    {
        return connected;
    }


    public void setConnected( final boolean connected )
    {
        this.connected = connected;
    }
}
