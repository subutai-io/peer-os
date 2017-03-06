package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "reject" )
public class RejectPeerCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;

    @Argument( index = 0, name = "peer id", required = true, multiValued = false, description = "peer identifier" )
    private String peerId;
    @Argument( index = 1, name = "with force", description = "perform with force" )
    private boolean force;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        peerManager.doRejectRequest( peerId, force );

        return null;
    }
}
