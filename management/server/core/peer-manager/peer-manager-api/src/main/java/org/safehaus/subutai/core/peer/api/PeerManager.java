package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.core.peer.api.message.PeerMessage;
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

    Peer getPeerByUUID( String uuid );

    public void addPeerMessageListener( PeerMessageListener listener );

    public void removePeerMessageListener( PeerMessageListener listener );

    public void sendPeerMessage( Peer peer, PeerMessage peerMessage ) throws PeerMessageException;

    public void processPeerMessage( Peer peer, PeerMessage peerMessage ) throws PeerMessageException;
}
