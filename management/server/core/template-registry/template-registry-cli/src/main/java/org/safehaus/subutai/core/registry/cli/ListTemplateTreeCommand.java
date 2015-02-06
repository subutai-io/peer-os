package org.safehaus.subutai.core.registry.cli;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * CLI for TemplateRegistryManager.ListTemplateTreeCommand command
 */
@Command( scope = "registry", name = "list-template-tree", description = "List templates tree" )
public class ListTemplateTreeCommand extends OsgiCommandSupport
{

    private final TemplateRegistry templateRegistry;


    private void listFamily( int level, Template currentTemplate )
    {
        System.out.println(
                String.format( "%" + ( level > 0 ? level : "" ) + "s %s", "", currentTemplate.getTemplateName() ) );
        List<Template> children = currentTemplate.getChildren();
        if ( !( children == null || children.isEmpty() ) )
        {
            for ( Template child : children )
            {
                listFamily( level + 1, child );
            }
        }
    }


    public ListTemplateTreeCommand( final TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( templateRegistry, "TemplateRegistry is null." );

        this.templateRegistry = templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        List<Template> templates = new ArrayList<>( templateRegistry.getTemplateTree() );
        for ( final Template template : templates )
        {
            listFamily( 0, template );
        }

        return null;
    }
}
