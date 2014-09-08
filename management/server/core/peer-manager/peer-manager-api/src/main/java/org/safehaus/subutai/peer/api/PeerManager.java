package org.safehaus.subutai.peer.api;


import java.util.List;


/**
 * Created by bahadyr on 8/28/14.
 */
public interface PeerManager {

    String registerPeer( Peer peer );

    String getHostId();

    List<Peer> peers();
}
