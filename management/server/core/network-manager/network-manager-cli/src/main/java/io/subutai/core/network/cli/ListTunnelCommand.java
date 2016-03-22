package io.subutai.core.network.cli;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.protocol.Tunnel;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


@Command( scope = "net", name = "tunnel-list", description = "Lists tunnels" )
public class ListTunnelCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ListTunnelCommand.class.getName() );

    private final NetworkManager networkManager;


    public ListTunnelCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            Set<Tunnel> tunnels = networkManager.listTunnels();
            System.out.format( "Found %d tunnel(s)%n", tunnels.size() );
            for ( Tunnel tunnel : tunnels )
            {
                System.out.format( "%s %s%n", tunnel.getTunnelName(), tunnel.getTunnelIp() );
            }
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in RemoveTunnelCommand", e );
        }

        return null;
    }
}
