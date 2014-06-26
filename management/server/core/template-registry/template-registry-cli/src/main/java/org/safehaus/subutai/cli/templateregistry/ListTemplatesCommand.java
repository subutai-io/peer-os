package org.safehaus.subutai.cli.templateregistry;


import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * CLI for TemplateRegistryManager.getTemplate command
 */
@Command( scope = "registry", name = "list-templates", description = "List templates" )
public class ListTemplatesCommand extends OsgiCommandSupport {

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception {

        TemplateTree tree = templateRegistryManager.getTemplateTree();

        //print template tree with indentation

        return null;
    }
}
