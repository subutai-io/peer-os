package io.subutai.core.peer.api;


/**
 * Peer action listener interface
 */
public interface PeerActionListener
{
    public String getName();

    public PeerActionResponse onPeerAction( PeerAction peerAction );
}
