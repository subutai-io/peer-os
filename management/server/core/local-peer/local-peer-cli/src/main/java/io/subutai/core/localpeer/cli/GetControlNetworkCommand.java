package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.ControlNetworkConfig;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "localpeer", name = "control-network-config" )
public class GetControlNetworkCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public GetControlNetworkCommand( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        final String localPeerId = peerManager.getLocalPeer().getId();
        for ( Peer peer : peerManager.getPeers() )
        {
            if ( peer.isOnline() )
            {
                final ControlNetworkConfig result = peer.getControlNetworkConfig( localPeerId );
                System.out.println(
                        String.format( "%s %s %s", result.getPeerId(), result.getFingerprint(), result.getAddress() ) );
                System.out.println( "Used networks:" );
                for ( String s : result.getUsedNetworks() )
                {
                    System.out.println( s );
                }
            }
            else
            {
                System.out.println( String.format( "Peer %s is down at this moment.", peer.getId() ) );
            }
        }
        return null;
    }
}
