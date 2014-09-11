package org.safehaus.subutai.peer.impl;


import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.peer.api.Peer;
import org.safehaus.subutai.peer.api.PeerManager;
import org.safehaus.subutai.peer.impl.dao.PeerDAO;

import java.util.List;
import java.util.logging.Logger;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerImpl implements PeerManager {

    private final static String source = "PEER_MANAGER";
    private final Logger LOG = Logger.getLogger(PeerImpl.class.getName());
    private String id;
    private DbManager dbManager;
    private PeerDAO peerDAO;


    public void setId(final String id) {
        this.id = id;
    }


    public void init() {
        LOG.info("SUBUTAID ID: " + id);
        peerDAO = new PeerDAO(dbManager);
    }


    public void destroy() {
        this.dbManager = null;
    }


    public void setDbManager(final DbManager dbManager) {
        this.dbManager = dbManager;
    }


    @Override
    public String register(final Peer peer) {

        //        LOG.info( "Registering peer: " + peer.getName() );
        try {
            String id = peer.getId();
            peerDAO.saveInfo(source, id, peer);
        } catch (DBException e) {
            //            e.printStackTrace();
            LOG.info(e.getMessage());
            return null;
        }
        return peer.getId();
    }


    @Override
    public String getHostId() {
        return id;
    }


    @Override
    public List<Peer> peers() {
        List<Peer> peers = null;
        try {
            peers = peerDAO.getInfo(source, Peer.class);
        } catch (DBException e) {
            LOG.info(e.getMessage());
            //            e.printStackTrace();
        }
        return peers;
    }


    @Override
    public boolean unregister(final String uuid) {
        try {
            peerDAO.deleteInfo(source, uuid);
        } catch (DBException e) {
            LOG.info(e.getMessage());
            //            e.printStackTrace();
            return false;
        }
        return true;
    }
}
