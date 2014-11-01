package org.safehaus.subutai.core.metric.impl;


import java.util.UUID;


/**
 * Remote containers host metric request
 */
public class ContainerHostMetricRequest
{
    private UUID environmentId;
    private UUID id;


    public ContainerHostMetricRequest( final UUID environmentId )
    {
        this.environmentId = environmentId;
        this.id = UUID.randomUUID();
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public UUID getId()
    {
        return id;
    }
}
