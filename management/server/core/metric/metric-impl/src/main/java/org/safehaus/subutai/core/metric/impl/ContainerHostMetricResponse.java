package org.safehaus.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.metric.api.ContainerHostMetric;


/**
 * Response to ContainerHostMetricRequest
 */
public class ContainerHostMetricResponse
{
    private UUID requestId;
    private Set<ContainerHostMetric> metrics;


    public ContainerHostMetricResponse( final UUID requestId, final Set<ContainerHostMetric> metrics )
    {
        this.requestId = requestId;
        this.metrics = metrics;
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public Set<ContainerHostMetric> getMetrics()
    {
        return metrics;
    }
}
