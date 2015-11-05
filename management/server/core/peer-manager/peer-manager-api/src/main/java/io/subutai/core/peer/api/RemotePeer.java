package io.subutai.core.peer.api;


import io.subutai.common.metric.HostMetric;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;


/**
 * Remote peer interface
 */
public interface RemotePeer extends Peer
{
    PeerInfo check() throws PeerException;

}
