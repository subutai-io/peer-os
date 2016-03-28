package io.subutai.core.network.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


@Command( scope = "net", name = "p2p-list", description = "List P2P connections" )
public class ListP2PCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ListP2PCommand.class.getName() );

    private final NetworkManager networkManager;

    @Argument( index = 0, name = "p2p hash", required = false, multiValued = false,
            description = "p2p hash" )
    String p2pHash;


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
            final P2PConnections connections = networkManager.getP2PConnections();
            if ( Strings.isNullOrEmpty( p2pHash ) )
            {

                System.out.format( "Found %d P2P connection(s)%n", connections.getConnections().size() );
                for ( P2PConnection connection : connections.getConnections() )
                {
                    System.out.format( "%s %s %s%n", connection.getIp(), connection.getHash(), connection.getMac() );
                }
            }
            else
            {
                final P2PConnection connection = connections.findConnectionByHash( p2pHash );
                if ( connection == null )
                {
                    System.out.println( "Connection not found" );
                }
                else
                {
                    System.out.format( "%s %s %s%n", connection.getIp(), connection.getHash(), connection.getMac() );
                }
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
