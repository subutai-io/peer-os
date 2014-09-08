package org.safehaus.subuta.peer.rest;


import java.util.UUID;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.peer.api.Peer;
import org.safehaus.subutai.peer.api.PeerManager;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {



    private static final String MSG_RESPONSE = "MSG_RESPONSE";
    private PeerManager peerManager;


    public RestServiceImpl() {
    }


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager ) {
        this.peerManager = peerManager;
    }


    @Override
    public String registerPeer( final String config ) {
        Peer peer = JsonUtil.fromJson( config, Peer.class );

        return peerManager.registerPeer( peer );
    }


    @Override
    public String getPeerJsonFormat() {
        Peer peer = new Peer();
        peer.setName( "Peer name" );
        peer.setIp( "10.10.10.10" );
        peer.setId( UUID.randomUUID().toString() );
        return JsonUtil.toJson( "PEER_FORMAT", peer );
    }
}