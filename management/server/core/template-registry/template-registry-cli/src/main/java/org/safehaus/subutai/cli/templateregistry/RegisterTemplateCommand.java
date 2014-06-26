package org.safehaus.subutai.cli.templateregistry;


import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.registerTemplate command
 */
@Command( scope = "registry", name = "register-template", description = "Register template with registry" )
public class RegisterTemplateCommand extends OsgiCommandSupport {
    @Argument( index = 0, name = "template config file", required = true, multiValued = false,
            description = "template config file" )
    String configFile;
    @Argument( index = 1, name = "template packages file", required = true, multiValued = false,
            description = "template packages file" )
    String packagesFile;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        templateRegistryManager.registerTemplate( configFile, packagesFile );

        System.out.println( "Template registered successfully" );

        return null;
    }
}
