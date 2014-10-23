package org.safehaus.subutai.core.metric.api;


import java.util.UUID;

import com.google.common.base.Preconditions;


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


    protected void setPeerId( final UUID peerId )
    {
        Preconditions.checkNotNull( peerId, "Invalid peer id" );

        this.peerId = peerId;
    }
}
