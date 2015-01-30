package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;


/**
 * Remote peer implementation
 */
public interface RemotePeer extends Peer
{
    public UUID getRemoteId() throws PeerException;
}
