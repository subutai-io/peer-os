package io.subutai.core.localpeer.cli;


import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.ReverseProxyConfig;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "add-reverse-proxy" )
public class AddReverseProxyCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;

    @Argument( index = 0, name = "container-id", multiValued = false, required = true, description = "Container ID" )
    private String containerId;

    @Argument( index = 1, name = "domain-name", multiValued = false, required = true, description = "Domain name" )
    private String domainName;

    @Argument( index = 2, name = "cert-path", multiValued = false, required = false, description = "Path to SSL pem "
            + "file" )
    private String certPath;


    public AddReverseProxyCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        String sslCert = "";
        if ( !StringUtils.isEmpty( certPath ) )
        {
            sslCert = readFile( certPath, Charset.defaultCharset() );
        }
        final ReverseProxyConfig config = new ReverseProxyConfig( containerId, domainName, sslCert );
        localPeer.addReverseProxy( config );
        return null;
    }


    private String readFile( String path, Charset encoding ) throws IOException
    {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, encoding );
    }
}
