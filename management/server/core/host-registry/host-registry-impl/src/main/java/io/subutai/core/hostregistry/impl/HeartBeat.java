package io.subutai.core.hostregistry.impl;


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
