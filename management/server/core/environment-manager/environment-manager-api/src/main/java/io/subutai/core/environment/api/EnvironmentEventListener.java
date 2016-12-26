package io.subutai.core.environment.api;


import java.util.Set;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.EnvironmentContainerHost;


/**
 * Environment Events Listener Listens for events applied to environment
 */
public interface EnvironmentEventListener
{
    /**
     * On environment created event
     *
     * @param environment - returns new environment
     */
    void onEnvironmentCreated( Environment environment );


    /**
     * Event on environment been changes/grown
     *
     * @param environment - target environment changed
     * @param newContainers - set of new container hosts
     */
    void onEnvironmentGrown( Environment environment, Set<EnvironmentContainerHost> newContainers );


    /**
     * Target environment whose container host has been destroyed
     *
     * @param environment - target environment
     * @param containerId - destroyed container host id
     */
    void onContainerDestroyed( Environment environment, String containerId );


    /**
     * Event on environment has been destroyed
     *
     * @param environmentId - destroyed environment id
     */
    void onEnvironmentDestroyed( String environmentId );

    /**
     * Event on environment container start
     *
     * @param environment - target environment
     * @param containerId - container id
     */
    void onContainerStarted( Environment environment, String containerId );

    /**
     * Event on environment container stop
     *
     * @param environment - target environment
     * @param containerId - container id
     */
    void onContainerStopped( Environment environment, String containerId );
}
