package io.subutai.core.environment.cli;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.environment.Environment;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.protocol.ReverseProxyConfig;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "environment", name = "add-reverse-proxy" )
public class AddReverseProxyCommand extends SubutaiShellCommandSupport
{

    private EnvironmentManager environmentManager;

    @Argument( index = 0, name = "environment-id", multiValued = false, required = true, description = "Environment "
            + "ID" )
    private String environmentId;

    @Argument( index = 1, name = "container-id", multiValued = false, required = true, description = "Container ID" )
    private String containerId;

    @Argument( index = 2, name = "domain-name", multiValued = false, required = true, description = "Domain name" )
    private String domainName;

    @Argument( index = 3, name = "cert-path", multiValued = false, required = false, description = "Path to SSL pem "
            + "file" )
    private String certPath;


    public AddReverseProxyCommand( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        final Environment environment = environmentManager.loadEnvironment( environmentId );

        String sslCert = "";
        if ( !StringUtils.isEmpty( certPath ) )
        {
            sslCert = readFile( certPath, Charset.defaultCharset() );
        }
        final ReverseProxyConfig config = new ReverseProxyConfig( environmentId, containerId, domainName, sslCert,
                ProxyLoadBalanceStrategy.NONE );
        environmentManager.addReverseProxy( environment, config );
        return null;
    }


    private String readFile( String path, Charset encoding ) throws IOException
    {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, encoding );
    }
}
