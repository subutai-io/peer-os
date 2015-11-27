package io.subutai.common.metric;


import io.subutai.common.host.HostId;


/**
 * Alert value interface
 */
public interface AlertValue
{
    HostId getHostId();

    String getDescription();
}
