package org.safehaus.subutai.core.network.impl;


import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
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
    protected Commands commands = new Commands();


    public NetworkManagerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );

        this.peerManager = peerManager;
    }


    @Override
    public void setupN2NConnection( final String superNodeIp, final int superNodePort, final String interfaceName,
                                    final String communityName, final String localIp ) throws NetworkManagerException
    {
        execute( getManagementHost(),
                commands.getSetupN2NConnectionCommand( superNodeIp, superNodePort, interfaceName, communityName,
                        localIp ) );
    }


    @Override
    public void removeN2NConnection( final String interfaceName, final String communityName )
            throws NetworkManagerException
    {
        execute( getManagementHost(), commands.getRemoveN2NConnectionCommand( interfaceName, communityName ) );
    }


    @Override
    public void setupTunnel( final String tunnelName, final String tunnelIp, final String tunnelType )
            throws NetworkManagerException
    {
        execute( getManagementHost(), commands.getSetupTunnelCommand( tunnelName, tunnelIp, tunnelType ) );
    }


    @Override
    public void removeTunnel( final String tunnelName ) throws NetworkManagerException
    {
        execute( getManagementHost(), commands.getRemoveTunnelCommand( tunnelName ) );
    }


    @Override
    public void setupGateway( final String gatewayIp, final int vLanId ) throws NetworkManagerException
    {
        execute( getManagementHost(), commands.getSetupGatewayCommand( gatewayIp, vLanId ) );
    }


    @Override
    public void setupGatewayOnContainer( final String containerName, final String gatewayIp,
                                         final String interfaceName ) throws NetworkManagerException
    {
        execute( getContainerHost( containerName ),
                commands.getSetupGatewayOnContainerCommand( gatewayIp, interfaceName ) );
    }


    @Override
    public void removeGateway( final int vLanId ) throws NetworkManagerException
    {
        execute( getManagementHost(), commands.getRemoveGatewayCommand( vLanId ) );
    }


    @Override
    public void removeGatewayOnContainer( final String containerName ) throws NetworkManagerException
    {
        execute( getContainerHost( containerName ), commands.getRemoveGatewayOnContainerCommand() );
    }


    @Override
    public void setContainerIp( final String containerName, final String ip, final int netMask, final int vLanId )
            throws NetworkManagerException
    {
        execute( getResourceHost( containerName ),
                commands.getSetContainerIpCommand( containerName, ip, netMask, vLanId ) );
    }


    @Override
    public void removeContainerIp( final String containerName ) throws NetworkManagerException
    {
        execute( getResourceHost( containerName ), commands.getRemoveContainerIpCommand( containerName ) );
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


    public ContainerHost getContainerHost( String containerName ) throws NetworkManagerException
    {
        try
        {
            return peerManager.getLocalPeer().getContainerHostByName( containerName );
        }
        catch ( PeerException e )
        {
            throw new NetworkManagerException( e );
        }
    }


    private void execute( Host host, RequestBuilder requestBuilder ) throws NetworkManagerException
    {
        try
        {
            CommandResult result = host.execute( requestBuilder );
            if ( !result.hasSucceeded() )
            {
                throw new NetworkManagerException(
                        String.format( "Command failed: %s, %s", result.getStdErr(), result.getStatus() ) );
            }
        }
        catch ( CommandException e )
        {
            throw new NetworkManagerException( e );
        }
    }
}
