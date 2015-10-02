package io.subutai.core.security.broker;


import io.subutai.common.host.ResourceHostInfo;


/**
 * Heartbeat response from resource host
 */
public class HeartBeat
{
    ResourceHostInfoImpl response;


    public ResourceHostInfo getHostInfo()
    {
        return response;
    }
}
