package org.safehaus.subutai.peer.impl;


import java.util.List;
import java.util.logging.Logger;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.peer.api.Peer;
import org.safehaus.subutai.peer.api.PeerManager;
import org.safehaus.subutai.peer.impl.dao.PeerDAO;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerImpl implements PeerManager {

    private final Logger LOG = Logger.getLogger( PeerImpl.class.getName() );
    private String source = "PEER_MANAGER";


    private DbManager dbManager;
    private PeerDAO peerDAO;


    public void init() {
        peerDAO = new PeerDAO( dbManager );
    }


    public void destroy() {
        System.out.println( "destroy" );
    }


    public DbManager getDbManager() {
        return dbManager;
    }


    public void setDbManager( final DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    @Override
    public String register( final Peer peer ) {

        LOG.info( "Registering peer: " + peer.getName() );
        try {
            String id = peer.getId();
            peerDAO.saveInfo( source, id, peer );
        }
        catch ( DBException e ) {
            e.printStackTrace();
            return null;
        }
        return peer.getId();
    }


    @Override
    public String getHostId() {
        return "id";
    }


    @Override
    public List<Peer> peers() {
        List<Peer> peers = null;
        try {
            peers = peerDAO.getInfo( source, Peer.class );
        }
        catch ( DBException e ) {
            e.printStackTrace();
        }
        return peers;
    }


    @Override
    public boolean unregister( final String uuid ) {
        try {
            peerDAO.deleteInfo( source, uuid );
        }
        catch ( DBException e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
