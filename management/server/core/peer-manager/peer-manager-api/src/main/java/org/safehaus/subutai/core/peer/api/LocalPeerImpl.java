package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Local peer implementation
 */
public class LocalPeerImpl extends Peer implements LocalPeer
{
    private PeerManager peerManager;


    public LocalPeerImpl( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }

    //
    //    private PeerManager getPeerManager() throws PeerException
    //    {
    //        PeerManager peerManager = null;
    //        try
    //        {
    //            ServiceLocator.getServiceNoCache( PeerManager.class );
    //        }
    //        catch ( NamingException e )
    //        {
    //            throw new PeerException( "Could not locate PeerManager" );
    //        }
    //        return peerManager;
    //    }


    @Override
    public UUID getOwnerId()
    {
        return null;
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID environmentId, final String templateName, final int quantity,
                                                final String strategyId, final List<Criteria> criteria )
            throws ContainerCreateException
    {
        Set<ContainerHost> result =
                peerManager.createContainers( environmentId, templateName, quantity, strategyId, criteria );
        return result;
    }


    @Override
    public Set<ContainerHost> getContainers( final UUID environmentId ) throws PeerException
    {
        //TODO: implement me
        return null;
    }


    @Override
    public void startContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        resourceHost.startContainerHost( containerHost );
    }


    @Override
    public void stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        resourceHost.stopContainerHost( containerHost );
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        ResourceHost resourceHost = getManagementHost().getResourceHostByName( containerHost.getParentHostname() );
        resourceHost.destroyContainerHost( containerHost );
    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        return peerManager.isConnected( host );
    }


    @Override
    public ManagementHost getManagementHost() throws PeerException
    {
        return peerManager.getManagementHost();
    }


    @Override
    public Set<ResourceHost> getResourceHosts() throws PeerException
    {
        return getManagementHost().getResourceHosts();
    }
}
