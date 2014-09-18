package org.safehaus.subutai.core.registry.cli;


import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.unregisterTemplate command
 */
@Command( scope = "registry", name = "unregister-template", description = "Unregister template" )
public class UnregisterTemplateCommand extends OsgiCommandSupport {
    @Argument( index = 0, name = "template name", required = true, multiValued = false,
            description = "template name" )
    String templateName;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager )
    {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        templateRegistryManager.unregisterTemplate( templateName );

        System.out.println( String.format( "Template %s unregistered successfully", templateName ) );


        return null;
    }
}
