package io.subutai.core.kurjun.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.kurjun.api.TemplateManager;
import java.net.URL;
import java.util.Set;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "kurjun", name = "list-remote-urls" )
public class ListRemoteRepoUrlsCommand extends SubutaiShellCommandSupport
{
    private final TemplateManager templateManager;


    public ListRemoteRepoUrlsCommand( final TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<URL> urls = templateManager.getRemoteRepoUrls();
        System.out.println( " URL list: " );
        for ( URL url : urls )
        {
            System.out.println( url );
        }
        return null;
    }
}
