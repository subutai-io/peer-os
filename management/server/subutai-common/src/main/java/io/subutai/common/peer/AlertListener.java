package io.subutai.common.peer;


/**
 * Alert Listener
 */
public interface AlertListener
{
    /**
     * Notifies listeners about quota excess on the container
     *
     * @param alert - {@code QuotaAlertResource} alert value of the host where thresholds are being exceeded
     */
    public void onAlert( AlertPack alert) throws Exception;

    /**
     * Returns the alert listeners identifier
     *
     * @return - template name
     */

    String getSubscriberId();
}
