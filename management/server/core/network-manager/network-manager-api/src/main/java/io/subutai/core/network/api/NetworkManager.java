package io.subutai.core.network.api;


import java.util.Set;

import io.subutai.common.network.SshTunnel;
import io.subutai.common.peer.Host;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPorts;
import io.subutai.common.protocol.Tunnels;


public interface NetworkManager
{

    /**
     * Sets up an P2P connection on specified host with explicit IP.
     */
    void joinP2PSwarm( Host host, String interfaceName, String localIp, String p2pHash, String secretKey,
                       long secretKeyTtlSec ) throws NetworkManagerException;

    /**
     * Sets up a P2P connection on given host with dynamic IP address.
     */
    void joinP2PSwarmDHCP( Host host, String interfaceName, String p2pHash, String secretKey, long secretKeyTtlSec )
            throws NetworkManagerException;

    void removeP2PSwarm( Host host, String p2pHash ) throws NetworkManagerException;

    void removeP2PIface( Host host, String interfaceName ) throws NetworkManagerException;

    /**
     * Resets a secret key for a given P2P network
     *
     * @param host - host
     * @param p2pHash - P2P network hash
     * @param newSecretKey - new secret key to set
     * @param ttlSeconds - time-to-live for the new secret key
     */
    void resetSwarmSecretKey( Host host, String p2pHash, String newSecretKey, long ttlSeconds )
            throws NetworkManagerException;


    /**
     * Returns all p2p connections running on the specified host
     *
     * @param host - host
     */

    P2PConnections getP2PConnections( Host host ) throws NetworkManagerException;

    String getP2pVersion( Host host ) throws NetworkManagerException;

    String getP2pStatusByP2PHash( Host host, String p2pHash ) throws NetworkManagerException;

    Set<String> getUsedP2pIfaceNames( final Host host ) throws NetworkManagerException;

    void createTunnel( Host host, String tunnelName, String tunnelIp, int vlan, long vni )
            throws NetworkManagerException;

    void deleteTunnel( Host host, String tunnelName ) throws NetworkManagerException;

    Tunnels getTunnels( Host host ) throws NetworkManagerException;


    /**
     * Sets up SSH connectivity for container identified by @param containerIp
     *
     * @param containerIp - ip fo container
     * @param sshIdleTimeout - timeout during which the ssh connectivity is active in seconds
     *
     * @return - port to which clients should connect to access the container via ssh
     */
    SshTunnel setupContainerSshTunnel( String containerIp, int sshIdleTimeout ) throws NetworkManagerException;

    ReservedPorts getReservedPorts( final Host host ) throws NetworkManagerException;

    ReservedPorts getContainerPortMappings( final Host host, final Protocol protocol ) throws NetworkManagerException;


    /**
     * Check if port is already mapped using 'subutai map' command
     *
     * @param host RH host on which to check mapping existence
     * @param ipAddress IP address of container of Resource Host
     */
    boolean isPortMappingReserved( final Host host, final Protocol protocol, final int externalPort,
                                   final String ipAddress, final int internalPort, final String domain )
            throws NetworkManagerException;


    /**
     * Maps specified container port to specified RH port (RH port acts as a clustered group for multiple containers)
     *
     * @param host RH host
     * @param protocol protocol
     * @param containerIp ip of container
     * @param containerPort container port
     * @param rhPort RH port
     */
    void mapContainerPort( Host host, Protocol protocol, String containerIp, int containerPort, int rhPort )
            throws NetworkManagerException;

    /**
     * Removes specified container port mapping
     *
     * @param host RH host
     * @param protocol protocol
     * @param containerIp ip of container
     * @param containerPort container port
     * @param rhPort RH port
     */
    void removeContainerPortMapping( Host host, Protocol protocol, String containerIp, int containerPort, int rhPort )
            throws NetworkManagerException;

    /**
     * Maps specified container port to specified RH port (RH port acts as a clustered group for multiple containers)
     *
     * @param host RH host
     * @param protocol protocol, can only be http or https
     * @param containerIp ip of container
     * @param containerPort container port
     * @param rhPort RH port
     * @param domain domain
     * @param sslCertPath optional path to SSL cert, pass null if not needed
     * @param loadBalancing optional load balancing method, pass null if not needed
     * @param sslBackend determines if backend is working over SSL or not
     */
    void mapContainerPortToDomain( Host host, Protocol protocol, String containerIp, int containerPort, int rhPort,
                                   String domain, String sslCertPath, LoadBalancing loadBalancing, boolean sslBackend,
                                   boolean redirect, boolean http2 ) throws NetworkManagerException;

    /**
     * Removes specified container port domain mapping
     *
     * @param host RH host
     * @param protocol protocol, can only be http or https
     * @param containerIp ip of container
     * @param containerPort container port
     * @param rhPort RH port
     * @param domain domain
     */
    void removeContainerPortDomainMapping( Host host, Protocol protocol, String containerIp, int containerPort,
                                           int rhPort, String domain ) throws NetworkManagerException;

    String getResourceHostIp( Host host ) throws NetworkManagerException;
}