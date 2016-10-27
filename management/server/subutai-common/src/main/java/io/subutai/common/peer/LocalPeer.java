package io.subutai.common.peer;


import java.util.List;
import java.util.Set;

import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.protocol.Template;
import io.subutai.common.util.HostUtil;
import io.subutai.hub.share.resource.PeerResources;


/**
 * Local peer interface
 */
public interface LocalPeer extends Peer
{


    /**
     * Binds host with given ID
     *
     * @param id ID of the host
     *
     * @return if host is registered and connected returns implementation of this host, otherwise throws exception.
     */
    Host bindHost( String id ) throws HostNotFoundException;

    Host bindHost( ContainerId id ) throws HostNotFoundException;


    /**
     * Returns implementation of ResourceHost interface.
     *
     * @param hostname name of the resource host
     */

    /**
     * Returns resource host instance by its hostname
     */
    ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns resource host instance by its id
     */
    ResourceHost getResourceHostById( String hostId ) throws HostNotFoundException;

    /**
     * Returns resource host instance by hostname of its container
     */
    ResourceHost getResourceHostByContainerName( String containerName ) throws HostNotFoundException;

    /**
     * Returns resource host instance by id ot its container
     */
    ResourceHost getResourceHostByContainerId( String hostId ) throws HostNotFoundException;


    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostname name of the container
     */

    ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostId ID of the container
     */
    ContainerHost getContainerHostById( String hostId ) throws HostNotFoundException;


    /**
     * Returns instance of management host
     */
    ResourceHost getManagementHost() throws HostNotFoundException;

    /**
     * Returns all local peer's resource hosts
     */
    Set<ResourceHost> getResourceHosts();


    void addRequestListener( RequestListener listener );

    void removeRequestListener( RequestListener listener );

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

    Set<ContainerHost> findContainersByOwnerId( final String ownerId );

    PeerResources getResources();

    Set<Template> getTemplates();

    SshTunnel setupSshTunnelForContainer( String containerIp, int sshIdleTimeout ) throws PeerException;

    List<ContainerHost> getPeerContainers( String peerId );

    Host findHostByName( String hostname ) throws HostNotFoundException;

    Set<HostUtil.Task> getTasks();

    void cancelAllTasks();

    void exchangeKeys( ResourceHost resourceHost, String hostname ) throws PeerException;

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

    Set<ContainerHost> listOrphanContainers();

    void removeOrphanContainers();

    Template getTemplateByName( String templateName ) throws PeerException;

    Template getTemplateById( String templateId ) throws PeerException;
}

