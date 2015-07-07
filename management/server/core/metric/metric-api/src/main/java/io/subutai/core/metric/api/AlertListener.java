package io.subutai.core.metric.api;


/**
 * AlertListener
 */
public interface AlertListener
{
    /**
     * Notifies listeners about threshold excess on the container
     *
     * @param metric - {@code ContainerHostMetric} metric of the host where thresholds are being exceeded
     */
    public void onAlert( ContainerHostMetric metric ) throws Exception;

    /**
     * Returns unique id of subscriber module for routing notifications
     *
     * @return - id of subscriber module
     */
    public String getSubscriberId();
}
