package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "reject" )
public class RejectRegistrationCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;

    @Argument( index = 0, name = "peer id", required = true, multiValued = false,
            description = "peer identifier" )
    private String peerId;


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
        Peer peer = peerManager.getPeer( peerId );
        RegistrationData request = new RegistrationData( peer.getPeerInfo(), RegistrationStatus.APPROVED );
        peerManager.doRejectRequest( request );
        return null;
    }
}
