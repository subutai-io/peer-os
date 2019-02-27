package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "upload-file" )
public class UploadRawFileToCdnCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "resource host", description = "Resource host id", required = true )
    private String rhId;
    @Argument( index = 1, name = "filepath", description = "Path to file", required = true )
    private String filePath;
    @Argument( index = 2, name = "token", description = "CDN token", required = true )
    private String token;

    private final LocalPeer localPeer;


    public UploadRawFileToCdnCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ResourceHost resourceHost = localPeer.getResourceHostById( rhId );

        String output = resourceHost.uploadRawFileToCdn( filePath, token );

        System.out.println( output );

        return null;
    }
}
