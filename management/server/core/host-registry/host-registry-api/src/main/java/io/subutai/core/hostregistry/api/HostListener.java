package io.subutai.core.hostregistry.api;


import java.util.Set;

import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;


/**
 * Notifies listener on host heartbeat
 */
public interface HostListener
{
    /**
     * Triggered on each hearbeat from any of connected resource hosts
     *
     * @param resourceHostInfo - resource host info of host from which this heartbeat came
     */
    public void onHeartbeat( ResourceHostInfo resourceHostInfo, Set<QuotaAlertValue> alerts );
}
