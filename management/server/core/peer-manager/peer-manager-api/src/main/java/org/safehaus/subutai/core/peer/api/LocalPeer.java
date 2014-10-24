package org.safehaus.subutai.core.peer.api;


import java.util.Set;


/**
 * Local peer interface
 */
public interface LocalPeer extends PeerInterface
{
    public ResourceHost getResourceHostByName( String hostname ) throws PeerException;

    public ContainerHost getContainerHostByName( String hostname ) throws PeerException;

    public ManagementHost getManagementHost() throws PeerException;

    public Set<ResourceHost> getResourceHosts() throws PeerException;
}
