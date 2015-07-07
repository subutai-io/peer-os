package io.subutai.core.metric.impl;


import java.util.UUID;


/**
 * Remote containers host metric request
 */
public class ContainerHostMetricRequest
{
    private UUID environmentId;


    public ContainerHostMetricRequest( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }
}
