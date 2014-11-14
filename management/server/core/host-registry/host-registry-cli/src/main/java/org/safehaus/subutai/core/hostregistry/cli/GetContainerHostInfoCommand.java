package org.safehaus.subutai.core.hostregistry.cli;


import java.util.UUID;

import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostDisconnectedException;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "host", name = "container-host", description = "Prints details about container host" )
public class GetContainerHostInfoCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( GetContainerHostInfoCommand.class.getName() );

    private final HostRegistry hostRegistry;

    @Argument( index = 0, name = "hostname or id", required = true, multiValued = false, description = "container "
            + "hostname or id" )
    String identifier;


    public GetContainerHostInfoCommand( final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( hostRegistry );

        this.hostRegistry = hostRegistry;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            ContainerHostInfo containerHostInfo;

            if ( UUIDUtil.isStringAUuid( identifier ) )
            {
                UUID id = UUIDUtil.generateUUIDFromString( identifier );
                containerHostInfo = hostRegistry.getContainerHostInfoById( id );
            }
            else
            {
                containerHostInfo = hostRegistry.getContainerHostInfoByHostname( identifier );
            }

            System.out.println( containerHostInfo );
        }
        catch ( HostDisconnectedException e )
        {
            System.out.println( "Host is not connected" );
            LOG.error( "Error in GetContainerHostInfoCommand", e );
        }

        return null;
    }
}
