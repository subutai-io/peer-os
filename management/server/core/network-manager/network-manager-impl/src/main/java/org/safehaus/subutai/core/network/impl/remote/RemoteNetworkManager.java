package org.safehaus.subutai.core.network.impl.remote;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.network.api.ContainerInfo;
import org.safehaus.subutai.core.network.api.N2NConnection;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.network.api.Tunnel;
import org.safehaus.subutai.core.network.impl.N2NConnectionImpl;
import org.safehaus.subutai.core.network.impl.TunnelImpl;


public class RemoteNetworkManager implements NetworkManager
{

    private final RemoteNetworkRestClient client;


    public RemoteNetworkManager( String host, int port )
    {
        Objects.requireNonNull( host, "remote peer" );
        this.client = new RemoteNetworkRestClient( host, port );
    }


    @Override
    public void setupN2NConnection( String superNodeIp, int superNodePort, String interfaceName, String communityName,
                                    String localIp, String pathToKeyFile ) throws NetworkManagerException
    {
        N2NConnection n2n = new N2NConnectionImpl( localIp, superNodeIp, superNodePort, interfaceName, communityName );
        client.setupN2NConnection( n2n, pathToKeyFile );
    }


    @Override
    public void removeN2NConnection( String interfaceName, String communityName ) throws NetworkManagerException
    {
        client.removeN2NConnection( interfaceName, communityName );
    }


    @Override
    public void setupTunnel( String tunnelName, String tunnelIp, String tunnelType ) throws NetworkManagerException
    {
        Tunnel t = new TunnelImpl( tunnelName, tunnelIp );
        client.setupTunnel( t, tunnelType );
    }


    @Override
    public void removeTunnel( String tunnelName ) throws NetworkManagerException
    {
        client.removeTunnel( tunnelName );
    }


    @Override
    public void setContainerIp( String containerName, String ip, int netMask, int vLanId )
            throws NetworkManagerException
    {
        client.setContainerIp( containerName, ip, netMask, vLanId );
    }


    @Override
    public void removeContainerIp( String containerName ) throws NetworkManagerException
    {
        client.removeContainerIp( containerName );
    }


    @Override
    public ContainerInfo getContainerIp( String containerName ) throws NetworkManagerException
    {
        return client.getContainerInfo( containerName );
    }


    @Override
    public void setupGateway( String gatewayIp, int vLanId ) throws NetworkManagerException
    {
        client.setupGateway( gatewayIp, vLanId );
    }


    @Override
    public void setupGatewayOnContainer( String containerName, String gatewayIp, String interfaceName )
            throws NetworkManagerException
    {
        client.setupGatewayOnContainer( containerName, gatewayIp, interfaceName );
    }


    @Override
    public void removeGateway( int vLanId ) throws NetworkManagerException
    {
        client.removeGateway( vLanId );
    }


    @Override
    public void removeGatewayOnContainer( String containerName ) throws NetworkManagerException
    {
        client.removeGatewayOnContainer( containerName );
    }


    @Override
    public Set<Tunnel> listTunnels() throws NetworkManagerException
    {
        return new HashSet<>( client.listTunnels() );
    }


    @Override
    public Set<N2NConnection> listN2NConnections() throws NetworkManagerException
    {
        return new HashSet<>( client.listN2NConnections() );
    }


    @Override
    public void setupVniVLanMapping( String tunnelName, int vni, int vLanId ) throws NetworkManagerException
    {
        client.setupVniVlanMapping( tunnelName, vni, vLanId );
    }


    @Override
    public void removeVniVLanMapping( String tunnelName, int vni, int vLanId ) throws NetworkManagerException
    {
        client.removeVniVlanMapping( tunnelName, vni, vLanId );
    }


    @Override
    public NetworkManager getRemoteManager( String host, int port )
    {
        return new RemoteNetworkManager( host, port );
    }


    @Override
    public void exchangeSshKeys( final Set<ContainerHost> containers ) throws NetworkManagerException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addSshKeyToAuthorizedKeys( final Set<ContainerHost> containers, final String sshKey )
            throws NetworkManagerException
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void registerHosts( final Set<ContainerHost> containers, final String domainName )
            throws NetworkManagerException
    {
        throw new UnsupportedOperationException();
    }
}

