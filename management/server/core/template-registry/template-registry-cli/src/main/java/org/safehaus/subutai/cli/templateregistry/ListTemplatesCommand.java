package org.safehaus.subutai.cli.templateregistry;


import java.util.List;

import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.listTemplates command
 */
@Command( scope = "registry", name = "list-templates", description = "List templates" )
public class ListTemplatesCommand extends OsgiCommandSupport {

    private TemplateRegistryManager templateRegistryManager;


    private void listFamily( int level, TemplateTree tree, Template currentTemplate ) {
        System.out.println(
                String.format( "%" + ( level > 0 ? level : "" ) + "s %s", "", currentTemplate.getTemplateName() ) );
        List<Template> children = tree.getChildrenTemplates( currentTemplate );
        if ( !( children == null || children.isEmpty() ) ) {
            for ( Template child : children ) {
                listFamily( level + 1, tree, child );
            }
        }
    }


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception {

        TemplateTree tree = templateRegistryManager.getTemplateTree();
        List<Template> uberTemplates = tree.getRootTemplates();
        if ( uberTemplates != null ) {
            for ( Template template : uberTemplates ) {
                listFamily( 0, tree, template );
            }
        }

        return null;
    }
}
