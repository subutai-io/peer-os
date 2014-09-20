package org.safehaus.subutai.core.template.cli;


import org.safehaus.subutai.core.template.api.TemplateManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "template", name = "promote", description = "promote clone to template")
public class PromoteClone extends OsgiCommandSupport
{

    private TemplateManager templateManager;

    @Argument(index = 0, required = true)
    private String hostName;
    @Argument(index = 1, required = true)
    private String cloneName;
    @Argument(index = 2, description = "new name for template")
    private String newName;
    @Argument(index = 3, description = "specify 'true' to copy clone first")
    private String copyit;


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
        boolean b = templateManager.promoteClone( hostName, cloneName, newName, Boolean.parseBoolean( copyit ) );
        if ( b )
        {
            System.out.println( "Clone successfully promoted to a template" );
        }
        else
        {
            System.out.println( "Failed to promote clone" );
        }
        return null;
    }
}
