package org.safehaus.subutai.core.ssl.manager.cli;


import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.ssl.manager.api.CustomSslContextFactory;

import org.apache.karaf.shell.commands.Command;


@Command( scope = "ssl-context", name = "ssl-context", description = "gets ssl context" )
public class ReloadKeyStoreCommand extends SubutaiShellCommandSupport
{
    private final CustomSslContextFactory sslContextFactory;


    public ReloadKeyStoreCommand( final CustomSslContextFactory sslContextFactory )
    {
        this.sslContextFactory = sslContextFactory;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        String context = String.valueOf( sslContextFactory.getSSLContext() );

        System.out.println( context );

        return null;
    }
}
