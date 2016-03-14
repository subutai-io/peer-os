package io.subutai.common.metric;


import io.subutai.common.host.HostId;
import io.subutai.common.peer.HostType;


/**
 * String alert value
 */
public class StringAlert extends AbstractAlert<StringAlertValue> implements Alert
{
    private final HostType hostType;
    private final AlertType alertType;


    public StringAlert( final HostId hostId, final HostType hostType, final AlertType alertType,
                        final StringAlertValue description )
    {
        super( hostId, description );
        this.hostType = hostType;
        this.alertType = alertType;
    }


    @Override
    public String getId()
    {
        return String.format( "%s:%s:%s", hostId, hostType, alertType );
    }


    @Override
    public HostId getHostId()
    {
        return hostId;
    }


    //    @Override
    public AlertType getType()
    {
        return alertType;
    }


    @Override
    public long getLiveTime()
    {
        return System.currentTimeMillis() - createdTime;
    }
}
