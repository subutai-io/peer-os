package io.subutai.core.environment.api;


import java.util.Set;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;


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
    public void onEnvironmentCreated( Environment environment );


    /**
     * Event on environment been changes/grown
     *
     * @param environment - target environment changed
     * @param newContainers - set of new container hosts
     */
    public void onEnvironmentGrown( Environment environment, Set<ContainerHost> newContainers );


    /**
     * Target environment whose container host has been destroyed
     *
     * @param environment - target environment
     * @param containerId - destroyed container host id
     */
    public void onContainerDestroyed( Environment environment, String containerId );


    /**
     * Event on environment has been destroyed
     *
     * @param environmentId - destroyed environment id
     */
    public void onEnvironmentDestroyed( String environmentId );
}
