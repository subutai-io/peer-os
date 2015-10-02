package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "id" )
public class GetIdCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        String id = peerManager.getLocalPeer().getId();
        System.out.println( "SUBUTAI ID: " + id );
        return null;
    }
}
