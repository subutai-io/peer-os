package org.safehaus.subutai.core.metric.api;


import java.util.UUID;

import com.google.common.base.Objects;


/**
 * Interface for ContainerHostMetric
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
        return Objects.toStringHelper( this ).add( "environmentId", environmentId ).toString();
    }
}
