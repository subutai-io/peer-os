package io.subutai.core.network.cli;


import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "net", name = "p2p-list", description = "List P2P connections" )
public class ListP2PCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;

    @Argument( index = 0, name = "host id", required = false, multiValued = false, description = "host id" )
    String hostId;


    public ListP2PCommand( final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( localPeer );

        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            final P2PConnections connections =
                    StringUtils.isBlank( hostId ) ? localPeer.getManagementHost().getP2PConnections() :
                    localPeer.getResourceHostById( hostId ).getP2PConnections();

            System.out.format( "Found %d P2P connection(s)%n", connections.getConnections().size() );

            for ( P2PConnection connection : connections.getConnections() )
            {
                System.out.format( "%s %s %s%n", connection.getIp(), connection.getHash(), connection.getMac() );
            }
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
            log.error( "Error in ListP2PCommand", e );
        }

        return null;
    }
}
