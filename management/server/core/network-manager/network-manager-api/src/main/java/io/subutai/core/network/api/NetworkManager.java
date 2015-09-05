package io.subutai.core.network.api;


import java.util.Set;
import java.util.UUID;

import io.subutai.common.network.Vni;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.peer.ContainerHost;


public interface NetworkManager
{
    String TUNNEL_PREFIX = "tunnel";
    String TUNNEL_TYPE = "vxlan";
    String N2N_STRING_KEY = "string";
    String N2N_FILE_KEY = "file";


    /**
     * Sets up an N2N connection to super node on management host
     */
    public void setupN2NConnection( String superNodeIp, int superNodePort, String interfaceName, String communityName,
                                    String localIp, String keyType, String pathToKeyFile )
            throws NetworkManagerException;

    /**
     * Removes N2N connection to super node on management host
     */
    public void removeN2NConnection( String interfaceName, String communityName ) throws NetworkManagerException;

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
     * Cleans up network settings left after environment
     *
     * @param environmentId - environment id
     */
    public void cleanupEnvironmentNetworkSettings( UUID environmentId ) throws NetworkManagerException;

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

    /**
     * Sets up VNI-VLAN mapping on management host
     */
    public void setupVniVLanMapping( int tunnelId, long vni, int vLanId, UUID environmentId )
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
    public Set<Vni> getReservedVnis() throws NetworkManagerException;

    /**
     * Enables passwordless ssh access between containers
     *
     * @param containers - set of {@code ContainerHost}
     */
    public void exchangeSshKeys( Set<ContainerHost> containers ) throws NetworkManagerException;

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
     */
    public void setVlanDomain( int vLanId, String domain ) throws NetworkManagerException;

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
}

