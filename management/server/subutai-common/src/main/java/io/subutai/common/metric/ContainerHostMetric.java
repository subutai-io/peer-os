package io.subutai.common.metric;


import com.google.common.base.MoreObjects;


/**
 * Interface for ContainerHostMetric
 */
public abstract class ContainerHostMetric extends Metric
{
    protected String environmentId;


    public String getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "metric", super.toString() ).add( "hostId", hostId )
                          .add( "environmentId", environmentId ).toString();
    }
}
