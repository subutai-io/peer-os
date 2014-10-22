package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Peer interface
 */
public interface PeerInterface
{
    public UUID getOwnerId();


    public Set<ContainerHost> createContainers( UUID environmentId, String templateName, int quantity,
                                                String strategyId, List<Criteria> criteria )
            throws ContainerCreateException;

    public Set<ContainerHost> getContainers( UUID environmentId ) throws PeerException;

    public void startContainer( ContainerHost containerHost ) throws PeerException;

    public void stopContainer( ContainerHost containerHost ) throws PeerException;

    public void destroyContainer( ContainerHost containerHost ) throws PeerException;

    public boolean isConnected( Host host ) throws PeerException;
}
