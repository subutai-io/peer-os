package io.subutai.core.peer.api;


/**
 * Peer action listener interface
 */
public interface PeerActionListener
{
    public PeerActionResponse onPeerAction( PeerAction peerAction );
}
