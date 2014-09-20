package org.safehaus.subutai.core.template.cli;


import org.safehaus.subutai.core.template.api.TemplateManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "template", name = "clone", description = "clone new instance")
public class CloneTemplate extends OsgiCommandSupport
{

    private TemplateManager templateManager;

    @Argument(index = 0, required = true)
    private String hostName;
    @Argument(index = 1, required = true)
    private String templateName;
    @Argument(index = 2, required = true)
    private String cloneName;
    @Argument(index = 3, required = true)
    private String envId;


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
        boolean b = templateManager.clone( hostName, templateName, cloneName, envId );
        if ( b )
        {
            System.out.println( String.format( "New instance '%s' is clone in %s", cloneName, hostName ) );
        }
        else
        {
            System.out.println( "Failed to clone new instance" );
        }
        return null;
    }
}
