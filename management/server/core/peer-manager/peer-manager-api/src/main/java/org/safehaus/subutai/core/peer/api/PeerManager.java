package org.safehaus.subutai.core.peer.api;


import java.util.List;


/**
 * Created by bahadyr on 8/28/14.
 */
public interface PeerManager {

    String register( Peer peer );

    String getHostId();

    List<Peer> peers();

    boolean unregister( String uuid );

    Peer getPeerByUUID( String uuid );

}
