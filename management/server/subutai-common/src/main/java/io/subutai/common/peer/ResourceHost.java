package io.subutai.common.peer;


import java.util.Set;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2pIps;
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
    public Set<ContainerHost> getContainerHosts();

    /**
     * Returns hosted container by its hostname
     */
    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns hosted container by its id
     */
    public ContainerHost getContainerHostById( String id ) throws HostNotFoundException;

    /**
     * Starts hosted container
     */
    public void startContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Stops hosted container
     */
    public void stopContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Destroys hosted container
     */
    public void destroyContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    /**
     * Returns network interfaces from db
     */
    Set<HostInterface> getSavedHostInterfaces();

    /**
     * Returns state of hosted container
     */
    public ContainerHostState getContainerHostState( final ContainerHost container ) throws ResourceHostException;

    public void setupTunnels( P2pIps p2pIps, NetworkResource networkResource ) throws ResourceHostException;

    Set<ContainerHost> getContainerHostsByEnvironmentId( String environmentId );

    Set<ContainerHost> getContainerHostsByOwnerId( String ownerId );

    Set<ContainerHost> getContainerHostsByPeerId( String peerId );

    void addContainerHost( ContainerHost host );

    void cleanup( EnvironmentId environmentId, int vlan ) throws ResourceHostException;

    int getNumberOfCpuCores() throws ResourceHostException;

    P2PConnections getP2PConnections() throws ResourceHostException;

    void joinP2PSwarm( String p2pIp, String interfaceName, String p2pHash, String secretKey, long secretKeyTtlSec )
            throws ResourceHostException;

    void resetSwarmSecretKey( String p2pHash, String newSecretKey, long ttlSeconds ) throws ResourceHostException;

    Tunnels getTunnels() throws ResourceHostException;

    void createTunnel( Tunnel tunnel ) throws ResourceHostException;

    void importTemplate( String templateName ) throws ResourceHostException;

    /**
     * Clones container based on the specified arguments
     *
     * @return ID of container
     */
    String cloneContainer( String templateName, String hostname, String ip, int vlan, String environmentId )
            throws ResourceHostException;

    void setContainerQuota( ContainerHost containerHost, ContainerSize containerSize ) throws ResourceHostException;
}
