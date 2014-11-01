package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.UUID;


public interface PeerManager
{

    @Deprecated
    boolean register( PeerInfo peerInfo );

    @Deprecated
    boolean update( PeerInfo peerInfo );


    @Deprecated
    public List<PeerInfo> peers();

    public PeerInfo getLocalPeerInfo();

    public PeerInfo getPeerInfo( UUID uuid );

    @Deprecated
    boolean unregister( String uuid );


    List<PeerGroup> peersGroups();

    void deletePeerGroup( PeerGroup group );

    boolean savePeerGroup( PeerGroup group );

    public Peer getPeer( UUID peerId );

    public List<Peer> getPeers();


    public LocalPeer getLocalPeer();

    PeerGroup getPeerGroup( UUID peerGroupId );
}
