package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "rename" )
public class RenamePeerCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;

    @Argument( index = 0, name = "peer id", required = true, multiValued = false,
            description = "Peer identifier" )
    private String peerId;

    @Argument( index = 1, name = "new peer name", required = true, multiValued = false,
            description = "New peer name" )
    private String newName;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        peerManager.setName( peerId, newName );

        System.out.println( "Peer renamed" );
        return null;
    }
}
