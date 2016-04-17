package io.subutai.core.kurjun.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.kurjun.api.TemplateManager;
import java.net.URL;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "kurjun", name = "remove-url" )
public class RemoveRemoteRepoUrlCommand extends SubutaiShellCommandSupport
{
    private final TemplateManager templateManager;

    @Argument( index = 0, name = "repository", multiValued = false, description = "Remote repo url" )
    private String url;


    public RemoveRemoteRepoUrlCommand( final TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        templateManager.removeRemoteRepository( new URL( url ) );
        return null;
    }
}
