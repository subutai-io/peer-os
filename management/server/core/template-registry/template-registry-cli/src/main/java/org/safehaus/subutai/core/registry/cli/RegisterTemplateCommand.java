package org.safehaus.subutai.core.registry.cli;


import java.nio.charset.Charset;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.registerTemplate command
 */
@Command(scope = "registry", name = "register-template", description = "Register template with registry")
public class RegisterTemplateCommand extends OsgiCommandSupport
{
    @Argument(index = 0, name = "path to template config file", required = true, multiValued = false,
            description = "path to template config file")
    String configFilePath;
    @Argument(index = 1, name = "path to template packages file", required = true, multiValued = false,
            description = "path to template packages file")
    String packagesFilePath;
    @Argument(index = 2, name = "md5sum of packages file", required = true, multiValued = false,
            description = "md5sum of packages file")
    String md5sum;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager )
    {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        templateRegistryManager.registerTemplate( FileUtil.readFile( configFilePath, Charset.defaultCharset() ),
                FileUtil.readFile( packagesFilePath, Charset.defaultCharset() ), md5sum );

        System.out.println( "Template registered successfully" );

        return null;
    }
}
