package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;


public interface PeerManager
{
    public static final String SOURCE_REMOTE_PEER = "PEER_REMOTE";
    public static final String SOURCE_LOCAL_PEER = "PEER_LOCAL";

    /**
     * Registers remote peer
     */
    boolean register( PeerInfo peerInfo ) throws PeerException;

    /**
     * Updates peer metadata
     */
    boolean update( PeerInfo peerInfo );

    /**
     * Returns all registered peers metadata objects
     */
    public List<PeerInfo> getPeerInfos();

    /**
     * Returns local peer's metadata
     */
    public PeerInfo getLocalPeerInfo();

    /**
     * Returns peer metadata by peer id
     */
    public PeerInfo getPeerInfo( UUID uuid );

    /**
     * Unregisters peer
     */
    boolean unregister( String uuid ) throws PeerException;

    /**
     * Returns peer instance by peer id
     */
    public Peer getPeer( UUID peerId );

    /**
     * Returns peer instance by peer id
     */
    public Peer getPeer( String peerId );

    /**
     * Returns all peer instances
     */
    public List<Peer> getPeers();

    /**
     * Returns local peer instance
     */
    public LocalPeer getLocalPeer();
}
