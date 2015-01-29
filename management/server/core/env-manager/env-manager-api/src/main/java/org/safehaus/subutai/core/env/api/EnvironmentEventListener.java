package org.safehaus.subutai.core.env.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.peer.ContainerHost;


public interface EnvironmentEventListener
{
    public void onEnvironmentCreated( Environment environment );

    public void onEnvironmentGrown( Environment environment, Set<ContainerHost> newContainers );

    public void onContainerDestroyed( Environment environment, UUID containerId );

    public void onEnvironmentDestroyed( UUID environmentId );
}
