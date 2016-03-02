package io.subutai.common.peer;


import java.util.List;
import java.util.Set;

import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.task.Task;


/**
 * Local peer interface
 */
public interface LocalPeer extends Peer
{


    /**
     * Returns public IP of peer determined by internet service
     */
    String getPublicIp() throws PeerException;

    Task getTask( Integer id );

    /**
     * Adds remote apt repository to local apt sources
     */
    @Deprecated
    void addRepository( final String ip ) throws PeerException;

    /**
     * Removes remote apt repository from local apt sources
     */
    @Deprecated
    void removeRepository( final String host, final String ip ) throws PeerException;

    /**
     * Removes a gateway
     */
    void removeGateway( int vlan ) throws PeerException;


    /**
     * Removes a tunnel to remote peer
     */
    void removeTunnel( String tunnelIp );


    /**
     * Returns external IP of mgmt host
     */
    String getExternalIp() throws PeerException;


    /**
     * Binds host with given ID
     *
     * @param id ID of the host
     *
     * @return if host is registered and connected returns implementation of this host, otherwise throws exception.
     */
    public Host bindHost( String id ) throws HostNotFoundException;

    public Host bindHost( ContainerId id ) throws HostNotFoundException;


    /**
     * Returns implementation of ResourceHost interface.
     *
     * @param hostname name of the resource host
     */

    /**
     * Returns resource host instance by its hostname
     */
    public ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns resource host instance by its id
     */
    public ResourceHost getResourceHostById( String hostId ) throws HostNotFoundException;

    /**
     * Returns resource host instance by hostname of its container
     */
    public ResourceHost getResourceHostByContainerName( String containerName ) throws HostNotFoundException;

    /**
     * Returns resource host instance by id ot its container
     */
    public ResourceHost getResourceHostByContainerId( String hostId ) throws HostNotFoundException;


    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostname name of the container
     */

    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostId ID of the container
     */
    public ContainerHost getContainerHostById( String hostId ) throws HostNotFoundException;


    /**
     * Returns instance of management host
     */
    public Host getManagementHost() throws HostNotFoundException;

    /**
     * Returns all local peer's resource hosts
     */
    public Set<ResourceHost> getResourceHosts();


    public void addRequestListener( RequestListener listener );

    public void removeRequestListener( RequestListener listener );

    public Set<RequestListener> getRequestListeners();


    /**
     * Returns domain assigned to vni if any
     *
     * @param vni - vni
     *
     * @return - domain or null if no domain assigned to the vni
     */
    public String getVniDomain( Long vni ) throws PeerException;

    /**
     * Removes domain from vni if any
     *
     * @param vni -vni
     */
    public void removeVniDomain( Long vni ) throws PeerException;

    /**
     * Assigns domain to vni
     *
     * @param vni - vni
     * @param domain -  domain to assign
     * @param domainLoadBalanceStrategy - strategy to load balance requests to the domain
     * @param sslCertPath - path to SSL certificate to enable HTTPS access to domai only, null if not needed
     */

    public void setVniDomain( Long vni, String domain, DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                              String sslCertPath ) throws PeerException;


    /**
     * Returns true if hostIp is added to domain by vni
     *
     * @param hostIp - ip of host to check
     * @param vni - vni
     */
    public boolean isIpInVniDomain( String hostIp, Long vni ) throws PeerException;

    public void addIpToVniDomain( String hostIp, Long vni ) throws PeerException;

    public void removeIpFromVniDomain( String hostIp, Long vni ) throws PeerException;

    void setPeerInfo( PeerInfo peerInfo );

    Set<ContainerHost> findContainersByEnvironmentId( final String environmentId );

    Set<ContainerHost> findContainersByOwnerId( final String ownerId );

    List<P2PConfig> setupP2PConnection( String environmentId, Set<Peer> peers ) throws PeerException;

    void addToTunnel( P2PConfig config ) throws PeerException;

    List<TemplateKurjun> getTemplates();

    TemplateKurjun getTemplateByName( String templateName );

    ContainerHost findContainerById( ContainerId containerId );

    int setupContainerSsh( String containerHostId, int sshIdleTimeout ) throws PeerException;

    List<ContainerHost> getPeerContainers( String peerId );

    String getCurrentControlNetwork() throws PeerException;

    List<Task> getTaskList();
}
