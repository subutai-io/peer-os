package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
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

    public String sendPeerMessage( Peer peer, String recipient, String message ) throws PeerMessageException;

    public String processPeerMessage( String peerId, String recipient, String message ) throws PeerMessageException;

    public boolean isPeerReachable( Peer peer ) throws PeerException;

    public Set<Agent> getConnectedAgents( String environmentId ) throws PeerException;
}
