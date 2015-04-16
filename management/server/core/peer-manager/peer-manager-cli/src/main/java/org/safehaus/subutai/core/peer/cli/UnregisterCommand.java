package org.safehaus.subutai.core.peer.cli;


import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "peer", name = "unregister" )
public class UnregisterCommand extends SubutaiShellCommandSupport
{

    @Argument( index = 0, name = "uuid", multiValued = false, description = "Peer UUID" )
    private String uuid;

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

        boolean result = peerManager.unregister( uuid );
        System.out.println( result );
        return null;
    }
}
