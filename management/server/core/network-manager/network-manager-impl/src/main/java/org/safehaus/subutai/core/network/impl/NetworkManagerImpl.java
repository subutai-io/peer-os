package org.safehaus.subutai.core.network.impl;


import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;


/**
 * Implementation of Network Manager
 */
public class NetworkManagerImpl implements NetworkManager
{
    private final PeerManager peerManager;


    public NetworkManagerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );

        this.peerManager = peerManager;
    }


    @Override
    public void setupN2NConnection( final String superNodeIp, final int superNodePort, final String tapInterfaceName,
                                    final String communityName, final String localIp ) throws NetworkManagerException
    {

    }


    @Override
    public void removeN2NConnection( final String tapInterfaceName, final String communityName )
            throws NetworkManagerException
    {

    }


    @Override
    public void setupTunnel( final String tunnelName, final String peerIp, final String connectionType )
            throws NetworkManagerException
    {

    }


    @Override
    public void removeTunnel( final String tunnelName ) throws NetworkManagerException
    {

    }


    @Override
    public void setContainerIp( final String containerName, final String ip, final int netMask, final int vLanId )
            throws NetworkManagerException
    {

    }


    @Override
    public void removeContainerIp( final String containerName ) throws NetworkManagerException
    {

    }


    private ManagementHost getManagementHost() throws NetworkManagerException
    {
        try
        {
            return peerManager.getLocalPeer().getManagementHost();
        }
        catch ( PeerException e )
        {
            throw new NetworkManagerException( e );
        }
    }


    public ResourceHost getResourceHost( String containerName ) throws NetworkManagerException
    {
        try
        {
            ContainerHost containerHost = peerManager.getLocalPeer().getContainerHostByName( containerName );
            return peerManager.getLocalPeer().getResourceHostByName( containerHost.getParentHostname() );
        }
        catch ( PeerException e )
        {
            throw new NetworkManagerException( e );
        }
    }
}
