package org.safehaus.subutai.core.peer.api;


/**
 * Remote peer implementation
 */
public interface RemotePeer extends PeerInterface
{
    public boolean isOnline() throws PeerException;
}
