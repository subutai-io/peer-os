package io.subutai.core.metric.api;


import io.subutai.common.metric.Alert;


/**
 * AlertListener
 */
public interface AlertListener
{
    /**
     * Notifies listeners about threshold excess on the container
     *
     * @param alert - {@code Alert} metric of the host where thresholds are being exceeded
     */
    public void onAlert( Alert alert ) throws Exception;

    /**
     * Returns unique id of subscriber module for routing notifications
     *
     * @return - id of subscriber module
     */
    public String getSubscriberId();
}
