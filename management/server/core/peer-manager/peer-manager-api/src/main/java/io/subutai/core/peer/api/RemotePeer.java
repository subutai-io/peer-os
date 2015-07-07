package io.subutai.core.peer.api;


import java.util.UUID;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;


/**
 * Remote peer implementation
 */
public interface RemotePeer extends Peer
{
    public UUID getRemoteId() throws PeerException;
}
