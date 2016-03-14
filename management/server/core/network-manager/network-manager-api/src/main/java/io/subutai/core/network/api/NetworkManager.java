package io.subutai.core.network.api;


import java.util.Set;

import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.Vni;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.network.Vnis;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PPeerInfo;
import io.subutai.common.protocol.PingDistance;
import io.subutai.common.protocol.Tunnel;


public interface NetworkManager
{
    String TUNNEL_PREFIX = "tunnel";
    String TUNNEL_TYPE = "vxlan";
    String P2P_STRING_KEY = "string";


    /**
     * Sets up an P2P connection to super node on management host
     */
    public void setupP2PConnection( String interfaceName, String localIp, String communityName, String secretKey,
                                    long secretKeyTtlSec ) throws NetworkManagerException;

    /**
     * Removes P2P connection
     */
    public void removeP2PConnection( String communityName ) throws NetworkManagerException;


    /**
     * Resets a secret key for a given P2P network
     *
     * @param p2pHash - P2P network hash
     * @param newSecretKey - new secret key to set
     * @param ttlSeconds - time-to-live for the new secret key
     */
    public void resetP2PSecretKey( String p2pHash, String newSecretKey, long ttlSeconds )
            throws NetworkManagerException;


    /**
     * Lists existing P2P connections on management host
     */
    public Set<P2PConnection> listP2PConnections() throws NetworkManagerException;


    PingDistance getPingDistance( Host host, String sourceHostIp, String targetHostIp ) throws NetworkManagerException;

    public Set<P2PPeerInfo> listPeersInEnvironment( String communityName ) throws NetworkManagerException;

    /**
     * Sets up tunnel to another peer on management host
     */
    public void setupTunnel( int tunnelId, String tunnelIp ) throws NetworkManagerException;

    /**
     * Removes tunnel to another peer on management host
     */
    public void removeTunnel( int tunnelId ) throws NetworkManagerException;

    /**
     * Sets container environment IP and VLAN ID on container
     */
    public void setContainerIp( String containerName, String ip, int netMask, int vLanId )
            throws NetworkManagerException;

    /**
     * Removes container environment IP and VLAN ID on container
     */
    public void removeContainerIp( String containerName ) throws NetworkManagerException;

    /**
     * Returns container environment IP on container
     */
    public ContainerInfo getContainerIp( String containerName ) throws NetworkManagerException;


    /**
     * Removes gateway IP for specified VLAN on management host
     */
    public void removeGateway( int vLanId ) throws NetworkManagerException;

    /**
     * Cleans up network settings left after environment
     *
     * @param environmentId - environment id
     */
    public void cleanupEnvironmentNetworkSettings( EnvironmentId environmentId ) throws NetworkManagerException;

    /**
     * Removes gateway IP on a container
     */
    public void removeGatewayOnContainer( String containerName ) throws NetworkManagerException;

    /**
     * Lists existing tunnels on management host
     */
    public Set<Tunnel> listTunnels() throws NetworkManagerException;


    /**
     * Sets up VNI-VLAN mapping on management host
     */
    public void setupVniVLanMapping( int tunnelId, long vni, int vLanId, String environmentId )
            throws NetworkManagerException;

    /**
     * Removes VNI-VLAN mapping on management host
     */
    public void removeVniVLanMapping( int tunnelId, long vni, int vLanId ) throws NetworkManagerException;

    /**
     * Returns all vni-vlan mappings on management host
     */
    public Set<VniVlanMapping> getVniVlanMappings() throws NetworkManagerException;

    /**
     * Reserves VNI on management host
     *
     * @param vni - vni to reserve
     */
    public void reserveVni( Vni vni ) throws NetworkManagerException;

    /**
     * Returns all reserved VNIs on management host
     */
    public Vnis getReservedVnis() throws NetworkManagerException;

