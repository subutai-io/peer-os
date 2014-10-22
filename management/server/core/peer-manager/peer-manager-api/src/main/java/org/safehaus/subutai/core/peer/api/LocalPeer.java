package org.safehaus.subutai.core.peer.api;


import java.util.Set;


/**
 * Local peer interface
 */
public interface LocalPeer extends PeerInterface
{
    public ManagementHost getManagementHost() throws PeerException;

    public Set<ResourceHost> getResourceHosts() throws PeerException;
}
