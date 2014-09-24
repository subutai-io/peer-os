package org.safehaus.subutai.core.template.cli;


import org.safehaus.subutai.core.template.api.TemplateManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "template", name = "import", description = "import template")
public class ImportTemplate extends OsgiCommandSupport
{

    private TemplateManager templateManager;

    @Argument(index = 0, required = true)
    private String hostName;
    @Argument(index = 1, required = true)
    private String templateName;


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
        boolean b = templateManager.importTemplate( hostName, templateName );
        if ( b )
        {
            System.out.println( "Template successfully imported" );
        }
        else
        {
            System.out.println( "Failed to import" );
        }
        return null;
    }
}
