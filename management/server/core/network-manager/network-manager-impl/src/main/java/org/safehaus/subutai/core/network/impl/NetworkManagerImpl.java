package org.safehaus.subutai.core.network.impl;


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.network.api.ContainerInfo;
import org.safehaus.subutai.core.network.api.N2NConnection;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.network.api.Tunnel;
import org.safehaus.subutai.core.network.impl.remote.RemoteNetworkManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


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
                                    final String communityName, final String localIp, final String pathToKeyFile )
            throws NetworkManagerException
    {
        execute( getManagementHost(),
                commands.getSetupN2NConnectionCommand( superNodeIp, superNodePort, interfaceName, communityName,
                        localIp, pathToKeyFile ) );
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
    public Set<Tunnel> listTunnels() throws NetworkManagerException
    {
        CommandResult result = execute( getManagementHost(), commands.getListTunnelsCommand() );

        Set<Tunnel> tunnels = Sets.newHashSet();
        Pattern pattern = Pattern.compile( "(tunnel.+)-(.+)" );
        Matcher m = pattern.matcher( result.getStdOut() );
        while ( m.find() && m.groupCount() == 2 )
        {
            tunnels.add( new TunnelImpl( m.group( 1 ), m.group( 2 ) ) );
        }
        return tunnels;
    }


    @Override
    public Set<N2NConnection> listN2NConnections() throws NetworkManagerException
    {
        CommandResult result = execute( getManagementHost(), commands.getListN2NConnectionsCommand() );

        Set<N2NConnection> connections = Sets.newHashSet();
        Pattern pattern = Pattern.compile(
                "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+(\\d+)"
                        + "\\s+(\\w+)\\s+(\\w+)" );
        Matcher m = pattern.matcher( result.getStdOut() );
        while ( m.find() && m.groupCount() == 5 )
        {
            connections.add( new N2NConnectionImpl( m.group( 1 ), m.group( 2 ), Integer.parseInt( m.group( 3 ) ),
                    m.group( 4 ), m.group( 5 ) ) );
        }

        return connections;
    }


    @Override
    public void setupVniVLanMapping( final String tunnelName, final int vni, final int vLanId )
            throws NetworkManagerException
    {
        execute( getManagementHost(), commands.getSetupVniVlanMappingCommand( tunnelName, vni, vLanId ) );
    }


    @Override
    public void removeVniVLanMapping( final String tunnelName, final int vni, final int vLanId )
            throws NetworkManagerException
    {
        execute( getManagementHost(), commands.getRemoveVniVlanMappingCommand( tunnelName, vni, vLanId ) );
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


    @Override
    public ContainerInfo getContainerIp( final String containerName ) throws NetworkManagerException
    {
        CommandResult result =
                execute( getResourceHost( containerName ), commands.getShowContainerIpCommand( containerName ) );

        Pattern pattern = Pattern.compile(
                "Environment IP:\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})/(\\d+)\\s+Vlan ID:\\s+(\\d+)\\s+" );
        Matcher m = pattern.matcher( result.getStdOut() );
        if ( m.find() && m.groupCount() == 3 )
        {
            return new ContainerInfoImpl( m.group( 1 ), Integer.parseInt( m.group( 2 ) ),
                    Integer.parseInt( m.group( 3 ) ) );
        }
        else
        {
            throw new NetworkManagerException( String.format( "Network info of %s not found", containerName ) );
        }
    }


    @Override
    public NetworkManager getRemoteManager( String host, int port )
    {
        return new RemoteNetworkManager( host, port );
    }


    protected ManagementHost getManagementHost() throws NetworkManagerException
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


    protected ResourceHost getResourceHost( String containerName ) throws NetworkManagerException
    {
        try
        {
            ContainerHost containerHost = getContainerHost( containerName );
            return peerManager.getLocalPeer().getResourceHostByName( containerHost.getParentHostname() );
        }
        catch ( PeerException e )
        {
            throw new NetworkManagerException( e );
        }
    }


    protected ContainerHost getContainerHost( String containerName ) throws NetworkManagerException
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


    protected CommandResult execute( Host host, RequestBuilder requestBuilder ) throws NetworkManagerException
    {
        try
        {
            CommandResult result = host.execute( requestBuilder );
            if ( !result.hasSucceeded() )
            {
                throw new NetworkManagerException(
                        String.format( "Command failed: %s, %s", result.getStdErr(), result.getStatus() ) );
            }

            return result;
        }
        catch ( CommandException e )
        {
            throw new NetworkManagerException( e );
        }
    }
}
