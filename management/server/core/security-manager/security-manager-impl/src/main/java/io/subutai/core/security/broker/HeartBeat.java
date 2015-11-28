package io.subutai.core.security.broker;


import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;


/**
 * Heartbeat response from resource host
 */
public class HeartBeat
{
    ResourceHostInfoModel response;


    public ResourceHostInfo getHostInfo()
    {
        return response;
    }
}
