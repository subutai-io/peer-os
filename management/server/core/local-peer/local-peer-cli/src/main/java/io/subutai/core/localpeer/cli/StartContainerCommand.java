package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "start-container" )
public class StartContainerCommand extends SubutaiShellCommandSupport
{

    @Argument( index = 0, name = "container name", multiValued = false, description = "Container name", required = true )
    private String containerName;

    private final LocalPeer localPeer;


    public StartContainerCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ContainerHost host = localPeer.getContainerHostByContainerName( containerName );

        localPeer.startContainer( host.getContainerId() );

        System.out.println( "Container started successfully" );

        return null;
    }
}
