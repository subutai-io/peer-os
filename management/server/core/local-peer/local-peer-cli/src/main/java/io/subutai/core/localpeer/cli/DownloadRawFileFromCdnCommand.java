package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "download-file" )
public class DownloadRawFileFromCdnCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "resource host", description = "Resource host id", required = true )
    private String rhId;
    @Argument( index = 1, name = "file id", description = "Id of file", required = true )
    private String fileId;

    private final LocalPeer localPeer;


    public DownloadRawFileFromCdnCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ResourceHost resourceHost = localPeer.getResourceHostById( rhId );

        String output = resourceHost.downloadRawFileFromCdn( fileId, null);

        System.out.println( output );

        return null;
    }
}
