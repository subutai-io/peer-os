package org.safehaus.subutai.core.metric.api;


/**
 * Interface for MetricListener
 */
public interface MetricListener
{
    public void alertThresholdExcess( ContainerHostMetric metric );
}
