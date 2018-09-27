package io.subutai.common.peer;


import java.util.List;
import java.util.Set;

import io.subutai.common.environment.Nodes;
import io.subutai.common.environment.PeerTemplatesUploadProgress;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Template;
import io.subutai.common.util.HostUtil;
import io.subutai.bazaar.share.quota.Quota;
import io.subutai.bazaar.share.resource.ContainerResourceType;
import io.subutai.bazaar.share.resource.PeerResources;


/**
 * Local peer interface
 */
public interface LocalPeer extends Peer
{


    /**
     * Finds host with given ID
     *
     * @param id ID of the host
     *
     * @return if host is registered then returns implementation of this host, otherwise throws exception.
     */
    Host findHost( String id ) throws HostNotFoundException;

    /**
     * Finds host with given hostname
     *
     * @param hostname hostname
     *
     * @return if host is registered then returns implementation of this host, otherwise throws exception.
     */
    Host findHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns not registered container hosts
     *
     * @return {@code ContainerHostInfo}
     */
    Set<ContainerHostInfo> getNotRegisteredContainers();

    Set<ContainerHost> getRegisteredContainers();

    /**
     * Returns resource host instance by its hostname
     */
    ResourceHost getResourceHostByHostName( String hostname ) throws HostNotFoundException;

    /**
     * Returns resource host instance by its id
     */
    ResourceHost getResourceHostById( String hostId ) throws HostNotFoundException;

    /**
     * Returns resource host instance by hostname of its container
     */
    ResourceHost getResourceHostByContainerHostName( String containerName ) throws HostNotFoundException;

    /**
     * Returns resource host instance by name of its container
     */
    ResourceHost getResourceHostByContainerName( String containerName ) throws HostNotFoundException;

    /**
     * Returns resource host instance by id ot its container
     */
    ResourceHost getResourceHostByContainerId( String hostId ) throws HostNotFoundException;


    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostname hostname of the container
     */

    ContainerHost getContainerHostByHostName( String hostname ) throws HostNotFoundException;

    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param containerName name of the container
     */

    ContainerHost getContainerHostByContainerName( String containerName ) throws HostNotFoundException;

    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostId ID of the container
     */
    ContainerHost getContainerHostById( String hostId ) throws HostNotFoundException;

    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostIp IP of the container (eth0 interface)
     */
    ContainerHost getContainerHostByIp( final String hostIp ) throws HostNotFoundException;


    /**
     * Returns instance of management host
     */
    ResourceHost getManagementHost() throws HostNotFoundException;

    /**
     * Returns all local peer's resource hosts
     */
    Set<ResourceHost> getResourceHosts();


    void addRequestListener( RequestListener listener );

    Set<RequestListener> getRequestListeners();

    void removeResourceHost( String rhId ) throws HostNotFoundException;


    /**
     * Returns domain assigned to vni if any
     *
     * @param vni - vni
     *
     * @return - domain or null if no domain assigned to the vni
     */
    String getVniDomain( Long vni ) throws PeerException;

    /**
     * Removes domain from vni if any
     *
     * @param vni -vni
     */
    void removeVniDomain( Long vni ) throws PeerException;

    /**
     * Assigns domain to vni
     *
     * @param vni - vni
     * @param domain -  domain to assign
     * @param proxyLoadBalanceStrategy - strategy to load balance requests to the domain
     * @param sslCertPath - path to SSL certificate to enable HTTPS access to domai only, null if not needed
     */

    void setVniDomain( Long vni, String domain, ProxyLoadBalanceStrategy proxyLoadBalanceStrategy, String sslCertPath )
            throws PeerException;


    /**
     * Returns true if hostIp is added to domain by vni
     *
     * @param hostIp - ip of host to check
     * @param vni - vni
     */
    boolean isIpInVniDomain( String hostIp, Long vni ) throws PeerException;

    void addIpToVniDomain( String hostIp, Long vni ) throws PeerException;

    void removeIpFromVniDomain( String hostIp, Long vni ) throws PeerException;

    Set<ContainerHost> findContainersByEnvironmentId( final String environmentId );

    PeerResources getResources();

    Set<Template> getTemplates();

    SshTunnel setupSshTunnelForContainer( String containerIp, int sshIdleTimeout ) throws PeerException;

    List<ContainerHost> getPeerContainers( String peerId );


    Set<HostUtil.Task> getTasks();

    void cancelAllTasks();

    void registerManagementContainer( ResourceHost resourceHost ) throws PeerException;

    void setPeerInfo( PeerInfo peerInfo );

    ReservedNetworkResources getReservedNetworkResources() throws PeerException;

    /**
     * Returns true if peer in initialized
     */
    boolean isInitialized();

    /**
     * Returns true if MH is connected
     */
    boolean isMHPresent();

    Set<ContainerHost> getOrphanContainers();

    void removeOrphanContainers();

    Template getTemplateByName( String templateName ) throws PeerException;

    Template getTemplateById( String templateId ) throws PeerException;

    boolean destroyNotRegisteredContainer( String containerId ) throws PeerException;

    //deletes tunnels to the given p2p ips from local RHs
    void deleteTunnels( P2pIps p2pIps, EnvironmentId environmentId ) throws PeerException;

    Quota getQuota( ContainerId containerId, ContainerResourceType containerResourceType ) throws PeerException;

    void setRhHostname( String rhId, String hostname ) throws PeerException;

    State getState();

    enum State
    {
        LOADING, FAILED, READY
    }

    void registerResourceHost( ResourceHostInfo resourceHostInfo );

    PeerTemplatesUploadProgress getTemplateUploadProgress( final String templateName ) throws PeerException;

    void exportTemplate( ContainerId containerId, String templateName, String version, boolean isPrivateTemplate,
                         String token ) throws PeerException;

    FitCheckResult checkResources( Nodes nodes ) throws PeerException;
}

