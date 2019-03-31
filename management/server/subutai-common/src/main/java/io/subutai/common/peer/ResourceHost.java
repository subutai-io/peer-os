package io.subutai.common.peer;


import java.util.Set;

import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.common.environment.RhTemplatesDownloadProgress;
import io.subutai.common.environment.RhTemplatesUploadProgress;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.Snapshots;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPort;
import io.subutai.common.protocol.ReservedPorts;
import io.subutai.common.protocol.Template;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;


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

    Snapshots listContainerHostSnapshots( ContainerHost containerHost ) throws ResourceHostException;

    void removeContainerSnapshot( ContainerHost containerHost, String partition, String label )
            throws ResourceHostException;

    void rollbackToContainerSnapshot( ContainerHost containerHost, String partition, String label, boolean force )
            throws ResourceHostException;

    void addContainerSnapshot( ContainerHost containerHost, String partition, String label, boolean stopContainer )
            throws ResourceHostException;

    /**
     * Saves container filesystem (i.e. its snapshots) to a file. An archive "tar.gz" file containing the specified
     * snapshots will be created and dumped to the specified directory as a result of this call
     *
     * @param label1 name of snapshot
     * @param label2 optional name of second (more recent) snapshot to have incremental snapshot between label1 and
     * label2, if null is passed then incremental snapshot between parent template and label1 is used
     * @param destinationDirectory directory to save snapshot to, if null is passed then Common.RH_CACHE_DIR  is used
     *
     * @return full path to a created archive file
     */
    String saveContainerFilesystem( ContainerHost containerHost, String label1, String label2,
                                    String destinationDirectory ) throws ResourceHostException;

    /**
     * Recreates container filesystem from snapshots. Note: Only a container filesystem is recreated, container itself
     * is not fully functional after this call and an additional call to {@link ResourceHost#recreateContainer(String,
     * String, String, int, String)} must be made as a last step. This method can be called as many times for the same
     * container as there are snapshots files. It is important to keep the order of precedence between snapshots in the
     * files, when calling this method several times for the same container
     *
     * @param containerName name for a new container
     * @param pathToFile absolute path to a file with container snapshots
     */
    void recreateContainerFilesystem( String containerName, String pathToFile ) throws ResourceHostException;

    /**
     * Recreates container. This is the last method to call when recreating a container from snapshots file(s). See
     * {@link ResourceHost#recreateContainerFilesystem(String, String)}
     *
     * @param containerName name of container
     * @param hostname hostname to set for container
     * @param ip address of container in form "ip/mask"
     * @param vlan vlan number of container
     * @param environmentId env id of container
     *
     * @return ID of container
     */
    String recreateContainer( String containerName, String hostname, String ip, int vlan, String environmentId )
            throws ResourceHostException;

    /**
     * Download a file from raw category on CDN
     *
     * @param fileId id of file to be downloaded
     * @param destinationDirectory destination directory to download file to, if null is passed then default directory
     * Common.RH_CACHE_DIR is used
     *
     * @return full path to downloaded file
     */
    String downloadRawFileFromCdn( String fileId, String destinationDirectory ) throws ResourceHostException;

    /**
     * Uploads a file to user raw category on CDN
     *
     * @param pathToFile full path to file to be uploaded
     * @param cdnToken user CDN token
     *
     * @return id of file on CDN
     */
    String uploadRawFileToCdn( String pathToFile, String cdnToken ) throws ResourceHostException;


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

    void exportTemplate( String containerName, String templateName, String version, boolean isPrivateTemplate,
                         String token ) throws ResourceHostException;

    Set<String> listExistingContainerNames() throws ResourceHostException;

    Set<ContainerInfo> listExistingContainersInfo() throws ResourceHostException;

    /**
     * Checks if lxc instance exists
     *
     * @param name name of container or template
     */
    Boolean lxcExists( String name ) throws ResourceHostException;


    ReservedPorts getReservedPorts() throws ResourceHostException;

    ReservedPorts getContainerPortMappings( final Protocol protocol ) throws ResourceHostException;

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
                                   String sslCertPath, LoadBalancing loadBalancing, boolean sslBackend,
                                   boolean redirect, boolean http2 ) throws ResourceHostException;

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

    Set<ReservedPort> getReservedPortMappings() throws ResourceHostException;

    String getIp() throws ResourceHostException;

    Set<String> getUsedP2pIfaceNames() throws ResourceHostException;

    /**
     * Updates RH
     *
     * @return - true if update was available, false otherwise
     */
    boolean update();

    boolean ping();

    String getTimezoneOffset() throws ResourceHostException;
}
