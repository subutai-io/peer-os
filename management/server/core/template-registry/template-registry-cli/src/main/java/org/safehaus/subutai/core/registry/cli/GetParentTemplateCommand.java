package org.safehaus.subutai.core.registry.cli;


import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.getParentTemplate command
 */
@Command(scope = "registry", name = "get-parent-template", description = "Get parent template")
public class GetParentTemplateCommand extends OsgiCommandSupport
{
    @Argument(index = 0, name = "child template name", required = true, multiValued = false,
            description = "child template name")
    String childTemplateName;
    @Argument(index = 1, name = "lxc arch", required = false, multiValued = false,
            description = "lxc arch, default = amd64")
    String lxcArch;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager )
    {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Template template =
                Strings.isNullOrEmpty( lxcArch ) ? templateRegistryManager.getParentTemplate( childTemplateName ) :
                templateRegistryManager.getParentTemplate( childTemplateName, lxcArch );

        if ( template != null )
        {
            System.out.println( template );
        }
        else
        {
            System.out.println( String.format( "Parent template of %s not found", childTemplateName ) );
        }

        return null;
    }
}
