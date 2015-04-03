package org.safehaus.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.listTemplates command
 */
@Command( scope = "registry", name = "list-templates", description = "List templates" )
public class ListTemplatesCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "lxc arch", required = false, multiValued = false,
            description = "lxc arch, default = amd64" )
    String lxcArch;

    private final TemplateRegistry templateRegistry;


    public ListTemplatesCommand( final TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( templateRegistry, "TemplateRegistry is null." );

        this.templateRegistry = templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        List<Template> templates = Strings.isNullOrEmpty( lxcArch ) ? templateRegistry.getAllTemplates() :
                                   templateRegistry.getAllTemplates( lxcArch );


        for ( Template template : templates )
        {
            System.out.println( String.format( "%s %s", template.getTemplateName(),
                    Strings.isNullOrEmpty( template.getParentTemplateName() ) ? "" :
                    template.getParentTemplateName() ) );
        }

        return null;
    }
}
