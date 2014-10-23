package org.safehaus.subutai.core.template.cli;


import org.safehaus.subutai.core.template.api.TemplateManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "template", name = "get-master-template-name", description = "get master template name" )
public class MasterTemplateName extends OsgiCommandSupport
{

    private TemplateManager templateManager;


    public TemplateManager getTemplateManager()
    {
        return templateManager;
    }


    public void setTemplateManager( TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( templateManager.getMasterTemplateName() );
        return null;
    }
}
