package io.subutai.core.peer.api;


import java.util.List;
import java.util.Set;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.N2NConfig;


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
    public PeerInfo getPeerInfo( String id );

    /**
     * Unregisters peer
     */
    boolean unregister( String id ) throws PeerException;

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

    List<N2NConfig> setupN2NConnection( final Set<Peer> peers ) throws PeerException;
}
