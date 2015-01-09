package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;


public interface PeerManager
{

    boolean register( PeerInfo peerInfo ) throws PeerException;

    boolean update( PeerInfo peerInfo );


    @Deprecated
    public List<PeerInfo> peers();

    public PeerInfo getLocalPeerInfo();

    public PeerInfo getPeerInfo( UUID uuid );

    boolean unregister( String uuid ) throws PeerException;


    List<PeerGroup> peersGroups();

    void deletePeerGroup( PeerGroup group );

    boolean savePeerGroup( PeerGroup group );

    public Peer getPeer( UUID peerId );

    public Peer getPeer( String peerId );

    public List<Peer> getPeers();


    public LocalPeer getLocalPeer();

    PeerGroup getPeerGroup( UUID peerGroupId );

    public void addRequestListener( RequestListener listener );

    public void removeRequestListener( RequestListener listener );

    EntityManagerFactory getEntityManagerFactory();
}
