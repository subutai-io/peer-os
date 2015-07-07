package io.subutai.core.metric.api;


import java.util.UUID;

import org.safehaus.subutai.common.metric.Metric;

import com.google.common.base.Objects;


/**
 * Interface for ContainerHostMetric
 *
 * TODO move this class to common.metric & update all plugins
 */
public abstract class ContainerHostMetric extends Metric
{
    protected UUID environmentId;


    public UUID getEnvironmentId()
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
