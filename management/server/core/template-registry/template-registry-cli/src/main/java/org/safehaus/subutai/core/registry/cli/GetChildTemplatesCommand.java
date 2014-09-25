package org.safehaus.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.getChildTemplates command
 */
@Command(scope = "registry", name = "get-child-templates",
        description = "Get child templates by parent template name")
public class GetChildTemplatesCommand extends OsgiCommandSupport
{
    @Argument(index = 0, name = "parent template name", required = true, multiValued = false,
            description = "parent template name")
    String parentTemplateName;
    @Argument(index = 1, name = "lxc arch", required = false, multiValued = false,
            description = "lxc arch, default = amd64")
    String lxcArch;

    private TemplateRegistry templateRegistry;


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
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
