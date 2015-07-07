package io.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registry.api.TemplateRegistry;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.getParentTemplates command
 */
@Command( scope = "registry", name = "get-parent-templates", description = "Get all parent templates" )
public class GetParentTemplatesCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "child template name", required = true, multiValued = false,
            description = "child template name" )
    String childTemplateName;
    @Argument( index = 1, name = "lxc arch", required = false, multiValued = false,
            description = "lxc arch, default = amd64" )
    String lxcArch;

    private final TemplateRegistry templateRegistry;


    public GetParentTemplatesCommand( final TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( templateRegistry, "TemplateRegistry is null" );


        this.templateRegistry = templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<Template> templates =
                Strings.isNullOrEmpty( lxcArch ) ? templateRegistry.getParentTemplates( childTemplateName ) :
                templateRegistry.getParentTemplates( childTemplateName, lxcArch );

        if ( !templates.isEmpty() )
        {
            for ( Template template : templates )
            {
                System.out.println( template );
            }
        }
        else
        {
            System.out.println( String.format( "Parent templates of %s not found", childTemplateName ) );
        }

        return null;
    }
}
