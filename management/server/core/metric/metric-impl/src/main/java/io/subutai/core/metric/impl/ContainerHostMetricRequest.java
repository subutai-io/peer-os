package io.subutai.core.metric.impl;


/**
 * Remote containers host metric request
 */
public class ContainerHostMetricRequest
{
    private String environmentId;


    public ContainerHostMetricRequest( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
