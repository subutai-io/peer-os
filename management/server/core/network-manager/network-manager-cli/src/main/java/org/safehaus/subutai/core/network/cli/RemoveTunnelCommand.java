package org.safehaus.subutai.core.network.cli;


import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "net", name = "remove-tunnel", description = "Removes tunnel" )
public class RemoveTunnelCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoveTunnelCommand.class.getName() );

    private final NetworkManager networkManager;

    @Argument( index = 0, name = "tunnel name", required = true, multiValued = false,
            description = "tunnel name" )
    String tunnelName;


    public RemoveTunnelCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            networkManager.removeTunnel( tunnelName );
            System.out.println( "OK" );
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in RemoveTunnelCommand", e );
        }

        return null;
    }
}
