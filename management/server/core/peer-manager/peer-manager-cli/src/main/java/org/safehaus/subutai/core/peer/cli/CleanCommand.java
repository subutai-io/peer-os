package org.safehaus.subutai.core.peer.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Command;


@Command( scope = "peer", name = "clean" )
public class CleanCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        localPeer.cleanDb();
        return null;
    }
}
