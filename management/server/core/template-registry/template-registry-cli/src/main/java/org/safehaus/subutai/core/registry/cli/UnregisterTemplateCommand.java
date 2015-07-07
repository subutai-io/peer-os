package org.safehaus.subutai.core.registry.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * CLI for TemplateRegistryManager.unregisterTemplate command
 */
@Command( scope = "registry", name = "unregister-template", description = "Unregister template" )
public class UnregisterTemplateCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "template name", required = true, multiValued = false,
            description = "template name" )
    String templateName;

    private final TemplateRegistry templateRegistry;


    public UnregisterTemplateCommand( final TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( templateRegistry, "TemplateRegistry is null" );

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
