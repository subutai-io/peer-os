package io.subutai.common.peer;


/**
 * Remote peer interface
 */
public interface RemotePeer extends Peer
{
    PeerInfo check() throws PeerException;

    void excludePeerFromEnvironment( String environmentId, String peerId ) throws PeerException;

    void excludeContainerFromEnvironment( String environmentId, String containerId ) throws PeerException;

    void updateContainerHostname( String environmentId, String containerId, String hostname ) throws PeerException;
}
