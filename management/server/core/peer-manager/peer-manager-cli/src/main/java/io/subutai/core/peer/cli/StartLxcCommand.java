package io.subutai.core.peer.cli;


import io.subutai.common.peer.ContainerHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "peer", name = "start-container" )
public class StartLxcCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Argument( index = 0, name = "hostname", multiValued = false, description = "LXC name" )
    private String hostname;


    @Override
    protected Object doExecute() throws Exception
    {

        LocalPeer localPeer = peerManager.getLocalPeer();

        ContainerHost host = localPeer.getContainerHostByName( hostname );

        localPeer.startContainer( host.getContainerId() );
        System.out.println( "Container started successfully" );


        return null;
    }
}
