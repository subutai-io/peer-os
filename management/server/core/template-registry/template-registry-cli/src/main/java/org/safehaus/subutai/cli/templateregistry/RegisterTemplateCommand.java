package org.safehaus.subutai.cli.templateregistry;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.registerTemplate command
 */
@Command(scope = "registry", name = "register-template", description = "Register template with registry")
public class RegisterTemplateCommand extends OsgiCommandSupport {
    @Argument(index = 0, name = "path to template config file", required = true, multiValued = false,
            description = "path to template config file")
    String configFilePath;
    @Argument(index = 1, name = "path to template packages file", required = true, multiValued = false,
            description = "path to template packages file")
    String packagesFilePath;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception {

        templateRegistryManager.registerTemplate( readFile( configFilePath, Charset.defaultCharset() ),
                readFile( packagesFilePath, Charset.defaultCharset() ) );

        System.out.println( "Template registered successfully" );

        return null;
    }


    private String readFile( String path, Charset encoding ) throws IOException {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, encoding );
    }
}
