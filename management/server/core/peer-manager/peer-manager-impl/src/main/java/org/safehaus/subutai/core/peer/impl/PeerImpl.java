package org.safehaus.subutai.core.peer.impl;


import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerImpl implements PeerManager {

    private final Logger LOG = Logger.getLogger( PeerImpl.class.getName() );

    private final String SOURCE = "PEER_MANAGER";
    private UUID id;
    private DbManager dbManager;
    private PeerDAO peerDAO;


    public void setId( final String id ) {
        this.id = UUID.fromString( id );
    }


    public void init() {
        LOG.info( "SUBUTAID ID: " + id );
        peerDAO = new PeerDAO( dbManager );
    }


    public void destroy() {
        this.dbManager = null;
    }


    public void setDbManager( final DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    @Override
    public String register( final Peer peer ) {

        try {
            String id = peer.getId();
            peerDAO.saveInfo( SOURCE, id, peer );
            return peer.getId();
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
    public Peer getPeerByUUID( String uuid ) {
        try {
            return peerDAO.getInfo( SOURCE, uuid, Peer.class );
        }
        catch ( DBException e ) {
            LOG.info( e.getMessage() );
        }
        return null;
    }
}
