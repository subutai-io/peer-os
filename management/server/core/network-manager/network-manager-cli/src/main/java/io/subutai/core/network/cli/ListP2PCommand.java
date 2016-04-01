package io.subutai.core.network.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;


@Command( scope = "net", name = "p2p-list", description = "List P2P connections" )
public class ListP2PCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ListP2PCommand.class.getName() );

    private final NetworkManager networkManager;
    private final LocalPeer localPeer;

    @Argument( index = 0, name = "host id", required = false, multiValued = false,
            description = "host id" )
    String hostId;


    public ListP2PCommand( final NetworkManager networkManager, final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( networkManager );
        Preconditions.checkNotNull( localPeer );

        this.networkManager = networkManager;
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            final P2PConnections connections = Strings.isNullOrEmpty( hostId ) ? networkManager.getP2PConnections() :
                                               networkManager
                                                       .getP2PConnections( localPeer.getResourceHostById( hostId ) );

            System.out.format( "Found %d P2P connection(s)%n", connections.getConnections().size() );

            for ( P2PConnection connection : connections.getConnections() )
            {
                System.out.format( "%s %s %s%n", connection.getIp(), connection.getHash(), connection.getMac() );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in ListP2PCommand", e );
        }

        return null;
    }
}