    /**
     * Enables passwordless ssh access between containers
     *
     * @param containers - set of {@code ContainerHost}
     * @param additionalSshKeys - set of additional ssh keys to add to each container
     */
    public void exchangeSshKeys( Set<ContainerHost> containers, Set<String> additionalSshKeys )
            throws NetworkManagerException;

    /**
     * Appends ssh keys to each container
     *
     * @param containers - containers
     * @param sshKeys - ssh keys
     */
    public void appendSshKeys( Set<ContainerHost> containers, Set<String> sshKeys ) throws NetworkManagerException;

    /**
     * Adds supplied ssh key to authorized_keys file of given containers
     *
     * @param containers- set of {@code ContainerHost}
     * @param sshKey - ssh key to add
     */
    public void addSshKeyToAuthorizedKeys( Set<ContainerHost> containers, String sshKey )
            throws NetworkManagerException;

    /**
     * Replaces supplied old ssh key with new ssh key in authorized_keys file of given containers
     *
     * @param containers set of {@code ContainerHost}
     * @param oldSshKey - old ssh key
     * @param newSshKey - new ssh key
     */
    public void replaceSshKeyInAuthorizedKeys( final Set<ContainerHost> containers, final String oldSshKey,
                                               final String newSshKey ) throws NetworkManagerException;

    /**
     * Removes supplied ssh key from authorized_keys file of given containers
     *
     * @param containers set of {@code ContainerHost}
     * @param sshKey - ssh key to remove
     */
    public void removeSshKeyFromAuthorizedKeys( final Set<ContainerHost> containers, final String sshKey )
            throws NetworkManagerException;

    /**
     * Registers containers in /etc/hosts of each other
     *
     * @param containers - set of {@code ContainerHost}
     * @param domainName - domain name e.g. "intra.lan"
     */
    public void registerHosts( Set<ContainerHost> containers, String domainName ) throws NetworkManagerException;

    /**
     * Returns reverse proxy domain assigned to vlan
     *
     * @param vLanId - vlan id
     *
     * @return - domain or null if not assigned
     */
    public String getVlanDomain( int vLanId ) throws NetworkManagerException;


    /**
     * Removes reverse proxy domain assigned to vlan if any
     *
     * @param vLanId - vlan id
     */
    public void removeVlanDomain( int vLanId ) throws NetworkManagerException;

    /**
     * Assigns reverse proxy domain to vlan
     *
     * @param vLanId - vlan id
     * @param domainLoadBalanceStrategy - strategy to load balance requests to the domain
     * @param sslCertPath - path to SSL certificate to enable HTTPS access to domai only, null if not needed
     */
    public void setVlanDomain( int vLanId, String domain, DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                               String sslCertPath ) throws NetworkManagerException;

    /**
     * Checks if IP is in vlan reverse proxy domain
     *
     * @param hostIp - ip to check
     * @param vLanId - vlan id
     *
     * @return - true if ip is in vlan domain, false otherwise
     */
    public boolean isIpInVlanDomain( String hostIp, int vLanId ) throws NetworkManagerException;

    /**
     * Adds ip to vlan reverse proxy domain
     *
     * @param hostIp - ip to add
     * @param vLanId - vlan id
     */
    public void addIpToVlanDomain( String hostIp, int vLanId ) throws NetworkManagerException;

    /**
     * Removes ip from reverse proxy domain
     *
     * @param hostIp - ip to remove
     * @param vLanId - vlan id
     */
    public void removeIpFromVlanDomain( String hostIp, int vLanId ) throws NetworkManagerException;

    /**
     * Sets up SSH connectivity for container identified by @param containerIp
     *
     * @param containerIp - ip fo container
     * @param sshIdleTimeout - timeout during which the ssh connectivity is active in seconds
     *
     * @return - port to which clients should connect to access the contaier via ssh
     */
    int setupContainerSsh( String containerIp, int sshIdleTimeout ) throws NetworkManagerException;
}

