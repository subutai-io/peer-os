package io.subutai.common.peer;


/**
 * Alert Listener
 */
//TODO: rename this to AlertHandler
public interface AlertListener
{
    /**
     * Notifies listeners about quota excess on the container
     *
     * @param alert - {@code AlertPack} alert value of the host where thresholds are being exceeded
     */
    public void onAlert( AlertPack alert ) throws Exception;

    /**
     * Returns the template name of container host which alerts listen this listener
     *
     * @return - template name
     */

    String getTemplateName();
}
