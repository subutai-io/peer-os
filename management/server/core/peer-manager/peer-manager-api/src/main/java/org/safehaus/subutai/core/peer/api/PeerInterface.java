package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Peer interface
 */
public interface PeerInterface
{

    public UUID getId();

    public UUID getOwnerId();

    public Set<ContainerHost> getContainerHostsByEnvironmentId( UUID environmentId ) throws PeerException;

    public Set<ContainerHost> createContainers( UUID creatorPeerId, UUID environmentId, List<Template> templates,
                                                int quantity, String strategyId, List<Criteria> criteria )
            throws ContainerCreateException;

    public boolean startContainer( ContainerHost containerHost ) throws PeerException;

    public boolean stopContainer( ContainerHost containerHost ) throws PeerException;

    public void destroyContainer( ContainerHost containerHost ) throws PeerException;

    public boolean isConnected( Host host ) throws PeerException;
}
