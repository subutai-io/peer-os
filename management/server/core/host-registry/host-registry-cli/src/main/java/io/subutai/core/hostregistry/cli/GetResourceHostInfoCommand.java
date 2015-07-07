package io.subutai.core.hostregistry.cli;


import java.util.UUID;

import org.safehaus.subutai.common.util.UUIDUtil;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.hostregistry.api.ResourceHostInfo;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


@Command( scope = "host", name = "resource-host", description = "Prints details about resource host" )
public class GetResourceHostInfoCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( GetResourceHostInfoCommand.class.getName() );

    private final HostRegistry hostRegistry;

    @Argument( index = 0, name = "hostname or id", required = true, multiValued = false, description = "resource host "
            + "hostname or id" )
    String identifier;


    public GetResourceHostInfoCommand( final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( hostRegistry );

        this.hostRegistry = hostRegistry;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            ResourceHostInfo resourceHostInfo;

            if ( UUIDUtil.isStringAUuid( identifier ) )
            {
                UUID id = UUIDUtil.generateUUIDFromString( identifier );
                resourceHostInfo = hostRegistry.getResourceHostInfoById( id );
            }
            else
            {
                resourceHostInfo = hostRegistry.getResourceHostInfoByHostname( identifier );
            }

            System.out.println( resourceHostInfo );
        }
        catch ( HostDisconnectedException e )
        {
            System.out.println( "Host is not connected" );
            LOG.error( "Error in GetResourceHostInfoCommand", e );
        }

        return null;
    }
}
