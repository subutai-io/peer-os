package org.safehaus.subutai.cli.templateregistry;


import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.getParentTemplate command
 */
@Command(scope = "registry", name = "get-parent-template", description = "Get parent template")
public class GetParentTemplateCommand extends OsgiCommandSupport {
    @Argument(index = 0, name = "child template name", required = true, multiValued = false,
            description = "child template name")
    String childTemplateName;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        Template template = templateRegistryManager.getParentTemplate( childTemplateName );
        if ( template != null ) {
            System.out.println( template );
        }
        else {
            System.out.println( String.format( "Parent template of %s not found", childTemplateName ) );
        }

        return null;
    }
}
