package org.safehaus.subutai.core.peer.impl;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.util.HttpUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.Common;
import org.safehaus.subutai.core.peer.api.message.PeerMessage;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.peer.api.message.PeerMessageImpl;
import org.safehaus.subutai.core.peer.api.message.PeerMessageListener;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;

import com.google.gson.JsonSyntaxException;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerImpl implements PeerManager {

    private final static Logger LOG = Logger.getLogger( PeerImpl.class.getName() );
    private final Queue<PeerMessageListener> peerMessageListeners = new ConcurrentLinkedQueue<>();


    private final String SOURCE = "PEER_MANAGER";
    private UUID id;
    private DbManager dbManager;
    private PeerDAO peerDAO;
    private final HttpUtil httpUtil;


    public PeerImpl() {
        this.httpUtil = new HttpUtil();
    }


    public void setId( final String id ) {
        this.id = UUID.fromString( id );
    }


    public void init() {
        LOG.info( "SUBUTAID ID: " + id );
        peerDAO = new PeerDAO( dbManager );
    }


    public void destroy() {
        httpUtil.dispose();
    }


    public void setDbManager( final DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    @Override
    public String register( final Peer peer ) {

        try {
            peerDAO.saveInfo( SOURCE, peer.getId().toString(), peer );
            return peer.getId().toString();
        }
        catch ( DBException e ) {
            LOG.info( e.getMessage() );
        }
        return null;
    }


    @Override
    public UUID getSiteId() {
        return id;
    }


    @Override
    public List<Peer> peers() {
        List<Peer> peers = null;
        try {
            peers = peerDAO.getInfo( SOURCE, Peer.class );
        }
        catch ( DBException e ) {
            LOG.info( e.getMessage() );
        }
        return peers;
    }


    @Override
    public boolean unregister( final String uuid ) {
        try {
            peerDAO.deleteInfo( SOURCE, uuid );
            return true;
        }
        catch ( DBException e ) {
            LOG.info( e.getMessage() );
        }
        return false;
    }


    @Override
    public Peer getPeerByUUID( UUID uuid ) {
        if ( getSiteId().compareTo( uuid ) == 0 ) {
            Peer peer = new Peer();
            peer.setId( uuid );
            peer.setIp( "127.0.0.1" );
            peer.setName( "Me" );
            return peer;
        }

        try {
            return peerDAO.getInfo( SOURCE, uuid.toString(), Peer.class );
        }
        catch ( DBException e ) {
            LOG.info( e.getMessage() );
        }
        return null;
    }


    public Collection<PeerMessageListener> getPeerMessageListeners() {
        return Collections.unmodifiableCollection( peerMessageListeners );
    }


    @Override
    public void addPeerMessageListener( PeerMessageListener listener ) {
        try {
            if ( !peerMessageListeners.contains( listener ) ) {
                peerMessageListeners.add( listener );
            }
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in addPeerMessageListener", ex );
        }
    }


    @Override
    public void removePeerMessageListener( PeerMessageListener listener ) {
        try {
            peerMessageListeners.remove( listener );
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in removePeerMessageListener", ex );
        }
    }


    @Override
    public void sendPeerMessage( final Peer peer, String recipient, final Object message ) throws PeerMessageException {
        PeerMessage peerMessage = new PeerMessageImpl( recipient, message );
        String ip = peer.getIp();

        Map<String, String> params = new HashMap<>();
        params.put( Common.PEER_ID_PARAM_NAME, getSiteId().toString() );
        params.put( Common.MESSAGE_PARAM_NAME, JsonUtil.toJson( peerMessage ) );
        try {
            int responseCode = httpUtil.post( String.format( Common.MESSAGE_REQUEST_URL, ip ), params );
            if ( responseCode != HttpUtil.RESPONSE_OK ) {
                String errMsg = String.format( "REST returned %d response code", responseCode );
                LOG.log( Level.WARNING, errMsg );
                throw new PeerMessageException( errMsg );
            }
        }
        catch ( IOException e ) {
            LOG.log( Level.SEVERE, "Error in sendPeerMessage", e );
            throw new PeerMessageException( e.getMessage() );
        }
    }


    @Override
    public void processPeerMessage( final String peerId, final String peerMessage ) throws PeerMessageException {

        try {
            UUID peerUUID = UUID.fromString( peerId );
            PeerMessage peerMsg = JsonUtil.fromJson( peerMessage, PeerMessage.class );

            for ( PeerMessageListener listener : peerMessageListeners ) {
                if ( listener.getName().equalsIgnoreCase( peerMsg.getRecipientName() ) ) {
                    Peer senderPeer = getPeerByUUID( peerUUID );
                    try {
                        listener.onMessage( senderPeer, peerMsg.getMessage() );
                    }
                    catch ( Exception e ) {
                        LOG.log( Level.SEVERE, "Error in processPeerMessage", e );
                        throw e;
                    }
                    //                break;
                }
            }
        }
        catch ( IllegalArgumentException | JsonSyntaxException e ) {
            LOG.log( Level.SEVERE, "Error in processPeerMessage", e );
            throw new PeerMessageException( e.getMessage() );
        }
    }


    @Override
    public void createContainers( final UUID envId, final String template, final int numberOfNodes,
                                  final String Strategy, final List<String> criteria ) {

    }
}
