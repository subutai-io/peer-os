package io.subutai.core.env.cli;


import java.util.HashSet;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;

import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.peer.api.PeerManager;


/**
 * List all existing environments
 */
@Command( scope = "env", name = "tunnel", description = "Command to create peers tunnel" )
public class CreateTunnelCommand extends OsgiCommandSupport
{

    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;

    @Argument( index = 0 )
    String peers;


    public CreateTunnelCommand( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        if ( peers == null )
        {
            System.out.println( "Peer list is empty! Exit." );
            return null;
        }
        System.out.println( "Peers: " + peers );
        final HashSet peerSet = new HashSet();

        for ( String peerId : peers.split( "\\s" ) )
        {
            System.out.println( String.format( "\tAdding peer %s to tunnel...", peerId ) );
            peerSet.add( peerManager.getPeer( peerId ) );
        }
        String tunnelNetwork = environmentManager.createN2NTunnel( peerSet );
        System.out.println( "Tunnel created successfully: " + tunnelNetwork );

        return null;
    }
}
