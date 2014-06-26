package org.safehaus.subutai.cli.templateregistry;


import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.getTemplate command
 */
@Command( scope = "registry", name = "get-template-by-name", description = "Get template by name" )
public class GetTemplateCommand extends OsgiCommandSupport {
    @Argument( index = 0, name = "template name", required = true, multiValued = false,
            description = "template name" )
    String templateName;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        Template template = templateRegistryManager.getTemplate( templateName );
        if ( template != null ) {
            System.out.println( template );
        }
        else {
            System.out.println( String.format( "Template %s not found", templateName ) );
        }

        return null;
    }
}
