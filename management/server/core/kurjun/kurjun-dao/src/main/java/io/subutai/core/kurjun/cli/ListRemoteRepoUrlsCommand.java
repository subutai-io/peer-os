package io.subutai.core.kurjun.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.kurjun.api.TemplateManager;

import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> urls = templateManager.getRemoteRepoUrls();
        for ( Map<String, Object> data : urls )
        {
            System.out.println( data );
        }
        return null;
    }
}
