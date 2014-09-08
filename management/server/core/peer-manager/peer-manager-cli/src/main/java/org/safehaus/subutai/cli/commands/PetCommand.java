package org.safehaus.subutai.cli.commands;


import org.safehaus.subutai.peer.api.PeerManager;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command( scope = "peer", name = "get-host-id" )
public class PetCommand extends OsgiCommandSupport {

    private PeerManager peerManager;


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager ) {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        System.out.println( peerManager.getHostId() );
        return null;
    }
}
