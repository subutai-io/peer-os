package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "peer", name = "start-container" )
public class StartContainerCommand extends SubutaiShellCommandSupport
{

    @Argument( index = 0, name = "hostname", multiValued = false, description = "Container name", required = true )
    private String hostname;

    private final LocalPeer localPeer;


    public StartContainerCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ContainerHost host = localPeer.getContainerHostByName( hostname );

        localPeer.startContainer( host.getContainerId() );

        System.out.println( "Container started successfully" );

        return null;
    }
}
