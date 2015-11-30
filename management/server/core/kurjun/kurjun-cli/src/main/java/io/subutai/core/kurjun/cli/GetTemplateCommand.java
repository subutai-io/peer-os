package io.subutai.core.kurjun.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.core.kurjun.api.TemplateManager;


@Command( scope = "kurjun", name = "get-template" )
public class GetTemplateCommand extends SubutaiShellCommandSupport
{

    private final TemplateManager templateManager;
    @Argument( index = 0, name = "repository", multiValued = false, description = "Repository name" )
    private String repository;

    @Argument( index = 1, name = "templateName", multiValued = false, description = "Template name" )
    private String templateName;


    public GetTemplateCommand( final TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        TemplateKurjun template = templateManager.getTemplate( repository, templateName, null );
        if ( template != null )
        {
            System.out.println( "Template: " + template.getName() + ", " + template.getVersion() + ", " + template.getArchitecture() );
        }
        else
        {
            System.out.println( "Template cannot be found" );
        }
        return null;
    }
}
