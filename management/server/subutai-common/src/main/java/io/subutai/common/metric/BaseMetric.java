package io.subutai.common.metric;


/**
 * Base metrics
 */
public abstract class BaseMetric
{
    protected String peerId;

    protected String hostId;


    abstract public void setHostName( String hostName );

    abstract public String getHostName();


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }
}
