package org.safehaus.subutai.core.peer.cli;


import java.util.UUID;

import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Command;



@Command( scope = "peer", name = "id" )
public class GetIdCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        UUID id = peerManager.getLocalPeer().getId();
        System.out.println( "SUBUTAI ID: " + id );
        return null;
    }
}
