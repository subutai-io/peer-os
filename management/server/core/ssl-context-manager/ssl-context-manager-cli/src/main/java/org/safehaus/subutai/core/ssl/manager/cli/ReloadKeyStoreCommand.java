package org.safehaus.subutai.core.ssl.manager.cli;


import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.ssl.manager.api.SubutaiSslContextFactory;

import org.apache.karaf.shell.commands.Command;


@Command( scope = "ssl-context", name = "ssl-context", description = "gets ssl context" )
public class ReloadKeyStoreCommand extends SubutaiShellCommandSupport
{
    private final SubutaiSslContextFactory sslContextFactory;


    public ReloadKeyStoreCommand( final SubutaiSslContextFactory sslContextFactory )
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
