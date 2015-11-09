package io.subutai.common.peer;


import java.io.IOException;
import java.util.Map;
import java.util.Set;

import io.subutai.common.host.HostInfo;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Tunnel;


/**
 * Management host interface.
 */
public interface ManagementHost extends Host, HostInfo
{
    /**
     * Adds remote apt repository to local apt sources
     */
    void addRepository( final String ip ) throws PeerException;

    /**
     * Removes remote apt repository from local apt sources
     */
    void removeRepository( final String host, final String ip ) throws PeerException;

    /**
     * Reads local file's contents as string
     */
    String readFile( String path ) throws IOException;

    /**
     * Sets up tunnels to remote peers in the context of environment
     *
     * @param peerIps - remote peer ips
     * @param environmentId -  context environment
     */
    int setupTunnels( Map<String, String> peerIps, String environmentId ) throws PeerException;

    public Vni findVniByEnvironmentId( String environmentId ) throws PeerException;

    /**
     * Returns reserved vnis
     */
    Set<Vni> getReservedVnis() throws PeerException;

    /**
     * Reserves VNI
     */
    Vni reserveVni( Vni vni ) throws PeerException;

    /**
     * Returns all existing gateways
     */
    Set<Gateway> getGateways() throws PeerException;

    /**
     * Create a gateway
     */
    void createGateway( String gatewayIp, int vlan ) throws PeerException;

    /**
     * Removes a gateway
     */
    void removeGateway( int vlan ) throws PeerException;

    /**
     * Cleans up environment networking settings. This method is called when an environment is being destroyed to clean
     * up its settings on the local peer.
     */
    void cleanupEnvironmentNetworkSettings( final EnvironmentId environmentId ) throws PeerException;

    /**
     * Removes a tunnel to remote peer
     */
    void removeTunnel( String tunnelIp );

    /**
     * Returns external IP of mgmt host
     */
    String getExternalIp();

    void setupN2NConnection( N2NConfig config ) throws PeerException;

    /**
     * Returns reverse proxy environment domain assigned to vlan if any
     *
     * @param vlan - vlan id
     *
     * @return - domain or null if no domain assigned to the vlan
     */
    String getVlanDomain( int vlan ) throws PeerException;

    /**
     * Removes domain from vlan if any
     *
     * @param vlan - vlan id
     */
    void removeVlanDomain( int vlan ) throws PeerException;

    /**
     * Assigns reverse proxy environment domain  to vlan
     *
     * @param vlan - vlan id
     * @param domain - domain to assign
     * @param domainLoadBalanceStrategy - strategy to load balance requests to the domain
     * @param sslCertPath - path to SSL certificate to enable HTTPS access to domai only, null if not needed
     */
    void setVlanDomain( int vlan, String domain, DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                        String sslCertPath ) throws PeerException;

    /**
     * Returns true if hostIp is added to reverse proxy environment domain  by vni
     *
     * @param hostIp - ip of host to check
     * @param vlan - vlan id
     */
    boolean isIpInVlanDomain( String hostIp, int vlan ) throws PeerException;

    /**
     * Adds ip to reverse proxy environment domain
     *
     * @param hostIp - ip to remove
     * @param vlan - vlan id
     */
    void addIpToVlanDomain( String hostIp, int vlan ) throws PeerException;

    /**
     * Removes ip from reverse proxy environment domain
     *
     * @param hostIp - ip to remove
     * @param vlan - vlan id
     */
    void removeIpFromVlanDomain( String hostIp, int vlan ) throws PeerException;

    void removeN2NConnection( N2NConfig config );

    int findTunnel( String tunnelIp, Set<Tunnel> tunnels );

    int calculateNextTunnelId( Set<Tunnel> tunnels );

    void setupVniVlanMapping( int tunnelId, long vni, int vlan, String environmentId ) throws PeerException;
}
