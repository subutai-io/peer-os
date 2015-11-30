package io.subutai.core.environment.api;


import io.subutai.common.metric.EnvironmentAlert;
import io.subutai.common.peer.EnvironmentId;


/**
 * Environment alert listener
 */
public interface EnvironmentAlertListener
{
    void onAlert( EnvironmentAlert alert );

    EnvironmentId getEnvironmentId();
}
