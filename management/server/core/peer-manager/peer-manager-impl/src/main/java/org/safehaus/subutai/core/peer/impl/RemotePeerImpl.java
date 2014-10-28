package org.safehaus.subutai.core.peer.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.RemotePeer;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Created by timur on 10/22/14.
 */
public class RemotePeerImpl implements RemotePeer
{
    protected Peer peer;


    public RemotePeerImpl( final Peer peer )
    {
        this.peer = peer;
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        return false;
    }


    @Override
    public UUID getId()
    {
        return peer.getId();
    }


    @Override
    public UUID getOwnerId()
    {
        return null;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId ) throws PeerException
    {
        return null;
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID creatorPeerId, final UUID environmentId,
                                                final List<Template> templates, final int quantity, final String strategyId,
                                                final List<Criteria> criteria ) throws ContainerCreateException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000 );
        return remotePeerRestClient
                .createRemoteContainers( peer.getIp(), "8181", creatorPeerId, environmentId, templates, quantity,
                        strategyId, criteria );
    }


    @Override
    public boolean startContainer( final ContainerHost containerHost ) throws PeerException
    {
        return false;
    }


    @Override
    public boolean stopContainer( final ContainerHost containerHost ) throws PeerException
    {
       return false;
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {

    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        return false;
    }
}
