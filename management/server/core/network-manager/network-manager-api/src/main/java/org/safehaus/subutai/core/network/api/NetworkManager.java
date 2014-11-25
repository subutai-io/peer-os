package org.safehaus.subutai.core.network.api;


import java.util.Set;


public interface NetworkManager
{
    /**
     * Sets up an N2N connection to super node
     */
    public void setupN2NConnection( String superNodeIp, int superNodePort, String interfaceName, String communityName,
                                    String localIp, String pathToKeyFile ) throws NetworkManagerException;

    /**
     * Removes N2N connection to super node
     */
    public void removeN2NConnection( String interfaceName, String communityName ) throws NetworkManagerException;

    /**
     * Sets up tunnel to another peer
     */
    public void setupTunnel( String tunnelName, String tunnelIp, String tunnelType ) throws NetworkManagerException;

    /**
     * Removes tunnel to another peer
     */
    public void removeTunnel( String tunnelName ) throws NetworkManagerException;

    /**
     * Sets container environment IP and VLAN ID
     */
    public void setContainerIp( String containerName, String ip, int netMask, int vLanId )
            throws NetworkManagerException;

    /**
     * Removes container environment IP and VLAN ID
     */
    public void removeContainerIp( String containerName ) throws NetworkManagerException;

    /**
     * Returns container environment IP
     */
    public ContainerInfo getContainerIp( String containerName ) throws NetworkManagerException;

    /**
     * Sets up gateway IP for specified VLAN on management host
     */
    public void setupGateway( String gatewayIp, int vLanId ) throws NetworkManagerException;

    /**
     * Sets up gateway IP on a container
     */
    public void setupGatewayOnContainer( String containerName, String gatewayIp, String interfaceName )
            throws NetworkManagerException;

    /**
     * Removes gateway IP for specified VLAN on management host
     */
    public void removeGateway( int vLanId ) throws NetworkManagerException;

    /**
     * Removes gateway IP on a container
     */
    public void removeGatewayOnContainer( String containerName ) throws NetworkManagerException;

    /**
     * Lists existing tunnels on management host
     */
    public Set<Tunnel> listTunnels() throws NetworkManagerException;

    /**
     * Lists existing N2N connections on management host
     */
    public Set<N2NConnection> listN2NConnections() throws NetworkManagerException;
}

