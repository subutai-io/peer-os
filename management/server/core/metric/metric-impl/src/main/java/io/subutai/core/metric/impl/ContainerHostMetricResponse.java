package io.subutai.core.metric.impl;


import java.util.Set;


/**
 * Response to ContainerHostMetricRequest
 */
public class ContainerHostMetricResponse
{

    private Set<ContainerHostMetricImpl> metrics;


    public ContainerHostMetricResponse( final Set<ContainerHostMetricImpl> metrics )
    {

        this.metrics = metrics;
    }


    public Set<ContainerHostMetricImpl> getMetrics()
    {
        return metrics;
    }
}
