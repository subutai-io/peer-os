package org.safehaus.subutai.core.peer.api;


/**
 * Remote peer implementation
 */
public interface RemotePeer extends Peer
{
    public boolean isOnline() throws PeerException;
}
