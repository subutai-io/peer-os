package org.safehaus.subutai.core.peer.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;


public interface ContainerGroup
{
    public UUID getEnvironmentId();

    public UUID getInitiatorPeerId();

    public UUID getOwnerId();

    public Set<ContainerHost> getContainerHosts();
}
