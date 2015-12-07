package io.subutai.common.peer;


/**
 * Alert Listener
 */
public interface AlertListener
{
    String getId();

    void onAlert( AlertEvent alertEvent );
}
