package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.peer.api.Peer;
import org.safehaus.subutai.peer.api.PeerManager;

import java.util.UUID;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command(scope = "peer", name = "unregister")
public class UnregisterCommand extends OsgiCommandSupport {

    @Argument(index = 0, name = "uuid", multiValued = false, description = "Peer UUID")
    private String uuid;

    private PeerManager peerManager;


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager(final PeerManager peerManager) {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception {

        boolean result = peerManager.unregister(uuid);
        System.out.println(result);
        return null;
    }


    private Peer getSamplePeer() {
        Peer peer = new Peer();
        peer.setName("Peer name");
        peer.setIp("10.10.10.10");
        peer.setId(UUID.randomUUID().toString());
        return peer;
    }
}
