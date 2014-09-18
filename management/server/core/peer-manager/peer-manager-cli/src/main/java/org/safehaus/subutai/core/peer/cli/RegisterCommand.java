package org.safehaus.subutai.core.peer.cli;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command(scope = "peer", name = "register")
public class RegisterCommand extends OsgiCommandSupport {

    private PeerManager peerManager;


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager ) {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        Peer peer = getSamplePeer();

        String result = peerManager.register( peer );
        System.out.println( result );
        return null;
    }


    private Peer getSamplePeer() {
        Peer peer = new Peer();
        peer.setName( "Peer name" );
        peer.setIp( "10.10.10.10" );
        peer.setId( UUID.randomUUID() );
        return peer;
    }
}
