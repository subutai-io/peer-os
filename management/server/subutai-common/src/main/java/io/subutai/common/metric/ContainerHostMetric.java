package io.subutai.common.metric;


import com.google.common.base.Objects;


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
        return Objects.toStringHelper( this ).add( "metric", super.toString() ).add( "hostId", hostId )
                      .add( "environmentId", environmentId ).toString();
    }
}
