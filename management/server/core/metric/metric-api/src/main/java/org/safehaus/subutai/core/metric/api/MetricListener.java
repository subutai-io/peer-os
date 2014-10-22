package org.safehaus.subutai.core.metric.api;


/**
 * Interface for MetricListener
 */
public interface MetricListener
{
    /**
     * Notifies listeners about threshold excess on the container
     *
     * @param metric - {@code ContainerHostMetric} metric of the host where thresholds are being exceeded
     */
    public void alertThresholdExcess( ContainerHostMetric metric );
}
