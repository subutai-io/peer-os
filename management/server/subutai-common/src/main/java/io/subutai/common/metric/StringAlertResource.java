package io.subutai.common.metric;


import io.subutai.common.host.HostId;
import io.subutai.common.peer.HostType;


/**
 * String alert value
 */
public class StringAlertResource implements AlertResource
{
    private final HostId hostId;
    private final HostType hostType;
    private final AlertType alertType;
    private String value;


    public StringAlertResource( final HostId hostId, final HostType hostType, final AlertType alertType,
                                final String description )
    {
        this.hostId = hostId;
        this.hostType = hostType;
        this.alertType = alertType;
        this.value = description;
    }


    @Override
    public String getId()
    {
        return String.format( "%s:%s:%s", hostId, hostType, alertType );
    }


    @Override
    public String getValue()
    {
        return value;
    }


    @Override
    public HostId getHostId()
    {
        return hostId;
    }


    @Override
    public AlertType getType()
    {
        return alertType;
    }
}
