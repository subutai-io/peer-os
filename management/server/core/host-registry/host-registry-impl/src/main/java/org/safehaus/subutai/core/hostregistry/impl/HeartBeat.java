package org.safehaus.subutai.core.hostregistry.impl;


import org.safehaus.subutai.core.hostregistry.api.HostInfo;


/**
 * Heartbeat response from resource host
 */
public class HeartBeat
{
    HostInfoImpl response;


    public HostInfo getHostInfo()
    {
        return response;
    }
}
