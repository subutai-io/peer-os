package io.subutai.core.peer.cli;


import java.util.Set;

import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.Peer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "peer", name = "get-reserved-vni" )
public class GetReservedVnisCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "peerId", multiValued = false, required = true, description = "Peer ID" )
    private String peerId;

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Peer peer = peerManager.getPeer( peerId );
        Set<Vni> reservedVnis = peer.getReservedVnis();
        System.out.println( reservedVnis );
        return null;
    }
}
