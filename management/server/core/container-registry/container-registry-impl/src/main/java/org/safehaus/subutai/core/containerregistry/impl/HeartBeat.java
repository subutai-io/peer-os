package org.safehaus.subutai.core.containerregistry.impl;


import org.safehaus.subutai.core.containerregistry.api.HostInfo;


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
