package io.subutai.core.peer.api;


import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;


/**
 * Remote peer implementation
 */
public interface RemotePeer extends Peer
{
    public String getRemoteId() throws PeerException;
}
