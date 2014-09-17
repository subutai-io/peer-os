package org.safehaus.subutai.core.peer.rest;


import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger LOG = Logger.getLogger( RestServiceImpl.class.getName() );
    private PeerManager peerManager;


    public RestServiceImpl() {
    }


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager( PeerManager peerManager ) {
        this.peerManager = peerManager;
    }


    @Override
    public Peer registerPeer( String config ) {
        if ( config != null ) {
            Peer peer = GSON.fromJson( config, Peer.class );
            peerManager.register( peer );
            return peer;
        }
        else {
            return null;
        }
    }


    @Override
    public String getPeerJsonFormat() {
        Peer peer = getSamplePeer();
        return GSON.toJson( peer );
    }


    @Override
    public String getId() {

        UUID id = peerManager.getSiteId();
        return GSON.toJson( id );
    }


    @Override
    public Response processMessage( final String peerId, final String recipient, final String message ) {
        try {
            String response = peerManager.processPeerMessage( peerId, recipient, message );

            return Response.ok( response ).build();
        }
        catch ( PeerMessageException e ) {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    private Peer getSamplePeer() {
        Peer peer = new Peer();
        peer.setName( "Peer name" );
        peer.setIp( "10.10.10.10" );
        peer.setId( UUID.randomUUID() );
        return peer;
    }
}