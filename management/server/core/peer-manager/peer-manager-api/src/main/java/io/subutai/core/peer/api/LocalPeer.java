package io.subutai.core.peer.api;


import java.util.Set;
import java.util.UUID;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.Template;


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
    public Host bindHost( String id ) throws HostNotFoundException;

    /**
     * Binds host with given ID
     *
     * @param id ID of the host
     *
     * @return if host is registered and connected returns implementation of this host, otherwise throws exception.
     */
    public Host bindHost( UUID id ) throws HostNotFoundException;

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
    public ResourceHost getResourceHostById( UUID hostId ) throws HostNotFoundException;

    /**
     * Returns resource host instance by hostname of its container
     */
    public ResourceHost getResourceHostByContainerName( String containerName ) throws HostNotFoundException;

    /**
     * Returns resource host instance by id ot its container
     */
    public ResourceHost getResourceHostByContainerId( UUID hostId ) throws HostNotFoundException;


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
    public ContainerHost getContainerHostById( UUID hostId ) throws HostNotFoundException;

    /**
     * Returns instance of management host
     */
    public ManagementHost getManagementHost() throws HostNotFoundException;

    /**
     * Returns all local peer's resource hosts
     */
    public Set<ResourceHost> getResourceHosts();

    public void cleanDb();

    /**
     * Creates container on the local peer
     *
     * @param resourceHost - target resource host where to host container
     * @param template - source template from which to clone container
     * @param containerName - container name
     */
    public ContainerHost createContainer( final ResourceHost resourceHost, final Template template,
                                          final String containerName ) throws PeerException;


    /**
     * Returns container group by container id
     *
     * @param containerId - id of container
     *
     * @return - {@code ContainerGroup}
     *
     * @throws ContainerGroupNotFoundException - thrown if container is created not as a part of environment
     */
    public ContainerGroup findContainerGroupByContainerId( UUID containerId ) throws ContainerGroupNotFoundException;

    /**
     * Returns container group by environment id
     *
     * @param environmentId - id of environment
     *
     * @return - {@code ContainerGroup}
     *
     * @throws ContainerGroupNotFoundException - thrown if group is not found
     */
    public ContainerGroup findContainerGroupByEnvironmentId( UUID environmentId )
            throws ContainerGroupNotFoundException;

    /**
     * Returns set of container groups by owner id
     *
     * @param ownerId - id of owner
     *
     * @return - set of {@code ContainerGroup}
     */
    public Set<ContainerGroup> findContainerGroupsByOwnerId( UUID ownerId );

    //networking

    /**
     * Sets up tunnels on the local peer to the specified remote peers
     */
    public int setupTunnels( Set<String> peerIps, UUID environmentId ) throws PeerException;


    public void addRequestListener( RequestListener listener );

    public void removeRequestListener( RequestListener listener );

    public Set<RequestListener> getRequestListeners();
}
