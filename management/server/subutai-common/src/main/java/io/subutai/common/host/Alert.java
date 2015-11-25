package io.subutai.common.host;


import java.util.Map;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.resource.BaseResource;
import io.subutai.common.resource.ResourceType;


/**
 * Alert class
 */
public class Alert
{
    ContainerId containerId;
    Map<ResourceType, BaseResource> alerts;


    public Alert( final ContainerId containerId, final Map<ResourceType, BaseResource> alerts )
    {
        this.containerId = containerId;
        this.alerts = alerts;
    }


    public ContainerId getContainerId()
    {
        return containerId;
    }


    public Map<ResourceType, BaseResource> getAlerts()
    {
        return alerts;
    }
}
