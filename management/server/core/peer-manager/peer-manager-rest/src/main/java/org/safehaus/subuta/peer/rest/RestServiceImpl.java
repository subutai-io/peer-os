package org.safehaus.subuta.peer.rest;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.peer.api.Peer;
import org.safehaus.subutai.peer.api.PeerManager;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private PeerManager peerManager;
    private static final String MSG_RESPONSE = "MSG_RESPONSE";


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager ) {
        this.peerManager = peerManager;
    }


    public RestServiceImpl() {
    }


    @Override
    public String registerPeer( final String config ) {
        Peer peer = JsonUtil.fromJson( config, Peer.class );
        return peerManager.registerPeer(peer);
    }
}