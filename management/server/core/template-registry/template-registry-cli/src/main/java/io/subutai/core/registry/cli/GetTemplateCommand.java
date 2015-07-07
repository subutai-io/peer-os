package io.subutai.core.registry.cli;


import org.safehaus.subutai.common.protocol.Template;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registry.api.TemplateRegistry;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.getTemplate command
 */
@Command( scope = "registry", name = "get-template", description = "Get template by name" )
public class GetTemplateCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "template name", required = true, multiValued = false,
            description = "template name" )
    String templateName;
    @Argument( index = 1, name = "lxc arch", required = false, multiValued = false,
            description = "lxc arch, default = amd64" )
    String lxcArch;


    private final TemplateRegistry templateRegistry;


    public GetTemplateCommand( final TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( templateRegistry, "TemplateRegistry is null." );

        this.templateRegistry = templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Template template = Strings.isNullOrEmpty( lxcArch ) ? templateRegistry.getTemplate( templateName ) :
                            templateRegistry.getTemplate( templateName, lxcArch );

        if ( template != null )
        {
            System.out.println( template );
        }
        else
        {
            System.out.println( String.format( "Template %s not found", templateName ) );
        }

        return null;
    }
}
