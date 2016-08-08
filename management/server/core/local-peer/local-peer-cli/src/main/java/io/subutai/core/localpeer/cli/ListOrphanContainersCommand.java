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


@Command( scope = "localpeer", name = "list-orphan-containers" )
public class ListOrphanContainersCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;


    public ListOrphanContainersCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<ContainerHost> hosts = localPeer.listOrphanContainers();

        for ( ContainerHost containerHost : hosts )
        {
            System.out.println( String.format( "%s\t%s\t%s", containerHost.getId(), containerHost.getContainerName(),
                    containerHost.getPeerId() ) );
        }

        return null;
    }
}
