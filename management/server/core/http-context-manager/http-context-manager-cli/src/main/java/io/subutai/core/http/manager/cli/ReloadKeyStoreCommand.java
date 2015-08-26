package io.subutai.core.http.manager.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.http.manager.api.HttpContextManager;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "ssl-context", name = "ssl-context", description = "gets ssl context" )
public class ReloadKeyStoreCommand extends SubutaiShellCommandSupport
{
    private final HttpContextManager httpContextManager;


    public ReloadKeyStoreCommand( final HttpContextManager httpContextManager )
    {
        this.httpContextManager = httpContextManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        String context = String.valueOf( httpContextManager.getSSLContext() );

        System.out.println( context );

        return null;
    }
}
