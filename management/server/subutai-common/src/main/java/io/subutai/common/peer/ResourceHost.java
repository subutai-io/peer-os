package io.subutai.common.peer;


import java.util.List;
import java.util.Set;

import io.subutai.common.environment.RhTemplatesDownloadProgress;
import io.subutai.common.environment.RhTemplatesUploadProgress;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPorts;
import io.subutai.common.protocol.Template;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.bazaar.share.dto.domain.ReservedPortMapping;
import io.subutai.bazaar.share.quota.ContainerQuota;


/**
 * Resource host interface.
 */
public interface ResourceHost extends Host, ResourceHostInfo
{
    /**
     * Returns hosts containers
     */
    Set<ContainerHost> getContainerHosts();

    /**
     * Returns hosted container by its hostname
     */
    ContainerHost getContainerHostByHostName( String hostname ) throws HostNotFoundException;

    /**
     * Returns hosted container by its container name
     */
    ContainerHost getContainerHostByContainerName( final String containerName ) throws HostNotFoundException;

    /**
     * Returns hosted container by its id
     */
    ContainerHost getContainerHostById( String id ) throws HostNotFoundException;

    /**
     * Returns hosted container by its eth0 ip
     */
    ContainerHost getContainerHostByIp( String hostIp ) throws HostNotFoundException;

    /**
     * Starts hosted container
     */
    void startContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Stops hosted container
     */
    void stopContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Destroys hosted container
     */
    void destroyContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Returns state of hosted container
     */
    ContainerHostState getContainerHostState( final ContainerHost container ) throws ResourceHostException;

    void setupTunnels( P2pIps p2pIps, NetworkResource networkResource ) throws ResourceHostException;

    void deleteTunnels( P2pIps p2pIps, NetworkResource networkResource ) throws ResourceHostException;

    Set<ContainerHost> getContainerHostsByEnvironmentId( String environmentId );

    Set<ContainerHost> getContainerHostsByOwnerId( String ownerId );

    Set<ContainerHost> getContainerHostsByPeerId( String peerId );

    void addContainerHost( ContainerHost host );

    void cleanup( EnvironmentId environmentId, int vlan ) throws ResourceHostException;

    int getNumberOfCpuCores() throws ResourceHostException;

    P2PConnections getP2PConnections() throws ResourceHostException;

    void joinP2PSwarm( String p2pIp, String interfaceName, String p2pHash, String secretKey, long secretKeyTtlSec )
            throws ResourceHostException;

    void joinP2PSwarmDHCP( String interfaceName, String p2pHash, String secretKey, long secretKeyTtlSec )
            throws ResourceHostException;

    void removeP2PSwarm( String p2pHash ) throws ResourceHostException;

    void removeP2PNetworkIface( String interfaceName ) throws ResourceHostException;

    void resetSwarmSecretKey( String p2pHash, String newSecretKey, long ttlSeconds ) throws ResourceHostException;

    Tunnels getTunnels() throws ResourceHostException;

    void createTunnel( Tunnel tunnel ) throws ResourceHostException;

    void importTemplate( Template template, String environmentId ) throws ResourceHostException;

    /**
     * Clones container based on the specified arguments
     *
     * @return ID of container
     */
    String cloneContainer( Template template, String containerName, String hostname, String ip, int vlan,
                           String environmentId ) throws ResourceHostException;

    void setContainerQuota( ContainerHost containerHost, ContainerQuota containerQuota ) throws ResourceHostException;

    String getRhVersion() throws ResourceHostException;

    String getP2pVersion() throws ResourceHostException;

    String getP2pStatusByP2PHash( String p2pHash ) throws ResourceHostException;

    String getOsName() throws ResourceHostException;

    void setContainerHostname( ContainerHost containerHost, String newHostname ) throws ResourceHostException;

    void setHostname( String newHostname ) throws ResourceHostException;

    int getVlan() throws ResourceHostException;

    boolean isManagementHost();

    RhTemplatesDownloadProgress getTemplateDownloadProgress( String environmentId );

    RhTemplatesUploadProgress getTemplateUploadProgress( String templateName );

    void removeContainerHost( ContainerHost containerHost );

    void exportTemplate( String containerName, String templateName, String version,
                                     boolean isPrivateTemplate, String token ) throws ResourceHostException;

    Set<String> listExistingContainerNames() throws ResourceHostException;

    Set<ContainerInfo> listExistingContainersInfo() throws ResourceHostException;


    ReservedPorts getReservedPorts() throws ResourceHostException;

    ReservedPorts getContainerPortMappings( final Protocol protocol ) throws ResourceHostException;

    /**
     * Maps specified container port to random RH port
     *
     * @param protocol protocol
     * @param containerIp ip of container
     * @param containerPort container port
     */
    int mapContainerPort( Protocol protocol, String containerIp, int containerPort ) throws ResourceHostException;

    /**
     * Maps specified container port to specified RH port (RH port acts as a clustered group for multiple containers)
     *
     * @param protocol protocol
     * @param containerIp ip of container
     * @param containerPort container port
     * @param rhPort RH port
     */
    void mapContainerPort( Protocol protocol, String containerIp, int containerPort, int rhPort )
            throws ResourceHostException;

    /**
     * Removes specified container port mapping
     *
     * @param protocol protocol
     * @param containerIp ip of container
     * @param containerPort container port
     * @param rhPort RH port
     */
    void removeContainerPortMapping( Protocol protocol, String containerIp, int containerPort, int rhPort )
            throws ResourceHostException;

    /**
     * Maps specified container port to specified RH port (RH port acts as a clustered group for multiple containers)
     *
     * @param protocol protocol, can only be http or https
     * @param containerIp ip of container
     * @param containerPort container port
     * @param rhPort RH port
     * @param domain domain
     * @param sslCertPath optional path to SSL cert, pass null if not needed
     * @param loadBalancing optional load balancing method, pass null if not needed
     * @param sslBackend determines if backend is working over SSL or not
     */
    void mapContainerPortToDomain( Protocol protocol, String containerIp, int containerPort, int rhPort, String domain,
                                   String sslCertPath, LoadBalancing loadBalancing, boolean sslBackend )
            throws ResourceHostException;

    /**
     * Removes specified container port domain mapping
     *
     * @param protocol protocol, can only be http or https
     * @param containerIp ip of container
     * @param containerPort container port
     * @param rhPort RH port
     * @param domain domain
     */
    void removeContainerPortDomainMapping( Protocol protocol, String containerIp, int containerPort, int rhPort,
                                           String domain ) throws ResourceHostException;

    boolean isPortMappingReserved( final Protocol protocol, final int externalPort, final String ipAddress,
                                   final int internalPort, final String domain ) throws ResourceHostException;

    List<ReservedPortMapping> getReservedPortMappings() throws ResourceHostException;

    String getIp() throws ResourceHostException;

    Set<String> getUsedP2pIfaceNames() throws ResourceHostException;

    /**
     * Updates RH
     *
     * @return - true if update was available, false otherwise
     */
    boolean update();

    boolean ping();
}
