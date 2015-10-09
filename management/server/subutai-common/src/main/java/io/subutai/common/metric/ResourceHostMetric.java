package io.subutai.common.metric;


import com.google.common.base.MoreObjects;


/**
 * Interface for ResourceHostMetric
 */
public abstract class ResourceHostMetric extends Metric
{
    protected String peerId;


    public String getPeerId()
    {
        return peerId;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "metric", super.toString() ).add( "peerId", peerId ).toString();
    }
}
