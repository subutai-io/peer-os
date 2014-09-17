package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.UUID;


/**
 * Created by bahadyr on 8/28/14.
 */
public interface PeerManager {

    String register( Peer peer );

    UUID getSiteId();

    List<Peer> peers();

    boolean unregister( String uuid );

    Peer getPeerByUUID( String uuid );
}
