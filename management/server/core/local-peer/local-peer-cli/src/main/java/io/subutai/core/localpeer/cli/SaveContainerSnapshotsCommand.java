package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "save-snapshots", description = "Saves container snapshots to file" )
public class SaveContainerSnapshotsCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "resource host", description = "Resource host id", required = true )
    private String rhId;
    @Argument( index = 1, name = "container name", description = "Name of container", required = true )
    private String containerName;
    @Argument( index = 2, name = "labels", description = "Comma separated names of snapshots", required = true )
    private String labels;

    private final LocalPeer localPeer;


    public SaveContainerSnapshotsCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ResourceHost resourceHost = localPeer.getResourceHostById( rhId );
        ContainerHost containerHost = resourceHost.getContainerHostByContainerName( containerName );

        String[] labelArr = labels.split( "," );
        if ( labelArr.length == 2 )
        {
            resourceHost.saveContainerFilesystem( containerHost, labelArr[0], labelArr[1], null );
        }
        else
        {
            resourceHost.saveContainerFilesystem( containerHost, labelArr[0], null, null );
        }

        System.out.println( "Done" );

        return null;
    }
}
