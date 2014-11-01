package org.safehaus.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;


/**
 * Response to ContainerHostMetricRequest
 */
public class ContainerHostMetricResponse
{
    private UUID requestId;
    private Set<ContainerHostMetricImpl> metrics;


    public ContainerHostMetricResponse( final UUID requestId, final Set<ContainerHostMetricImpl> metrics )
    {
        this.requestId = requestId;
        this.metrics = metrics;
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public Set<ContainerHostMetricImpl> getMetrics()
    {
        return metrics;
    }
}
