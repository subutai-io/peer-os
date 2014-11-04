package org.safehaus.subutai.core.peer.api;


import java.util.Set;


/**
 * Local peer interface
 */
public interface LocalPeer extends Peer
{
    public ResourceHost getResourceHostByName( String hostname ) throws PeerException;

    public ContainerHost getContainerHostByName( String hostname ) throws PeerException;

    public ManagementHost getManagementHost() throws PeerException;

    public Set<ResourceHost> getResourceHosts() throws PeerException;

    void init();

    void shutdown();

    public void clean();
}
