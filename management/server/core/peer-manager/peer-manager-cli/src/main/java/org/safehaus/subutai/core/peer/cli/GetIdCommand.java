package org.safehaus.subutai.core.peer.cli;


import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.core.peer.api.PeerManager;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command(scope = "peer", name = "id")
public class GetIdCommand extends OsgiCommandSupport {

    private PeerManager peerManager;


    public PeerManager getPeerManager() {
        return peerManager;
    }


    public void setPeerManager(final PeerManager peerManager) {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        String id = peerManager.getHostId();
        System.out.println("SUBUTAI ID: " + id);
        return null;
    }
}
