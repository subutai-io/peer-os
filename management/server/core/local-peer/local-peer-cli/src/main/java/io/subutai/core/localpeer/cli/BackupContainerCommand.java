package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "backup-container" )
public class BackupContainerCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "container name", description = "Container name", required = true )
    private String containerName;

    private final LocalPeer localPeer;


    public BackupContainerCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ContainerHost containerHost = localPeer.getContainerHostByContainerName( containerName );

        ResourceHost resourceHost = localPeer.getResourceHostById( containerHost.getResourceHostId().getId() );

        String output = resourceHost.backupContainer( containerHost, null );

        System.out.println( output );

        return null;
    }
}
