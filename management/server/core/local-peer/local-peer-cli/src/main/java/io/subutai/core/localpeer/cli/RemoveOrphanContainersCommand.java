package io.subutai.core.localpeer.cli;


import java.util.HashSet;
import java.util.Set;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "remove-orphan-containers" )
public class RemoveOrphanContainersCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;
    private final PeerManager peerManager;


    public RemoveOrphanContainersCommand( final PeerManager peerManager, final LocalPeer localPeer )
    {
        this.peerManager = peerManager;
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<String> registeredPeers = getRegisteredPeers();
        localPeer.removeOrphanContainers( registeredPeers );

        return null;
    }


    protected Set<String> getRegisteredPeers()
    {
        final Set<String> registeredPeers = new HashSet<>();
        for ( Peer peer : peerManager.getPeers() )
        {
            registeredPeers.add( peer.getId() );
        }
        return registeredPeers;
    }
}
