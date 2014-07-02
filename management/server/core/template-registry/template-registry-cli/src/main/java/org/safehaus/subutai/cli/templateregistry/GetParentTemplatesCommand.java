package org.safehaus.subutai.cli.templateregistry;


import java.util.List;

import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.getTemplate command
 */
@Command( scope = "registry", name = "get-parent-templates", description = "Get all parent templates" )
public class GetParentTemplatesCommand extends OsgiCommandSupport {
    @Argument( index = 0, name = "child template name", required = true, multiValued = false,
            description = "child template name" )
    String childTemplateName;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        List<Template> templates = templateRegistryManager.getParentTemplates( childTemplateName );
        if ( !templates.isEmpty() ) {
            for ( Template template : templates ) {
                System.out.println( template );
            }
        }
        else {
            System.out.println( String.format( "Parent templates of %s not found", childTemplateName ) );
        }

        return null;
    }
}
