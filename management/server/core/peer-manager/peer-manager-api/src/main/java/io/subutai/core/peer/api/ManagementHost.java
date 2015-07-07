package io.subutai.core.peer.api;


import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.network.Gateway;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.PeerException;


/**
 * Management host interface.
 */
public interface ManagementHost extends Host
{
    /**
     * Adds remote apt repository to local apt sources
     */
    public void addAptSource( final String host, final String ip ) throws PeerException;

    /**
     * Removes remote apt repository from local apt sources
     */
    public void removeAptSource( final String host, final String ip ) throws PeerException;

    /**
     * Reads local file's contents as string
     */
    public String readFile( String path ) throws IOException;

    /**
     * Sets up tunnels to remote peers in the context of environment
     *
     * @param peerIps - remote peer ips
     * @param environmentId -  context environment
     */
    public int setupTunnels( Set<String> peerIps, UUID environmentId ) throws PeerException;

    /**
     * Returns reserved vnis
     */
    public Set<Vni> getReservedVnis() throws PeerException;

    /**
     * Reserves VNI
     */
    public int reserveVni( Vni vni ) throws PeerException;

    /**
     * Retursn all existing gateways
     */
    public Set<Gateway> getGateways() throws PeerException;

    /**
     * Create a gateway
     */
    public void createGateway( String gatewayIp, int vlan ) throws PeerException;

    /**
     * Removes a gateway
     */
    public void removeGateway( int vlan ) throws PeerException;

    /**
     * Cleans up environment networking settings. This method is called when an environment is being destroyed to clean
     * up its settings on the local peer.
     */
    public void cleanupEnvironmentNetworkSettings( final UUID environmentId ) throws PeerException;

    /**
     * Removes a tunnel to remote peer
     */
    public void removeTunnel( String peerIp ) throws PeerException;

    /**
     * Returns external IP of mgmt host
     */
    public String getExternalIp();
}
