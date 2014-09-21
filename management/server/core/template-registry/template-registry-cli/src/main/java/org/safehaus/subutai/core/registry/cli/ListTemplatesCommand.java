package org.safehaus.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.listTemplates command
 */
@Command(scope = "registry", name = "list-templates", description = "List templates")
public class ListTemplatesCommand extends OsgiCommandSupport
{
    @Argument(index = 0, name = "lxc arch", required = false, multiValued = false,
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

        List<Template> templates = Strings.isNullOrEmpty( lxcArch ) ? templateRegistryManager.getAllTemplates() :
                                   templateRegistryManager.getAllTemplates( lxcArch );


        for ( Template template : templates )
        {
            System.out.println( String.format( "%s %s", template.getTemplateName(),
                    Strings.isNullOrEmpty( template.getParentTemplateName() ) ? "" :
                    template.getParentTemplateName() ) );
        }

        return null;
    }
}
