package org.safehaus.subutai.core.peer.cli;


import java.util.Set;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "peer", name = "get-taken-vni" )
public class GetTakenVnisCommand extends OsgiCommandSupport
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
        Set<Long> takenVnis = peer.getTakenVniIds();
        System.out.println( takenVnis );
        return null;
    }
}
