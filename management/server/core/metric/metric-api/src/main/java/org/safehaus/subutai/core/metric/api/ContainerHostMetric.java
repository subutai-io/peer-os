package org.safehaus.subutai.core.metric.api;


import java.util.UUID;

import com.google.common.base.Preconditions;


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


    protected void setEnvironmentId( final UUID environmentId )
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        this.environmentId = environmentId;
    }
}
