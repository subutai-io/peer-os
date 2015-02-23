package org.safehaus.subutai.core.peer.api;


import java.util.Set;
import java.util.UUID;


public interface ContainerGroup
{
    public UUID getEnvironmentId();

    public UUID getInitiatorPeerId();

    public UUID getOwnerId();

    public Set<UUID> getContainerIds();
}
