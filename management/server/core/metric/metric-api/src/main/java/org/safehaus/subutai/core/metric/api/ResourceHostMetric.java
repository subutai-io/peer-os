package org.safehaus.subutai.core.metric.api;


import java.util.UUID;


/**
 * Interface for ResourceHostMetric
 */
public abstract class ResourceHostMetric extends Metric
{
    protected UUID peerId;


    public UUID getPeerId()
    {
        return peerId;
    }
}
