package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.network.Vni;
import io.subutai.common.peer.Peer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


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
        for ( Vni vni : peer.getReservedVnis().list() )
        {
            System.out.println( String.format( "%d %d %s", vni.getVni(), vni.getVlan(), vni.getEnvironmentId() ) );
        }
        return null;
    }
}
