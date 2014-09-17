package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.peer.api.message.PeerMessageListener;


/**
 * Created by bahadyr on 8/28/14.
 */
public interface PeerManager {

    String register( Peer peer );

    UUID getSiteId();

    List<Peer> peers();

    boolean unregister( String uuid );

    Peer getPeerByUUID( UUID uuid );

    public void addPeerMessageListener( PeerMessageListener listener );

    public void removePeerMessageListener( PeerMessageListener listener );

    public void sendPeerMessage( Peer peer, String recipient, Object message ) throws PeerMessageException;

    public void processPeerMessage( String peerId, String peerMessage ) throws PeerMessageException;

    void createContainers( UUID envId, String template, int numberOfNodes, String Strategy, List<String> criteria );
}
