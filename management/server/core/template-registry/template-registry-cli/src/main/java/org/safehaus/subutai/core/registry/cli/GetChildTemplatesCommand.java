package org.safehaus.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.getChildTemplates command
 */
@Command( scope = "registry", name = "get-child-templates",
        description = "Get child templates by parent template name" )
public class GetChildTemplatesCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "parent template name", required = true, multiValued = false,
            description = "parent template name" )
    String parentTemplateName;
    @Argument( index = 1, name = "lxc arch", required = false, multiValued = false,
            description = "lxc arch, default = amd64" )
    String lxcArch;

    private final TemplateRegistry templateRegistry;


    public GetChildTemplatesCommand( final TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( templateRegistry, "TemplateRegistry is null." );

        this.templateRegistry = templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<Template> templates =
                Strings.isNullOrEmpty( lxcArch ) ? templateRegistry.getChildTemplates( parentTemplateName ) :
                templateRegistry.getChildTemplates( parentTemplateName, lxcArch );

        if ( templates != null && !templates.isEmpty() )
        {
            for ( Template template : templates )
            {
                System.out.println( template + "\n" );
            }
        }
        else
        {
            System.out.println( String.format( "Child templates of %s not found", parentTemplateName ) );
        }

        return null;
    }
}
