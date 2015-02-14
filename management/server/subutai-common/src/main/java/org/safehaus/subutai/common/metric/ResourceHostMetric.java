package org.safehaus.subutai.common.metric;


import java.util.UUID;

import com.google.common.base.Objects;


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


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "metric", super.toString() ).add( "peerId", peerId ).toString();
    }
}
