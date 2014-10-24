package org.safehaus.subutai.core.metric.api;


import java.util.UUID;


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
}
