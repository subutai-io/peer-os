package io.subutai.core.localpeer.cli;


import java.util.HashSet;
import java.util.Set;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "localpeer", name = "remove-orphan-containers" )
public class RemoveOrphanContainersCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;


    public RemoveOrphanContainersCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        localPeer.removeOrphanContainers();

        return null;
    }
}
