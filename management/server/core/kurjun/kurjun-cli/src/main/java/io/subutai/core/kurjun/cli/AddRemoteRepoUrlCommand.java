package io.subutai.core.kurjun.cli;


import java.net.URL;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.kurjun.api.TemplateManager;


@Command( scope = "kurjun", name = "add-url" )
public class AddRemoteRepoUrlCommand extends SubutaiShellCommandSupport
{

    private final TemplateManager templateManager;
    @Argument( index = 0, name = "repository", multiValued = false, description = "Remote repo url" )
    private String url;


    public AddRemoteRepoUrlCommand( final TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        templateManager.addRemoteRepository( new URL( url ) );
        System.out.println( "Url added ok" );
        return null;
    }
}
