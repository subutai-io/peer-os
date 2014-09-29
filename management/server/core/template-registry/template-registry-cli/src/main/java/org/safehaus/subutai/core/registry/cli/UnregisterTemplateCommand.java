package org.safehaus.subutai.core.registry.cli;


import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.unregisterTemplate command
 */
@Command(scope = "registry", name = "unregister-template", description = "Unregister template")
public class UnregisterTemplateCommand extends OsgiCommandSupport
{
    @Argument(index = 0, name = "template name", required = true, multiValued = false,
            description = "template name")
    String templateName;

    private TemplateRegistry templateRegistry;


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        templateRegistry.unregisterTemplate( templateName );

        System.out.println( String.format( "Template %s unregistered successfully", templateName ) );


        return null;
    }
}
