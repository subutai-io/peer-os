package org.safehaus.subutai.core.container.cli;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "container", name = "clone" )
public class Clone extends OsgiCommandSupport {

    ContainerManager containerManager;

    @Argument( index = 0, required = true )
    private String hostname;
    @Argument( index = 1, required = true )
    private String templateName;
    @Argument( index = 2, required = true )
    private String cloneName;


    public void setContainerManager( ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        UUID envId = UUID.randomUUID();
        try
        {
            Agent a = containerManager.clone( envId, hostname, templateName, cloneName );
            System.out.println( String.format("Container cloned successfully. Agent %s", a) );
        }
        catch ( ContainerCreateException cce )
        {
            System.out.println( "Container cloning failed!" );
        }
        return null;
    }
}
