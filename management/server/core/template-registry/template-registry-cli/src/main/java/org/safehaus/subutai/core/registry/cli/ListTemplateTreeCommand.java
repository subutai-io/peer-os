package org.safehaus.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.ListTemplateTreeCommand command
 */
@Command( scope = "registry", name = "list-template-tree", description = "List templates tree" )
public class ListTemplateTreeCommand extends OsgiCommandSupport
{

    private TemplateRegistry templateRegistry;


    private void listFamily( int level, TemplateTree tree, Template currentTemplate )
    {
        System.out.println(
                String.format( "%" + ( level > 0 ? level : "" ) + "s %s", "", currentTemplate.getTemplateName() ) );
        List<Template> children = tree.getChildrenTemplates( currentTemplate );
        if ( !( children == null || children.isEmpty() ) )
        {
            for ( Template child : children )
            {
                listFamily( level + 1, tree, child );
            }
        }
    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        TemplateTree tree = templateRegistry.getTemplateTree();
        List<Template> uberTemplates = tree.getRootTemplates();
        if ( uberTemplates != null )
        {
            for ( Template template : uberTemplates )
            {
                listFamily( 0, tree, template );
            }
        }

        return null;
    }
}
