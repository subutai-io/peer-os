package io.subutai.core.network.cli;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.protocol.P2PConnection;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


@Command( scope = "p2p", name = "list", description = "List P2P connection" )
public class ListP2PCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ListP2PCommand.class.getName() );

    private final NetworkManager networkManager;


    public ListP2PCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            final Set<P2PConnection> connections = networkManager.listP2PConnections();
            System.out.println( String.format( "Found %d P2P connection(s).", connections.size() ) );
            for ( P2PConnection connection : connections )
            {
                System.out.println( String.format( "%s %s %s", connection.getInterfaceName(), connection.getLocalIp(),
                        connection.getP2pHash() ) );
            }
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in ListP2PCommand", e );
        }

        return null;
    }
}
