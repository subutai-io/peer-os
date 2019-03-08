package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "recreate-snapshots", description = "Recreates container snapshots from file" )
public class RecreateContainerSnapshotsCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "resource host", description = "Resource host id", required = true )
    private String rhId;
    @Argument( index = 1, name = "container name", description = "Name of new container", required = true )
    private String containerName;
    @Argument( index = 2, name = "file", description = "Path to file with snapshots", required = true )
    private String file;

    private final LocalPeer localPeer;


    public RecreateContainerSnapshotsCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ResourceHost resourceHost = localPeer.getResourceHostById( rhId );

        resourceHost.recreateContainerFilesystem( containerName, file );

        System.out.println( "Done" );

        return null;
    }
}
