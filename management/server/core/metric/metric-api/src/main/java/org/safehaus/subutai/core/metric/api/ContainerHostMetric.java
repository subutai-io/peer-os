package org.safehaus.subutai.core.metric.api;


import java.util.UUID;

import org.safehaus.subutai.common.metric.Metric;

import com.google.common.base.Objects;


/**
 * Interface for ContainerHostMetric
 */
public abstract class ContainerHostMetric extends Metric
{
    protected UUID environmentId;
    protected UUID hostId;


    public UUID getHostId()
    {
        return hostId;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "hostId", hostId ).add( "environmentId", environmentId ).toString();
    }
}
