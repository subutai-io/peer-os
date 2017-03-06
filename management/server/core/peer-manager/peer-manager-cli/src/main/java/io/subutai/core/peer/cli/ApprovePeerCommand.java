package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "approve" )
public class ApprovePeerCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;

    @Argument( name = "peer id", required = true, description = "peer identifier" )
    private String peerId;
    @Argument( index = 1, name = "keyphrase", required = true, description = "key phrase" )
    private String keyPhrase;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        peerManager.doApproveRequest( keyPhrase, peerId );

        return null;
    }
}
