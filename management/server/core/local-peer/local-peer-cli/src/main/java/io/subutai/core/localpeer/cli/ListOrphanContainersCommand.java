package io.subutai.core.localpeer.cli;


import java.util.HashSet;
import java.util.Set;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "list-orphan-containers" )
public class ListOrphanContainersCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;
    private final PeerManager peerManager;


    public ListOrphanContainersCommand( final PeerManager peerManager, final LocalPeer localPeer )
    {
        this.peerManager = peerManager;
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Set<String> registeredPeers = getRegisteredPeers();
        Set<ContainerHost> hosts = localPeer.listOrphanContainers( registeredPeers );

        for ( ContainerHost containerHost : hosts )
        {
            System.out.println( String.format( "%s\t%s\t%s", containerHost.getId(), containerHost.getContainerName(),
                    containerHost.getPeerId() ) );
        }

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
