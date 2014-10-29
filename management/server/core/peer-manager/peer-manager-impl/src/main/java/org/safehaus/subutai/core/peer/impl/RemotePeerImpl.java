package org.safehaus.subutai.core.peer.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.RemotePeer;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Created by timur on 10/22/14.
 */
public class RemotePeerImpl implements RemotePeer
{
    protected PeerInfo peerInfo;


    public RemotePeerImpl( final PeerInfo peerInfo )
    {
        this.peerInfo = peerInfo;
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        return false;
    }


    @Override
    public UUID getId()
    {
        return peerInfo.getId();
    }


    @Override
    public String getName()
    {
        return peerInfo.getName();
    }


    @Override
    public UUID getOwnerId()
    {
        return null;
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient.getContainerHostsByEnvironmentId( environmentId );
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID creatorPeerId, final UUID environmentId,
                                                final List<Template> templates, final int quantity,
                                                final String strategyId, final List<Criteria> criteria )
            throws ContainerCreateException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient
                .createContainers( creatorPeerId, environmentId, templates, quantity, strategyId, criteria );
    }


    @Override
    public boolean startContainer( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        return remotePeerRestClient.startContainer( containerHost );
    }


    @Override
    public boolean stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        return remotePeerRestClient.stopContainer( containerHost );
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        remotePeerRestClient.destroyContainer( containerHost );
    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient.isConnected( host );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient.execute( requestBuilder, host );
    }
}
