package org.safehaus.subutai.core.container.cli;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerEvent;
import org.safehaus.subutai.core.container.api.ContainerEventListener;
import org.safehaus.subutai.core.container.api.ContainerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "container", name = "clone" )
public class Clone extends OsgiCommandSupport implements ContainerEventListener
{

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
        containerManager.addListener( this );
        UUID envId = UUIDUtil.generateTimeBasedUUID();
        ;
        try
        {
            Agent a = containerManager.clone( envId, hostname, templateName, cloneName );
            System.out.println( String.format( "Container cloned successfully. Agent %s", a ) );
        }
        catch ( ContainerCreateException cce )
        {
            System.out.println( "Container cloning failed!" );
        }
        containerManager.removeListener( this );
        return null;
    }


    @Override
    public void onContainerEvent( final ContainerEvent containerEvent )
    {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis( containerEvent.getTimestamp() );
    }
}
