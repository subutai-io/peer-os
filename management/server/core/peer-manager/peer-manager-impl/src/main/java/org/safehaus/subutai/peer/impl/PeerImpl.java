package org.safehaus.subutai.peer.impl;


import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.peer.api.Peer;
import org.safehaus.subutai.peer.api.PeerManager;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerImpl implements PeerManager {

    private DbManager dbManager;


    public void init() {
        System.out.println( "init" );
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
    public String registerPeer( final Peer peer ) {
        return "registered";
    }


    @Override
    public String getHostId() {
        return "id";
    }
}
