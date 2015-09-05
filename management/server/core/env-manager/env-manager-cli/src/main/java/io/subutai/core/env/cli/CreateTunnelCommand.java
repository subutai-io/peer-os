package io.subutai.core.env.cli;


import java.util.HashSet;
import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;

import io.subutai.common.protocol.N2NConfig;
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
        List<N2NConfig> tunnelNetwork = environmentManager.createN2NTunnel( peerSet );
        System.out.println( "Tunnel created successfully: " + tunnelNetwork.size() );
        for ( N2NConfig config : tunnelNetwork )
        {
            System.out.println( "\tPeer ID\tSuper node\tAddreess\tInterface\tCommunity" );
            System.out.println( String.format( "\t%s\t%s:%d\t%s\t%s\t%s", config.getPeerId(), config.getSuperNodeIp(),
                    config.getN2NPort(),config.getAddress(),  config.getInterfaceName(), config.getCommunityName() ) );
        }

        return null;
    }
}
