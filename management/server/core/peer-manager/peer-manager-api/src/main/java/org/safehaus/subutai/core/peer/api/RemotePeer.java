package org.safehaus.subutai.core.peer.api;


import java.util.UUID;


/**
 * Remote peer implementation
 */
public interface RemotePeer extends Peer
{
    public UUID getRemoteId() throws PeerException;
}
