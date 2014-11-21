package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;


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
    public Host bindHost( String id ) throws HostNotFoundException, HostNotConnectedException;

    /**
     * Binds host with given ID
     *
     * @param id ID of the host
     *
     * @return if host is registered and connected returns implementation of this host, otherwise throws exception.
     */
    public Host bindHost( UUID id ) throws HostNotFoundException, HostNotConnectedException;

    /**
     * Returns implementation of ResourceHost interface.
     *
     * @param hostname name of the resource host
     */

    public ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException;

    public ResourceHost getResourceHostByContainerName( String containerName ) throws HostNotFoundException;

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

    <T extends Host> T bindHost( T host ) throws HostNotFoundException;

    public ManagementHost getManagementHost() throws HostNotFoundException;

    public Set<ResourceHost> getResourceHosts();

    /**
     * Returns the templates list
     */
    public List<String> getTemplates();

    void init();

    void shutdown();

    public void clean();

    public ContainerHost createContainer( String hostName, String templateName, String cloneName, UUID envId )
            throws PeerException;

    //    Agent waitForAgent( String containerName, int timeout );

    public void onPeerEvent( PeerEvent event );
}
