package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "encrypt-file" )
public class EncryptFileCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "resource host", description = "Resource host id", required = true )
    private String rhId;
    @Argument( index = 1, name = "file path", description = "Path to file", required = true )
    private String filePath;
    @Argument( index = 2, name = "password", description = "Password to use for encryption", required = true )
    private String password;

    private final LocalPeer localPeer;


    public EncryptFileCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ResourceHost resourceHost = localPeer.getResourceHostById( rhId );

        String output = resourceHost.encryptFile( filePath, password );

        System.out.println( output );

        return null;
    }
}
