package org.safehaus.subutai.core.network.api;


import java.util.Set;


public interface NetworkManager
{
    public void setupN2NConnection( String superNodeIp, int superNodePort, String interfaceName, String communityName,
                                    String localIp ) throws NetworkManagerException;

    public void removeN2NConnection( String interfaceName, String communityName ) throws NetworkManagerException;

    public void setupTunnel( String tunnelName, String tunnelIp, String tunnelType ) throws NetworkManagerException;

    public void removeTunnel( String tunnelName ) throws NetworkManagerException;

    public void setContainerIp( String containerName, String ip, int netMask, int vLanId )
            throws NetworkManagerException;

    public void removeContainerIp( String containerName ) throws NetworkManagerException;

    public ContainerInfo getContainerIp( String containerName ) throws NetworkManagerException;

    public void setupGateway( String gatewayIp, int vLanId ) throws NetworkManagerException;

    public void setupGatewayOnContainer( String containerName, String gatewayIp, String interfaceName )
            throws NetworkManagerException;

    public void removeGateway( int vLanId ) throws NetworkManagerException;

    public void removeGatewayOnContainer( String containerName ) throws NetworkManagerException;

    public Set<Tunnel> listTunnels() throws NetworkManagerException;

    public Set<N2NConnection> listN2NConnections() throws NetworkManagerException;
}

