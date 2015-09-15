package io.subutai.common.metric;


import com.google.common.base.Objects;


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
        return Objects.toStringHelper( this ).add( "metric", super.toString() ).add( "peerId", peerId ).toString();
    }
}
