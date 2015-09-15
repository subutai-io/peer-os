package io.subutai.core.metric.api;


import com.google.common.base.Objects;

import io.subutai.common.metric.Metric;


/**
 * Interface for ContainerHostMetric
 *
 * TODO move this class to common.metric & update all plugins
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
